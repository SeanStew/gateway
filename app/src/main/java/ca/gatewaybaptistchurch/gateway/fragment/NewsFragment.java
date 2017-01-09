package ca.gatewaybaptistchurch.gateway.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.gatewaybaptistchurch.gateway.GatewayFragment;
import ca.gatewaybaptistchurch.gateway.R;
import ca.gatewaybaptistchurch.gateway.adapter.EventAdapter;
import ca.gatewaybaptistchurch.gateway.model.Event;
import ca.gatewaybaptistchurch.gateway.viewHolder.EventViewHolder;
import timber.log.Timber;

/**
 * Created by Sean on 5/29/2016.
 */
public class NewsFragment extends GatewayFragment {
	private DatabaseReference database;
	private EventAdapter adapter;

	//<editor-fold desc"View Initialization">
	@BindView(R.id.newsFragment_emptyViewHolder) View emptyViewHolder;
	@BindView(R.id.newsFragment_recyclerView) RecyclerView recyclerView;
	//</editor-fold>

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.content_news, container, false);
		ButterKnife.bind(this, rootView);
		emptyViewHolder.setVisibility(View.GONE);
		setupDatabase();
		return rootView;
	}

	private void setupDatabase() {
		if (database == null) {
			database = FirebaseDatabase.getInstance().getReference();
		}

		Query postQuery = database.child("events").orderByChild("timestamp");
		adapter = new EventAdapter(postQuery, eventItemClicked);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		recyclerView.setAdapter(adapter);
	}

	//<editor-fold desc="Listeners">
	EventViewHolder.OnEventClickListener eventItemClicked = new EventViewHolder.OnEventClickListener() {
		@Override public void onClick(Event event) {
			Timber.tag(NewsFragment.class.getSimpleName()).d("%s clicked", event.eventName);
		}
	};
	//</editor-fold>
}
