package ca.gatewaybaptistchurch.gateway.model;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.InputStream;

import ca.gatewaybaptistchurch.gateway.GatewayApplication;
import ca.gatewaybaptistchurch.gateway.R;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.annotations.PrimaryKey;

/**
 * Created by sean1 on 1/22/2017.
 */

public class Chapter extends RealmObject {
	@PrimaryKey
	private long id;
	private long bookId;
	private int number;
	private int verseCount = 0;
	private String chapterText;

	public static Chapter createChapter(long bookId, int number, String chapterText) {
		Chapter chapter = new Chapter();
		chapter.setId(bookId + number);
		chapter.setBookId(bookId);
		chapter.setNumber(number);
		chapter.setChapterText(chapterText);
		return chapter;
	}

	//<editor-fold desc="Fetching">
	public static Chapter getChaper(Realm realm, long id) {
		if (id == -1) {
			return realm.where(Chapter.class).findFirst();
		}

		return realm.where(Chapter.class).equalTo("id", id).findFirst();
	}

	public static RealmResults<Chapter> getChapters(Realm realm, long bookId) {
		return realm.where(Chapter.class).equalTo("bookId", bookId).findAllSorted("number", Sort.DESCENDING);
	}
	//</editor-fold>

	//<editor-fold desc="Getter and Setters">
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getBookId() {
		return bookId;
	}

	public void setBookId(long bookId) {
		this.bookId = bookId;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public int getVerseCount() {
		return verseCount;
	}

	public void setVerseCount(int verseCount) {
		this.verseCount = verseCount;
	}

	public String getChapterText() {
		try {
			Document document = Jsoup.parse(chapterText);
			InputStream inputStream = GatewayApplication.getContext().getResources().openRawResource(R.raw.style);
			byte[] buffer = new byte[inputStream.available()];
			inputStream.read(buffer);
			inputStream.close();
			Element head = document.head();
			head.append(new String(buffer));
			return document.toString();
		} catch (Exception ignored) {
		}

		return chapterText;
	}

	public void setChapterText(String chapterText) {
		this.chapterText = chapterText;
	}
	//</editor-fold>
}
