package ca.gatewaybaptistchurch.gateway.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ca.gatewaybaptistchurch.gateway.GatewayActivity;
import ca.gatewaybaptistchurch.gateway.R;
import ca.gatewaybaptistchurch.gateway.adapter.HomeAdapter;
import ca.gatewaybaptistchurch.gateway.fragment.BibleFragment;
import ca.gatewaybaptistchurch.gateway.fragment.ConnectFragment;
import ca.gatewaybaptistchurch.gateway.fragment.GiveFragment;
import ca.gatewaybaptistchurch.gateway.fragment.MessagesFragment;
import ca.gatewaybaptistchurch.gateway.fragment.NewsFragment;
import ca.gatewaybaptistchurch.gateway.model.Podcast;
import ca.gatewaybaptistchurch.gateway.service.MusicService;
import ca.gatewaybaptistchurch.gateway.utils.Constants;
import ca.gatewaybaptistchurch.gateway.utils.ESVToRealm;
import ca.gatewaybaptistchurch.gateway.utils.Utils;
import ca.gatewaybaptistchurch.gateway.view.NonSwipeableViewPager;
import io.realm.Realm;
import it.sephiroth.android.library.bottomnavigation.BottomNavigation;

import static ca.gatewaybaptistchurch.gateway.utils.Constants.Actions.BIBLE_CHAPTER_CARD_UPDATE;
import static ca.gatewaybaptistchurch.gateway.utils.Constants.Actions.PODCAST_STATE_UPDATE;

public class MainActivity extends GatewayActivity {
	//<editor-fold desc="View Initialization">
	@BindView(R.id.drawer_layout) DrawerLayout drawer;
	@BindView(R.id.nav_view) NavigationView navigationView;
	@BindView(R.id.toolbar) Toolbar toolbar;
	@BindView(R.id.mainActivity_bottomBar) BottomNavigation bottomNavigationView;
	@BindView(R.id.view_pager) NonSwipeableViewPager viewPager;

	@BindView(R.id.mainActivity_mediaCardView) View mediaBottomCard;
	@BindView(R.id.mainActivity_mediaImageView) ImageView mediaImageView;
	@BindView(R.id.mainActivity_mediaTextView) TextView mediaTextView;
	@BindView(R.id.mainActivity_mediaPlayButton) MaterialIconView mediaPlayButton;

	@BindView(R.id.mainActivity_bibleCardView) View bibleBottomCard;
	@BindView(R.id.mainActivity_bibleCardViewText) TextView bibleBottomCardText;
	@BindView(R.id.mainActivity_previousFab) FloatingActionButton previousFab;
	@BindView(R.id.mainActivity_nextFab) FloatingActionButton nextFab;
	//</editor-fold>

