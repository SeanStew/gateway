package ca.gatewaybaptistchurch.gateway.model;

import org.w3c.dom.Element;

import io.realm.Realm;
import io.realm.RealmList;
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

	private RealmList<Verse> verses;

	public static Chapter createChapter(long bookId, Element chapterElement) {
		Chapter chapter = new Chapter();
		int number = -1;
		try {
			number = Integer.valueOf(chapterElement.getAttribute("cnumber"));
		} catch (Exception ignored) {
		}
		chapter.setId(bookId + number);
		chapter.setBookId(bookId);
		chapter.setNumber(number);
		return chapter;
	}

	public String getVersesFormatted() {
		String verseText = "";
		for (int i = 0; i < getVerses().size(); i++) {
			Verse verse = getVerses().get(i);
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

	public Verse getVerse(int verseNumber) {
		return getVerses().where().equalTo("number", verseNumber).findFirst();
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

	public RealmList<Verse> getVerses() {
		if (verses == null) {
			verses = new RealmList<>();
		}
		return verses;
	}

	public void setVerses(RealmList<Verse> verses) {
		this.verses = verses;
	}
	//</editor-fold>
}
