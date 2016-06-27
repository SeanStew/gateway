package ca.gatewaybaptistchurch.gateway.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.gatewaybaptistchurch.gateway.R;
import ca.gatewaybaptistchurch.gateway.model.NewsPost;

/**
 * Created by Sean on 6/26/2016.
 */
public class NewsPostViewHolder extends RecyclerView.ViewHolder {
    //<editor-fold desc"View Initialization">
    @BindView(R.id.newsPostItem_rootView) View rootView;
    @BindView(R.id.newsPostItem_image) ImageView imageView;
    @BindView(R.id.newsPostItem_title) TextView titleText;
    @BindView(R.id.newsPostItem_description) TextView descriptionText;
    //</editor-fold>

    public NewsPostViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bindToPost(NewsPost post, View.OnClickListener onClickListener) {
        if (post.imageLocation == null) {
            imageView.setVisibility(View.GONE);
        }

        titleText.setText(post.title);
        descriptionText.setText(post.shortDetails);
        titleText.setSelected(true);

        rootView.setOnClickListener(onClickListener);
    }
}
