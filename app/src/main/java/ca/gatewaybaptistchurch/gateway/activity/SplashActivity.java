package ca.gatewaybaptistchurch.gateway.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;
import ca.gatewaybaptistchurch.gateway.GatewayApplication;
import ca.gatewaybaptistchurch.gateway.R;
import ca.gatewaybaptistchurch.gateway.utils.Constants;
import io.realm.ObjectServerError;
import io.realm.SyncCredentials;
import io.realm.SyncUser;
import timber.log.Timber;

/**
 * Created by sean1 on 1/15/2017.
 */

public class SplashActivity extends AppCompatActivity {
	private static String TAG = SplashActivity.class.getSimpleName();
	private static String DEBUG_USER = "sean1991stewart@gmail.com";
	private static String DEBUG_USER_PASSWORD = "xdfsj2eq";

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		ButterKnife.bind(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		SyncUser syncUser = SyncUser.currentUser();
		if (syncUser != null) {
			loginUserAndContinue(syncUser);
		} else {
			SyncUser.loginAsync(SyncCredentials.usernamePassword(DEBUG_USER, DEBUG_USER_PASSWORD, false), Constants.AUTH_URL, new SyncUser.Callback() {
				@Override
				public void onSuccess(SyncUser user) {
					loginUserAndContinue(user);
				}

				@Override
				public void onError(ObjectServerError error) {
					Timber.tag(TAG).e(error.getErrorMessage());
				}
			});
		}
	}

	private void loginUserAndContinue(SyncUser syncUser) {
		GatewayApplication application = (GatewayApplication) getApplication();
		if (application.getDefaultRealmConfig() == null) {
			application.setDefaultRealmConfig(syncUser);
		}

		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}
}
