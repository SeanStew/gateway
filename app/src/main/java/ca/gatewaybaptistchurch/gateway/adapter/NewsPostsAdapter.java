package ca.gatewaybaptistchurch.gateway.adapter;

import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.Query;

import java.util.ArrayList;

import ca.gatewaybaptistchurch.gateway.R;
import ca.gatewaybaptistchurch.gateway.model.NewsPost;
import ca.gatewaybaptistchurch.gateway.viewHolder.NewsPostViewHolder;
import timber.log.Timber;

/**
 * Created by Sean on 6/27/2016.
 */
public class NewsPostsAdapter extends FirebaseRecyclerAdapter<NewsPostViewHolder, NewsPost> {
    public NewsPostsAdapter(Query query, Class<NewsPost> itemClass, @Nullable ArrayList<NewsPost> items, @Nullable ArrayList<String> keys) {
        super(query, itemClass, items, keys);
    }

    @Override
    public NewsPostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_post, parent, false);
        return new NewsPostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NewsPostViewHolder holder, int position) {
        holder.bindToPost(getItem(position), new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    protected void itemAdded(NewsPost item, String key, int position) {
        Timber.d("added an item");
    }

    @Override
    protected void itemChanged(NewsPost oldItem, NewsPost newItem, String key, int position) {
        Timber.d("changed an item");
    }

    @Override
    protected void itemRemoved(NewsPost item, String key, int position) {
        Timber.d("removed an item");
    }

    @Override
    protected void itemMoved(NewsPost item, String key, int oldPosition, int newPosition) {
        Timber.d("moved an item");
    }
}
