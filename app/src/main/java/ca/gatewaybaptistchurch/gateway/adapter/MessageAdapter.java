package ca.gatewaybaptistchurch.gateway.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ca.gatewaybaptistchurch.gateway.R;
import ca.gatewaybaptistchurch.gateway.model.Podcast;
import ca.gatewaybaptistchurch.gateway.utils.Utils;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by Sean on 1/7/2017.
 */
public class MessageAdapter extends RealmRecyclerViewAdapter<Podcast, MessageAdapter.MessageViewHolder> {
	private MessageViewHolder.MessageViewHolderClickListener clickListener;
	public static String playingUrl;

	public MessageAdapter(Context context, OrderedRealmCollection<Podcast> podcasts, MessageViewHolder.MessageViewHolderClickListener clickListener) {
		super(context, podcasts, true);
		this.clickListener = clickListener;
	}

	public void setPlayingUrl(String url) {
		this.playingUrl = url;
		notifyDataSetChanged();
	}

	@Override
	public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_message, parent, false);
		return new MessageViewHolder(view);
	}

	@Override
	public void onBindViewHolder(MessageViewHolder holder, int position) {
		if (getData() != null && getData().size() > position) {
			holder.bindItem(getData().get(position), clickListener);
		}
	}

	public static class MessageViewHolder extends RecyclerView.ViewHolder {
		//<editor-fold desc="View Initialization">
		@BindView(R.id.messageItem_image) ImageView imageView;
		@BindView(R.id.messageItem_title) TextView titleTextView;
		@BindView(R.id.messageItem_date) TextView dateTextView;
		@BindView(R.id.messageItem_duration) TextView durationTextView;
		@BindView(R.id.messageItem_playButton) MaterialIconView playButton;
		//</editor-fold>

		Context context;
		MessageViewHolderClickListener clickListener;
		String podcastUrl;

		public MessageViewHolder(View itemView) {
			super(itemView);
			ButterKnife.bind(this, itemView);
			context = imageView.getContext();
		}

		public void bindItem(Podcast message, MessageViewHolderClickListener clickListener) {
			Glide.with(context).load(message.getImageUrl()).placeholder(R.drawable.messages_placeholder).error(R.drawable.messages_placeholder).into(imageView);

			titleTextView.setText(message.getTitle());
			dateTextView.setText(message.getDateString());
			durationTextView.setText(message.getDuration());

			this.podcastUrl = message.getPodcastUrl();
			this.clickListener = clickListener;

			if (Utils.isNotNullOrEmpty(playingUrl) && Utils.isNotNullOrEmpty(podcastUrl)) {
				playButton.setIcon(playingUrl.equalsIgnoreCase(podcastUrl) ? MaterialDrawableBuilder.IconValue.PAUSE_CIRCLE_OUTLINE : MaterialDrawableBuilder.IconValue.PLAY_CIRCLE_OUTLINE);
			} else {
				playButton.setIcon(MaterialDrawableBuilder.IconValue.PLAY_CIRCLE_OUTLINE);
			}
		}

		public interface MessageViewHolderClickListener {
			void onPlayClicked(String podcastUrl);
		}

		@OnClick({R.id.messageItem_playButton})
		public void onButtonClicked(View view) {
			if (!Utils.isNotNullOrEmpty(podcastUrl) || clickListener == null) {
				return;
			}

			clickListener.onPlayClicked(podcastUrl);
		}
	}
}