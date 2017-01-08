package ca.gatewaybaptistchurch.gateway.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;

/**
 * Created by Sean on 6/26/2016.
 */
@IgnoreExtraProperties
public class Event {
	public String uid;
	public long timestamp;
	public String eventName;
	public String shortDescription;
	public String longDescription;
	public String recurring;
	public String tags;
	public String urlLink;
	public String urlTitle;

	public Event() {
	}
}
