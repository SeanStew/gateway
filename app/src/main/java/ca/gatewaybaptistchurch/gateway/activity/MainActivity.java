package ca.gatewaybaptistchurch.gateway.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

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
import ca.gatewaybaptistchurch.gateway.utils.Utils;
import ca.gatewaybaptistchurch.gateway.view.NonSwipeableViewPager;

import static ca.gatewaybaptistchurch.gateway.utils.Constants.Actions.PODCAST_STATE_UPDATE;

public class MainActivity extends GatewayActivity {
	//<editor-fold desc="View Initialization">
	@BindView(R.id.drawer_layout) DrawerLayout drawer;
	@BindView(R.id.nav_view) NavigationView navigationView;
	@BindView(R.id.toolbar) Toolbar toolbar;
	@BindView(R.id.tab_layout) TabLayout tabLayout;
	@BindView(R.id.view_pager) NonSwipeableViewPager viewPager;

	@BindView(R.id.mainActivity_mediaCardView) View mediaBottomCard;
	@BindView(R.id.mainActivity_mediaImageView) ImageView mediaImageView;
	@BindView(R.id.mainActivity_mediaTextView) TextView mediaTextView;
	@BindView(R.id.mainActivity_mediaPlayButton) MaterialIconView mediaPlayButton;
	//</editor-fold>

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

	//<editor-fold desc="View Setup">
	private void setupViews() {
		setSupportActionBar(toolbar);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.addDrawerListener(toggle);
		toggle.syncState();

		navigationView.setNavigationItemSelectedListener(navigationItemSelectedListener);
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
		tabLayout.setupWithViewPager(viewPager);
		tabLayout.addOnTabSelectedListener(onTabSelected);

		setupIcon(0, MaterialDrawableBuilder.IconValue.NEWSPAPER, adapter.getPageTitle(0).toString());
		setupIcon(1, MaterialDrawableBuilder.IconValue.BOOK_OPEN_VARIANT, adapter.getPageTitle(1).toString());
		setupIcon(2, MaterialDrawableBuilder.IconValue.CALENDAR_TEXT, adapter.getPageTitle(2).toString());
		setupIcon(3, MaterialDrawableBuilder.IconValue.ACCOUNT_MULTIPLE, adapter.getPageTitle(3).toString());
		setupIcon(4, MaterialDrawableBuilder.IconValue.CREDIT_CARD, adapter.getPageTitle(4).toString());
		setTabSelected(tabLayout.getTabAt(0).getCustomView(), true);
	}

	private void setupIcon(int position, MaterialDrawableBuilder.IconValue iconValue, String title) {
		View rootView = LayoutInflater.from(this).inflate(R.layout.tab_custom, null);
		TextView textView = (TextView) rootView.findViewById(R.id.tab_text);
		MaterialIconView iconView = (MaterialIconView) rootView.findViewById(R.id.tab_icon);
		textView.setText(title);
		iconView.setIcon(iconValue);
		tabLayout.getTabAt(position).setCustomView(rootView);
	}

	private void setTabSelected(View rootView, boolean selected) {
		int color = selected ? getResources().getColor(R.color.icons) : getResources().getColor(R.color.iconsDisabled);
		TextView textView = (TextView) rootView.findViewById(R.id.tab_text);
		MaterialIconView iconView = (MaterialIconView) rootView.findViewById(R.id.tab_icon);

		textView.setTextColor(color);
		iconView.setColor(color);
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
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		if (id == R.id.action_settings) {
			//new ZefaniaToRealm().execute();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
	//</editor-fold>

	//<editor-fold desc="Receivers">
	private void registerReceivers() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(PODCAST_STATE_UPDATE);
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
			}
		}
	};

	private void updatePodcastState(Intent intent) {
		String podcastUrl = intent.getStringExtra(Constants.Extras.PODCAST_URL);
		Constants.PodcastState podcastState = Utils.getPodcastStateFromIntent(intent);

		Podcast podcast = Podcast.getPodcast(realm, podcastUrl);
		if (podcast == null || podcastState == Constants.PodcastState.STOPPED) {
			showBottomSheet(false);
			return;
		}

		showBottomSheet(true);
		Glide.with(MainActivity.this).load(podcast.getImageUrl()).into(mediaImageView);
		mediaPlayButton.setIcon(podcastState == Constants.PodcastState.PLAYING ? MaterialDrawableBuilder.IconValue.PAUSE : MaterialDrawableBuilder.IconValue.PLAY);
		mediaTextView.setText(podcast.getTitle());
		mediaTextView.setSelected(true);
	}

	private void showBottomSheet(boolean expand) {
		mediaBottomCard.setVisibility(expand ? View.VISIBLE : View.GONE);
	}
	//</editor-fold>

	//<editor-fold desc="Listeners">
	@OnClick(R.id.mainActivity_mediaPlayButton)
	public void onMediaPlayClicked() {
		podcastSelected(null);
	}

	TabLayout.OnTabSelectedListener onTabSelected = new TabLayout.OnTabSelectedListener() {
		@Override
		public void onTabSelected(TabLayout.Tab tab) {
			viewPager.setCurrentItem(tab.getPosition(), true);
			setTabSelected(tab.getCustomView(), true);
		}

		@Override
		public void onTabUnselected(TabLayout.Tab tab) {
			setTabSelected(tab.getCustomView(), false);
		}

		@Override
		public void onTabReselected(TabLayout.Tab tab) {

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
