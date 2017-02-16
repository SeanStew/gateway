package ca.gatewaybaptistchurch.gateway.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.gatewaybaptistchurch.gateway.GatewayFragment;
import ca.gatewaybaptistchurch.gateway.R;
import ca.gatewaybaptistchurch.gateway.activity.MainActivity;
import ca.gatewaybaptistchurch.gateway.adapter.MessageAdapter;
import ca.gatewaybaptistchurch.gateway.model.Podcast;
import ca.gatewaybaptistchurch.gateway.utils.Constants;
import ca.gatewaybaptistchurch.gateway.utils.Utils;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import timber.log.Timber;

import static ca.gatewaybaptistchurch.gateway.utils.Constants.Actions.PODCAST_STATE_UPDATE;

/**
 * Created by Sean on 5/29/2016.
 */
public class MessagesFragment extends GatewayFragment {
	//<editor-fold desc"View Initialization">
	@BindView(R.id.messagesFragment_emptyViewHolder) View emptyViewHolder;
	@BindView(R.id.messagesFragment_recyclerView) RecyclerView recyclerView;
	//</editor-fold>

	MainActivity activity;
	MessageAdapter adapter;
	RealmResults<Podcast> podcasts;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.content_messages, container, false);
		ButterKnife.bind(this, rootView);
		activity = (MainActivity) getActivity();
		//getPodcastTask.execute("http://gatewaybaptistchurch.ca/podcast/56b986a6-4ea1-4d20-bb6f-f0ad7e64f5c5.xml");
		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();
		registerReceivers();
		podcasts = Podcast.getPodcasts(realm);
		podcasts.addChangeListener(onPodcastsChanged);
		setupRecyclerView();
	}

	@Override
	public void onStop() {
		unregisterReceivers();
		super.onStop();
	}

	public void setupEmptyView() {
		recyclerView.setVisibility(View.GONE);
		emptyViewHolder.setVisibility(View.VISIBLE);
	}

	public void setupRecyclerView() {
		if (podcasts.isEmpty()) {
			setupEmptyView();
			return;
		}

		emptyViewHolder.setVisibility(View.GONE);
		recyclerView.setVisibility(View.VISIBLE);

		adapter = new MessageAdapter(getActivity(), podcasts, messageClickListener);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		recyclerView.setAdapter(adapter);
	}

	//<editor-fold desc="Listeners">
	RealmChangeListener<RealmResults<Podcast>> onPodcastsChanged = new RealmChangeListener<RealmResults<Podcast>>() {
		@Override
		public void onChange(RealmResults<Podcast> element) {
			if (emptyViewHolder.getVisibility() == View.VISIBLE && !podcasts.isEmpty()) {
				setupRecyclerView();
			} else if (emptyViewHolder.getVisibility() == View.GONE && podcasts.isEmpty()) {
				setupEmptyView();
			}
		}
	};

	MessageAdapter.MessageViewHolder.MessageViewHolderClickListener messageClickListener = new MessageAdapter.MessageViewHolder.MessageViewHolderClickListener() {
		@Override
		public void onPlayClicked(String podcastUrl) {
			Timber.d("play: %s", podcastUrl);
			if (getActivity() == null) {
				return;
			}

			activity.podcastSelected(podcastUrl);
		}
	};
	//</editor-fold>

	//<editor-fold desc="Receivers">
	private void registerReceivers() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(PODCAST_STATE_UPDATE);
		getActivity().registerReceiver(broadcastReceiver, filter);
	}

	private void unregisterReceivers() {
		getActivity().unregisterReceiver(broadcastReceiver);
	}

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null || intent.getAction() == null) {
				return;
			}

			switch (intent.getAction()) {
				case PODCAST_STATE_UPDATE:
					Constants.PodcastState state = Utils.getPodcastStateFromIntent(intent);
					if (state == Constants.PodcastState.PLAYING) {
						adapter.setPlayingUrl(intent.getStringExtra(Constants.Extras.PODCAST_URL));
					} else {
						adapter.setPlayingUrl(null);
					}
					break;
			}
		}
	};
	//</editor-fold>

	AsyncTask<String, Void, Void> getPodcastTask = new AsyncTask<String, Void, Void>() {
		@Override
		protected Void doInBackground(String... url) {
			Realm realm = Realm.getDefaultInstance();
			try {
				DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
				Document document = documentBuilder.parse(new URL(url[0]).openStream());
				NodeList nodes = document.getElementsByTagName("item");
				for (int i = 0; i < nodes.getLength(); i++) {
					Podcast podcast = Podcast.parsePodcast(nodes.item(i));
					if (podcast != null) {
						realm.beginTransaction();
						realm.copyToRealmOrUpdate(podcast);
						realm.commitTransaction();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				realm.close();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void value) {
			MessagesFragment.this.podcasts = Podcast.getPodcasts(realm);
			setupRecyclerView();
		}
	};
}
