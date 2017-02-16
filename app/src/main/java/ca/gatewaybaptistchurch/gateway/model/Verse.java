package ca.gatewaybaptistchurch.gateway.model;

import org.w3c.dom.Element;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by sean1 on 1/22/2017.
 */

public class Verse extends RealmObject {
	@PrimaryKey
	private long id;
	private Long chapterId;
	private Integer number;
	private String text;

	public static Verse createVerse(Realm realm, long chapterId, Element verseElement) {
		Verse verse = new Verse();
		int number = -1;
		try {
			String[] sNumbers = verseElement.getAttribute("osisID").split("[.]");
			String sNumber = sNumbers[sNumbers.length - 1];
			number = Integer.valueOf(sNumber);
		} catch (Exception ignored) {
		}
		String text = verseElement.getTextContent();
		verse.setId(text.hashCode() + number);
		verse.setChapterId(chapterId);
		verse.setNumber(number);
		verse.setText(text);
		realm.beginTransaction();
		realm.copyToRealmOrUpdate(verse);
		realm.commitTransaction();
		return verse;
	}

	//<editor-fold desc="Getter and Setters">
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getChapterId() {
		if (chapterId == null) {
			chapterId = -1L;
		}
		return chapterId;
	}

	public void setChapterId(long chapterId) {
		this.chapterId = chapterId;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	//</editor-fold>
}
