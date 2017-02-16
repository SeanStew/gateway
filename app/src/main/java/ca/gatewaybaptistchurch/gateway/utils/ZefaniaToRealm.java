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

			try (Realm realm = Realm.getInstance(GatewayApplication.getBibleConfig())) {
				return buildBible(realm, document, bibleInfoNode);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	protected void onPostExecute(final Bible aBible) {
		if (aBible == null) {
			Timber.tag(TAG).e("Did not create bible");
		}
	}

	private Bible buildBible(Realm realm, Document document, Node bibleInfoNode) {
		if (!(bibleInfoNode instanceof Element)) {
			return null;
		}
		Element infoElement = (Element) bibleInfoNode;
		Bible bible = Bible.createBible(infoElement);

		NodeList bookNodes = document.getElementsByTagName("BIBLEBOOK");
		bible.setBookCount(bookNodes.getLength());
		Timber.tag(TAG).d("Built bible %s - bookCount %s", bible.getShortName(), bookNodes.getLength());
		for (int i = 0; i < bookNodes.getLength(); i++) {
			buildBook(realm, bible.getId(), bookNodes.item(i));
		}
		realm.beginTransaction();
		realm.copyToRealmOrUpdate(bible);
		realm.commitTransaction();
		return bible;
	}

	private void buildBook(Realm realm, long bibleId, Node bookNode) {
		Element bookElement = (Element) bookNode;
		Book book = Book.createBook(bibleId, bookElement);

		NodeList chapterNodes = bookElement.getElementsByTagName("CHAPTER");
		book.setChapterCount(chapterNodes.getLength());
		Timber.tag(TAG).d("Built book %s - chapterCount %s", book.getAbbreviation(), chapterNodes.getLength());
		for (int i = 0; i < chapterNodes.getLength(); i++) {
			buildChapter(realm, book.getId(), chapterNodes.item(i));
		}
		realm.beginTransaction();
		realm.copyToRealmOrUpdate(book);
		realm.commitTransaction();
	}

	private void buildChapter(Realm realm, long bookId, Node chapterNode) {
		if (!(chapterNode instanceof Element)) {
			return;
		}
		Element chapterElement = (Element) chapterNode;
		Chapter chapter = Chapter.createChapter(bookId, chapterElement);

		NodeList verseNodes = chapterElement.getElementsByTagName("VERS");
		chapter.setVerseCount(verseNodes.getLength());
		Timber.tag(TAG).d("Build chapter %s - verseCount = %s", chapter.getNumber(), verseNodes.getLength());
		for (int i = 0; i < verseNodes.getLength(); i++) {
			buildVerse(realm, chapter.getId(), verseNodes.item(i));
		}
		realm.beginTransaction();
		realm.copyToRealmOrUpdate(chapter);
		realm.commitTransaction();
	}

	private void buildVerse(Realm realm, long chapterId, Node verseNode) {
		if (!(verseNode instanceof Element)) {
			Timber.tag(TAG).e("Failed to create Verse");
			return;
		}
		Element verseElement = (Element) verseNode;
		Verse verse = Verse.createVerse(realm, chapterId, verseElement);
		Timber.tag(TAG).d("Build verse %s - chapter %s", verse.getNumber(), verse.getChapterId());
		realm.beginTransaction();
		realm.copyToRealmOrUpdate(verse);
		realm.commitTransaction();
	}
}
