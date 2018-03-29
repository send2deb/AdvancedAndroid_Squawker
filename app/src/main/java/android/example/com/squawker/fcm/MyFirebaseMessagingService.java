package android.example.com.squawker.fcm;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.example.com.squawker.MainActivity;
import android.example.com.squawker.R;
import android.example.com.squawker.provider.SquawkContract;
import android.example.com.squawker.provider.SquawkProvider;
import android.os.AsyncTask;

import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Created by debashispaul on 29/03/2018.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMessagingServ";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived: is called");

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        Map<String, String> data = remoteMessage.getData();
        if(!data.isEmpty()) {
            insertSquawk(data);
            sendNotification(data);
        }
    }

    private void insertSquawk(final Map<String, String> data) {
        Log.d(TAG, "insertSquawk: is called");
        AsyncTask<Void,Void,Void> insertSquawk = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                ContentValues contentValues = new ContentValues();


                contentValues.put(SquawkContract.COLUMN_AUTHOR, data.get("author"));
                contentValues.put(SquawkContract.COLUMN_AUTHOR_KEY, data.get("authorKey"));

                contentValues.put(SquawkContract.COLUMN_MESSAGE, data.get("message"));
                contentValues.put(SquawkContract.COLUMN_DATE, data.get("date"));

                getContentResolver()
                        .insert(SquawkProvider.SquawkMessages.CONTENT_URI, contentValues);

                return null;
            }
        };

        insertSquawk.execute();
    }

    private void sendNotification(Map<String, String> data) {
        String author = data.get("author");
        String msg = data.get("message");
        Log.d(TAG, "sendNotification: author ->" + author + " and msg -> " + msg);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_duck)
                .setContentTitle(getResources().getString(R.string.notification_message, author))
                .setContentText(msg.substring(0,29) + "...")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(1,mBuilder.build());
    }
}
