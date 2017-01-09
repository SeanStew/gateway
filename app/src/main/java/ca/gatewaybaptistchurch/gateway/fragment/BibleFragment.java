package ca.gatewaybaptistchurch.gateway.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.gatewaybaptistchurch.gateway.GatewayFragment;
import ca.gatewaybaptistchurch.gateway.R;

/**
 * Created by Sean on 5/29/2016.
 */
public class BibleFragment extends GatewayFragment {
	//<editor-fold desc"View Initialization">
	@BindView(R.id.bibleFragment_emptyViewHolder) View emptyViewHolder;
	//</editor-fold>

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.content_bible, container, false);
		ButterKnife.bind(this, rootView);
		return rootView;
	}
}
