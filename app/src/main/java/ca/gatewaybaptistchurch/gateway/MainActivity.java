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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

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
        tabLayout.setOnTabSelectedListener(onTabSelected);

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

    private void setTabSelected(View rootView, boolean selected){
        int color = selected ? getResources().getColor(R.color.icons) : getResources().getColor(R.color.iconsDisabled);
        TextView textView = (TextView) rootView.findViewById(R.id.tab_text);
        MaterialIconView iconView = (MaterialIconView) rootView.findViewById(R.id.tab_icon);

        textView.setTextColor(color);
        iconView.setColor(color);
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

    //<editor-fold desc="Listeners">
    TabLayout.OnTabSelectedListener onTabSelected = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
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
