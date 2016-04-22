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

package org.oucho.radio2.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import org.oucho.radio2.utils.Counter;
import org.oucho.radio2.utils.Later;
import org.oucho.radio2.PlayerService;
import org.oucho.radio2.utils.State;

public class Connectivity extends BroadcastReceiver {

   private static ConnectivityManager connectivity = null;

   private Context context = null;
   private PlayerService player = null;
   private static final int TYPE_NONE = -1;

   private static int previous_type = TYPE_NONE;

    private static AsyncTask<Integer,Void,Void> disable_task = null;
    private int then = 0;


   public Connectivity(Context a_context, PlayerService a_player) {

      context = a_context;
      player = a_player;

      initConnectivity(context);
      context.registerReceiver(this, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
   }

   static private void initConnectivity(Context context) {

      if ( connectivity == null )
         connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
      if ( connectivity != null )
         previous_type = getType();
   }

   public void destroy() {
      context.unregisterReceiver(this);
   }

   static private int getType() {
      return getType(null);
   }

   static private int getType(Intent intent) {

      if (connectivity == null)
         return TYPE_NONE;

      if ( intent != null && intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false) )
         return TYPE_NONE;

      NetworkInfo network = connectivity.getActiveNetworkInfo();
      if ( network != null && network.isConnected() ) {

         int type = network.getType();
         switch (type) {
            // These cases all fall through.
            case ConnectivityManager.TYPE_WIFI:
            case ConnectivityManager.TYPE_MOBILE:
            case ConnectivityManager.TYPE_WIMAX:
               if ( network.getState() == NetworkInfo.State.CONNECTED )
                  return type;

             default: //do nothing
                 break;
         }
      }

      return TYPE_NONE;
   }

   public static boolean onWifi() {

      return previous_type == ConnectivityManager.TYPE_WIFI;
   }

   static public boolean isConnected(Context context) {
       initConnectivity(context);
      return (getType() != TYPE_NONE);
   }



   @Override
   public void onReceive(Context context, Intent intent) {

      int type = getType(intent);
      boolean want_network_playing = State.isWantPlaying() && player.isNetworkUrl();

      if ( type == TYPE_NONE && previous_type != TYPE_NONE && want_network_playing )
         dropped_connection();

      if ( previous_type == TYPE_NONE && type != previous_type && Counter.still(then) )
         restart();

      if ( previous_type != TYPE_NONE && type != TYPE_NONE && type != previous_type && want_network_playing )
         restart();

      previous_type = type;
   }

   public void dropped_connection() {  // We've lost connectivity.
      player.stop();
      then = Counter.now();
      State.setState(context, State.STATE_DISCONNECTED, true);

      if ( disable_task != null )
         disable_task.cancel(true);

      disable_task = new Later(300) {
            @Override
            public void later() {
               player.stop();
               disable_task = null;
            }
         }.start();
   }

   private void restart() {
      if ( disable_task != null ) {
         disable_task.cancel(true);
         disable_task = null;
      }

      SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
      if ( settings.getBoolean("reconnect", false) )
         player.play();
   }
}

