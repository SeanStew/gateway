package ca.gatewaybaptistchurch.gateway.model;

import org.w3c.dom.Element;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
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

	private RealmList<Book> books;

	public static Bible createBible(Element bibleInfoElement) {
		Bible bible = new Bible();
		String longName = bibleInfoElement.getAttribute("title");

		bible.setLongName(longName);
		bible.setShortName(bibleInfoElement.getAttribute("identifier"));
		bible.setDescription(bibleInfoElement.getAttribute("description"));
		bible.setLocale(bibleInfoElement.getAttribute("language"));
		bible.setId(longName.hashCode());
		return bible;
	}

	//<editor-fold desc="Fetching">
	public static Bible getBible(Realm realm, long id) {
		if (id == -1) {
			return realm.where(Bible.class).findFirst();
		}

		return realm.where(Bible.class).equalTo("id", id).findFirst();
	}

	public Book getBook(int bookNumber) {
		return getBooks().where().equalTo("number", bookNumber).findFirst();
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

	public RealmList<Book> getBooks() {
		if (books == null) {
			books = new RealmList<>();
		}
		return books;
	}

	public void setBooks(RealmList<Book> books) {
		this.books = books;
	}
	//</editor-fold>
}
