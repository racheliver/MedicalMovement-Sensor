package com.chenp_racheliv.finalProject;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class MusicService extends Service {
    private MediaPlayer player;
    public static boolean isRunning;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        player = MediaPlayer.create(this, R.raw.sound);
        player.setLooping(true);
        player.start();

        new Thread(new Runnable() {
            @Override
            public void run()
            {
                isRunning = true;
                int i = 0;
                while(isRunning)
                {
                    Log.d("debug","i="+i);
                    i++;
                    SystemClock.sleep(500);
                }
            }
        }).start();

        return START_STICKY;
    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();

        player.stop();
        Log.d("debug","MyService onDestroy()");
        isRunning = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
