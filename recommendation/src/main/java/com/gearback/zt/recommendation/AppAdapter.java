package com.gearback.zt.recommendation;

import android.app.Activity;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Recommend.SimilarApp> mainItems;
    private Activity activity;
    private Recommend classes = new Recommend();
    private OnItemClickListener listener;
    private String categoryName;

    public AppAdapter(Activity a, String categoryName, List<Recommend.SimilarApp> list, OnItemClickListener listener) {
        mainItems = list;
        activity = a;
        this.listener = listener;
        this.categoryName = categoryName;
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_row, parent, false);
        return classes.new AppViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final Recommend.AppViewHolder viewHolder = (Recommend.AppViewHolder) holder;
        final Recommend.SimilarApp item = mainItems.get(position);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(position);
            }
        });
        viewHolder.name.setText(item.getName());
        viewHolder.description.setText(item.getDescription());
        Picasso.with(activity).load(item.getIcon()).into(viewHolder.icon);
        viewHolder.category.setText(categoryName);
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
