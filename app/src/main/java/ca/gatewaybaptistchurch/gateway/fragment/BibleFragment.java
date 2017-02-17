package ca.gatewaybaptistchurch.gateway.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.gatewaybaptistchurch.gateway.GatewayApplication;
import ca.gatewaybaptistchurch.gateway.GatewayFragment;
import ca.gatewaybaptistchurch.gateway.R;
import ca.gatewaybaptistchurch.gateway.model.Bible;
import ca.gatewaybaptistchurch.gateway.model.Book;
import ca.gatewaybaptistchurch.gateway.model.Chapter;
import ca.gatewaybaptistchurch.gateway.utils.AppValues;
import ca.gatewaybaptistchurch.gateway.utils.Constants;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmModel;
import timber.log.Timber;

import static ca.gatewaybaptistchurch.gateway.utils.Constants.Actions.BIBLE_CHAPTER_CARD_UPDATE;
import static ca.gatewaybaptistchurch.gateway.utils.Constants.Actions.BIBLE_CHAPTER_UPDATE_REQUEST;

/**
 * Created by Sean on 5/29/2016.
 */
public class BibleFragment extends GatewayFragment {
	private static final String TAG = BibleFragment.class.getSimpleName();
	//<editor-fold desc"View Initialization">
	@BindView(R.id.bibleFragment_webView) WebView webView;
	@BindView(R.id.bibleFragment_scrollView) NestedScrollView scrollView;
	//</editor-fold>

	Realm bibleRealm;
	Bible bible;
	Book book;
	Chapter chapter;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.content_bible, container, false);
		ButterKnife.bind(this, rootView);
		setHasOptionsMenu(true);
		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();
		bibleRealm = Realm.getInstance(GatewayApplication.getBibleConfig());
		getBible();
		registerReceivers();
	}

	@Override
	public void onStop() {
		bibleRealm.close();
		unregisterReceivers();
		super.onStop();
	}

	private void getBible() {
		bible = Bible.getBible(bibleRealm, -1);
		if (bible == null) {
			return;
		}
		bible.addChangeListener(new RealmChangeListener<RealmModel>() {
			@Override
			public void onChange(RealmModel element) {
				if (!bible.isValid()) {
					getBible();
					return;
				}
				setupDetails();
			}
		});

		setupDetails();
	}

	private void setupDetails() {
		book = bible.getBook(bibleRealm, AppValues.getLastBibleBookNumber());
		if (book == null) {
			book = bible.getBook(bibleRealm, 1);
			if (book == null) {
				return;
			}
			AppValues.setLastBibleBookNumber(1);
		}

		chapter = book.getChapter(bibleRealm, AppValues.getLastBibleChapterNumber());
		if (chapter == null) {
			chapter = book.getChapter(bibleRealm, 1);
			if (chapter == null) {
				return;
			}
			AppValues.setLastBibleChapterNumber(1);
		}

//		bookButton.setText(String.valueOf(book.getName()));
//		chapterButton.setText(String.valueOf(chapter.getNumber()));
		updateBibleBottomCard();
		setupWebView();
	}

	private void setupWebView() {
		webView.getSettings().setTextZoom(110);
		webView.loadDataWithBaseURL("", chapter.getChapterText(), "text/html", "UTF-8", "");
		scrollView.scrollTo(0, 0);
	}

	private void updateBibleBottomCard() {
		if (book == null || chapter == null || bible == null) {
			return;
		}

		Intent intent = new Intent(BIBLE_CHAPTER_CARD_UPDATE);
		intent.putExtra(Constants.Extras.CHAPTER_TEXT_CHANGED, String.format("%s %s %s", book.getName(), chapter.getNumber(), bible.getShortName()));
		getActivity().sendBroadcast(intent);

		getActivity().invalidateOptionsMenu();
	}

	private void goToNextChapter(boolean nextChapter) {
		int nextChapterNumber = 1;
		if (nextChapter) {
			nextChapterNumber = chapter.getNumber() + 1;
			if (nextChapterNumber > book.getChapterCount()) {
				goToNextBook(true);
				return;
			}
			AppValues.setLastBibleChapterNumber(nextChapterNumber);
		} else {
			nextChapterNumber = chapter.getNumber() - 1;
			if (nextChapterNumber < 1) {
				goToNextBook(false);
				return;
			}
		}
		AppValues.setLastBibleChapterNumber(nextChapterNumber);
		setupDetails();
	}

	private void goToNextBook(boolean nextBook) {
		int nextBookNumber;
		int nextChapterNumber = 1;
		if (nextBook) {
			nextBookNumber = book.getNumber() + 1;
			if (nextBookNumber > bible.getBookCount()) {
				nextBookNumber = 1;
			}
		} else {
			nextBookNumber = book.getNumber() - 1;
			if (nextBookNumber < 0) {
				nextBookNumber = bible.getBookCount();
			}
			nextChapterNumber = bible.getBook(bibleRealm, nextBookNumber).getChapterCount();
		}
		AppValues.setLastBibleChapterNumber(nextChapterNumber);
		AppValues.setLastBibleBookNumber(nextBookNumber);
		setupDetails();
	}

	//<editor-fold desc="Option Menu">
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.bible_menu, menu);
		MenuItem item = menu.findItem(R.id.action_chapterSelection);
		item.setActionView(R.layout.menu_chapter_selection);

		item.getActionView().findViewById(R.id.menuChapterSelection_bookSelection).setOnClickListener(onChapterSelectionClicked);
		item.getActionView().findViewById(R.id.menuChapterSelection_chapterSelection).setOnClickListener(onChapterSelectionClicked);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		if (chapter == null || book == null) {
			return;
		}

		MenuItem item = menu.findItem(R.id.action_chapterSelection);
		item.setActionView(R.layout.menu_chapter_selection);

		((TextView) item.getActionView().findViewById(R.id.menuChapterSelection_bookSelectionText)).setText(book.getAbbreviation());
		((TextView) item.getActionView().findViewById(R.id.menuChapterSelection_chapterSelectionText)).setText(String.valueOf(chapter.getNumber()));
	}

	//</editor-fold>

	//<editor-fold desc="Receivers">
	private void registerReceivers() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(BIBLE_CHAPTER_UPDATE_REQUEST);
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
				case BIBLE_CHAPTER_UPDATE_REQUEST:
					if (intent.hasExtra(Constants.Extras.NEXT_CHAPTER)) {
						goToNextChapter(intent.getBooleanExtra(Constants.Extras.NEXT_CHAPTER, true));
					}
					break;
			}
		}
	};
	//</editor-fold>

	//<editor-fold desc="Listeners">
	View.OnClickListener onChapterSelectionClicked = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Timber.tag(TAG).d("clicked %s", v.getId());
		}
	};
	//</editor-fold>
}
