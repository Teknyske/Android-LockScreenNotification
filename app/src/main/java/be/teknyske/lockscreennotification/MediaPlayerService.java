package be.teknyske.lockscreennotification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import static android.R.attr.action;

/**
 * Created by cerseilannister on 19/10/16.
 */

public class MediaPlayerService extends Service
{
    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_STOP = "action_stop";
    public static final String ACTION_PAUSE = "action_pause";


    private MediaPlayer mMediaPlayer;
    private MediaSessionManager mManager;

    private MediaSession mSession;
    private MediaController mController;

    @Override
    public IBinder onBind(Intent intent)
        {
        return null;
        }

    @Override
    public boolean onUnbind(Intent intent)
        {
        mSession.release();
        return super.onUnbind(intent);
        }


    private void handleIntent(Intent intent)
        {
        if (intent == null || intent.getAction() == null)
            return;
        String action =intent.getAction();
        if (action.equalsIgnoreCase (ACTION_PLAY))
            {
            mController.getTransportControls().play();
            }
        else if (action.equalsIgnoreCase (ACTION_PAUSE))
        {
        mController.getTransportControls().pause();
        }
        else if (action.equalsIgnoreCase (ACTION_STOP))
            {
            mController.getTransportControls().stop();
            }
        }

    private Notification.Action generateAction(int icon, String title, String intentAction)
        {
        Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new Notification.Action.Builder(icon, title, pendingIntent).build();
        }


    private void buildNotification(Notification.Action action)
        {
        Notification.MediaStyle style = new Notification.MediaStyle();
        Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
        intent.setAction(ACTION_STOP);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Media Title")
                .setContentText("Media Artist")
                .setDeleteIntent(pendingIntent)
                .setStyle(style);

        builder.addAction(action);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());

        }

    @Override
    public void onCreate()
        {
        Toast.makeText(this, "My Service created", Toast.LENGTH_LONG).show();
        mMediaPlayer = MediaPlayer.create(this, R.raw.sound);
        mMediaPlayer.setLooping(false);
        }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
        {
        if (mManager == null)
            {
            initMediaSessions();
            }
        handleIntent(intent);
        return super.onStartCommand(intent, flags, startId);
        }

    private void initMediaSessions()
        {
        mSession = new MediaSession(getApplicationContext(), "simple player session");
        mController = new MediaController(getApplicationContext(), mSession.getSessionToken());
        mSession.setCallback(new MediaSession.Callback()
        {

            @Override
            public void onPlay()
                {
                super.onPlay();
                if (mMediaPlayer != null && !(mMediaPlayer.isPlaying()))
                    {
                    mMediaPlayer.start();
                    }
                Log.e("MediaPlayerService", "onPlay");
                buildNotification(generateAction(android.R.drawable.ic_media_pause, "pause", ACTION_PAUSE));

                }

            @Override
            public void onPause()
                {
                super.onPause();
                if (mMediaPlayer.isPlaying())
                    {
                    mMediaPlayer.pause();
                    }
                Log.e("MediaPlayerService", "onPause");
                }

            @Override
            public void onStop()
                {
                super.onStop();
                mMediaPlayer.stop();
                Log.e("MediaPlayerService", "onSstop");
                NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(1);
                Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
                stopService(intent);
                }


        });

        }


}





