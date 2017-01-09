package ca.gatewaybaptistchurch.gateway.model;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.Date;

import ca.gatewaybaptistchurch.gateway.utils.Utils;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Sean on 1/7/2017.
 */

public class Podcast extends RealmObject {
	@Ignore
	private static final DateTimeFormatter podcastXMLFormat = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss -hhmm");
	@Ignore
	private static final DateTimeFormatter dateFormat = DateTimeFormat.forPattern("MMMM dd, yyyy");

	@PrimaryKey
	private String podcastUrl;
	private String title = "Message";
	private String speaker = "Gateway Baptist Church";
	private String imageUrl;
	private String duration;
	private Date date;

	//<editor-fold desc="Fetching">
	public static RealmResults<Podcast> getPodcasts(Realm realm) {
		return realm.where(Podcast.class).findAllSorted("date", Sort.DESCENDING);
	}

	public static Podcast getPodcast(Realm realm, String podcastUrl) {
		if (podcastUrl == null || realm == null) {
			return null;
		}
		return realm.where(Podcast.class).equalTo("podcastUrl", podcastUrl).findFirst();
	}
	//</editor-fold>

	public static Podcast parsePodcast(Node node) {
		Podcast podcast = new Podcast();

		if (node == null || !Utils.isNotNullOrEmpty(node.getNodeName()) || !node.getNodeName().equalsIgnoreCase("item")) {
			return null;
		}

		for (int i = 0; i < node.getChildNodes().getLength(); i++) {
			Node childNode = node.getChildNodes().item(i);
			String childName = childNode.getNodeName();
			if (childName == null || childName.equalsIgnoreCase("#text")) {
				continue;
			}

			if (childName.equalsIgnoreCase("title")) {
				String title = childNode.getTextContent();
				if (Utils.isNotNullOrEmpty(title)) {
					podcast.title = title;
				}
			} else if (childName.equalsIgnoreCase("itunes:image")) {
				NamedNodeMap attributes = childNode.getAttributes();
				if (attributes == null || attributes.getLength() < 1) {
					continue;
				}
				if (!attributes.item(0).getNodeName().equalsIgnoreCase("href")) {
					continue;
				}
				String imageUrl = attributes.item(0).getNodeValue();
				if (Utils.isNotNullOrEmpty(imageUrl)) {
					podcast.imageUrl = imageUrl;
				}
			} else if (childName.equalsIgnoreCase("enclosure")) {
				NamedNodeMap attributes = childNode.getAttributes();
				if (attributes == null || attributes.getLength() < 1) {
					continue;
				}
				if (!attributes.item(0).getNodeName().equalsIgnoreCase("url")) {
					continue;
				}
				String podcastUrl = attributes.item(0).getNodeValue();
				if (Utils.isNotNullOrEmpty(podcastUrl)) {
					podcast.podcastUrl = podcastUrl;
				}
			} else if (childName.equalsIgnoreCase("pubdate")) {
				String date = childNode.getTextContent();
				if (Utils.isNotNullOrEmpty(date)) {
					try {
						podcast.date = podcastXMLFormat.parseDateTime(date).toDate();
					} catch (Exception ignored) {
					}
				}
			} else if (childName.equalsIgnoreCase("itunes:duration")) {
				String duration = childNode.getTextContent();
				if (Utils.isNotNullOrEmpty(duration)) {
					podcast.duration = duration;
				}
			}
		}

		if (podcast.podcastUrl == null) {
			return null;
		}

		return podcast;
	}

	public Podcast getPreviousPodcast(Realm realm) {
		RealmResults<Podcast> podcasts = getPodcasts(realm);

		boolean isNext = false;
		for (int i = 0; i < podcasts.size(); i++) {
			Podcast podcast = podcasts.get(i);
			if (isNext) {
				return podcast;
			}

			if (podcast.getPodcastUrl().equalsIgnoreCase(this.getPodcastUrl())) {
				isNext = true;
			}
		}

		return podcasts.get(0);
	}

	public Podcast getNextPodcast(Realm realm) {
		RealmResults<Podcast> podcasts = realm.where(Podcast.class).findAllSorted("date", Sort.ASCENDING);

		boolean isNext = false;
		for (int i = 0; i < podcasts.size(); i++) {
			Podcast podcast = podcasts.get(i);
			if (isNext) {
				return podcast;
			}

			if (podcast.getPodcastUrl().equalsIgnoreCase(this.getPodcastUrl())) {
				isNext = true;
			}
		}

		return podcasts.get(0);
	}

	//<editor-fold desc="Getter and Setters">
	public String getDateString() {
		if (date == null) {
			return "";
		}

		return dateFormat.print(new DateTime(getDate()));
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPodcastUrl() {
		return podcastUrl;
	}

	public void setPodcastUrl(String podcastUrl) {
		this.podcastUrl = podcastUrl;
	}

	public String getSpeaker() {
		return speaker;
	}

	public void setSpeaker(String speaker) {
		this.speaker = speaker;
	}
	//</editor-fold>

	@Override public String toString() {
		String toPrint = title;
		if (Utils.isNotNullOrEmpty(imageUrl)) {
			toPrint = String.format("%s, imageUrl: %s", toPrint, imageUrl);
		}
		if (Utils.isNotNullOrEmpty(podcastUrl)) {
			toPrint = String.format("%s, podcastUrl: %s", toPrint, podcastUrl);
		}
		if (Utils.isNotNullOrEmpty(duration)) {
			toPrint = String.format("%s, duration: %s", toPrint, duration);
		}
		if (Utils.isNotNullOrEmpty(date.toString())) {
			toPrint = String.format("%s, date: %s", toPrint, date.toString());
		}
		return toPrint;
	}
}
