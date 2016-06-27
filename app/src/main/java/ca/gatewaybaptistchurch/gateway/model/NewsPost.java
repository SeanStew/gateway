package ca.gatewaybaptistchurch.gateway.model;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Sean on 6/26/2016.
 */
@IgnoreExtraProperties
public class NewsPost {
    public String uid;
    public Long timestamp;
    public String title;
    public String shortDetails;
    public String longDetails;
    public String imageLocation;
    public String fileLocation;
    public String urlTitle;
    public String urlLink;
    public String tags;

    public NewsPost() {
    }
}
