package com.gearback.modules.recommendation;

import android.app.Activity;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import java.util.List;

public class AppPreviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Recommend.SimilarApp> mainItems;
    private Activity activity;
    private Recommend classes = new Recommend();
    private OnItemClickListener listener;

    public AppPreviewAdapter(Activity a, List<Recommend.SimilarApp> list, OnItemClickListener listener) {
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_item, parent, false);
        return classes.new AppPreviewViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Recommend.AppPreviewViewHolder viewHolder = (Recommend.AppPreviewViewHolder) holder;
        final Recommend.SimilarApp item = mainItems.get(position);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(position);
            }
        });
        viewHolder.name.setText(item.getName());
        Picasso.with(activity).load(item.getIcon()).into(viewHolder.icon);
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
