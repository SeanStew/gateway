package ca.gatewaybaptistchurch.gateway.fragment;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import ca.gatewaybaptistchurch.gateway.GatewayFragment;
import ca.gatewaybaptistchurch.gateway.R;
import ca.gatewaybaptistchurch.gateway.model.Bible;
import ca.gatewaybaptistchurch.gateway.model.Book;
import ca.gatewaybaptistchurch.gateway.model.Chapter;
import ca.gatewaybaptistchurch.gateway.utils.AppValues;
import io.realm.RealmChangeListener;
import io.realm.RealmModel;

/**
 * Created by Sean on 5/29/2016.
 */
public class BibleFragment extends GatewayFragment {
	//<editor-fold desc"View Initialization">
	@BindView(R.id.bibleFragment_emptyViewHolder) View emptyView;
	@BindView(R.id.bibleFragment_textView) TextView textView;
	@BindView(R.id.bibleFragment_controlHolder) View controlHolder;
	@BindView(R.id.bibleFragment_bookButton) Button bookButton;
	@BindView(R.id.bibleFragment_chapterButton) Button chapterButton;
	//</editor-fold>

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

		bible = Bible.getBible(realm, -1);
		bible.addChangeListener(new RealmChangeListener<RealmModel>() {
			@Override
			public void onChange(RealmModel element) {

			}
		});

		if (bible == null) {
			setupEmptyView();
			return;
		}

		setupDetails();
	}

	private void setupEmptyView() {
		emptyView.setVisibility(View.VISIBLE);
		controlHolder.setVisibility(View.GONE);
		textView.setVisibility(View.GONE);
	}

	private void setupDetails() {
		emptyView.setVisibility(View.GONE);
		controlHolder.setVisibility(View.VISIBLE);
		textView.setVisibility(View.VISIBLE);

		book = bible.getBook(AppValues.getLastBibleBookNumber());
		if (book == null) {
			book = bible.getBook(1);
			if (book == null) {
				setupEmptyView();
				return;
			}
			AppValues.setLastBibleBookNumber(1);
		}

		chapter = book.getChapter(AppValues.getLastBibleChapterNumber());
		if (chapter == null) {
			chapter = book.getChapter(1);
			if (chapter == null) {
				setupEmptyView();
				return;
			}
			AppValues.setLastBibleChapterNumber(1);
		}

		bookButton.setText(String.valueOf(book.getName()));
		chapterButton.setText(String.valueOf(chapter.getNumber()));
		textView.setText(Html.fromHtml(chapter.getVersesFormatted()));
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
