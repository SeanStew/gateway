package ca.gatewaybaptistchurch.gateway.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.gatewaybaptistchurch.gateway.R;

/**
 * Created by Sean on 5/29/2016.
 */
public class MessagesFragment extends Fragment {
	public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.content_messages, container, false);
		return rootView;
	}
}
