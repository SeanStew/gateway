package ca.gatewaybaptistchurch.gateway.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.gatewaybaptistchurch.gateway.GatewayFragment;
import ca.gatewaybaptistchurch.gateway.R;
import ca.gatewaybaptistchurch.gateway.activity.NewsDetailActivity;
import ca.gatewaybaptistchurch.gateway.adapter.EventAdapter;
import ca.gatewaybaptistchurch.gateway.model.Event;
import ca.gatewaybaptistchurch.gateway.utils.Constants;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by Sean on 5/29/2016.
 */
public class NewsFragment extends GatewayFragment {
	//<editor-fold desc"View Initialization">
	@BindView(R.id.newsFragment_emptyViewHolder) View emptyViewHolder;
	@BindView(R.id.newsFragment_recyclerView) RecyclerView recyclerView;
	//</editor-fold>

	EventAdapter adapter;
	RealmResults<Event> events;


	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.content_news, container, false);
		ButterKnife.bind(this, rootView);

		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();

		events = Event.getEvents(realm);
		events.addChangeListener(new RealmChangeListener<RealmResults<Event>>() {
			@Override
			public void onChange(RealmResults<Event> element) {
				if (emptyViewHolder.getVisibility() == View.VISIBLE && !events.isEmpty()) {
					setupRecyclerView();
				} else if (emptyViewHolder.getVisibility() == View.GONE && events.isEmpty()) {
					setupEmptyView();
				}
				events.removeChangeListener(this);
			}
		});

		setupRecyclerView();
	}

	public void setupEmptyView() {
		recyclerView.setVisibility(View.GONE);
		emptyViewHolder.setVisibility(View.VISIBLE);
	}

	public void setupRecyclerView() {
		if (events.isEmpty()) {
			setupEmptyView();
		}

		emptyViewHolder.setVisibility(View.GONE);
		recyclerView.setVisibility(View.VISIBLE);

		adapter = new EventAdapter(getActivity(), events, eventClickListener);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		recyclerView.setAdapter(adapter);
	}

	//<editor-fold desc="Listeners">
	EventAdapter.EventViewHolder.OnEventClickListener eventClickListener = new EventAdapter.EventViewHolder.OnEventClickListener() {
		@Override
		public void onClick(String eventUuid) {
			Event event = Event.getEvent(realm, eventUuid);
			if (event == null) {
				return;
			}

			Intent intent = new Intent(getContext(), NewsDetailActivity.class);
			intent.putExtra(Constants.Extras.EVENT_UUID, eventUuid);
			startActivity(intent);
		}
	};
	//</editor-fold>
}
