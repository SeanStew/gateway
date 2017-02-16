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

public class Book extends RealmObject {
	@PrimaryKey
	private long id;
	private Long bibleId;
	private Integer number;
	private String name;
	private String abbreviation;
	private int chapterCount = 0;

	public static Book createBook(long bibleId, Element bookElement) {
		Book book = new Book();
		String name = bookElement.getAttribute("name");
		book.setId(bibleId + name.hashCode());
		book.setBibleId(bibleId);
		book.setName(name);
		book.setAbbreviation(bookElement.getAttribute("osisID"));
		try {
			book.setNumber(Integer.valueOf(bookElement.getAttribute("number")));
		} catch (Exception ignored) {
		}
		return book;
	}

	//<editor-fold desc="Fetching">
	public static Book getBook(Realm realm, long id) {
		if (id == -1) {
			return realm.where(Book.class).findFirst();
		}

		return realm.where(Book.class).equalTo("id", id).findFirst();
	}

	public RealmResults<Chapter> getChapters(Realm realm) {
		return realm.where(Chapter.class).equalTo("bookId", getId()).findAllSorted("number", Sort.ASCENDING);
	}

	public Chapter getChapter(Realm realm, int chapterNumber) {
		return realm.where(Chapter.class).equalTo("bookId", id).equalTo("number", chapterNumber).findFirst();
	}
	//</editor-fold>

	//<editor-fold desc="Getter and Setters">
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getBibleId() {
		if (bibleId == null) {
			bibleId = -1L;
		}
		return bibleId;
	}

	public void setBibleId(long bibleId) {
		this.bibleId = bibleId;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public int getChapterCount() {
		return chapterCount;
	}

	public void setChapterCount(int chapterCount) {
		this.chapterCount = chapterCount;
	}
	//</editor-fold>
}
