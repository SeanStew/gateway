package ca.gatewaybaptistchurch.gateway;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import ca.gatewaybaptistchurch.gateway.utils.Constants;
import io.realm.Realm;
import io.realm.SyncConfiguration;
import io.realm.SyncUser;
import timber.log.Timber;

/**
 * Created by Sean on 1/7/2017.
 */

public class GatewayApplication extends Application {
	private static SyncConfiguration defaultConfig;
	private static Context context;

	@Override
	public void onCreate() {
		super.onCreate();
		context = this;
		Realm.init(this);

		if (BuildConfig.DEBUG) {
			Timber.plant(new DebugCrashReportingTree());
		} else {
			Timber.plant(new CrashReportingTree());
		}
	}

	public void setDefaultRealmConfig(SyncUser syncUser) {
		defaultConfig = new SyncConfiguration.Builder(syncUser, Constants.REALM_URL).build();
		Realm.setDefaultConfiguration(defaultConfig);
	}

	public SyncConfiguration getDefaultRealmConfig() {
		return defaultConfig;
	}

	public static Context getContext() {
		return context;
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
