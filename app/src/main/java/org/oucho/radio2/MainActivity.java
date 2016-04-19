package org.oucho.radio2;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.oucho.radio2.egaliseur.AudioEffects;
import org.oucho.radio2.egaliseur.EqualizerActivity;
import org.oucho.radio2.itf.ListsClickListener;
import org.oucho.radio2.itf.RadioAdapter;
import org.oucho.radio2.itf.PlayableItem;
import org.oucho.radio2.itf.Radio;
import org.oucho.radio2.timer.GetAudioFocusTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener {

    private static Context context;


    private static final String PLAY = "play";
    private static final String STOP = "stop";
    private static final String STATE = "org.oucho.radio2.STATE";

    private static final int USER_SETTINGS_REQUEST = 10;
    private static final int NOTIF_ID = 15;

    private static final String fichier_préférence = "org.oucho.radio2_preferences";
    private static SharedPreferences préférences = null;



    private static String nom_radio = "";
    private static final String nom_radio_pref = "";
    private static String action_lecteur = "";

    private static String etat_lecture = "";
    private static final String etat_lecture_pref = "";


    private Etat_player Etat_player_Receiver;
    private NotificationManager notificationManager;


    public Player musicService;


    protected LinearLayoutManager layoutManager;

    RecyclerView radioView;

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ActionBarDrawerToggle mDrawerToggle;


    private static boolean running;

    private static ScheduledFuture mTask;


    /* *********************************************************************************************
     * Création de l'activité
     * ********************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        context = getApplicationContext();

        préférences = getSharedPreferences(fichier_préférence, MODE_PRIVATE);


        Etat_player_Receiver = new Etat_player();


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


        radioView = (RecyclerView)findViewById(R.id.recyclerView);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);

        mNavigationView.setNavigationItemSelectedListener(this);

        layoutManager = new LinearLayoutManager(this);
        radioView.setLayoutManager(layoutManager);


        updateListView();
        AudioEffects.init(this);
        getBitRate();

        nom_radio = préférences.getString("name", nom_radio_pref);
        action_lecteur = préférences.getString("action", etat_lecture_pref);

        createNotification(nom_radio, action_lecteur);

        updatePlayStatus();

        volume();


        this.findViewById(R.id.add).setOnClickListener(this);
        this.findViewById(R.id.timer).setOnClickListener(this);
        this.findViewById(R.id.play).setOnClickListener(this);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,toolbar ,  R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        mDrawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        final Drawable upArrow = ContextCompat.getDrawable(context, R.drawable.ic_menu_black_24dp);
        upArrow.setColorFilter(ContextCompat.getColor(context, R.color.controls_tint_light), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

    }



    /* *********************************************************************************************
     * Passage en arrière plan de l'application
     * ********************************************************************************************/

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences.Editor editor = préférences.edit();
        editor.putString("etat", etat_lecture);
        editor.apply();

        if (!"play".equals(etat_lecture))
            killNotif();
    }

    /* *********************************************************************************************
     * Réactivation de l'application
     * ********************************************************************************************/

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter(STATE);
        registerReceiver(Etat_player_Receiver, filter);

        TextView nomStation = (TextView) findViewById(R.id.station);
        nomStation.setText(nom_radio);

        etat_lecture = préférences.getString("etat", etat_lecture_pref);

        TextView myAwesomeTextView = (TextView) findViewById(R.id.etat);
        myAwesomeTextView.setText(action_lecteur);

        nom_radio = préférences.getString("name", nom_radio_pref);
        action_lecteur = préférences.getString("action", etat_lecture_pref);

        createNotification(nom_radio, action_lecteur);

        updatePlayStatus();

        volume();

    }
    /* *********************************************************************************************
     * Navigation Drawer
     * ********************************************************************************************/

    public DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        mDrawerLayout.closeDrawers();

        Intent player = new Intent(this, Player.class);

        switch (menuItem.getItemId()) {
            case R.id.action_equalizer:
                //NavigationUtils.showEqualizer(MainActivity.this);
                equalizer();
                break;

            case R.id.action_musique:
                musique();
                break;

            case R.id.action_sleep_timer:
                if (! running) {
                    showDatePicker();
                } else {
                    showTimerInfo();
                }
                break;

            case R.id.nav_help:
                about();
                break;

            case R.id.nav_exit:
                etat_lecture = "stop";
                player.putExtra("action", STOP);
                startService(player);

                SharedPreferences.Editor editor = préférences.edit();
                editor.putString("etat", "stop");
                editor.putString("action", "");
                editor.apply();

                stopTimer();

                killNotif();

                finish();

                break;

            default: //do nothing
                break;
        }
        return true;
    }

    private void equalizer() {
        Intent intent = new Intent();
        intent.setClass(this, EqualizerActivity.class);
        startActivityForResult(intent, 0);
    }



    /* **************************
     * Lance l'application radio
     * **************************/

    private void musique() {

        Context context = getApplicationContext();

        PackageManager pm = context.getPackageManager();
        Intent appStartIntent = pm.getLaunchIntentForPackage("org.oucho.musicplayer");
        context.startActivity(appStartIntent);
        //killNotif();
    }

    /* *********************************************************************************************
     * Gestion des clicks
     * ********************************************************************************************/

    @Override
    public void onClick(View v) {

        Intent player = new Intent(this, Player.class);

        switch (v.getId()) {
            case R.id.play:
                switch (etat_lecture) {
                    case "stop":
                        etat_lecture = "play";
                        player.putExtra("action", PLAY);
                        startService(player);
                        updatePlayStatus();
                        break;

                    case "play":
                        etat_lecture = "stop";
                        player.putExtra("action", STOP);
                        startService(player);
                        updatePlayStatus();
                        break;

                    default: //do nothing
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

            default: //do nothing
                break;
        }
    }


    public void updateListView() {
        Radio playingRadio = null;
        if(getCurrentPlayingItem() instanceof Radio)
            playingRadio = (Radio)getCurrentPlayingItem();

        ArrayList<Object> items = new ArrayList<>();
        items.addAll(Radio.getRadios());
        radioView.setAdapter(new RadioAdapter(this, items, playingRadio, clickListener));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public ListsClickListener clickListener = new ListsClickListener() {
        @Override public void onHeaderClick() {}

        @Override
        public void onPlayableItemClick(PlayableItem item) {
            play((Radio) item);
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
            }
        }

        @Override public void onCategoryClick(Object object) {}
        @Override public void onCategoryMenuClick(Object item, int menuId) {}
    };

    private void play(Radio radio) {

        String url = radio.getPlayableUri();

        String name = radio.getName();

        Intent player = new Intent(this, Player.class);

        player.putExtra("action", "play");
        player.putExtra("url", url);
        player.putExtra("name", name);
        startService(player);

        etat_lecture = "play";
        updatePlayStatus();

        gotoPlayingItemPosition(radio);

    }

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

    private void editRadio(final Radio oldRadio) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        int title = oldRadio==null ? R.string.addRadio : R.string.edit;
        builder.setTitle(getResources().getString(title));
        final View view = getLayoutInflater().inflate(R.layout.layout_editwebradio, null);
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
                if(url.equals("") || url.equals("http://")) {
                    Toast.makeText(context, R.string.errorInvalidURL, Toast.LENGTH_SHORT).show();
                    return;
                }
                if(name.equals("")) name = url;

                if(oldRadio!=null) {
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

    public void gotoPlayingItemPosition(PlayableItem playingItem) {
        int position = ((RadioAdapter)radioView.getAdapter()).getPlayableItemPosition(playingItem);
        layoutManager.scrollToPosition(position);
    }

    public static Context getContext() {
        return context;
    }

    public PlayableItem getCurrentPlayingItem() {
        if(musicService==null);
        return null;
        //return musicService.getCurrentPlayingItem();
    }



    /* *********************************************************************************************
     * Fcontionnalités sur l'afficheur
     * ********************************************************************************************/

    private void updatePlayStatus() {
        ImageView equalizer = (ImageView) findViewById(R.id.icon_equalizer);

        ImageView button = (ImageView) findViewById(R.id.play);

        if ("stop".equals(etat_lecture)) {
            equalizer.setBackground(getDrawable(R.drawable.ic_equalizer0));
            button.setImageResource(R.drawable.musicplayer_play);
        } else {
            equalizer.setBackground(getDrawable(R.drawable.ic_equalizer));
            button.setImageResource(R.drawable.musicplayer_pause);
        }
    }


    /***********************************************************************************************
     * Volume observer
     **********************************************************************************************/

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

    private void volume() {

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        ImageView play = (ImageView) findViewById(R.id.icon_volume);

        if (currentVolume == 0) {
            play.setBackground(getDrawable(R.drawable.volume0));
        } else if (currentVolume < 4) {
            play.setBackground(getDrawable(R.drawable.volume1));
        } else if (currentVolume < 7) {
            play.setBackground(getDrawable(R.drawable.volume2));
        } else if (currentVolume < 10) {
            play.setBackground(getDrawable(R.drawable.volume3));
        } else if (currentVolume < 13) {
            play.setBackground(getDrawable(R.drawable.volume4));
        } else if (currentVolume < 16) {
            play.setBackground(getDrawable(R.drawable.volume5));
        }
    }



    /***********************************************************************************************
     * Get bitrate
     **********************************************************************************************/

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
                String bitrate = String.valueOf(ByteToBit);

                TextView BitRate = (TextView) findViewById(R.id.bitrate);
                BitRate.setText(bitrate + " kb/s");
            }
        }, 1000);
    }



    /***********************************************************************************************
     * Broadcast receiver
     **********************************************************************************************/

    private class Etat_player extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            TextView status = (TextView) findViewById(R.id.etat);
            String action_lecteur = intent.getStringExtra("state");
            status.setText(action_lecteur);

            SharedPreferences.Editor editor = préférences.edit();

            if ("Déconnecté".equals(action_lecteur)) {
                etat_lecture = "stop";
                editor.putString("etat", etat_lecture);
            }

            editor.putString("action", action_lecteur);
            editor.apply();

            TextView StationTextView = (TextView) findViewById(R.id.station);
            String lecture = intent.getStringExtra("name");
            StationTextView.setText(lecture);

            nom_radio = préférences.getString("name", nom_radio_pref);
            action_lecteur = préférences.getString("action", etat_lecture_pref);
            createNotification(nom_radio, action_lecteur);
        }
    }



    /***********************************************************************************************
     * Sleep Timer
     **********************************************************************************************/

    private void showDatePicker() {

        final String start = getString(R.string.start);
        final String cancel = getString(R.string.cancel);

        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.date_picker_dialog, null);

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

    private void showTimerInfo() {

        final String continuer = getString(R.string.continuer);
        final String cancelTimer = getString(R.string.cancel_timer);


        if (mTask.getDelay(TimeUnit.MILLISECONDS) < 0) {
            stopTimer();
            return;
        }
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.timer_info_dialog, null);
        final TextView timeLeft = ((TextView) view.findViewById(R.id.time_left));


        final String stopTimer = getString(R.string.stop_timer);

        final AlertDialog dialog = new AlertDialog.Builder(this).setPositiveButton(continuer, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setNegativeButton(cancelTimer, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                stopTimer();

                Context context = getApplicationContext();

                Toast.makeText(context, stopTimer, Toast.LENGTH_LONG).show();
            }
        }).setView(view).create();

        new CountDownTimer(mTask.getDelay(TimeUnit.MILLISECONDS), 1000) {
            @Override
            public void onTick(long seconds) {

                long secondes = seconds;

                secondes = secondes / 1000;
                timeLeft.setText(String.format(getString(R.string.timer_info), (secondes / 3600), ((secondes % 3600) / 60), ((secondes % 3600) % 60)));
            }

            @Override
            public void onFinish() {
                dialog.dismiss();
            }
        }.start();

        dialog.show();
    }

    private void startTimer(final int hours, final int minutes) {

        final String impossible = getString(R.string.impossible);

        final String heureSingulier = getString(R.string.heure_singulier);
        final String heurePluriel = getString(R.string.heure_pluriel);

        final String minuteSingulier = getString(R.string.minute_singulier);
        final String minutePluriel = getString(R.string.minute_pluriel);

        final String arret = getString(R.string.arret);
        final String et = getString(R.string.et);

        final String heureTxt;
        final String minuteTxt;

        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        final int delay = ((hours * 3600) + (minutes * 60)) * 1000;

        if (delay == 0) {
            Toast.makeText(this, impossible, Toast.LENGTH_LONG).show();
            return;
        }

        if (hours == 1) {
            heureTxt = heureSingulier;
        } else {
            heureTxt = heurePluriel;
        }

        if (minutes == 1) {
            minuteTxt = minuteSingulier;
        } else {
            minuteTxt = minutePluriel;
        }
        mTask = scheduler.schedule(new GetAudioFocusTask(this), delay, TimeUnit.MILLISECONDS);

        if (hours == 0) {
            Toast.makeText(this, arret + " " + minutes + " " + minuteTxt, Toast.LENGTH_LONG).show();
        } else if (minutes == 0) {
            Toast.makeText(this, arret + " " + hours + " " + heureTxt, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, arret + " " + hours + " " + heureTxt + " " + et + " " + minutes + " " + minuteTxt, Toast.LENGTH_LONG).show();
        }

        running = true;
    }

    public static void stopTimer() {
        if (running) mTask.cancel(true);

        running = false;

    }


    public static void stop(Context context) {
        Intent player = new Intent(context, Player.class);
        player.putExtra("action", "stop");
        context.startService(player);

        running = false;
    }



    /* *********************************************************************************************
     * Notification
     * ********************************************************************************************/

    private void createNotification(String nom, String action) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(intent);
        builder.setSmallIcon(R.drawable.notification);
        builder.setOngoing(true);

        Boolean unlock;
        if ("play".equals(etat_lecture)) {
            unlock = true;
        } else {
            unlock = false;
        }
        builder.setOngoing(unlock);

        Notification notification = builder.build();
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notification);

        contentView.setTextViewText(R.id.notif_name, nom);
        contentView.setTextViewText(R.id.notif_text, action);

        notification.contentView = contentView;

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIF_ID, notification);
    }

    private void killNotif() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @SuppressLint("SetTextI18n")
            public void run() {

                notificationManager.cancel(NOTIF_ID);

            }
        }, 500);
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
