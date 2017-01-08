package ca.gatewaybaptistchurch.gateway;

import android.app.Application;
import android.util.Log;

import timber.log.Timber;

/**
 * Created by Sean on 1/7/2017.
 */

public class GatewayApplication extends Application {
	@Override public void onCreate() {
		super.onCreate();

		if (BuildConfig.DEBUG) {
			Timber.plant(new DebugCrashReportingTree());
		} else {
			Timber.plant(new CrashReportingTree());
		}
	}

	//<editor-fold desc="Timber">
	private static class CrashReportingTree extends Timber.Tree {
		//Raven raven;
		public CrashReportingTree() {
			//raven = RavenFactory.ravenInstance(BuildConfig.SENTRY_DSN);
		}

		@Override
		protected void log(int priority, String tag, String message, Throwable throwable) {
			if (tag == null) {
				tag = "";
			}
			if (priority == Log.VERBOSE || priority == Log.DEBUG) {
				String debugToLog = String.format("Debug: %s-%s", tag, message);
			} else if (priority == Log.ERROR) {
				String errorToLog = String.format("Error: %s-%s:%s", tag, message, throwable != null ? throwable.getMessage() : "");
			} else if (priority == Log.WARN) {
				String warningToLog = String.format("Warning: %s-%s", tag, message);
			}
		}
	}

	private static class DebugCrashReportingTree extends Timber.DebugTree {
		@Override
		protected void log(int priority, String tag, String message, Throwable throwable) {
			super.log(priority, tag, message, throwable);
			if (tag == null) {
				tag = "";
			}
			if (priority == Log.VERBOSE || priority == Log.DEBUG) {
				String debugToLog = String.format("Debug: %s-%s", tag, message);
			} else if (priority == Log.ERROR) {
				String errorToLog = String.format("Error: %s-%s:%s", tag, message, throwable != null ? throwable.getMessage() : "");
			} else if (priority == Log.WARN) {
				String warningToLog = String.format("Warning: %s-%s", tag, message);
			}
		}
	}
	//</editor-fold>
}
