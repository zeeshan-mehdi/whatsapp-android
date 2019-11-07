package com.shaikhutech.whatsapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.shaikhutech.whatsapp.Adapters.ChatAdapter;
import com.shaikhutech.whatsapp.Fragments.APIService;
import com.shaikhutech.whatsapp.Model.Message;
import com.shaikhutech.whatsapp.Model.User;
import com.shaikhutech.whatsapp.Notifications.Client;
import com.shaikhutech.whatsapp.Notifications.Data;
import com.shaikhutech.whatsapp.Notifications.MyResponse;
import com.shaikhutech.whatsapp.Notifications.Sender;
import com.shaikhutech.whatsapp.Notifications.Token;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE;
import static android.provider.Telephony.Mms.Part.CONTENT_TYPE;

public class MessageActivity extends AppCompatActivity {

    private static final String AUTHORIZATION ="Authorization-key";
    CircleImageView profilePic;

    TextView txtUserName, txtMessage, txtUserOnline;

    ImageButton sendButton;

    Toolbar toolbar;
    String id;

    FirebaseDatabase firebaseDatabase;

    DatabaseReference ref;

    FirebaseAuth mAuth;

    ArrayList<Message> messageArrayList;

    RecyclerView recyclerView;


    APIService apiService;

    final String fcmUrl ="https://fcm.googleapis.com/fcm/send";

    final String APPLICATION_JSON ="application/json";

    final String AUTHORIZATION_KEY ="AAAAEYLtyIw:APA91bFfEMkYn0xuWrQcSUamRL-qAfw4_q8vllv6cVclZYSbwaSxISOGn0dOdAHHsMWrvnsAQFoHnN1AAusT20C749WxQmHJRh9Vv-RgBqZjxcCN1gUNrb-JE1Nrzar_AWL0Gs38R3_z";
    boolean notify = false;
    private String TAG ="MessageActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        id = getIntent().getStringExtra("userId");


        profilePic = findViewById(R.id.userProfileIcon);

        txtUserName = findViewById(R.id.txtUser_name);

        toolbar = findViewById(R.id.toolbar);

        txtMessage = findViewById(R.id.messageText);


        txtUserOnline = findViewById(R.id.userisOnline);


        mAuth = FirebaseAuth.getInstance();
        sendButton = findViewById(R.id.btnSend);

        messageArrayList = new ArrayList<>();

        recyclerView = findViewById(R.id.messagesRecyclerView);


        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());

        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(linearLayoutManager);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                String message = txtMessage.getText().toString();

                if (!message.equals("")) {

                    Message chat = new Message(mAuth.getCurrentUser().getUid(), id, message, false);

                    sendMessage(chat);

                } else {
                    Toast.makeText(MessageActivity.this, "Can't Send Empty Message", Toast.LENGTH_SHORT).show();
                }


                txtMessage.setText("");

            }
        });


        try {


            setSupportActionBar(toolbar);


            setTitle("");


            getSupportActionBar().setDisplayHomeAsUpEnabled(true);


            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            getUserInfo();

        }


    }

    private void sendMessage(final Message chat) {

        DatabaseReference ref = firebaseDatabase.getReference();

        ref.child("chats").push().setValue(chat).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MessageActivity.this, "We failed to Send Message...", Toast.LENGTH_SHORT).show();
            }
        });

        final String msg = chat.message;

        DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        ref2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                if (notify)
                    sendNotification(chat.reciever, user.name, msg);
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    public void sendNotification(final String reciever, final String userName, final String message) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");

        Query query = tokens.orderByKey().equalTo(reciever);

        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Token token = ds.getValue(Token.class);


                    Data data = new Data(userId, R.mipmap.ic_launcher, userName + " " + message, "New Message", reciever);


                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            Toast.makeText(MessageActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    Log.e("error", t.getMessage());

                                    Toast.makeText(MessageActivity.this, "Failed" + t.toString(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void notify_(final String reciever, final String userName, final String message) {

        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");

        Query query = tokens.orderByKey().equalTo(reciever);

        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Token token = ds.getValue(Token.class);


                    Data data = new Data(userId, R.mipmap.ic_launcher, userName + " " + message, "New Message", reciever);


                    Sender sender = new Sender(data, token.getToken());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        RequestBody requestBody = null;
//        try {
//            //requestBody = RequestBody.create( getValidJsonBody());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

        Request request = new Request.Builder()
                .addHeader(CONTENT_TYPE, APPLICATION_JSON)
                .addHeader(AUTHORIZATION, AUTHORIZATION_KEY)
                .url(fcmUrl)
                .post(requestBody)
                .build();

        okhttp3.Call call = new OkHttpClient().newCall(request);
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e(TAG,"failed");
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if(response.isSuccessful()){
                    Log.e(TAG,"successful");
                }else{
                    Log.e(TAG,"failed");
                }
            }
        });

    }

    private JSONObject getValidJsonBody(String mReceiverFirebaseToken,String mTitle,String mMessage,String mUsername,String mUid,String mFirebaseToken) throws JSONException {
//        JSONObject jsonObjectBody = new JSONObject();
//        jsonObjectBody.put(KEY_TO, mReceiverFirebaseToken);
//
//        JSONObject jsonObjectData = new JSONObject();
//        jsonObjectData.put(KEY_TITLE, mTitle);
//        jsonObjectData.put(KEY_TEXT, mMessage);
//        jsonObjectData.put(KEY_USERNAME, mUsername);
//        jsonObjectData.put(KEY_UID, mUid);
//        jsonObjectData.put(KEY_FCM_TOKEN, mFirebaseToken);
//        jsonObjectBody.put(KEY_DATA, jsonObjectData);
//
        return null;
    }

    private void getUserInfo() {
        firebaseDatabase = FirebaseDatabase.getInstance();

        ref = firebaseDatabase.getReference("Users").child(id);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {


                    User user = dataSnapshot.getValue(User.class);


                    assert user != null;
                    if (user.profilePicUrl != null)
                        Glide.with(getApplicationContext()).load(user.profilePicUrl).into(profilePic);
                    else
                        // profilePic.setImageResource(R.drawable.placeholder);
                        txtUserName.setText(user.name);

                    txtUserOnline.setText(user.status);


                    getMessages(mAuth.getCurrentUser().getUid());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void getMessages(final String userId) {
        DatabaseReference reference = firebaseDatabase.getReference("chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageArrayList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {


                    Message chat = ds.getValue(Message.class);


                    if (chat != null && (chat.sender.equals(userId) && chat.reciever.equals(id) || chat.reciever.equals(userId) && chat.sender.equals(id))) {
                        messageArrayList.add(chat);
                    }


                }

                if (messageArrayList.size() != 0) {

                    ChatAdapter chatAdapter = new ChatAdapter(getApplicationContext(), messageArrayList);

                    recyclerView.setAdapter(chatAdapter);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
