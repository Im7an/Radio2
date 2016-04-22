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

package org.oucho.radio2.utils;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;

import org.oucho.radio2.PlayerService;

public class StopReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {


        String etat = intent.getAction();

        if ("org.oucho.radio2.STATE".equals(etat)) {

            String fichier_préférence = "org.oucho.radio2_preferences";

            SharedPreferences préférences = context.getSharedPreferences(fichier_préférence, 0);

            String nom_radio = intent.getStringExtra("name");

            if (nom_radio == null) {
                nom_radio = préférences.getString("name", "");
            }

            String action_lecteur = intent.getStringExtra("state");

            Notification.updateNotification(context, nom_radio, action_lecteur);
        }


        if ( "org.oucho.radio2.STOP".equals(etat) && ( State.isPlaying() || State.isPaused() ) ) {

            String halt = intent.getStringExtra("halt");
            Intent player = new Intent(context, PlayerService.class);
            player.putExtra("action", halt);
            context.startService(player);

            final Context ctx = context;
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                @SuppressLint("SetTextI18n")
                public void run() {

                    if (!State.isPlaying())
                        Notification.removeNotification(ctx);

                }
            }, 500);
        }
    }
}
