package ca.gatewaybaptistchurch.gateway.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.PlaybackState;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;

import java.io.IOException;

import ca.gatewaybaptistchurch.gateway.R;
import ca.gatewaybaptistchurch.gateway.activity.MainActivity;
import ca.gatewaybaptistchurch.gateway.model.Podcast;
import ca.gatewaybaptistchurch.gateway.utils.AudioFocusHelper;
import ca.gatewaybaptistchurch.gateway.utils.Constants;
import ca.gatewaybaptistchurch.gateway.utils.Utils;
import io.realm.Realm;
import timber.log.Timber;

import static ca.gatewaybaptistchurch.gateway.utils.Constants.Actions.REQUEST_PODCAST_STATE_UPDATE;

/**
 * Created by sean1 on 1/20/2017.
 */

public class MusicService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, AudioFocusHelper.MusicFocusable {
	//<editor-fold desc="Variables">
	final static String TAG = MusicService.class.getSimpleName();
	final static int NOTIFICATION_ID = 1;
	public static final float DUCK_VOLUME = 0.1f;

	private enum AudioFocus {
		NoFocusNoDuck,    // we don't have audio focus, and can't duck
		NoFocusCanDuck,   // we don't have focus, but can play at a low volume ("ducking")
		Focused           // we have full audio focus
	}

	private enum AudioState {
		PLAYING, PAUSED, STOPPED
	}

	MediaPlayer mediaPlayer = null;
	String currentPodcastUrl;
	WifiManager.WifiLock mWifiLock;

	AudioManager audioManager;
	AudioFocusHelper mAudioFocusHelper = null;
	AudioFocus audioFocus = AudioFocus.NoFocusNoDuck;
	MediaSessionCompat mediaSession;

	NotificationManager notificationManager;
	NotificationCompat.Builder notificationBuilder = null;

	int state = PlaybackStateCompat.STATE_STOPPED;

	//</editor-fold>

	void createMediaPlayerIfNeeded() {
		if (mediaPlayer == null) {
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

			mediaPlayer.setOnPreparedListener(this);
			mediaPlayer.setOnCompletionListener(this);
			mediaPlayer.setOnErrorListener(this);
		} else
			mediaPlayer.reset();
	}

	//<editor-fold desc="Lifecycle">
	@Override
	public void onCreate() {
		mWifiLock = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		mAudioFocusHelper = new AudioFocusHelper(getApplicationContext(), this);

		mediaSession = new MediaSessionCompat(this, TAG);
		mediaSession.setCallback(new MediaSessionCompat.Callback() {
			@Override
			public void onPlay() {
				processPlayRequest(null);
			}

			@Override
			public void onPause() {
				processPauseRequest();
			}

			@Override
			public void onSkipToNext() {
				processNextRequest();
			}

			@Override
			public void onSkipToPrevious() {
				processPreviousRequest();
			}

			@Override
			public void onStop() {
				processStopRequest();
			}
		});
		mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

		registerReceivers();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String action = intent.getAction();
		switch (action) {
			case Constants.MediaActions.TOGGLE_PLAYBACK:
				processTogglePlaybackRequest();
				break;
			case Constants.MediaActions.PLAY:
				processPlayRequest(intent);
				break;
			case Constants.MediaActions.PAUSE:
				processPauseRequest();
				break;
			case Constants.MediaActions.NEXT:
				processNextRequest();
				break;
			case Constants.MediaActions.STOP:
				processStopRequest();
				break;
			case Constants.MediaActions.PREVIOUS:
				processPreviousRequest();
				break;

		}

		return START_NOT_STICKY; // Means we started the service, but don't want it to
		// restart in case it's killed.
	}

