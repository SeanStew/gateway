package ca.gatewaybaptistchurch.gateway.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ca.gatewaybaptistchurch.gateway.R;
import ca.gatewaybaptistchurch.gateway.model.ConnectCategory;

/**
 * Created by Sean on 1/7/2017.
 */
public class ConnectAdapter extends RecyclerView.Adapter<ConnectAdapter.ConnectViewHolder> {
	private ConnectViewHolder.ConnectViewHolderClickListener clickListener;

	private List<ConnectCategory> connectCategories;

	public ConnectAdapter(List<ConnectCategory> connectCategories, ConnectViewHolder.ConnectViewHolderClickListener clickListener) {
		this.clickListener = clickListener;
		this.connectCategories = connectCategories;
	}

	@Override
	public ConnectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_message, parent, false);
		return new ConnectViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ConnectViewHolder holder, int position) {
		if (connectCategories == null || connectCategories.size() <= position) {
			return;
		}

		holder.bindItem(connectCategories.get(position), clickListener);
	}

	@Override
	public int getItemCount() {
		if (connectCategories == null) {
			return 0;
		}
		return connectCategories.size();
	}

	public static class ConnectViewHolder extends RecyclerView.ViewHolder {
		//<editor-fold desc="View Initialization">
		@BindView(R.id.messageItem_image) ImageView imageView;
		@BindView(R.id.messageItem_title) View titleTextView;
		@BindView(R.id.messageItem_speaker) View speakerTextView;
		@BindView(R.id.messageItem_date) View dateTextView;
		@BindView(R.id.messageItem_duration) View durationTextView;
		@BindView(R.id.messageItem_playButton) View playButton;
		//</editor-fold>

		Context context;
		ConnectViewHolderClickListener clickListener;
		ConnectCategory.Category category;

		public ConnectViewHolder(View itemView) {
			super(itemView);
			ButterKnife.bind(this, itemView);
			context = imageView.getContext();
		}

		public void bindItem(ConnectCategory connectCategory, ConnectViewHolderClickListener clickListener) {
			Glide.with(context).load(connectCategory.imageResource).into(imageView);
			titleTextView.setVisibility(View.GONE);
			speakerTextView.setVisibility(View.GONE);
			dateTextView.setVisibility(View.GONE);
			durationTextView.setVisibility(View.GONE);
			playButton.setVisibility(View.GONE);

			this.category = connectCategory.category;
			this.clickListener = clickListener;
		}

		public interface ConnectViewHolderClickListener {
			void onCategorySelected(ConnectCategory.Category category);
		}

		@OnClick({R.id.messageItem_rootView})
		public void onButtonClicked() {
			if (category == null || clickListener == null) {
				return;
			}

			clickListener.onCategorySelected(category);
		}
	}
}