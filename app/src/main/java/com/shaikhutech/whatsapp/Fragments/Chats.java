package com.shaikhutech.whatsapp.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.shaikhutech.whatsapp.Adapters.UserAdapter;
import com.shaikhutech.whatsapp.Model.Message;
import com.shaikhutech.whatsapp.Model.User;
import com.shaikhutech.whatsapp.Notifications.Token;
import com.shaikhutech.whatsapp.R;

import java.util.ArrayList;

public class Chats extends Fragment {

    RecyclerView recyclerView;
    ArrayList<User> users;

    UserAdapter userAdapter;

    FirebaseDatabase firebaseDatabase;

    FirebaseAuth auth;

    FirebaseUser fuser;

    ArrayList<String> userIds;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setTitle("Chats");

        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView = view.findViewById(R.id.chatsRecyclerView);

        users = new ArrayList<>();
        userIds = new ArrayList<>();

        auth = FirebaseAuth.getInstance();



        firebaseDatabase = FirebaseDatabase.getInstance();

        fuser = auth.getCurrentUser();

        grabUsersList(fuser);


        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(getActivity(),  new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String newToken = instanceIdResult.getToken();
                Log.e("newToken",newToken);
                updateToken(newToken);

            }
        });

        return view;
    }


    public void grabUsersList(final FirebaseUser fuser){

        DatabaseReference ref = firebaseDatabase.getReference("chats");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userIds.clear();
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    Message chat = ds.getValue(Message.class);


                    if(chat.sender.equals(fuser.getUid())){
                        userIds.add(chat.reciever);
                    }

                    if(chat.reciever.equals(fuser.getUid())){
                        userIds.add(chat.sender);
                    }

                    fetchUsers();


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




        //updateToken(FirebaseInstanceId.getInstance().getToken());


    }


    public void updateToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");

        Token token1 = new Token(token);

        reference.child(fuser.getUid()).setValue(token1);


    }

    private void fetchUsers() {

        DatabaseReference ref = firebaseDatabase.getReference("Users");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    for(String userId:userIds){
                        if(userId.equals(user.id)){
                            if(!userExists(userId)){

                                users.add(user);
                            }
                        }
                    }


                }

                UserAdapter userAdapter = new UserAdapter(getContext(),users);

                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

                recyclerView.setAdapter(userAdapter);





            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private boolean userExists(String userId) {

        for(User user:users){
            if(user.id.equals(userId)){
                return true;
            }
        }

        return false;


    }


}