	@Override
	public void onDestroy() {
		updatePlaybackState(PlaybackStateCompat.STATE_STOPPED);
		relaxResources(true);
		giveUpAudioFocus();
		unregisterReceivers();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	//</editor-fold>

	//<editor-fold desc="Media Player Actions">
	void processTogglePlaybackRequest() {
		if (state == PlaybackStateCompat.STATE_PAUSED || state == PlaybackStateCompat.STATE_STOPPED) {
			processPlayRequest(null);
		} else {
			processPauseRequest();
		}
	}

	void processPlayRequest(Intent intent) {
		String nextToPlay = null;
		if (intent == null || !intent.hasExtra(Constants.Extras.PODCAST_URL)) {
			if (currentPodcastUrl == null) {
				try (Realm realm = Realm.getDefaultInstance()) {
					Podcast podcast = Podcast.getNewestPodcast(realm);
					if (podcast != null) {
						nextToPlay = podcast.getPodcastUrl();
					}
				}
			} else {
				nextToPlay = currentPodcastUrl;
			}
		} else {
			nextToPlay = intent.getStringExtra(Constants.Extras.PODCAST_URL);
		}

		if (getCurrentPodcast() != null && getCurrentPodcast().getPodcastUrl().equalsIgnoreCase(nextToPlay) && mediaPlayer.isPlaying()) {
			processPauseRequest();
			return;
		}

		tryToGetAudioFocus();
		if (state == PlaybackStateCompat.STATE_STOPPED || state == PlaybackStateCompat.STATE_PLAYING) {
			playNextSong(nextToPlay);
		} else if (state == PlaybackStateCompat.STATE_PAUSED && getCurrentPodcast() != null) {
			updatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
			configAndStartMediaPlayer();
		}
	}

	void processPauseRequest() {
		if (state == PlaybackStateCompat.STATE_PLAYING) {
			updatePlaybackState(PlaybackStateCompat.STATE_PAUSED);
			mediaPlayer.pause();
			relaxResources(false);
		}
	}

	void processPreviousRequest() {
		if (mediaPlayer == null || getCurrentPodcast() == null) {
			return;
		}

		if (state != PlaybackStateCompat.STATE_PLAYING && state != PlaybackStateCompat.STATE_PAUSED) {
			return;
		}

		if (mediaPlayer.getCurrentPosition() > 10000) {
			mediaPlayer.seekTo(0);
		} else {
			try (Realm realm = Realm.getDefaultInstance()) {
				Podcast previousPodcast = getCurrentPodcast().getPreviousPodcast(realm);
				playNextSong(previousPodcast.getPodcastUrl());
			}
		}
	}

	void processNextRequest() {
		if (state == PlaybackStateCompat.STATE_PLAYING || state == PlaybackStateCompat.STATE_PAUSED) {
			tryToGetAudioFocus();
			playNextSong(null);
		}
	}

	void processStopRequest() {
		processStopRequest(false);
	}

	void processStopRequest(boolean force) {
		if (state == PlaybackStateCompat.STATE_PLAYING || state == PlaybackStateCompat.STATE_PAUSED || force) {
			updatePlaybackState(PlaybackStateCompat.STATE_STOPPED);
			relaxResources(true);
			giveUpAudioFocus();

			stopSelf();
		}
	}

	void playNextSong(String podcastUrl) {
		relaxResources(false);

		try {
			final Podcast podcast;
			if (podcastUrl != null) {
				try (Realm realm = Realm.getDefaultInstance()) {
					podcast = Podcast.getPodcast(realm, podcastUrl);
				}
			} else {
				try (Realm realm = Realm.getDefaultInstance()) {
					if (getCurrentPodcast() == null) {
						podcast = Podcast.getNewestPodcast(realm);
					} else {
						podcast = getCurrentPodcast().getNextPodcast(realm);
					}
				}

				if (podcast == null) {
					processStopRequest(true); // stop everything!
					return;
				}
			}

			createMediaPlayerIfNeeded();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setDataSource(podcastUrl);
			setCurrentPodcast(podcast);

			// Update the remote controls
			Utils.getPodcastArtAndIcon(podcast.getImageUrl(), new Utils.GlideArtAndIconListener() {
				@Override
				public void onFetched(String artUrl, Bitmap bigImage, Bitmap iconImage) {
					mediaSession.setMetadata(getMetadata(podcast, bigImage));
					updatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
				}
			});

			mediaPlayer.prepareAsync();
			mWifiLock.acquire();
		} catch (IOException error) {
			Timber.tag(TAG).e("Error playing next song: %s", error.getMessage());
		}
	}
	//</editor-fold>

	//<editor-fold desc="State/Notification Management">
	private void updatePlaybackState(int newState) {
		state = newState;

		Intent intent = new Intent(Constants.Actions.PODCAST_STATE_UPDATE);
		if (getCurrentPodcast() != null) {
			intent.putExtra(Constants.Extras.PODCAST_URL, getCurrentPodcast().getPodcastUrl());
		}
		String podcastState = Constants.PodcastState.STOPPED.name();
		switch (newState) {
			case PlaybackStateCompat.STATE_PLAYING:
				podcastState = Constants.PodcastState.PLAYING.name();
				break;
			case PlaybackStateCompat.STATE_PAUSED:
				podcastState = Constants.PodcastState.PAUSED.name();
				break;
			case PlaybackStateCompat.STATE_STOPPED:
				podcastState = Constants.PodcastState.STOPPED.name();
				break;
		}
		intent.putExtra(Constants.Extras.PODCAST_STATE, podcastState);
		sendBroadcast(intent);
		updateNotification();

		if (mediaSession != null) {
			mediaSession.setPlaybackState(getCurrentPlaybackState());
		}
	}

	private PlaybackStateCompat getCurrentPlaybackState() {
		long position = -1;
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			position = mediaPlayer.getCurrentPosition();
		}

		return new PlaybackStateCompat.Builder().setActions(getAvailableActions()).setState(state, position, 1.0f, SystemClock.elapsedRealtime()).build();
	}

