package ca.gatewaybaptistchurch.gateway.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.gatewaybaptistchurch.gateway.GatewayFragment;
import ca.gatewaybaptistchurch.gateway.R;
import ca.gatewaybaptistchurch.gateway.adapter.ConnectAdapter;
import ca.gatewaybaptistchurch.gateway.model.ConnectCategory;

/**
 * Created by Sean on 5/29/2016.
 */
public class ConnectFragment extends GatewayFragment {
	//<editor-fold desc="View Initialization">
	@BindView(R.id.connectFragment_recyclerView) RecyclerView recyclerView;
	//</editor-fold>

	ConnectAdapter adapter;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.content_connect, container, false);
		ButterKnife.bind(this, rootView);
		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();

		List<ConnectCategory> categories = new ArrayList<>();
		categories.add(new ConnectCategory(ConnectCategory.Category.PRAYER_REQUEST, R.drawable.prayer_requests));
		categories.add(new ConnectCategory(ConnectCategory.Category.SPIRITUAL_GIFTS, R.drawable.spiritual_gifts));
		categories.add(new ConnectCategory(ConnectCategory.Category.GROUPS, R.drawable.small_groups));
		categories.add(new ConnectCategory(ConnectCategory.Category.NEW_FAITH, R.drawable.new_life));

		if (adapter == null) {
			adapter = new ConnectAdapter(categories, listItemListener);
			recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
			recyclerView.setAdapter(adapter);
		}
	}

	ConnectAdapter.ConnectViewHolder.ConnectViewHolderClickListener listItemListener = new ConnectAdapter.ConnectViewHolder.ConnectViewHolderClickListener() {
		@Override
		public void onCategorySelected(ConnectCategory.Category category) {

		}
	};
}
