package com.shaikhutech.whatsapp.Notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.shaikhutech.whatsapp.MessageActivity;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String sented = remoteMessage.getData().get("sented");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user!=null && sented.equals(user.getUid())){
            sendNotification(remoteMessage);
        }


    }



    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            updateToken(s);
        }

    }



    private void updateToken(String s) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");

        Token token = new Token(s);

        ref.child(user.getUid()).setValue(token);



    }

    private void sendNotification(RemoteMessage remoteMessage) {

        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");


        RemoteMessage.Notification notification = remoteMessage.getNotification();

        int j = Integer.parseInt(user.replaceAll("[\\D]",""));

        Intent intent = new Intent(this, MessageActivity.class);

        Bundle bundle = new Bundle();

        bundle.putString("userid",user);

        intent.putExtras(bundle);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,j,intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setContentIntent(pendingIntent);

        NotificationManager noti = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int i =0;

        if(j>0){
            i=j;
        }

        noti.notify(i,builder.build());



    }
}