	private long getAvailableActions() {
		long actions = PlaybackStateCompat.ACTION_PLAY;
		if (state == PlaybackState.STATE_PLAYING) {
			actions |= PlaybackStateCompat.ACTION_PAUSE;
		}

		actions |= PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
		actions |= PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
		return actions;
	}

	private MediaMetadataCompat getMetadata(Podcast podcast, Bitmap albumArt) {
		return new MediaMetadataCompat.Builder()
				.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, podcast.getPodcastUrl())
				.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "Gateway Baptist Church")
				.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, podcast.getSpeaker())
				.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, podcast.getImageUrl())
				.putString(MediaMetadataCompat.METADATA_KEY_TITLE, podcast.getTitle())
				.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, podcast.getDurationLong())
				.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt).build();
	}

	void updateNotification() {
		if (getCurrentPodcast() == null) {
			return;
		}

		if (state == PlaybackStateCompat.STATE_STOPPED) {
			return;
		}

		final boolean isPlaying = state == PlaybackStateCompat.STATE_PLAYING;
		final PendingIntent playPauseIntent = isPlaying ? playbackAction(1) : playbackAction(0);
		final int playPauseIconResource = isPlaying ? R.drawable.ic_pause_black_24dp : R.drawable.ic_play_arrow_black_24dp;
		final String playPauseString = isPlaying ? "Pause" : "Play";
		final Intent cancelIntent = new Intent(this, MusicService.class);
		cancelIntent.setAction(Constants.MediaActions.STOP);

		Utils.getPodcastArtAndIcon(getCurrentPodcast().getImageUrl(), new Utils.GlideArtAndIconListener() {
			@Override
			public void onFetched(String artUrl, Bitmap bigImage, Bitmap iconImage) {
				handleNotification(bigImage);
			}

			@Override
			public void onError(String artUrl, Bitmap errorBitmap) {
				handleNotification(errorBitmap);
			}

			private void handleNotification(Bitmap bigImage) {
				if (notificationBuilder == null) {

					notificationBuilder = (android.support.v7.app.NotificationCompat.Builder) new NotificationCompat.Builder(MusicService.this)
							.setSmallIcon(R.mipmap.ic_status_icon)
							.setShowWhen(false)
							.setDeleteIntent(PendingIntent.getService(getApplicationContext(), 1, cancelIntent, 0))
							.setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));
				}
				notificationBuilder.mActions.clear();

				notificationBuilder.setStyle(new NotificationCompat.MediaStyle()
						.setMediaSession(mediaSession.getSessionToken())
						.setShowActionsInCompactView(0, 1, 2))
						.setContentTitle(getCurrentPodcast().getTitle())
						.setContentText(getCurrentPodcast().getSpeaker())
						.addAction(R.drawable.ic_skip_previous_black_24dp, "previous", playbackAction(3))
						.addAction(playPauseIconResource, playPauseString, playPauseIntent)
						.addAction(R.drawable.ic_skip_next_black_24dp, "next", playbackAction(2));

				if (bigImage != null) {
					notificationBuilder.setLargeIcon(bigImage);
				}

				if (isPlaying) {
					startForeground(NOTIFICATION_ID, notificationBuilder.build());
				} else {
					stopForeground(false);
					notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
				}
			}
		});
	}

	/**
	 * 0 = Play |
	 * 1 = Pause |
	 * 2 = Next |
	 * 3 = Previous |
	 *
	 * @param actionNumber
	 * @return
	 */
	private PendingIntent playbackAction(int actionNumber) {
		Intent playbackAction = new Intent(this, MusicService.class);
		switch (actionNumber) {
			case 0:
				playbackAction.setAction(Constants.MediaActions.PLAY);
				return PendingIntent.getService(this, actionNumber, playbackAction, 0);
			case 1:
				playbackAction.setAction(Constants.MediaActions.PAUSE);
				return PendingIntent.getService(this, actionNumber, playbackAction, 0);
			case 2:
				playbackAction.setAction(Constants.MediaActions.NEXT);
				return PendingIntent.getService(this, actionNumber, playbackAction, 0);
			case 3:
				playbackAction.setAction(Constants.MediaActions.PREVIOUS);
				return PendingIntent.getService(this, actionNumber, playbackAction, 0);
			default:
				break;
		}
		return null;
	}
	//</editor-fold>

	//<editor-fold desc="Resource Handling">
	void relaxResources(boolean releaseMediaPlayer) {
		stopForeground(releaseMediaPlayer && mediaPlayer != null);

		if (releaseMediaPlayer && mediaPlayer != null) {
			mediaPlayer.reset();
			mediaPlayer.release();
			mediaPlayer = null;
			mediaSession.release();
		}

		if (mWifiLock.isHeld()) mWifiLock.release();
	}

	void giveUpAudioFocus() {
		if (audioFocus == AudioFocus.Focused && mAudioFocusHelper != null && mAudioFocusHelper.abandonFocus()) {
			audioFocus = AudioFocus.NoFocusNoDuck;
		}
	}

	void configAndStartMediaPlayer() {
		if (audioFocus == AudioFocus.NoFocusNoDuck) {
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.pause();
			}
			return;
		} else if (audioFocus == AudioFocus.NoFocusCanDuck) {
			mediaPlayer.setVolume(DUCK_VOLUME, DUCK_VOLUME);  // we'll be relatively quiet
		} else {
			mediaPlayer.setVolume(1.0f, 1.0f); // we can be loud
		}

		if (!mediaPlayer.isPlaying()) {
			mediaPlayer.start();
		}
	}

	void tryToGetAudioFocus() {
		if (audioFocus != AudioFocus.Focused && mAudioFocusHelper != null
				&& mAudioFocusHelper.requestFocus())
			audioFocus = AudioFocus.Focused;
	}

	private Podcast currentPodcast;

	private Podcast getCurrentPodcast() {
		if (currentPodcastUrl == null) {
			return null;
		}

		if (currentPodcast != null && currentPodcast.isValid()) {
			return currentPodcast;
		}

		try (Realm realm = Realm.getDefaultInstance()) {
			return Podcast.getPodcast(realm, currentPodcastUrl);
		}
	}

	private void setCurrentPodcast(Podcast podcast) {
		if (podcast == null || !podcast.isValid()) {
			return;
		}
		currentPodcastUrl = podcast.getPodcastUrl();
		currentPodcast = podcast;
	}
	//</editor-fold>

	//<editor-fold desc="Media Player Listeners">
	@Override
	public void onCompletion(MediaPlayer player) {
		playNextSong(null);
	}

	@Override
	public void onPrepared(MediaPlayer player) {
		updatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
		configAndStartMediaPlayer();
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Timber.tag(TAG).e("Error: %s extras: %s", String.valueOf(what), String.valueOf(extra));
		updatePlaybackState(PlaybackStateCompat.STATE_STOPPED);
		relaxResources(true);
		giveUpAudioFocus();
		return true;
	}
	//</editor-fold>

	//<editor-fold desc="Audio Focus Listeners">
	@Override
	public void onGainedAudioFocus() {
		audioFocus = AudioFocus.Focused;

		// restart media player with new focus settings
		if (state == PlaybackStateCompat.STATE_PLAYING) {
			configAndStartMediaPlayer();
		}
	}

	@Override
	public void onLostAudioFocus(boolean canDuck) {
		audioFocus = canDuck ? AudioFocus.NoFocusCanDuck : AudioFocus.NoFocusNoDuck;

		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			configAndStartMediaPlayer();
		}
	}
	//</editor-fold>

	//<editor-fold desc="Receivers">
	private void registerReceivers() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(REQUEST_PODCAST_STATE_UPDATE);
		registerReceiver(broadcastReceiver, filter);
	}

	private void unregisterReceivers() {
		unregisterReceiver(broadcastReceiver);
	}

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null || intent.getAction() == null) {
				return;
			}

			switch (intent.getAction()) {
				case REQUEST_PODCAST_STATE_UPDATE:
					updatePlaybackState(state);
					break;
			}
		}
	};
	//</editor-fold>
}