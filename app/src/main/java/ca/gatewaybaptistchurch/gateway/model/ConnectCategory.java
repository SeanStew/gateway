package ca.gatewaybaptistchurch.gateway.model;

/**
 * Created by sean1 on 1/21/2017.
 */

public class ConnectCategory {
	public enum Category {
		PRAYER_REQUEST, SPIRITUAL_GIFTS, GROUPS, NEW_FAITH
	}

	public Category category;
	public int imageResource;

	public ConnectCategory(Category category, int imageResource) {
		this.category = category;
		this.imageResource = imageResource;
	}
}
