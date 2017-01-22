package ca.gatewaybaptistchurch.gateway.utils;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

import ca.gatewaybaptistchurch.gateway.GatewayApplication;
import ca.gatewaybaptistchurch.gateway.R;
import timber.log.Timber;

/**
 * Created by Sean on 1/7/2017.
 */

public class Utils {

	//<editor-fold desc="Dates">
	private static final DateTimeFormatter fullDateFormat = DateTimeFormat.forPattern("EEE MMMM dd - h:mmaa");

	public static String getFullDateString(Date date) {
		if (date == null) {
			return "";
		}

		return fullDateFormat.print(new DateTime(date));
	}
	//</editor-fold>

	//<editor-fold desc="Null checks">
	public static boolean isNotNullOrEmpty(String string) {
		return string != null && !string.isEmpty();
	}

	public static boolean isTrueAndNotNull(Boolean aBoolean) {
		return aBoolean != null && aBoolean;
	}
	//</editor-fold>

	//<editor-fold desc="Podcast">
	public static Constants.PodcastState getPodcastStateFromIntent(Intent intent) {
		if (intent == null || intent.getExtras() == null) {
			return Constants.PodcastState.STOPPED;
		}
		if (!intent.hasExtra(Constants.Extras.PODCAST_STATE)) {
			return Constants.PodcastState.STOPPED;
		}

		try {
			return Constants.PodcastState.valueOf(intent.getExtras().getString(Constants.Extras.PODCAST_STATE));
		} catch (Exception e) {
			return Constants.PodcastState.STOPPED;
		}
	}

	public static void getPodcastArtAndIcon(final String artUrl, final GlideArtAndIconListener listener) {
		Glide.with(GatewayApplication.getContext()).load(artUrl).asBitmap().into(new SimpleTarget<Bitmap>() {
			@Override
			public void onResourceReady(final Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
				Glide.with(GatewayApplication.getContext()).load(artUrl).asBitmap().into(new SimpleTarget<Bitmap>(128, 128) {
					@Override
					public void onResourceReady(Bitmap icon, GlideAnimation<? super Bitmap> glideAnimation) {
						listener.onFetched(artUrl, bitmap, icon);
					}

					@Override
					public void onLoadFailed(Exception e, Drawable errorDrawable) {
						super.onLoadFailed(e, errorDrawable);

						listener.onError(artUrl, getErrorBitmap());
					}
				});
			}

			@Override
			public void onLoadFailed(Exception e, Drawable errorDrawable) {
				super.onLoadFailed(e, errorDrawable);
				listener.onError(artUrl, getErrorBitmap());
			}

			private Bitmap getErrorBitmap() {
				Bitmap errorBitmap = null;
				try {
					errorBitmap = BitmapFactory.decodeResource(GatewayApplication.getContext().getResources(), R.drawable.messages_placeholder);
				} catch (Exception ignored) {
				}
				return errorBitmap;
			}
		});
	}

	public static abstract class GlideArtAndIconListener {
		public abstract void onFetched(String artUrl, Bitmap bigImage, Bitmap iconImage);

		public void onError(String artUrl, Bitmap errorBitmap) {
			Timber.tag("GlideArtAndIconListener").e("Error while downloading %s", artUrl);
		}
	}
	//</editor-fold>
}
