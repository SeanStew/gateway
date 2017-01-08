package ca.gatewaybaptistchurch.gateway.adapter;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;

import ca.gatewaybaptistchurch.gateway.R;
import ca.gatewaybaptistchurch.gateway.model.Event;
import ca.gatewaybaptistchurch.gateway.viewHolder.EventViewHolder;

/**
 * Created by Sean on 6/27/2016.
 */
public class EventAdapter extends FirebaseRecyclerAdapter<Event, EventViewHolder> {
	private EventViewHolder.OnEventClickListener eventClickListener;

	public EventAdapter(Query query, EventViewHolder.OnEventClickListener eventClickListener) {
		super(Event.class, R.layout.list_item_news_post, EventViewHolder.class, query);
		this.eventClickListener = eventClickListener;
	}

	@Override protected void populateViewHolder(EventViewHolder viewHolder, Event event, int position) {
		viewHolder.bindToPost(event, eventClickListener);
	}


}
