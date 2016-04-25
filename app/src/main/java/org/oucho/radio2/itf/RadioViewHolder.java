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

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.oucho.radio2.MainActivity;
import org.oucho.radio2.R;

class RadioViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final TextView text;
    private final ImageButton menu;
    private final ImageView image;
    private Radio radio;
    private final MainActivity activity;
    private final ListsClickListener clickListener;


    public RadioViewHolder(View view, MainActivity activity, ListsClickListener clickListener) {
        super(view);
        text = (TextView)view.findViewById(R.id.textViewRadio);
        menu = (ImageButton)view.findViewById(R.id.buttonMenu);

        image = (ImageView)view.findViewById(R.id.imageViewRadio);

        this.activity = activity;
        this.clickListener = clickListener;
        view.setOnClickListener(this);
        menu.setOnClickListener(this);
        menu.setFocusable(false);
    }

    public void update(Radio radio, String nomRadio) {
        this.radio = radio;
        text.setText(radio.getTitle());

        if (radio.getTitle().equals(nomRadio))
            image.setColorFilter(ContextCompat.getColor(MainActivity.getContext(), R.color.colorAccent));

    }

    @Override
    public void onClick(View view) {
        if(view.equals(menu)) {
            final PopupMenu popup = new PopupMenu(activity, menu);
            popup.getMenuInflater().inflate(R.menu.contextmenu_editdelete, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    clickListener.onPlayableItemMenuClick(radio, item.getItemId());
                    return true;
                }
            });
            popup.show();
        } else {
            clickListener.onPlayableItemClick(radio);
        }
    }
}
