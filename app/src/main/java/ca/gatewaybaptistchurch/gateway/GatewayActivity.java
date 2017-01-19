package ca.gatewaybaptistchurch.gateway;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import io.realm.Realm;

/**
 * Created by Sean on 1/8/2017.
 */

public class GatewayActivity extends AppCompatActivity {
	protected Realm realm;

	@Override protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		realm = Realm.getDefaultInstance();
	}

	@Override protected void onDestroy() {
		super.onDestroy();
		realm.close();
	}
}
