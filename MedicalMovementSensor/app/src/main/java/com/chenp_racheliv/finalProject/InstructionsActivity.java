package com.chenp_racheliv.finalProject;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;

public class InstructionsActivity extends AppCompatActivity {
    private ViewPager slideViewPager;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private SliderAdapter sliderAdapter;
    private Button btnStart;
    private ImageView imgPlay;
    private ImageView imgStop;
    private boolean isPlaying;

    public static int currentPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);

        // get all refrences from XML
        slideViewPager = findViewById(R.id.slideViewPagerID);
        dotsLayout = findViewById(R.id.dotsLayoutID);
        btnStart = findViewById(R.id.btnStartID);
        imgPlay = findViewById(R.id.imgPlayID);
        imgStop = findViewById(R.id.imgStopID);

        sliderAdapter = new SliderAdapter(this);
        slideViewPager.setAdapter(sliderAdapter);

        addDotsIndicator(0);

        slideViewPager.addOnPageChangeListener(viewListener);

        // event listeners
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), IndicesActivity.class);
                startActivity(intent);
            }
        });

        imgPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(new Intent(view.getContext(), MusicService.class));
                imgPlay.setVisibility(View.INVISIBLE);
                imgStop.setVisibility(View.VISIBLE);
            }
        });

        imgStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService(new Intent(view.getContext(), MusicService.class));
                imgStop.setVisibility(View.INVISIBLE);
                imgPlay.setVisibility(View.VISIBLE);
            }
        });
    }

    // create 3 dots to slider
    public void addDotsIndicator(int position){
        dots = new TextView[3];
        dotsLayout.removeAllViews();

        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(getResources().getColor(R.color.gray));

            dotsLayout.addView(dots[i]);
        }

        if(dots.length > 0){
            dots[position].setTextColor(getResources().getColor(R.color.green));
        }
    }

    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            addDotsIndicator(position);
            currentPage = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @Override
    // create 3 points menu
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        MenuItem menuAbout = menu.add("About");
        MenuItem menuIntroduction = menu.add("Introduction");
        MenuItem menuNotification = menu.add("Set daily notification");
        MenuItem menuExit = menu.add("Exit");

        menuAbout.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                showAboutDialog();
                return true;
            }
        });

        menuIntroduction.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                showIntroductionDialog();
                return true;
            }
        });

        menuNotification.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                showNotificationDialog();
                return true;
            }
        });

        menuExit.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                showExitDialog();
                return true;
            }
        });
        return true;
    }

    private void showAboutDialog()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setIcon(R.drawable.ic_info_outline);
        alertDialog.setTitle("About Medical Movement Sensor");
        alertDialog.setMessage("This app was developed by\n\nChen Parnasa and Racheli Verechzon (c)");
        alertDialog.show();
    }

    private void showIntroductionDialog()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setIcon(R.drawable.ic_add_circle_outline);
        alertDialog.setTitle("The idea behind the app");
        alertDialog.setMessage("As a software engineering students, and as a researchers, we learned to ask the right questions to make theory to reality.\n\n"
        +"We used the software and technology to create a tool that can help to a big part of general population.\n\n"+"The purpose of the project is to provide information for a person with movement disorders in their hands (or anyone who wants to be aware to the level of stability of his hands).\n" +
                "The most common movement disorder is known as tremor, which can appear at rest, movement or static state. There are many causes of tremor, most tremors can aggravated by many reasons.\n" +
                "Today, medicine does not provide enough answers to what are the reasons for tremor in hands and moreover it does not check the level of tremor and what conditions increase the tremor for each person, therefore came up an idea for an app that gives the possibility to examine and estimate tremor level in various situations, also known as tremor amplifiers.\n\n"
        +"The app will use a sensor accelerator that measures mobility and unique for Android.");
        alertDialog.show();
    }

    private void showNotificationDialog()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setIcon(R.drawable.ic_notifications);
        alertDialog.setTitle("Set daily notification");
        alertDialog.setMessage("Do you want us to remind you to use this app once a day?");
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY,11);
                calendar.set(Calendar.MINUTE,57);
                calendar.set(Calendar.SECOND,0);

                Intent intent = new Intent(getApplicationContext(), AlarmNotificationReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),100,intent,PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //        Toast.makeText(SplashActivity.this, "NO", Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.show();
    }

    private void showExitDialog()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setIcon(R.drawable.ic_exit);
        alertDialog.setTitle("Exit App");
        alertDialog.setMessage("Do you really want to exit?");
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //Toast.makeText(SplashActivity.this, "Bye Bye!", Toast.LENGTH_SHORT).show();
                finishAffinity();
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //        Toast.makeText(SplashActivity.this, "NO", Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentPage = 0;

        // display an icon depending on the state of the music
        SharedPreferences sharedPreferences = getSharedPreferences("music", MODE_PRIVATE);
        isPlaying = sharedPreferences.getBoolean("isPlaying", false);

        if(isPlaying) {
            imgPlay.setVisibility(View.INVISIBLE);
            imgStop.setVisibility(View.VISIBLE);
        }
        else{
            imgPlay.setVisibility(View.VISIBLE);
            imgStop.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // save music state
        SharedPreferences sharedPreferences = getSharedPreferences("music", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if(MusicService.isRunning)
            editor.putBoolean("isPlaying", true);
        else
            editor.putBoolean("isPlaying", false);
        editor.commit();
    }
}