	//<editor-fold desc="Activity Lifecycle">
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);

		setupViews();
		setupViewPager();
	}

	@Override
	protected void onStart() {
		registerReceivers();
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		sendBroadcast(new Intent(Constants.Actions.REQUEST_PODCAST_STATE_UPDATE));
	}

	@Override
	protected void onStop() {
		unregisterReceivers();
		super.onStop();
	}

	@Override
	public void onBackPressed() {
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}
	//</editor-fold>

	//<editor-fold desc="View Setup">
	private void setupViews() {
		setSupportActionBar(toolbar);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.addDrawerListener(toggle);
		toggle.syncState();

		navigationView.setNavigationItemSelectedListener(navigationItemSelectedListener);

		previousFab.setImageDrawable(MaterialDrawableBuilder.with(this).setIcon(MaterialDrawableBuilder.IconValue.CHEVRON_LEFT).setColorResource(android.R.color.black).setSizeDp(24).build());
		nextFab.setImageDrawable(MaterialDrawableBuilder.with(this).setIcon(MaterialDrawableBuilder.IconValue.CHEVRON_RIGHT).setColorResource(android.R.color.black).setSizeDp(24).build());
	}

	private void setupViewPager() {
		HomeAdapter adapter = new HomeAdapter(getSupportFragmentManager());
		adapter.addFragment(new MessagesFragment(), "Messages");
		adapter.addFragment(new BibleFragment(), "Bible");
		adapter.addFragment(new NewsFragment(), "News");
		adapter.addFragment(new ConnectFragment(), "Connect");
		adapter.addFragment(new GiveFragment(), "Give");
		viewPager.setAdapter(adapter);
		viewPager.setOffscreenPageLimit(4);

		bottomNavigationView.setOnMenuItemClickListener(bottomMenuItemSelected);
	}
	//</editor-fold>

	public void podcastSelected(String podcastUrl) {
		Intent playerIntent = new Intent(this, MusicService.class);
		playerIntent.setAction(Constants.MediaActions.PLAY);
		if (podcastUrl != null) {
			playerIntent.putExtra(Constants.Extras.PODCAST_URL, podcastUrl);
		}
		startService(playerIntent);
	}

	//<editor-fold desc="Option Menu">
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_esvDownload:
				new ESVToRealm().execute();
				break;
			case R.id.action_updatePodcasts:
				getPodcastTask.execute("http://gatewaybaptistchurch.ca/podcast/56b986a6-4ea1-4d20-bb6f-f0ad7e64f5c5.xml");
				break;
		}

		return super.onOptionsItemSelected(item);
	}
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
	};

	//<editor-fold desc="Receivers">
	private void registerReceivers() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(PODCAST_STATE_UPDATE);
		filter.addAction(BIBLE_CHAPTER_CARD_UPDATE);
		registerReceiver(broadcastReceiver, filter);
	}

	private void unregisterReceivers() {
		unregisterReceiver(broadcastReceiver);
	}

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null || intent.getAction() == null) {
				return;
			}

			switch (intent.getAction()) {
				case PODCAST_STATE_UPDATE:
					updatePodcastState(intent);
					break;
				case BIBLE_CHAPTER_CARD_UPDATE:
					if (intent.hasExtra(Constants.Extras.CHAPTER_TEXT_CHANGED)) {
						bibleBottomCardText.setText(intent.getStringExtra(Constants.Extras.CHAPTER_TEXT_CHANGED));
					}
					break;
			}
		}
	};

	private void updatePodcastState(Intent intent) {
		String podcastUrl = intent.getStringExtra(Constants.Extras.PODCAST_URL);
		Constants.PodcastState podcastState = Utils.getPodcastStateFromIntent(intent);

		Podcast podcast = Podcast.getPodcast(realm, podcastUrl);
		if (podcast == null || podcastState == Constants.PodcastState.STOPPED) {
			showMediaBottomSheet(false);
			return;
		}

		showMediaBottomSheet(true);
		Glide.with(MainActivity.this).load(podcast.getImageUrl()).into(mediaImageView);
		mediaPlayButton.setIcon(podcastState == Constants.PodcastState.PLAYING ? MaterialDrawableBuilder.IconValue.PAUSE : MaterialDrawableBuilder.IconValue.PLAY);
		mediaTextView.setText(podcast.getTitle());
		mediaTextView.setSelected(true);
	}

	private void showMediaBottomSheet(boolean expand) {
		mediaBottomCard.setVisibility(expand ? View.VISIBLE : View.GONE);
	}
	//</editor-fold>

	//<editor-fold desc="Listeners">
	@OnClick(R.id.mainActivity_mediaPlayButton)
	public void onMediaPlayClicked() {
		podcastSelected(null);
	}

	@OnClick({R.id.mainActivity_previousFab, R.id.mainActivity_nextFab})
	public void onBibleFabClicked(View view) {
		Intent intent = new Intent(Constants.Actions.BIBLE_CHAPTER_UPDATE_REQUEST);
		switch (view.getId()) {
			case R.id.mainActivity_previousFab:
				intent.putExtra(Constants.Extras.NEXT_CHAPTER, false);
				break;
			case R.id.mainActivity_nextFab:
				intent.putExtra(Constants.Extras.NEXT_CHAPTER, true);
				break;
		}
		sendBroadcast(intent);
	}

	BottomNavigation.OnMenuItemSelectionListener bottomMenuItemSelected = new BottomNavigation.OnMenuItemSelectionListener() {
		@Override
		public void onMenuItemSelect(@IdRes final int resId, int i1, boolean b) {
			switch (resId) {
				case R.id.action_messages:
					viewPager.setCurrentItem(0, true);
					break;
				case R.id.action_bible:
					viewPager.setCurrentItem(1, true);
					break;
				case R.id.action_news:
					viewPager.setCurrentItem(2, true);
					break;
				case R.id.action_connect:
					viewPager.setCurrentItem(3, true);
					break;
				case R.id.action_give:
					viewPager.setCurrentItem(4, true);
					break;
			}

			if (resId == R.id.action_bible) {
				bibleBottomCard.setVisibility(View.VISIBLE);
				previousFab.show();
				nextFab.show();
			} else {
				bibleBottomCard.setVisibility(View.GONE);
				previousFab.hide();
				nextFab.hide();
			}
		}

		@Override
		public void onMenuItemReselect(@IdRes int i, int i1, boolean b) {

		}
	};

	NavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new NavigationView.OnNavigationItemSelectedListener() {
		@Override
		public boolean onNavigationItemSelected(MenuItem item) {
			// Handle navigation view item clicks here.
			int id = item.getItemId();
			if (id == R.id.nav_about) {

			} else if (id == R.id.nav_help) {

			} else if (id == R.id.nav_settings) {

			} else if (id == R.id.nav_share) {

			}

			DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
			drawer.closeDrawer(GravityCompat.START);
			return true;
		}
	};
	//</editor-fold>
}
