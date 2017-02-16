package ca.gatewaybaptistchurch.gateway.utils;

import ca.gatewaybaptistchurch.gateway.BuildConfig;

/**
 * Created by Sean on 1/8/2017.
 */

public class Constants {
	public static final String AUTH_URL = "http://" + BuildConfig.OBJECT_SERVER_IP + ":9080/auth";
	public static final String REALM_URL = "realm://" + BuildConfig.OBJECT_SERVER_IP + ":9080/gatewayApp";

	public enum PodcastState {
		PLAYING, PAUSED, STOPPED
	}

	public interface MediaActions {
		String TOGGLE_PLAYBACK = BuildConfig.APPLICATION_ID + ".TOGGLE_PLAYBACK";
		String PLAY = BuildConfig.APPLICATION_ID + ".PLAY";
		String PAUSE = BuildConfig.APPLICATION_ID + ".PAUSE";
		String PREVIOUS = BuildConfig.APPLICATION_ID + ".PREVIOUS";
		String NEXT = BuildConfig.APPLICATION_ID + ".NEXT";
		String STOP = BuildConfig.APPLICATION_ID + ".STOP";
	}

	public interface Extras {
		String PODCAST_URL = BuildConfig.APPLICATION_ID + ".podcastUrl";
		String PODCAST_STATE = BuildConfig.APPLICATION_ID + ".podcastState";
		String EVENT_UUID = BuildConfig.APPLICATION_ID + ".eventUuid";
	}

	public interface Actions {
		String REQUEST_PODCAST_STATE_UPDATE = BuildConfig.APPLICATION_ID + ".requestPodcastStateUpdate";
		String PODCAST_STATE_UPDATE = BuildConfig.APPLICATION_ID + ".podcastStateUpdate";
		String PLAY_PODCAST = BuildConfig.APPLICATION_ID + ".playPodcast";
		String PAUSE_PODCAST = BuildConfig.APPLICATION_ID + ".pausePodcast";
	}

	public enum PlaybackStatus {
		PLAYING, PAUSED
	}
}
