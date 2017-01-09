package ca.gatewaybaptistchurch.gateway.utils;

import ca.gatewaybaptistchurch.gateway.BuildConfig;

/**
 * Created by Sean on 1/8/2017.
 */

public class Constants {
	public interface MediaActions {
		String PLAY = BuildConfig.APPLICATION_ID + ".ACTION_PLAY";
		String PAUSE = BuildConfig.APPLICATION_ID + ".ACTION_PAUSE";
		String PREVIOUS = BuildConfig.APPLICATION_ID + ".ACTION_PREVIOUS";
		String NEXT = BuildConfig.APPLICATION_ID + ".ACTION_NEXT";
		String STOP = BuildConfig.APPLICATION_ID + ".ACTION_STOP";
	}

	public interface Extras {
		String PODCAST_URL = BuildConfig.APPLICATION_ID + ".podcastUrl";
	}

	public interface Actions {
		String PLAY_PODCAST = BuildConfig.APPLICATION_ID + ".playPodcast";
	}

	public enum PlaybackStatus {
		PLAYING, PAUSED
	}
}
