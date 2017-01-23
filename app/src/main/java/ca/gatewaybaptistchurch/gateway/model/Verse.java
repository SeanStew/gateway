package ca.gatewaybaptistchurch.gateway.model;

import org.w3c.dom.Element;

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

	public static Verse createVerse(long chapterId, Element verseElement) {
		Verse verse = new Verse();
		int number = -1;
		try {
			number = Integer.valueOf(verseElement.getAttribute("vnumber"));
		} catch (Exception ignored) {
		}
		verse.setId(chapterId + number);
		verse.setChapterId(chapterId);
		verse.setNumber(number);
		verse.setText(verseElement.getTextContent());
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
