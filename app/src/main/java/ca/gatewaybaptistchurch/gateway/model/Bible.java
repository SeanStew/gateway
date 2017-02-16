package ca.gatewaybaptistchurch.gateway.model;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.annotations.PrimaryKey;

/**
 * Created by sean1 on 1/22/2017.
 */

public class Bible extends RealmObject {
	@PrimaryKey
	private long id;
	private String shortName;
	private String longName;
	private String description;
	private String locale;
	private int bookCount = 0;

	public static Bible createBible() {
		Bible bible = new Bible();
		String longName = "English Standard Version";

		bible.setLongName(longName);
		bible.setShortName("ESV");
		bible.setId(longName.hashCode());
		bible.setDescription("The English Standard Version (ESV) is a revision of the 1971 edition of the Revised Standard Version that employs an \"essentially literal\" translation philosophy.");
		bible.setLocale("en");
		bible.setBookCount(Book.bookNames.length);
		return bible;
	}

	public RealmResults<Book> getBooks(Realm realm) {
		return realm.where(Book.class).equalTo("bibleId", getId()).findAllSorted("number", Sort.ASCENDING);
	}

	//<editor-fold desc="Fetching">
	public static RealmResults<Bible> getBibles(Realm realm) {
		return realm.where(Bible.class).findAllSorted("longName");
	}

	public static Bible getBible(Realm realm, long id) {
		if (id == -1) {
			return realm.where(Bible.class).findFirst();
		}

		return realm.where(Bible.class).equalTo("id", id).findFirst();
	}

	public Book getBook(Realm realm, int bookNumber) {
		return realm.where(Book.class).equalTo("bibleId", id).equalTo("number", bookNumber).findFirst();
		//return getBooks().where().equalTo("number", bookNumber).findFirst();
	}
	//</editor-fold>

	//<editor-fold desc="Getter and Setters">
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getLongName() {
		return longName;
	}

	public void setLongName(String longName) {
		this.longName = longName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public int getBookCount() {
		return bookCount;
	}

	public void setBookCount(int bookCount) {
		this.bookCount = bookCount;
	}
	//</editor-fold>
}
