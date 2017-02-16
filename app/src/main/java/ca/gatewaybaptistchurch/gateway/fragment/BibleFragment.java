package ca.gatewaybaptistchurch.gateway.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import ca.gatewaybaptistchurch.gateway.GatewayApplication;
import ca.gatewaybaptistchurch.gateway.GatewayFragment;
import ca.gatewaybaptistchurch.gateway.R;
import ca.gatewaybaptistchurch.gateway.model.Bible;
import ca.gatewaybaptistchurch.gateway.model.Book;
import ca.gatewaybaptistchurch.gateway.model.Chapter;
import ca.gatewaybaptistchurch.gateway.utils.AppValues;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmModel;

/**
 * Created by Sean on 5/29/2016.
 */
public class BibleFragment extends GatewayFragment {
	//<editor-fold desc"View Initialization">
	@BindView(R.id.bibleFragment_emptyViewHolder) View emptyView;
	@BindView(R.id.bibleFragment_webView) WebView webView;
	@BindView(R.id.bibleFragment_controlHolder) View controlHolder;
	@BindView(R.id.bibleFragment_bookButton) Button bookButton;
	@BindView(R.id.bibleFragment_chapterButton) Button chapterButton;
	//</editor-fold>

	Realm bibleRealm;
	Bible bible;
	Book book;
	Chapter chapter;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.content_bible, container, false);
		ButterKnife.bind(this, rootView);
		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();
		bibleRealm = Realm.getInstance(GatewayApplication.getBibleConfig());
		getBible();
	}

	@Override
	public void onStop() {
		super.onStop();
		bibleRealm.close();
	}

	private void getBible() {
		bible = Bible.getBible(bibleRealm, -1);
		if (bible == null) {
			setupEmptyView();
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

	private void setupEmptyView() {
		emptyView.setVisibility(View.VISIBLE);
		controlHolder.setVisibility(View.GONE);
		webView.setVisibility(View.GONE);
	}

	private void setupDetails() {
		emptyView.setVisibility(View.GONE);
		controlHolder.setVisibility(View.VISIBLE);
		webView.setVisibility(View.VISIBLE);

		book = bible.getBook(bibleRealm, AppValues.getLastBibleBookNumber());
		if (book == null) {
			book = bible.getBook(bibleRealm, 1);
			if (book == null) {
				setupEmptyView();
				return;
			}
			AppValues.setLastBibleBookNumber(1);
		}

		chapter = book.getChapter(bibleRealm, AppValues.getLastBibleChapterNumber());
		if (chapter == null) {
			chapter = book.getChapter(bibleRealm, 1);
			if (chapter == null) {
				setupEmptyView();
				return;
			}
			AppValues.setLastBibleChapterNumber(1);
		}

		bookButton.setText(String.valueOf(book.getName()));
		chapterButton.setText(String.valueOf(chapter.getNumber()));

		// Enable Javascript
		webView.getSettings().setTextZoom(110);

		// Load a webpage
		webView.loadDataWithBaseURL("", chapter.getChapterText(), "text/html", "UTF-8", "");
	}

	//<editor-fold desc="Listeners">
	@OnClick(R.id.bibleFragment_bookButton)
	public void onBookButtonClicked() {
		int nextBookNumber = book.getNumber() + 1;
		if (bible.getBookCount() < nextBookNumber) {
			nextBookNumber = 1;
		}
		AppValues.setLastBibleBookNumber(nextBookNumber);
		setupDetails();
	}

	@OnLongClick(R.id.bibleFragment_bookButton)
	public boolean onBookButtonLongClicked() {
		int previousBookNumber = book.getNumber() - 1;
		if (previousBookNumber < 1) {
			previousBookNumber = bible.getBookCount();
		}
		AppValues.setLastBibleBookNumber(previousBookNumber);
		setupDetails();
		return true;
	}

	@OnClick(R.id.bibleFragment_chapterButton)
	public void onChapterButtonClicked() {
		int nextChapterNumber = chapter.getNumber() + 1;
		if (book.getChapterCount() < nextChapterNumber) {
			nextChapterNumber = 1;
		}
		AppValues.setLastBibleChapterNumber(nextChapterNumber);

		setupDetails();
	}

	@OnLongClick(R.id.bibleFragment_chapterButton)
	public boolean onChapterButtonLongClicked() {
		int previousChapterNumber = chapter.getNumber() - 1;
		if (previousChapterNumber < 1) {
			previousChapterNumber = book.getChapterCount();
		}
		AppValues.setLastBibleChapterNumber(previousChapterNumber);

		setupDetails();
		return true;
	}
	//</editor-fold>
}
