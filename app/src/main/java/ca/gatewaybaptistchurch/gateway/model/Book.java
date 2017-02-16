package ca.gatewaybaptistchurch.gateway.model;

import java.util.ArrayList;
import java.util.List;

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
	private long bibleId;
	private int number;
	private String name;
	private String abbreviation;
	private int chapterCount = 0;

	public static final String[] bookNames = new String[]{
			"Genesis", "Exodus", "Leviticus", "Numbers",
			"Deuteronomy", "Joshua", "Judges", "Ruth",
			"1 Samuel", "2 Samuel", "1 Kings", "2 Kings",
			"1 Chronicles", "2 Chronicles", "Ezra", "Nehemiah",
			"Esther", "Job", "Psalm", "Proverbs",
			"Ecclesiastes", "Song of Solomon", "Isaiah", "Jeremiah",
			"Lamentations", "Ezekiel", "Daniel", "Hosea",
			"Joel", "Amos", "Obadiah", "Jonah",
			"Micah", "Nahum", "Habakkuk", "Zephaniah",
			"Haggai", "Zechariah", "Malachi", "Matthew",
			"Mark", "Luke", "John", "Acts",
			"Romans", "1 Corinthians", "2 Corinthians", "Galatians",
			"Ephesians", "Philippians", "Colossians", "1 Thessalonians",
			"2 Thessalonians", "1 Timothy", "2 Timothy", "Titus",
			"Philemon", "Hebrews", "James", "1 Peter",
			"2 Peter", "1 John", "2 John", "3 John",
			"Jude", "Revelation"};

	private static final int[] chaperCount = new int[]{
			50, 40, 27, 36,
			34, 24, 21, 4,
			31, 24, 22, 25,
			29, 36, 10, 13,
			10, 42, 150, 31,
			12, 8, 66, 52,
			5, 48, 12, 14,
			3, 9, 1, 4,
			7, 3, 3, 3,
			2, 14, 4, 28,
			16, 24, 21, 28,
			16, 16, 13, 6,
			6, 4, 4, 5,
			3, 6, 4, 3,
			1, 13, 5, 5,
			3, 5, 1, 1,
			1, 22};

	private static final String[] bookAbbreviations = new String[]{
			"Gen.", "Ex.", "Lev.", "Num.",
			"Deut.", "Josh.", "Judg.", "Ruth",
			"1 Sam.", "2 Sam.", "1 Kings", "2 Kings",
			"1 Chron.", "2 Chron.", "Ezra", "Neh.",
			"Est.", "Job", "Ps.", "Prov.",
			"Eccles.", "Song", "Isa.", "Jer.",
			"Lam.", "Ezek.", "Dan.", "Hos.",
			"Joel", "Amos", "Obad.", "Jonah",
			"Mic.", "Nah.", "Hab.", "Zeph.",
			"Hag.", "Zech.", "Mal.", "Matt.",
			"Mark", "Luke", "John", "Acts",
			"Rom.", "1 Cor.", "2 Cor.", "Gal.",
			"Eph.", "Phil.", "Col.", "1 Thess.",
			"2 Thess.", "1 Tim.", "2 Tim.", "Titus",
			"Philem.", "Heb.", "James", "1 Pet.",
			"2 Pet.", "1 John", "2 John", "3 John",
			"Jude", "Rev."};

	public static List<Book> createBooksOfBible() {
		String bibleLongName = "English Standard Version";
		List<Book> books = new ArrayList<>();
		for (int i = 0; i < bookNames.length; i++) {
			Book book = new Book();
			book.setName(bookNames[i]);
			book.setAbbreviation(bookAbbreviations[i]);
			book.setChapterCount(chaperCount[i]);
			book.setNumber(i + 1);
			book.setBibleId(bibleLongName.hashCode());
			book.setId(bibleLongName.hashCode() + bookNames[i].hashCode());
			books.add(book);
		}
		return books;
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
		return bibleId;
	}

	public void setBibleId(long bibleId) {
		this.bibleId = bibleId;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
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

	public static String[] getBookNames() {
		return bookNames;
	}

	public static int[] getChaperCount() {
		return chaperCount;
	}

	public static String[] getBookAbbreviations() {
		return bookAbbreviations;
	}
	//</editor-fold>
}
