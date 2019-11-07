package com.shaikhutech.whatsapp.Notifications;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;

public class MyFirebaseIdService extends FirebaseMessagingService {

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
}
