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
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.IOException;

import ca.gatewaybaptistchurch.gateway.R;
import ca.gatewaybaptistchurch.gateway.model.Podcast;
import ca.gatewaybaptistchurch.gateway.utils.Constants;
import io.realm.Realm;

public class PodcastPlaybackService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
		MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnInfoListener,
		MediaPlayer.OnBufferingUpdateListener, AudioManager.OnAudioFocusChangeListener {
	//<editor-fold desc="Variables">
	private MediaPlayer mediaPlayer;

	//MediaSession
	private MediaSessionManager mediaSessionManager;
	private MediaSessionCompat mediaSession;
	private MediaControllerCompat.TransportControls transportControls;

	//AudioPlayer notification ID
	private static final int NOTIFICATION_ID = 101;

	//Used to pause/resume MediaPlayer
	private int resumePosition;

	//AudioFocus
	private AudioManager audioManager;

	// Binder given to clients
	private final IBinder iBinder = new LocalBinder();

	private Podcast activePodcast;
	private PodcastPlaybackListener podcastListener;

	//Handle incoming phone calls
	private boolean ongoingCall = false;
	private PhoneStateListener phoneStateListener;
	private TelephonyManager telephonyManager;
	//</editor-fold>

	//<editor-fold desc="Service lifecycle methods">
	@Override
	public void onCreate() {
		super.onCreate();
		// Perform one-time setup procedures

		// Manage incoming phone calls during playback.
		// Pause MediaPlayer on incoming call,
		// Resume on hangup.
		callStateListener();
		//ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs -- BroadcastReceiver
		registerBecomingNoisyReceiver();
		//Listen for new Audio to play -- BroadcastReceiver
		registerPlayNewAudio();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try (Realm realm = Realm.getDefaultInstance()) {
			Podcast podcast = Podcast.getPodcast(realm, intent.getStringExtra(Constants.Extras.PODCAST_URL));
			if (podcast != null) {
				activePodcast = podcast;
			}

			//Request audio focus\
			if (!requestAudioFocus()) {
				//Could not gain focus
				stopSelf();
			}

			if (mediaSessionManager == null) {
				try {
					initMediaSession();
					initMediaPlayer();
				} catch (RemoteException e) {
					e.printStackTrace();
					stopSelf();
				}
				buildNotification(Constants.PlaybackStatus.PLAYING);
			}

			//Handle Intent action from MediaSession.TransportControls
			handleIncomingActions(intent);
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mediaPlayer != null) {
			stopMedia();
			mediaPlayer.release();
		}
		removeAudioFocus();
		//Disable the PhoneStateListener
		if (phoneStateListener != null) {
			telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
		}

		removeNotification();

		//unregister BroadcastReceivers
		unregisterReceiver(becomingNoisyReceiver);
		unregisterReceiver(playNewAudio);
	}
	//</editor-fold>

	//<editor-fold desc="Service Binder">
	public class LocalBinder extends Binder {
		public PodcastPlaybackService getService() {
			// Return this instance of LocalService so clients can call public methods
			return PodcastPlaybackService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return iBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		podcastListener = null;
		return super.onUnbind(intent);
	}

	public void setPodcastListener(PodcastPlaybackListener listener) {
		this.podcastListener = listener;
	}

	public String getActivePodcastUrl() {
		if (activePodcast == null) {
			return null;
		}

		return activePodcast.getPodcastUrl();
	}
	//</editor-fold>

	//<editor-fold desc="MediaPlayer callback methods">
	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		//Invoked indicating buffering status of
		//a media resource being streamed over the network.
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		//Invoked when playback of a media source has completed.
		stopMedia();

		removeNotification();
		//stop the service
		stopSelf();
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		//Invoked when there has been an error during an asynchronous operation
		switch (what) {
			case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
				Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
				break;
			case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
				Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
				break;
			case MediaPlayer.MEDIA_ERROR_UNKNOWN:
				Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
				break;
		}
		return false;
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		//Invoked to communicate some info
		return false;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		//Invoked when the media source is ready for playback.
		playMedia();
	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		//Invoked indicating the completion of a seek operation.
	}

	@Override
	public void onAudioFocusChange(int focusState) {
		//Invoked when the audio focus of the system is updated.
		switch (focusState) {
			case AudioManager.AUDIOFOCUS_GAIN:
				// resume playback
				if (mediaPlayer == null) {
					initMediaPlayer();
				} else if (!mediaPlayer.isPlaying()) {
					mediaPlayer.start();
				}
				mediaPlayer.setVolume(1.0f, 1.0f);
				break;
			case AudioManager.AUDIOFOCUS_LOSS:
				// Lost focus for an unbounded amount of time: stop playback and release media player
				if (mediaPlayer.isPlaying()) {
					mediaPlayer.stop();
				}
				mediaPlayer.release();
				mediaPlayer = null;
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
				// Lost focus for a short time, but we have to stop
				// playback. We don't release the media player because playback
				// is likely to resume
				if (mediaPlayer.isPlaying()) {
					mediaPlayer.pause();
				}
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
				// Lost focus for a short time, but it's ok to keep playing
				// at an attenuated level
				if (mediaPlayer.isPlaying()) {
					mediaPlayer.setVolume(0.1f, 0.1f);
				}
				break;
		}
	}
	//</editor-fold>

	//<editor-fold desc="AudioFocus">
	private boolean requestAudioFocus() {
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			//Focus gained
			return true;
		}
		//Could not gain focus
		return false;
	}

	private boolean removeAudioFocus() {
		return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this);
	}
	//</editor-fold>

	//<editor-fold desc="MediaPlayer actions">
	private void initMediaPlayer() {
		if (mediaPlayer == null) {
			mediaPlayer = new MediaPlayer();//new MediaPlayer instance
		}

		//Set up MediaPlayer event listeners
		mediaPlayer.setOnCompletionListener(this);
		mediaPlayer.setOnErrorListener(this);
		mediaPlayer.setOnPreparedListener(this);
		mediaPlayer.setOnBufferingUpdateListener(this);
		mediaPlayer.setOnSeekCompleteListener(this);
		mediaPlayer.setOnInfoListener(this);
		//Reset so that the MediaPlayer is not pointing to another data source
		mediaPlayer.reset();


		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try {
			// Set the data source to the mediaFile location
			mediaPlayer.setDataSource(activePodcast.getPodcastUrl());
		} catch (IOException e) {
			e.printStackTrace();
			stopSelf();
		}
		mediaPlayer.prepareAsync();
	}

	private void playMedia() {
		if (!mediaPlayer.isPlaying()) {
			mediaPlayer.start();

			if (podcastListener != null) {
				podcastListener.onPodcastStarted(activePodcast.getPodcastUrl());
			}
		}
	}

	private void stopMedia() {
		if (mediaPlayer == null) return;
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.stop();

			if (podcastListener != null) {
				podcastListener.onPodcastStopped();
			}
		}
	}

	private void pauseMedia() {
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
			resumePosition = mediaPlayer.getCurrentPosition();

			if (podcastListener != null) {
				podcastListener.onPodcastPaused(activePodcast.getPodcastUrl());
			}
		}
	}

	private void resumeMedia() {
		if (!mediaPlayer.isPlaying()) {
			mediaPlayer.seekTo(resumePosition);
			mediaPlayer.start();
		}
	}

	private void skipToNext() {
		try (Realm realm = Realm.getDefaultInstance()) {
			activePodcast = activePodcast.getNextPodcast(realm);

			stopMedia();
			//reset mediaPlayer
			mediaPlayer.reset();
			initMediaPlayer();
		}
	}

	private void skipToPrevious() {
		try (Realm realm = Realm.getDefaultInstance()) {
			activePodcast = activePodcast.getPreviousPodcast(realm);

			stopMedia();
			//reset mediaPlayer
			mediaPlayer.reset();
			initMediaPlayer();
		}
	}
	//</editor-fold>

	//<editor-fold desc="ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs">
	private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			//pause audio on ACTION_AUDIO_BECOMING_NOISY
			pauseMedia();
			buildNotification(Constants.PlaybackStatus.PAUSED);
		}
	};

	private void registerBecomingNoisyReceiver() {
		//register after getting audio focus
		IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
		registerReceiver(becomingNoisyReceiver, intentFilter);
	}
	//</editor-fold>

	/**
	 * Handle PhoneState changes
	 */
	private void callStateListener() {
		// Get the telephony manager
		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		//Starting listening for PhoneState changes
		phoneStateListener = new PhoneStateListener() {
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				switch (state) {
					//if at least one call exists or the phone is ringing
					//pause the MediaPlayer
					case TelephonyManager.CALL_STATE_OFFHOOK:
					case TelephonyManager.CALL_STATE_RINGING:
						if (mediaPlayer != null) {
							pauseMedia();
							ongoingCall = true;
						}
						break;
					case TelephonyManager.CALL_STATE_IDLE:
						// Phone idle. Start playing.
						if (mediaPlayer != null) {
							if (ongoingCall) {
								ongoingCall = false;
								resumeMedia();
							}
						}
						break;
				}
			}
		};
		// Register the listener with the telephony manager
		// Listen for changes to the device call state.
		telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
	}

	//<editor-fold desc="MediaSession and Notification actions">
	private void initMediaSession() throws RemoteException {
		if (mediaSessionManager != null) {
			return; //mediaSessionManager exists
		}

		mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
		// Create a new MediaSession
		mediaSession = new MediaSessionCompat(getApplicationContext(), PodcastPlaybackService.class.getSimpleName());
		//Get MediaSessions transport controls
		transportControls = mediaSession.getController().getTransportControls();
		//set MediaSession -> ready to receive media commands
		mediaSession.setActive(true);
		//indicate that the MediaSession handles transport control commands
		// through its MediaSessionCompat.Callback.
		mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

		//Set mediaSession's MetaData
		updateMetaData();

		// Attach Callback to receive MediaSession updates
		mediaSession.setCallback(new MediaSessionCompat.Callback() {
			// Implement callbacks
			@Override
			public void onPlay() {
				super.onPlay();

				resumeMedia();
				buildNotification(Constants.PlaybackStatus.PLAYING);
			}

			@Override
			public void onPause() {
				super.onPause();

				pauseMedia();
				buildNotification(Constants.PlaybackStatus.PAUSED);
			}

			@Override
			public void onSkipToNext() {
				super.onSkipToNext();

				skipToNext();
				updateMetaData();
				buildNotification(Constants.PlaybackStatus.PLAYING);
			}

			@Override
			public void onSkipToPrevious() {
				super.onSkipToPrevious();

				skipToPrevious();
				updateMetaData();
				buildNotification(Constants.PlaybackStatus.PLAYING);
			}

			@Override
			public void onStop() {
				super.onStop();
				removeNotification();
				//Stop the service
				stopSelf();
			}

			@Override
			public void onSeekTo(long position) {
				super.onSeekTo(position);
			}
		});
	}

	private void updateMetaData() {
		Glide.with(getApplicationContext())
				.load(activePodcast.getImageUrl())
				.asBitmap()
				.error(R.drawable.header_image)
				.into(new SimpleTarget<Bitmap>(256, 256) {
					@Override
					public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
						// Update the current metadata
						mediaSession.setMetadata(new MediaMetadataCompat.Builder()
								.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, resource)
								.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, activePodcast.getSpeaker())
								.putString(MediaMetadataCompat.METADATA_KEY_TITLE, activePodcast.getTitle())
								.build());
					}
				});
	}

	private void buildNotification(Constants.PlaybackStatus playbackStatus) {

		/**
		 * Notification actions -> playbackAction()
		 *  0 -> Play
		 *  1 -> Pause
		 *  2 -> Next track
		 *  3 -> Previous track
		 */

		int notificationAction = android.R.drawable.ic_media_pause;//needs to be initialized
		PendingIntent play_pauseAction = null;

		//Build a new notification according to the current state of the MediaPlayer
		if (playbackStatus == Constants.PlaybackStatus.PLAYING) {
			notificationAction = android.R.drawable.ic_media_pause;
			//create the pause action
			play_pauseAction = playbackAction(1);
		} else if (playbackStatus == Constants.PlaybackStatus.PAUSED) {
			notificationAction = android.R.drawable.ic_media_play;
			//create the play action
			play_pauseAction = playbackAction(0);
		}

		final int finalNotificationAction = notificationAction;
		final PendingIntent finalPlay_pauseAction = play_pauseAction;
		Glide.with(getApplicationContext())
				.load(activePodcast.getImageUrl())
				.asBitmap()
				.error(R.drawable.header_image)
				.into(new SimpleTarget<Bitmap>(256, 256) {
					@Override
					public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
						// Create a new Notification
						NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(PodcastPlaybackService.this)
								// Hide the timestamp
								.setShowWhen(false)
								// Set the Notification style
								.setStyle(new NotificationCompat.MediaStyle()
										// Attach our MediaSession token
										.setMediaSession(mediaSession.getSessionToken())
										// Show our playback controls in the compat view
										.setShowActionsInCompactView(0, 1, 2))
								// Set the large and small icons
								.setLargeIcon(resource)
								.setSmallIcon(android.R.drawable.stat_sys_headset)
								// Set Notification content information
								.setContentText(activePodcast.getSpeaker())
								.setContentTitle(activePodcast.getTitle())
								// Add playback actions
								.addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
								.addAction(finalNotificationAction, "pause", finalPlay_pauseAction)
								.addAction(android.R.drawable.ic_media_next, "next", playbackAction(2));

						((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());

						// Update the current metadata
						mediaSession.setMetadata(new MediaMetadataCompat.Builder()
								.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, resource)
								.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, activePodcast.getSpeaker())
								.putString(MediaMetadataCompat.METADATA_KEY_TITLE, activePodcast.getTitle())
								.build());
					}
				});
	}
	//</editor-fold>

	private PendingIntent playbackAction(int actionNumber) {
		Intent playbackAction = new Intent(this, PodcastPlaybackService.class);
		switch (actionNumber) {
			case 0:
				// Play
				playbackAction.setAction(Constants.MediaActions.PLAY);
				return PendingIntent.getService(this, actionNumber, playbackAction, 0);
			case 1:
				// Pause
				playbackAction.setAction(Constants.MediaActions.PAUSE);
				return PendingIntent.getService(this, actionNumber, playbackAction, 0);
			case 2:
				// Next track
				playbackAction.setAction(Constants.MediaActions.NEXT);
				return PendingIntent.getService(this, actionNumber, playbackAction, 0);
			case 3:
				// Previous track
				playbackAction.setAction(Constants.MediaActions.PREVIOUS);
				return PendingIntent.getService(this, actionNumber, playbackAction, 0);
			default:
				break;
		}
		return null;
	}

	private void removeNotification() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(NOTIFICATION_ID);
	}

	private void handleIncomingActions(Intent playbackAction) {
		if (playbackAction == null || playbackAction.getAction() == null) {
			return;
		}

		String actionString = playbackAction.getAction();
		if (actionString.equalsIgnoreCase(Constants.MediaActions.PLAY)) {
			transportControls.play();
		} else if (actionString.equalsIgnoreCase(Constants.MediaActions.PAUSE)) {
			transportControls.pause();
		} else if (actionString.equalsIgnoreCase(Constants.MediaActions.NEXT)) {
			transportControls.skipToNext();
		} else if (actionString.equalsIgnoreCase(Constants.MediaActions.PREVIOUS)) {
			transportControls.skipToPrevious();
		} else if (actionString.equalsIgnoreCase(Constants.MediaActions.STOP)) {
			transportControls.stop();
		}
	}

	//<editor-fold desc="Play new Audio">
	private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try (Realm realm = Realm.getDefaultInstance()) {
				activePodcast = Podcast.getPodcast(realm, intent.getExtras().getString(Constants.Extras.PODCAST_URL, ""));
				if (activePodcast == null) {
					stopSelf();
				}

				//A PLAY_NEW_AUDIO action received
				//reset mediaPlayer to play the new Audio
				stopMedia();
				mediaPlayer.reset();
				initMediaPlayer();
				updateMetaData();
				buildNotification(Constants.PlaybackStatus.PLAYING);
			}
		}
	};

	private void registerPlayNewAudio() {
		//Register playNewMedia receiver
		IntentFilter filter = new IntentFilter(Constants.Actions.PLAY_PODCAST);
		registerReceiver(playNewAudio, filter);
	}
	//</editor-fold>

	public interface PodcastPlaybackListener {
		void onPodcastStarted(String podcastUrl);

		void onPodcastPaused(String podcastUrl);

		void onPodcastStopped();
	}
}