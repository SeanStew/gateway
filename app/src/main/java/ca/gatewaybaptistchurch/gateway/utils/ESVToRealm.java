package ca.gatewaybaptistchurch.gateway.utils;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import ca.gatewaybaptistchurch.gateway.GatewayApplication;
import ca.gatewaybaptistchurch.gateway.model.Bible;
import ca.gatewaybaptistchurch.gateway.model.Book;
import ca.gatewaybaptistchurch.gateway.model.Chapter;
import io.realm.Realm;
import timber.log.Timber;

/**
 * Created by sean1 on 1/22/2017.
 */

public class ESVToRealm extends AsyncTask<Void, Void, Bible> {
	private static final String TAG = ESVToRealm.class.getSimpleName();

	@Override
	protected Bible doInBackground(Void... voids) {
		try {
			Bible bible = Bible.createBible();
			List<Book> books = Book.createBooksOfBible();
			List<Chapter> chapters = new ArrayList<>();
			for (Book book : books) {
				for (int i = 1; i <= book.getChapterCount(); i++) {
					Document doc = Jsoup.connect(String.format("http://www.esvapi.org/v2/rest/passageQuery?key=IP&include-short-copyright=false&include-audio-link=false&include-footnotes=false&include-passage-references=false&passage=%s+%s", book.getName(), i)).get();
					Chapter chapter = new Chapter();
					chapter.setId(book.getId() + i);
					chapter.setBookId(book.getId());
					chapter.setNumber(i);
					chapter.setVerseCount(doc.getElementsByAttributeValue("class", "verse-num").size() + 1);
					Elements elements = doc.getElementsByAttributeValue("class", "chapter-num");
					if (elements != null && elements.size() > 0) {
						elements.get(0).html("1&nbsp;");
					}
					chapter.setChapterText(doc.toString());
					chapters.add(chapter);
					Timber.tag(TAG).d("%s - %s", book.getAbbreviation(), i);
				}
			}

			try (Realm realm = Realm.getInstance(GatewayApplication.getBibleConfig())) {
				realm.beginTransaction();
				realm.copyToRealmOrUpdate(bible);
				realm.copyToRealmOrUpdate(books);
				realm.copyToRealmOrUpdate(chapters);
				realm.commitTransaction();
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
}
