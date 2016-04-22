package org.oucho.radio2.itf;


import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.oucho.radio2.MainActivity;
import org.oucho.radio2.R;

import java.util.ArrayList;

public class RadioAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final MainActivity activity;
    private final ArrayList<Object> items;
    private final LayoutInflater inflater;
    private final ListsClickListener clickListener;


    public RadioAdapter(MainActivity activity, ArrayList<Object> items, ListsClickListener clickListener) {
        this.activity = activity;
        this.items = items;
        this.clickListener = clickListener;
        inflater = activity.getLayoutInflater();
    }


    @SuppressLint("InflateParams")
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new RadioViewHolder(inflater.inflate(R.layout.radio_item, null), activity, clickListener);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);

        ((RadioViewHolder) holder).update((Radio)item);

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}
