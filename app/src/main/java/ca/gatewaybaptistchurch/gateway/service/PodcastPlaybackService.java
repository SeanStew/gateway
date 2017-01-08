package ca.gatewaybaptistchurch.gateway.service;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import java.util.List;

import ca.gatewaybaptistchurch.gateway.R;

/**
 * Created by Sean on 1/7/2017.
 */

public class PodcastPlaybackService extends MediaBrowserServiceCompat {
	private MediaSessionCompat mediaSession;
	private PlaybackStateCompat.Builder stateBuilder;

	@Override public void onCreate() {
		super.onCreate();

		// Create a MediaSessionCompat
		mediaSession = new MediaSessionCompat(this, PodcastPlaybackService.class.getSimpleName());

		// Enable callbacks from MediaButtons and TransportControls
		mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

		// Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
		stateBuilder = new PlaybackStateCompat.Builder().setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE);
		mediaSession.setPlaybackState(stateBuilder.build());

		// MySessionCallback() has methods that handle callbacks from a media controller
		//mediaSession.setCallback(new MySessionCallback());

		// For Android 5.0 (API version 21) or greater
		// To enable restarting an inactive session in the background,
		// You must create a pending intent and setMediaButtonReceiver.
		Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);

		mediaButtonIntent.setClass(this, PodcastPlaybackService.class);
		PendingIntent mbrIntent = PendingIntent.getService(this, 0, mediaButtonIntent, 0);

		mediaSession.setMediaButtonReceiver(mbrIntent);

		// Set the session's token so that client activities can communicate with it.
		setSessionToken(mediaSession.getSessionToken());
	}

	@Nullable @Override public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
		return new BrowserRoot(getString(R.string.app_name), null);
	}

	@Override public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
		result.sendResult(null);
	}
}
