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

package org.oucho.radio2;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.oucho.radio2.itf.ListsClickListener;
import org.oucho.radio2.itf.PlayableItem;
import org.oucho.radio2.itf.Radio;
import org.oucho.radio2.itf.RadioAdapter;
import org.oucho.radio2.utils.GetAudioFocusTask;
import org.oucho.radio2.utils.Notification;
import org.oucho.radio2.utils.State;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity
        implements
        NavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener {



   /* **********************************************************************************************
    * Déclaration des variables
    * *********************************************************************************************/

    private static Context context;

    private static final String PLAY = "play";
    private static final String STOP = "stop";
    private static final String RESTART = "restart";

    private static final String STATE = "org.oucho.radio2.STATE";

    private static String etat_lecture = "";
    private static String nom_radio = "";

    private RecyclerView radioView;
    private DrawerLayout mDrawerLayout;

    private static boolean running;

    private static ScheduledFuture mTask;

    private CountDownTimer timerEcran;

    private ImageView timeAfficheur0;
    private TextView timeAfficheur1;

    private ImageButton iconTimer;


    private Etat_player Etat_player_Receiver;
    private boolean isRegistered = false;



   /* **********************************************************************************************
    * Création de l'activité
    * *********************************************************************************************/

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        Etat_player_Receiver = new Etat_player();
        IntentFilter filter = new IntentFilter(STATE);
        registerReceiver(Etat_player_Receiver, filter);
        isRegistered = true;

        Control_Volume niveau_Volume = new Control_Volume(this, new Handler());
        getApplicationContext().getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, niveau_Volume);


        int couleurTitre = ContextCompat.getColor(context, R.color.colorAccent);

        int couleurFond = ContextCompat.getColor(context, R.color.colorPrimary);


        String titre = context.getString(R.string.app_name);

        ColorDrawable colorDrawable = new ColorDrawable(couleurFond);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setBackgroundDrawable(colorDrawable);
        actionBar.setTitle(Html.fromHtml("<font color='" + couleurTitre + "'>" + titre + "</font>"));


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView mNavigationView = (NavigationView) findViewById(R.id.navigation_view);

        assert mNavigationView != null;
        mNavigationView.setNavigationItemSelectedListener(this);


        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {

        };

        mDrawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        final Drawable upArrow = ContextCompat.getDrawable(context, R.drawable.ic_menu_black_24dp);
        upArrow.setColorFilter(ContextCompat.getColor(context, R.color.controls_tint_light), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);


        radioView = (RecyclerView)findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        radioView.setLayoutManager(layoutManager);



        this.findViewById(R.id.add).setOnClickListener(this);
        this.findViewById(R.id.timer).setOnClickListener(this);
        this.findViewById(R.id.play).setOnClickListener(this);


        updateListView();

        getBitRate();
        volume();

        State.getState(context);

    }


    public static Context getContext() {
        return context;
    }


   /* **********************************************************************************************
    * Pause / résume / etc.
    * *********************************************************************************************/


   /* ****************************************
    * Passage en arrière plan de l'application
    * ****************************************/

    @Override
    protected void onPause() {
        super.onPause();

        if (isRegistered) {
            unregisterReceiver(Etat_player_Receiver);
            isRegistered = false;
        }

        killNotif();
    }


   /* ****************************************
    * Passage au premier plan de l'application
    * ****************************************/

    @Override
    protected void onResume() {
        super.onResume();

        if (!isRegistered) {
            IntentFilter filter = new IntentFilter(STATE);
            registerReceiver(Etat_player_Receiver, filter);
            isRegistered = true;
        }

        if (running)
            showTimeEcran();
    }


   /* ****************************
    * Destruction de l'application
    * ****************************/

    @Override
    protected void onDestroy() {
        super.onDestroy();

        killNotif();

    }



   /* **********************************************************************************************
    * Broadcast receiver
    * *********************************************************************************************/

    private class Etat_player extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {


            String receiveIntent = intent.getAction();

            if ("org.oucho.radio2.STATE".equals(receiveIntent)) {

                /* Statut player */
                TextView status = (TextView) findViewById(R.id.etat);

                etat_lecture = intent.getStringExtra("state");

                assert status != null;
                status.setText(etat_lecture);

                nom_radio = intent.getStringExtra("name");

                updateNomRadio();

                updatePlayPause();

            }
        }
    }


   /* *********************************
    * Affiche le nom de la radio active
    * *********************************/

    private void updateNomRadio() {

        /* Nom radio */
        TextView StationTextView = (TextView) findViewById(R.id.station);

        if (nom_radio == null) {

            String fichier_préférence = "org.oucho.radio2_preferences";

            SharedPreferences préférences = getSharedPreferences(fichier_préférence, MODE_PRIVATE);

            nom_radio = préférences.getString("name", "");
        }

        assert StationTextView != null;
        StationTextView.setText(nom_radio);
    }


   /* ****************************
    * Changement d'état play/pause
    * ****************************/

    @SuppressWarnings("ConstantConditions")
    private void updatePlayPause() {
        ImageView equalizer = (ImageView) findViewById(R.id.icon_equalizer);
        ImageView button = (ImageView) findViewById(R.id.play);

        if ("Stop".equals(etat_lecture)) {
            equalizer.setBackground(getDrawable(R.drawable.ic_equalizer0));
            button.setImageResource(R.drawable.musicplayer_play);

        } else {
            equalizer.setBackground(getDrawable(R.drawable.ic_equalizer1));
            button.setImageResource(R.drawable.musicplayer_pause);
        }
    }



   /* **********************************************************************************************
    * Navigation Drawer
    * *********************************************************************************************/

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        mDrawerLayout.closeDrawers();

        switch (menuItem.getItemId()) {
            case R.id.action_musique:
                musique();
                break;

            case R.id.nav_help:
                about();
                break;

            case R.id.nav_exit:
                exit();
                break;

            default:
                break;
        }
        return true;
    }


   /* *************************
    * Lance l'application radio
    * *************************/

    private void musique() {

        Context context = getApplicationContext();

        PackageManager pm = context.getPackageManager();
        Intent appStartIntent = pm.getLaunchIntentForPackage("org.oucho.musicplayer");
        context.startActivity(appStartIntent);

        killNotif();
    }


   /* *******
    * Quitter
    * *******/

    private void exit() {
        Intent player = new Intent(this, PlayerService.class);
        player.putExtra("action", STOP);
        startService(player);


        if (isRegistered) {
            unregisterReceiver(Etat_player_Receiver);
            isRegistered = false;
        }

        stopTimer();

        killNotif();

        finish();

    }



   /* **********************************************************************************************
    * Gestion des clicks sur l'interface
    * *********************************************************************************************/

    @Override
    public void onClick(View v) {

        Intent player = new Intent(this, PlayerService.class);

        switch (v.getId()) {
            case R.id.play:
                switch (etat_lecture) {
                    case "Stop":
                        player.putExtra("action", PLAY);
                        startService(player);
                        break;

                    case "Pause":
                        player.putExtra("action", RESTART);
                        startService(player);
                        break;

                    case "Lecture":
                        player.putExtra("action", STOP);
                        startService(player);
                        break;

                    default:
                        break;
                }
                break;

            case R.id.add:
                editRadio(null);
                break;

            case R.id.timer:
                if (!running) {
                    showDatePicker();
                } else {
                    showTimerInfo();
                }
                break;

            default:
                break;
        }
    }




   /* *********************************************************************************************
    * Mise à jour de la vue de la liste des radios
    * ********************************************************************************************/

    private void updateListView() {

        ArrayList<Object> items = new ArrayList<>();
        items.addAll(Radio.getRadios());
        radioView.setAdapter(new RadioAdapter(this, items, clickListener));
    }



   /* *********************************************************************************************
    * Click radio et menu radio
    * ********************************************************************************************/

    private final ListsClickListener clickListener = new ListsClickListener() {

        @Override
        public void onPlayableItemClick(PlayableItem item) {
            play((Radio)item);
        }

        @Override
        public void onPlayableItemMenuClick(PlayableItem item, int menuId) {
            switch(menuId) {
                case R.id.menu_edit:
                    editRadio((Radio)item);
                    break;
                case R.id.menu_delete:
                    deleteRadio((Radio)item);
                    break;
                default:
                    break;
            }
        }
    };

   /* **********************************************************************************************
    * Lecture de la radio
    * *********************************************************************************************/

    private void play(Radio radio) {

        String url = radio.getPlayableUri();

        String name = radio.getName();

        Intent player = new Intent(this, PlayerService.class);

        player.putExtra("action", "play");
        player.putExtra("url", url);
        player.putExtra("name", name);
        startService(player);


    }



   /* **********************************************************************************************
    * Suppression de la radio
    * *********************************************************************************************/

    private void deleteRadio(final Radio radio) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.deleteRadioConfirm));
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Radio.deleteRadio(radio);
                updateListView();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }



   /* **********************************************************************************************
    * Ajout ou édition d'une radio
    * *********************************************************************************************/

    private void editRadio(final Radio oldRadio) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        int title = oldRadio==null ? R.string.addRadio : R.string.edit;
        builder.setTitle(getResources().getString(title));
        @SuppressLint("InflateParams") final View view = getLayoutInflater().inflate(R.layout.layout_editwebradio, null);
        builder.setView(view);

        final EditText editTextUrl = (EditText)view.findViewById(R.id.editTextUrl);
        final EditText editTextName = (EditText)view.findViewById(R.id.editTextName);
        if(oldRadio!=null) {
            editTextUrl.setText(oldRadio.getUrl());
            editTextName.setText(oldRadio.getName());
        }

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String url = editTextUrl.getText().toString();
                String name = editTextName.getText().toString();
                if("".equals(url) || "http://".equals(url)) {
                    Toast.makeText(context, R.string.errorInvalidURL, Toast.LENGTH_SHORT).show();
                    return;
                }
                if("".equals(name))
                    name = url;

                if(oldRadio != null) {
                    Radio.deleteRadio(oldRadio);
                }

                Radio newRadio = new Radio(url, name);
                Radio.addRadio(newRadio);
                updateListView();
            }
        });

        builder.setNegativeButton(R.string.cancel, null);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }



   /* **********************************************************************************************
    * Fermeture notification
    * *********************************************************************************************/

    private void killNotif() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @SuppressLint("SetTextI18n")
            public void run() {

                if (!State.isPlaying())
                    Notification.removeNotification(context);

            }
        }, 500);
    }



   /* **********************************************************************************************
    * Volume observer
    * *********************************************************************************************/

    public class Control_Volume extends ContentObserver {
        private int previousVolume;
        private final Context context;

        public Control_Volume(Context c, Handler handler) {
            super(handler);
            context=c;

            AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            previousVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            volume();

            AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);

            int delta=previousVolume-currentVolume;

            if (delta >0) {
                previousVolume=currentVolume;
            }
            else if(delta<0) {
                previousVolume=currentVolume;
            }
        }
    }


    /* ******************************************
     * Gestion de l'affichage de l'icon de volume
     * ******************************************/

    private void volume() {

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        ImageView play = (ImageView) findViewById(R.id.icon_volume);

        if (currentVolume == 0) {
            assert play != null;
            play.setBackground(getDrawable(R.drawable.volume0));
        } else if (currentVolume < 4) {
            assert play != null;
            play.setBackground(getDrawable(R.drawable.volume1));
        } else if (currentVolume < 7) {
            assert play != null;
            play.setBackground(getDrawable(R.drawable.volume2));
        } else if (currentVolume < 10) {
            assert play != null;
            play.setBackground(getDrawable(R.drawable.volume3));
        } else if (currentVolume < 13) {
            assert play != null;
            play.setBackground(getDrawable(R.drawable.volume4));
        } else if (currentVolume < 16) {
            assert play != null;
            play.setBackground(getDrawable(R.drawable.volume5));
        }
    }



   /* **********************************************************************************************
    * Get bitrate
    * *********************************************************************************************/

    private void getBitRate() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            public void run() {
                bitRate();
                handler.postDelayed(this, 2000);
            }
        }, 1);
    }

    private void bitRate() {
        final int uid = android.os.Process.myUid();
        final long received = TrafficStats.getUidRxBytes(uid) / 1024;

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @SuppressLint("SetTextI18n")
            public void run() {

                long current = TrafficStats.getUidRxBytes(uid) / 1024;
                long total = current - received;

                long ByteToBit = total * 8;

                TextView BitRate = (TextView) findViewById(R.id.bitrate);

                if (ByteToBit <= 1024 ) {

                    String bitrate = String.valueOf(ByteToBit);
                    assert BitRate != null;
                    BitRate.setText(bitrate + " Kb/s");

                } else {
                    long megaBit = ByteToBit / 1024;
                    String bitrate = String.valueOf(megaBit);
                    assert BitRate != null;
                    BitRate.setText(bitrate + " Mb/s");
                }

            }
        }, 1000);
    }


   /* **********************************************************************************************
    * Sleep Timer
    * *********************************************************************************************/

    private void showDatePicker() {

        final String start = getString(R.string.start);
        final String cancel = getString(R.string.cancel);

        @SuppressLint("InflateParams")
        View view = getLayoutInflater().inflate(R.layout.date_picker_dialog, null);

        final TimePicker picker = (TimePicker) view.findViewById(R.id.time_picker);
        final Calendar cal = Calendar.getInstance();

        picker.setIs24HourView(true);

        picker.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(start, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int hours;
                int mins;

                int hour = picker.getCurrentHour();
                int minute = picker.getCurrentMinute();
                int curHour = cal.get(Calendar.HOUR_OF_DAY);
                int curMin = cal.get(Calendar.MINUTE);

                if (hour < curHour) hours =  (24 - curHour) + (hour);
                else hours = hour - curHour;

                if (minute < curMin) {
                    hours--;
                    mins = (60 - curMin) + (minute);
                } else mins = minute - curMin;

                startTimer(hours, mins);
            }
        });

        builder.setNegativeButton(cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // This constructor is intentionally empty, pourquoi ? parce que !
            }
        });

        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();
    }


   /* ***********
    * Start timer
    * ***********/

    private void startTimer(final int hours, final int minutes) {

        final String impossible = getString(R.string.impossible);

        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        final int delay = ((hours * 3600) + (minutes * 60)) * 1000;

        if (delay == 0) {
            Toast.makeText(this, impossible, Toast.LENGTH_LONG).show();
            return;
        }

        mTask = scheduler.schedule(new GetAudioFocusTask(this), delay, TimeUnit.MILLISECONDS);

        Notification.setState(true);
        running = true;
        State.getState(context);

        showTimeEcran();
    }


   /* ***************************************
    * Afficher temps restant dans AlertDialog
    * ***************************************/

    private void showTimerInfo() {

        final String continuer = getString(R.string.continuer);
        final String cancelTimer = getString(R.string.cancel_timer);


        if (mTask.getDelay(TimeUnit.MILLISECONDS) < 0) {
            stopTimer();
            return;
        }
        @SuppressLint("InflateParams")
        View view = getLayoutInflater().inflate(R.layout.timer_info_dialog, null);

        final TextView timeLeft = ((TextView) view.findViewById(R.id.time_left));

        final AlertDialog dialog = new AlertDialog.Builder(this).setPositiveButton(continuer, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }

        }).setNegativeButton(cancelTimer, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                stopTimer();

            }

        }).setView(view).create();

        new CountDownTimer(mTask.getDelay(TimeUnit.MILLISECONDS), 1000) {
            @Override
            public void onTick(long seconds) {

                long secondes = seconds;

                secondes = secondes / 1000;

                String textTemps = String.format(getString(R.string.timer_info), (secondes / 3600), ((secondes % 3600) / 60), ((secondes % 3600) % 60));

                timeLeft.setText(textTemps);
            }

            @Override
            public void onFinish() {
                dialog.dismiss();
            }
        }.start();

        dialog.show();
    }


   /* ********************************
    * Afficher temps restant à l'écran
    * ********************************/

    private void showTimeEcran() {

        iconTimer = ((ImageButton) findViewById(R.id.timer));
        iconTimer.setColorFilter(ContextCompat.getColor(context, R.color.colorAccent));

        timeAfficheur0 = ((ImageView) findViewById(R.id.icon_time));
        timeAfficheur1 = ((TextView) findViewById(R.id.time_ecran));

        timeAfficheur0.setVisibility(View.VISIBLE);
        timeAfficheur1.setVisibility(View.VISIBLE);


        timerEcran = new CountDownTimer(mTask.getDelay(TimeUnit.MILLISECONDS), 1000) {
            @Override
            public void onTick(long seconds) {

                long secondes = seconds;

                secondes = secondes / 1000;

                String textTemps = String.format(getString(R.string.timer_info), (secondes / 3600), ((secondes % 3600) / 60), ((secondes % 3600) % 60));

                timeAfficheur1.setText(textTemps);
            }

            @Override
            public void onFinish() {
                timeAfficheur0.setVisibility(View.INVISIBLE);
                timeAfficheur1.setVisibility(View.INVISIBLE);
            }

        }.start();
    }


   /* ****************
    * Annuler le timer
    * ****************/

    private void stopTimer() {
        if (running) {
            mTask.cancel(true);
            timerEcran.cancel();
        }

        running = false;

        Notification.setState(false);
        State.getState(context);

        timeAfficheur0 = ((ImageView) findViewById(R.id.icon_time));
        timeAfficheur1 = ((TextView) findViewById(R.id.time_ecran));

        timeAfficheur0.setVisibility(View.INVISIBLE);
        timeAfficheur1.setVisibility(View.INVISIBLE);

        iconTimer = ((ImageButton) findViewById(R.id.timer));
        iconTimer.setColorFilter(ContextCompat.getColor(context, R.color.controls_tint_light));
    }


    /* *****************
     * Arrêt de la radio
     * *****************/

    public static void stop(Context context) {

        Intent player = new Intent(context, PlayerService.class);
        player.putExtra("action", "stop");
        context.startService(player);

        running = false;

        Notification.setState(false);
        State.getState(context);
    }



    /***********************************************************************************************
     * About dialog
     **********************************************************************************************/

    private void about() {

        String title = getString(R.string.about);
        AlertDialog.Builder about = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();

        @SuppressLint("InflateParams") View dialoglayout = inflater.inflate(R.layout.alertdialog_main_noshadow, null);
        Toolbar toolbar = (Toolbar) dialoglayout.findViewById(R.id.dialog_toolbar_noshadow);
        toolbar.setTitle(title);
        toolbar.setTitleTextColor(0xffffffff);

        final TextView text = (TextView) dialoglayout.findViewById(R.id.showrules_dialog);
        text.setText(getString(R.string.about_message));

        about.setView(dialoglayout);

        AlertDialog dialog = about.create();
        dialog.show();
    }

}
