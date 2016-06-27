package ca.gatewaybaptistchurch.gateway.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.gatewaybaptistchurch.gateway.R;
import ca.gatewaybaptistchurch.gateway.adapter.NewsPostsAdapter;
import ca.gatewaybaptistchurch.gateway.model.NewsPost;

/**
 * Created by Sean on 5/29/2016.
 */
public class NewsFragment extends Fragment {
    private DatabaseReference database;
    private NewsPostsAdapter adapter;

    //<editor-fold desc"View Initialization">
    @BindView(R.id.newsFragment_emptyViewHolder) View emptyViewHolder;
    @BindView(R.id.newsFragment_recyclerView) RecyclerView recyclerView;
    //</editor-fold>

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.content_news, container, false);
        ButterKnife.bind(this, rootView);
        emptyViewHolder.setVisibility(View.GONE);
        setupDatabase();
        return rootView;
    }

    private void setupDatabase() {
        if (database == null) {
            database = FirebaseDatabase.getInstance().getReference();
        }

        Query postQuery = database.child("newsPosts").orderByChild("timestamp");
        adapter = new NewsPostsAdapter(postQuery, NewsPost.class, new ArrayList<NewsPost>(), new ArrayList<String>());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
    }
}
