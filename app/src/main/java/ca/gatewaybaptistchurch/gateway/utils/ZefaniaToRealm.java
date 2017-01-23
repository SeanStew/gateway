package ca.gatewaybaptistchurch.gateway.utils;

import android.os.AsyncTask;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import ca.gatewaybaptistchurch.gateway.GatewayApplication;
import ca.gatewaybaptistchurch.gateway.R;
import ca.gatewaybaptistchurch.gateway.model.Bible;
import ca.gatewaybaptistchurch.gateway.model.Book;
import ca.gatewaybaptistchurch.gateway.model.Chapter;
import ca.gatewaybaptistchurch.gateway.model.Verse;
import io.realm.Realm;
import timber.log.Timber;

/**
 * Created by sean1 on 1/22/2017.
 */

public class ZefaniaToRealm extends AsyncTask<Void, Void, Bible> {
	private static final String TAG = ZefaniaToRealm.class.getSimpleName();

	@Override
	protected Bible doInBackground(Void... params) {
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(GatewayApplication.getContext().getResources().openRawResource(R.raw.american_standard_version));
			NodeList infoNodes = document.getElementsByTagName("INFORMATION");
			if (infoNodes == null || infoNodes.getLength() <= 0) {
				return null;
			}
			Node bibleInfoNode = infoNodes.item(0);

			return buildBible(document, bibleInfoNode);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	protected void onPostExecute(final Bible aBible) {
		if (aBible == null) {
			Timber.tag(TAG).e("Did not create bible");
			return;
		}

		try (Realm realm = Realm.getDefaultInstance()) {
			realm.executeTransactionAsync(new Realm.Transaction() {
				@Override
				public void execute(Realm realm) {
					realm.copyToRealmOrUpdate(aBible);
				}
			});
		}
	}

	private Bible buildBible(Document document, Node bibleInfoNode) {
		if (!(bibleInfoNode instanceof Element)) {
			return null;
		}
		Element infoElement = (Element) bibleInfoNode;
		Bible bible = Bible.createBible(infoElement);

		NodeList bookNodes = document.getElementsByTagName("BIBLEBOOK");
		bible.setBookCount(bookNodes.getLength());
		Timber.tag(TAG).d("Built bible %s - bookCount %s", bible.getShortName(), bookNodes.getLength());
		for (int i = 0; i < bookNodes.getLength(); i++) {
			Book book = buildBook(bible, bookNodes.item(i));
			if (book == null) {
				Timber.tag(TAG).e("Failed to create book");
				continue;
			}
			bible.getBooks().add(book);
		}

		return bible;
	}

	private Book buildBook(Bible bible, Node bookNode) {
		if (!(bookNode instanceof Element)) {
			return null;
		}
		Element bookElement = (Element) bookNode;
		Book book = Book.createBook(bible.getId(), bookElement);

		NodeList chapterNodes = bookElement.getElementsByTagName("CHAPTER");
		book.setChapterCount(chapterNodes.getLength());
		Timber.tag(TAG).d("Built book %s - chapterCount %s", book.getAbbreviation(), chapterNodes.getLength());
		for (int i = 0; i < chapterNodes.getLength(); i++) {
			Node chapterNode = chapterNodes.item(i);
			Chapter chapter = buildChapter(book.getId(), chapterNode);
			if (chapter == null) {
				Timber.tag(TAG).e("Failed to create Book");
				continue;
			}
			book.getChapters().add(chapter);
		}
		return book;
	}

	private Chapter buildChapter(long bookId, Node chapterNode) {
		if (!(chapterNode instanceof Element)) {
			return null;
		}
		Element chapterElement = (Element) chapterNode;
		Chapter chapter = Chapter.createChapter(bookId, chapterElement);

		NodeList verseNodes = chapterElement.getElementsByTagName("VERS");
		chapter.setVerseCount(verseNodes.getLength());
		Timber.tag(TAG).d("Build chapter %s - verseCount = %s", chapter.getNumber(), verseNodes.getLength());
		for (int i = 0; i < verseNodes.getLength(); i++) {
			Node verseNode = verseNodes.item(i);
			Verse verse = buildVerse(chapter.getId(), verseNode);
			if (verse == null) {
				Timber.tag(TAG).e("Failed to create Chapter");
				continue;
			}
			chapter.getVerses().add(verse);
		}

		return chapter;
	}

	private Verse buildVerse(long chapterId, Node verseNode) {
		if (!(verseNode instanceof Element)) {
			Timber.tag(TAG).e("Failed to create Verse");
			return null;
		}
		Element verseElement = (Element) verseNode;
		return Verse.createVerse(chapterId, verseElement);
	}
}
