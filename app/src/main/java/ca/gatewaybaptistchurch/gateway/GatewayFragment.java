package ca.gatewaybaptistchurch.gateway;

import android.support.v4.app.Fragment;

import io.realm.Realm;

/**
 * Created by Sean on 1/8/2017.
 */

public class GatewayFragment extends Fragment {
	protected Realm realm;

	@Override public void onStart() {
		super.onStart();
		realm = Realm.getDefaultInstance();
	}

	@Override public void onStop() {
		super.onStop();
		realm.close();
	}
}
