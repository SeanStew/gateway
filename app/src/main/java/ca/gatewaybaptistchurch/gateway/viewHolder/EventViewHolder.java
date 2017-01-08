package ca.gatewaybaptistchurch.gateway.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.gatewaybaptistchurch.gateway.R;
import ca.gatewaybaptistchurch.gateway.model.Event;

/**
 * Created by Sean on 6/26/2016.
 */
public class EventViewHolder extends RecyclerView.ViewHolder {
	//<editor-fold desc"View Initialization">
	@BindView(R.id.eventItem_date) TextView dateTextView;
	@BindView(R.id.eventItem_rootView) View rootView;
	@BindView(R.id.eventItem_title) TextView titleText;
	@BindView(R.id.eventItem_description) TextView descriptionText;
	//</editor-fold>

	private static final DateTimeFormatter dateFormat = DateTimeFormat.forPattern("EEE MMMM dd - hh:mmaa");

	public EventViewHolder(View itemView) {
		super(itemView);
		ButterKnife.bind(this, itemView);
	}

	public void bindToPost(final Event event, final OnEventClickListener eventClickListener) {
		dateTextView.setText(dateFormat.print(new DateTime(event.timestamp)));

		titleText.setText(event.eventName);
		descriptionText.setText(event.shortDescription);
		titleText.setSelected(true);

		if (eventClickListener != null) {
			rootView.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {
					eventClickListener.onClick(event);
				}
			});
		}
	}

	public interface OnEventClickListener {
		void onClick(Event event);
	}
}
