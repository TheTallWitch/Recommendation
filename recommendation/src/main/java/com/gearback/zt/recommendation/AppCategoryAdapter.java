package com.gearback.zt.recommendation;

import android.app.Activity;
import android.graphics.Typeface;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class AppCategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Recommend.AppCategory> mainItems;
    private Activity activity;
    private Recommend classes = new Recommend();
    private OnItemClickListener listener;

    public AppCategoryAdapter(Activity a, List<Recommend.AppCategory> list, OnItemClickListener listener) {
        mainItems = list;
        activity = a;
        this.listener = listener;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return mainItems.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_category_row, parent, false);
        return classes.new AppCategoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final Recommend.AppCategoryViewHolder viewHolder = (Recommend.AppCategoryViewHolder) holder;
        final Recommend.AppCategory item = mainItems.get(position);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onCategoryClick(position);
            }
        });
        viewHolder.category.setText(item.getName());
        if (position == 1) {
            viewHolder.header.setVisibility(View.VISIBLE);
            viewHolder.header.setTypeface(viewHolder.header.getTypeface(), Typeface.ITALIC);
        }
        else {
            viewHolder.header.setVisibility(View.GONE);
        }
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, true);
        mLinearLayoutManager.setReverseLayout(true);
        viewHolder.itemList.setLayoutManager(mLinearLayoutManager);

        AppPreviewAdapter adapter = new AppPreviewAdapter(activity, item.getApps(), new AppPreviewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                listener.onItemClick(item.getApps().get(position).getUrl());
            }
        });
        viewHolder.itemList.setAdapter(adapter);
    }

    public interface OnItemClickListener {
        void onCategoryClick(int position);
        void onItemClick(String url);
    }
}
