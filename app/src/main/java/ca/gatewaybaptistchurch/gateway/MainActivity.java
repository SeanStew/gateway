package ca.gatewaybaptistchurch.gateway;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.gatewaybaptistchurch.gateway.adapter.HomeAdapter;
import ca.gatewaybaptistchurch.gateway.view.NonSwipeableViewPager;

public class MainActivity extends AppCompatActivity {
	@BindView(R.id.drawer_layout) DrawerLayout drawer;
	@BindView(R.id.nav_view) NavigationView navigationView;
	@BindView(R.id.toolbar) Toolbar toolbar;
	@BindView(R.id.tab_layout) TabLayout tabLayout;
	@BindView(R.id.view_pager) NonSwipeableViewPager viewPager;
	@BindView(R.id.fab) FloatingActionButton fab;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.setDebug(true);
		ButterKnife.bind(this);

		setupViews();
		setupViewPager();
	}

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
		adapter.addFragment(new MessagesFragment(), "Bible");
		adapter.addFragment(new MessagesFragment(), "News");
		adapter.addFragment(new MessagesFragment(), "Connect");
		adapter.addFragment(new MessagesFragment(), "Give");
		viewPager.setAdapter(adapter);
		viewPager.setOffscreenPageLimit(4);
		tabLayout.setupWithViewPager(viewPager);

		tabLayout.getTabAt(0).setIcon(MaterialDrawableBuilder.with(this).setIcon(MaterialDrawableBuilder.IconValue.NEWSPAPER).setColorResource(R.color.icons).setToActionbarSize().build());
		tabLayout.getTabAt(1).setIcon(MaterialDrawableBuilder.with(this).setIcon(MaterialDrawableBuilder.IconValue.BOOK_OPEN_VARIANT).setColorResource(R.color.icons).setToActionbarSize().build());
		tabLayout.getTabAt(2).setIcon(MaterialDrawableBuilder.with(this).setIcon(MaterialDrawableBuilder.IconValue.CALENDAR_TEXT).setColorResource(R.color.icons).setToActionbarSize().build());
		tabLayout.getTabAt(3).setIcon(MaterialDrawableBuilder.with(this).setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_MULTIPLE).setColorResource(R.color.icons).setToActionbarSize().build());
		tabLayout.getTabAt(4).setIcon(MaterialDrawableBuilder.with(this).setIcon(MaterialDrawableBuilder.IconValue.CREDIT_CARD).setColorResource(R.color.icons).setToActionbarSize().build());
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

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	//Listeners
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
}
