package org.oucho.radio2.itf;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.oucho.radio2.MainActivity;
import org.oucho.radio2.R;

import java.util.ArrayList;

public class RadioAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private MainActivity activity;
    private ArrayList<Object> items;
    private LayoutInflater inflater;
    private PlayableItem playingItem;
    private ListsClickListener clickListener;


    public RadioAdapter(MainActivity activity, ArrayList<Object> items, PlayableItem playingItem, ListsClickListener clickListener) {
        this.activity = activity;
        this.items = items;
        this.playingItem = playingItem;
        this.clickListener = clickListener;
        inflater = activity.getLayoutInflater();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new RadioViewHolder(inflater.inflate(R.layout.radio_item, null), activity, clickListener);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);

        ((RadioViewHolder) holder).update((Radio)item, (Radio)playingItem);

    }

    public int getPlayableItemPosition(PlayableItem item) {
        return items.indexOf(item);
    }

    public void swapItems(int from, int to) {
        Object item = items.remove(from);
        items.add(to, item);
        notifyItemMoved(from, to);
    }

    public Object deleteItem(int position) {
        Object item = items.remove(position);
        notifyItemRemoved(position);
        return item;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}
