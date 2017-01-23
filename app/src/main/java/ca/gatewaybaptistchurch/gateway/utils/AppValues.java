package ca.gatewaybaptistchurch.gateway.utils;

import android.content.SharedPreferences;

import ca.gatewaybaptistchurch.gateway.BuildConfig;
import ca.gatewaybaptistchurch.gateway.GatewayApplication;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

/**
 * Created by sean1 on 1/22/2017.
 */

public class AppValues {
	private static SharedPreferences sharedPrefs() {
		return getDefaultSharedPreferences(GatewayApplication.getContext());
	}

	//<editor-fold desc="Bible">
	private interface BSP {
		String bookNumber = BuildConfig.APPLICATION_ID + "bookNumber";
		String chapterNumber = BuildConfig.APPLICATION_ID + "chapterNumber";
	}

	public static void setLastBibleBookNumber(int bookNumber) {
		sharedPrefs().edit().putInt(BSP.bookNumber, bookNumber).apply();
	}

	public static int getLastBibleBookNumber() {
		return sharedPrefs().getInt(BSP.bookNumber, 1);
	}

	public static void setLastBibleChapterNumber(int chapterNumber) {
		sharedPrefs().edit().putInt(BSP.chapterNumber, chapterNumber).apply();
	}

	public static int getLastBibleChapterNumber() {
		return sharedPrefs().getInt(BSP.chapterNumber, 1);
	}

	//</editor-fold>
}
