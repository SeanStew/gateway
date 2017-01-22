package ca.gatewaybaptistchurch.gateway.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.gatewaybaptistchurch.gateway.R;
import ca.gatewaybaptistchurch.gateway.model.Event;
import ca.gatewaybaptistchurch.gateway.utils.Utils;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by Sean on 6/27/2016.
 */
public class EventAdapter extends RealmRecyclerViewAdapter<Event, EventAdapter.EventViewHolder> {
	private static final DateTimeFormatter dateFormat = DateTimeFormat.forPattern("EEE MMMM dd - h:mmaa");

	private EventViewHolder.OnEventClickListener clickListener;

	public EventAdapter(@NonNull Context context, @Nullable OrderedRealmCollection<Event> events, EventViewHolder.OnEventClickListener eventClickListener) {
		super(context, events, true);
		this.clickListener = eventClickListener;
	}

	@Override
	public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_post, parent, false);
		return new EventViewHolder(view);
	}

	@Override
	public void onBindViewHolder(EventViewHolder holder, int position) {
		if (getData() != null && getData().size() > position) {
			holder.bindItem(getData().get(position), clickListener);
		}
	}

	public static class EventViewHolder extends RecyclerView.ViewHolder {
		//<editor-fold desc"View Initialization">
		@BindView(R.id.eventItem_date) TextView dateTextView;
		@BindView(R.id.eventItem_rootView) View rootView;
		@BindView(R.id.eventItem_title) TextView titleText;
		@BindView(R.id.eventItem_description) TextView descriptionText;
		//</editor-fold>

		public EventViewHolder(View itemView) {
			super(itemView);
			ButterKnife.bind(this, itemView);
		}

		public void bindItem(final Event event, final OnEventClickListener eventClickListener) {
			if (event.isShowAtTopWithoutDate()) {
				dateTextView.setVisibility(View.GONE);
			} else {
				dateTextView.setVisibility(View.VISIBLE);
				dateTextView.setText(Utils.getFullDateString(event.getDate()));
			}

			titleText.setText(event.getEventName());
			descriptionText.setText(event.getShortDescription());
			titleText.setSelected(true);

			if (eventClickListener != null) {
				rootView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						eventClickListener.onClick(event.getUuid());
					}
				});
			}
		}

		public interface OnEventClickListener {
			void onClick(String eventUuid);
		}
	}
}
