package com.mobiledev.infinitetabs;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import static com.mobiledev.infinitetabs.InfiniteTabView.getTextSelectedColor;

/**
 * Created by mdev3 on 4/11/17.
 */

public class InfiniteTabAdapter extends RecyclerView.Adapter<InfiniteTabAdapter.ViewHolder> implements View.OnClickListener {

    private List<String> mItems;
    private OnItemClickListener mOnItemClickListener;

    public InfiniteTabAdapter(List<String> items) {
        this.mItems = items;
    }

    public InfiniteTabAdapter setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
        return this;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tab_layout, parent, false);
        v.setOnClickListener(this);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int temp = position;
        if (position >= mItems.size()) {
            position = position % mItems.size();
        }
        String item = mItems.get(position);
        holder.text.setText(item);
       holder.text.setTextColor(getTextSelectedColor());
        holder.itemView.setTag(temp);
    }

    public static int LOOPS_COUNT = 1000;

    @Override
    public int getItemCount() {
        if (mItems != null && mItems.size() > 0) {
            return mItems.size() * LOOPS_COUNT;
        } else {
            return 1;
        }
    }

    @Override
    public void onClick(final View v) {
        if (mOnItemClickListener != null) {
            int position = (int) v.getTag();
            mOnItemClickListener.onItemClick(v, position);
        }
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView text;
        public ViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.item_tv_title);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}
