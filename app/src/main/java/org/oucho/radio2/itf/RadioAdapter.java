/**
 *  Radio for android, internet radio.
 *
 * Copyright (C) 2016 Old Geek
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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

    private final String nomRadio;


    public RadioAdapter(MainActivity activity, ArrayList<Object> items, String nomRadio, ListsClickListener clickListener) {
        this.activity = activity;
        this.items = items;
        this.clickListener = clickListener;
        inflater = activity.getLayoutInflater();

        this.nomRadio = nomRadio;
    }


    @SuppressLint("InflateParams")
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new RadioViewHolder(inflater.inflate(R.layout.radio_item, null), activity, clickListener);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);

        ((RadioViewHolder) holder).update((Radio)item, nomRadio);

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}
