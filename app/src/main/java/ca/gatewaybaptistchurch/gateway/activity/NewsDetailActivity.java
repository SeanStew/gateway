package ca.gatewaybaptistchurch.gateway.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ca.gatewaybaptistchurch.gateway.GatewayActivity;
import ca.gatewaybaptistchurch.gateway.R;
import ca.gatewaybaptistchurch.gateway.model.Event;
import ca.gatewaybaptistchurch.gateway.utils.Constants;
import ca.gatewaybaptistchurch.gateway.utils.Utils;
import ca.gatewaybaptistchurch.gateway.view.FixedRatioImageView;
import io.realm.RealmChangeListener;
import io.realm.RealmModel;

/**
 * Created by sean1 on 1/21/2017.
 */

public class NewsDetailActivity extends GatewayActivity {
	private static final String TAG = NewsDetailActivity.class.getSimpleName();
	//<editor-fold desc="View Initialization">
	@BindView(R.id.fab) FloatingActionButton fab;
	@BindView(R.id.toolbar_image) FixedRatioImageView toolbarImage;
	@BindView(R.id.toolbar) Toolbar toolbar;
	@BindView(R.id.toolbar_layout) CollapsingToolbarLayout collapsingToolbarLayout;

	@BindView(R.id.newsDetailActivity_timeCard) View timeCard;
	@BindView(R.id.newsDetailActivity_timeTextView) TextView timeTextView;
	@BindView(R.id.newsDetailActivity_detailTextView) TextView detailTextView;
	//</editor-fold>

	Event event;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_news_detail);
		ButterKnife.bind(this);

		if (!getEvent()) {
			Toast.makeText(this, "Unable to view event", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		event.addChangeListener(new RealmChangeListener<RealmModel>() {
			@Override
			public void onChange(RealmModel element) {
				setupToolbar();
				setupDetails();
			}
		});

		setupToolbar();
		setupDetails();
	}

	private boolean getEvent() {
		Intent intent = getIntent();
		if (intent == null) {
			return false;
		}
		String eventUuid = intent.getExtras().getString(Constants.Extras.EVENT_UUID, null);
		if (!Utils.isNotNullOrEmpty(eventUuid)) {
			return false;
		}

		event = Event.getEvent(realm, eventUuid);
		return event != null;
	}

	private void setupToolbar() {
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		if (Utils.isNotNullOrEmpty(event.getEventName())) {
			collapsingToolbarLayout.setTitle(event.getEventName());
			if (Utils.isNotNullOrEmpty(event.getImageUrl())) {
				toolbarImage.setVisibility(View.VISIBLE);
				Glide.with(getApplicationContext()).load(event.getImageUrl()).error(R.drawable.header_image).placeholder(R.drawable.header_image).into(toolbarImage);
				findViewById(R.id.toolbar_shadows).setVisibility(View.VISIBLE);
			}
		}
	}

	private void setupDetails() {
		timeCard.setVisibility(Utils.isTrueAndNotNull(event.isShowAtTopWithoutDate()) ? View.GONE : View.VISIBLE);
		timeTextView.setText(Utils.getFullDateString(event.getDate()));

		detailTextView.setText(getDetailText());

		if (Utils.isNotNullOrEmpty(event.getUrlLink())) {
			Drawable fabDrawable;
			if (event.getUrlLink().contains("@")) {
				fabDrawable = MaterialDrawableBuilder.with(this).setIcon(MaterialDrawableBuilder.IconValue.EMAIL).setColorResource(android.R.color.white).build();
			} else if (event.getUrlLink().startsWith("https://docs.google.com/forms")) {
				fabDrawable = MaterialDrawableBuilder.with(this).setIcon(MaterialDrawableBuilder.IconValue.PENCIL).setColorResource(android.R.color.white).build();
			} else {
				fabDrawable = MaterialDrawableBuilder.with(this).setIcon(MaterialDrawableBuilder.IconValue.GOOGLE_CHROME).setColorResource(android.R.color.white).build();
			}
			fab.setImageDrawable(fabDrawable);
			fab.show();
		} else {
			fab.hide();
		}
	}

	private String getDetailText() {
		String detailText = event.getLongDescription();

		if (Utils.isNotNullOrEmpty(event.getUrlTitle())) {
			detailText = String.format("%s\n\n%s", detailText, event.getUrlTitle());
		}

		return detailText;
	}

	//<editor-fold desc="Listeners">
	@OnClick(R.id.fab)
	public void onFabClicked() {
		if (!Utils.isNotNullOrEmpty(event.getUrlLink())) {
			return;
		}

		if (event.getUrlLink().contains("@")) {
			Intent send = new Intent(Intent.ACTION_SENDTO);
			String uriText = String.format("mailto:%s?subject=%s", Uri.encode(event.getUrlLink()), Uri.encode(event.getEventName()));
			Uri uri = Uri.parse(uriText);
			send.setData(uri);
			startActivity(Intent.createChooser(send, "Send email..."));
		} else {
			CustomTabsIntent customChromeTabIntent = new CustomTabsIntent.Builder().setToolbarColor(getResources().getColor(R.color.primary)).build();
			customChromeTabIntent.launchUrl(getApplicationContext(), Uri.parse(event.getUrlLink()));
		}
	}
	//</editor-fold>
}
