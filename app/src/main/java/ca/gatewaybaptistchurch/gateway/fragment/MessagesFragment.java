package ca.gatewaybaptistchurch.gateway.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.gatewaybaptistchurch.gateway.R;
import ca.gatewaybaptistchurch.gateway.adapter.MessageAdapter;
import ca.gatewaybaptistchurch.gateway.model.Podcast;
import timber.log.Timber;

/**
 * Created by Sean on 5/29/2016.
 */
public class MessagesFragment extends Fragment {
	//<editor-fold desc"View Initialization">
	@BindView(R.id.messagesFragment_emptyViewHolder) View emptyViewHolder;
	@BindView(R.id.messagesFragment_recyclerView) RecyclerView recyclerView;
	@BindView(R.id.messagesFragment_emptyViewIcon) MaterialIconView emptyViewIcon;
	@BindView(R.id.messagesFragment_emptyViewText) TextView emptyViewText;
	//</editor-fold>

	MessageAdapter adapter;
	List<Podcast> podcasts = new ArrayList<>();

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.content_messages, container, false);
		ButterKnife.bind(this, rootView);
		getPodcastTask.execute("http://gatewaybaptistchurch.ca/podcast/56b986a6-4ea1-4d20-bb6f-f0ad7e64f5c5.xml");
		return rootView;
	}

	public void setupEmptyView(boolean isLoading) {
		recyclerView.setVisibility(View.GONE);
		emptyViewHolder.setVisibility(View.VISIBLE);

		emptyViewText.setText(isLoading ? "Loading messages" : "No messages at this time");
		emptyViewIcon.setIcon(isLoading ? MaterialDrawableBuilder.IconValue.RELOAD : MaterialDrawableBuilder.IconValue.NEWSPAPER);
	}

	public void setupRecyclerView() {
		if (podcasts.isEmpty()) {
			setupEmptyView(false);
		}

		emptyViewHolder.setVisibility(View.GONE);
		recyclerView.setVisibility(View.VISIBLE);

		adapter = new MessageAdapter(podcasts, messageClickListener);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		recyclerView.setAdapter(adapter);
	}

	//<editor-fold desc="Listeners">
	MessageAdapter.MessageViewHolder.MessageViewHolderClickListener messageClickListener = new MessageAdapter.MessageViewHolder.MessageViewHolderClickListener() {
		@Override public void onPlayClicked(String podcastUrl) {
			Timber.d("play: %s", podcastUrl);
		}

		@Override public void onDownloadClicked(String podcastUrl) {
			Timber.d("download: %s", podcastUrl);
		}
	};
	//</editor-fold>

	AsyncTask<String, Void, List<Podcast>> getPodcastTask = new AsyncTask<String, Void, List<Podcast>>() {
		@Override protected List<Podcast> doInBackground(String... url) {
			List<Podcast> podcasts = new ArrayList<>();

			try {
				DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
				Document document = documentBuilder.parse(new URL(url[0]).openStream());
				NodeList nodes = document.getElementsByTagName("item");
				for (int i = 0; i < nodes.getLength(); i++) {
					Podcast podcast = Podcast.parsePodcast(nodes.item(i));
					if (podcast != null) {
						podcasts.add(podcast);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return podcasts;
		}

		@Override protected void onPostExecute(List<Podcast> podcasts) {
			MessagesFragment.this.podcasts = podcasts;
			setupRecyclerView();
		}
	};
}
