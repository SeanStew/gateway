package ca.gatewaybaptistchurch.gateway.model;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Sean on 6/26/2016.
 */

public class Event extends RealmObject {
	@PrimaryKey
	private String uuid;
	private Date date;
	private Boolean showAtTopWithoutDate = false;
	private String eventName;
	private String shortDescription;
	private String longDescription;
	private String urlLink;
	private String urlTitle;
	private String imageUrl;

	//<editor-fold desc="Fetching">
	public static RealmResults<Event> getEvents(Realm realm) {
		return realm.where(Event.class).findAllSorted("showAtTopWithoutDate", Sort.DESCENDING, "date", Sort.ASCENDING);
	}

	public static Event getEvent(Realm realm, String eventUuid) {
		return realm.where(Event.class).equalTo("uuid", eventUuid).findFirst();
	}
	//</editor-fold>

	//<editor-fold desc="Getter and Setters">
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	public String getLongDescription() {
		return longDescription;
	}

	public void setLongDescription(String longDescription) {
		this.longDescription = longDescription;
	}

	public String getUrlLink() {
		return urlLink;
	}

	public void setUrlLink(String urlLink) {
		this.urlLink = urlLink;
	}

	public String getUrlTitle() {
		return urlTitle;
	}

	public void setUrlTitle(String urlTitle) {
		this.urlTitle = urlTitle;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public boolean isShowAtTopWithoutDate() {
		return showAtTopWithoutDate == null ? false : showAtTopWithoutDate;
	}

	public void setShowAtTopWithoutDate(boolean showAtTopWithoutDate) {
		this.showAtTopWithoutDate = showAtTopWithoutDate;
	}
	//</editor-fold>
}
