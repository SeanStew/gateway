package ca.gatewaybaptistchurch.gateway.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import net.steamcrafted.materialiconlib.MaterialIconView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ca.gatewaybaptistchurch.gateway.R;
import ca.gatewaybaptistchurch.gateway.model.Podcast;
import ca.gatewaybaptistchurch.gateway.utils.Utils;

/**
 * Created by Sean on 1/7/2017.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
	private MessageViewHolder.MessageViewHolderClickListener clickListener;
	private List<Podcast> messages;

	public MessageAdapter(List<Podcast> messages, MessageViewHolder.MessageViewHolderClickListener clickListener) {
		this.clickListener = clickListener;
		this.messages = messages;
	}

	@Override public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_message, parent, false);
		return new MessageViewHolder(view);
	}

	@Override
	public void onBindViewHolder(MessageViewHolder holder, int position) {
		if (messages != null) {
			holder.bindItem(messages.get(position), clickListener);
		}
	}

	@Override
	public int getItemCount() {
		return messages.size();
	}


	public static class MessageViewHolder extends RecyclerView.ViewHolder {
		//<editor-fold desc="View Initialization">
		@BindView(R.id.messageItem_image) ImageView imageView;
		@BindView(R.id.messageItem_title) TextView titleTextView;
		@BindView(R.id.messageItem_date) TextView dateTextView;
		@BindView(R.id.messageItem_playButton) MaterialIconView playButton;
		@BindView(R.id.messageItem_seekbar) SeekBar seekBar;
		@BindView(R.id.messageItem_durationTextView) TextView durationTextView;
		@BindView(R.id.messageItem_downloadButton) MaterialIconView downloadButton;
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
			Glide.with(context).load(message.imageUrl).error(R.drawable.header_image).into(imageView);

			titleTextView.setText(message.title);
			dateTextView.setText(message.getDateString());
			durationTextView.setText(message.duration);

			this.podcastUrl = message.podcastUrl;
			this.clickListener = clickListener;
		}

		public interface MessageViewHolderClickListener {
			void onPlayClicked(String podcastUrl);

			void onDownloadClicked(String podcastUrl);
		}

		@OnClick({R.id.messageItem_playButton, R.id.messageItem_downloadButton})
		public void onButtonClicked(View view) {
			if (!Utils.isNotNullOrEmpty(podcastUrl) || clickListener == null) {
				return;
			}

			switch (view.getId()) {
				case R.id.messageItem_playButton:
					clickListener.onPlayClicked(podcastUrl);
					break;
				case R.id.messageItem_downloadButton:
					clickListener.onDownloadClicked(podcastUrl);
					break;
			}
		}
	}
}