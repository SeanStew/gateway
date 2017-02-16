package ca.gatewaybaptistchurch.gateway.model;

import org.w3c.dom.Element;

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
	private Long bookId;
	private Integer number;
	private int verseCount = 0;

	public static Chapter createChapter(long bookId, Element chapterElement) {
		Chapter chapter = new Chapter();
		int number = -1;
		try {
			String sNumber = chapterElement.getAttribute("osisID").replaceAll("[^0-9]", "");
			number = Integer.valueOf(sNumber);
		} catch (Exception ignored) {
		}
		chapter.setId(bookId + number);
		chapter.setBookId(bookId);
		chapter.setNumber(number);
		return chapter;
	}

	public String getVersesFormatted(Realm realm) {
		String verseText = "";
		RealmResults<Verse> verses = getVerses(realm);
		for (int i = 0; i < verses.size(); i++) {
			Verse verse = verses.get(i);
			verseText = String.format("%s<sub>%s</sub>%s", verseText, verse.getNumber(), verse.getText());
		}

		return verseText;
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

	public RealmResults<Verse> getVerses(Realm realm) {
		return realm.where(Verse.class).equalTo("chapterId", getId()).findAllSorted("number", Sort.ASCENDING);
	}

	public Verse getVerse(Realm realm, int verseNumber) {
		return realm.where(Verse.class).equalTo("chapterId", getId()).equalTo("number", verseNumber).findFirst();
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
		if (bookId == null) {
			bookId = -1L;
		}
		return bookId;
	}

	public void setBookId(long bookId) {
		this.bookId = bookId;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public int getVerseCount() {
		return verseCount;
	}

	public void setVerseCount(int verseCount) {
		this.verseCount = verseCount;
	}
	//</editor-fold>
}
