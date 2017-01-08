package ca.gatewaybaptistchurch.gateway.model;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import ca.gatewaybaptistchurch.gateway.utils.Utils;

/**
 * Created by Sean on 1/7/2017.
 */

public class Podcast {
	private static final DateTimeFormatter podcastXMLFormat = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss -hhmm");
	private static final DateTimeFormatter dateFormat = DateTimeFormat.forPattern("MMMM dd, yyyy");

	public String title = "Message";
	public String imageUrl;
	public String podcastUrl;
	public String duration;
	public DateTime date;

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
						podcast.date = podcastXMLFormat.parseDateTime(date);
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

	public String getDateString() {
		if (date == null) {
			return "";
		}

		return dateFormat.print(date);
	}

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
