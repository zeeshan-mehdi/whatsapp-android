package com.shaikhutech.whatsapp.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shaikhutech.whatsapp.Adapters.CallsAdapter;
import com.shaikhutech.whatsapp.Adapters.UserAdapter;
import com.shaikhutech.whatsapp.Model.User;
import com.shaikhutech.whatsapp.PlaceCallActivity;
import com.shaikhutech.whatsapp.R;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;

import java.util.ArrayList;
import java.util.List;

import static org.webrtc.ContextUtils.getApplicationContext;

public class Calls extends Fragment {

    RecyclerView recyclerView;
    ArrayList<User> users;

    CallsAdapter userAdapter;
    FirebaseDatabase firebaseDatabase;

    FirebaseAuth auth;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                if (getActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || getActivity().checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED || getActivity().checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED || getActivity().checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.READ_PHONE_STATE}, 100);
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        getActivity().setTitle("Calls");

        View view = inflater.inflate(R.layout.fragment_calls, container, false);

        recyclerView = view.findViewById(R.id.userRecyclerView);

        users = new ArrayList<>();

        auth = FirebaseAuth.getInstance();



        firebaseDatabase = FirebaseDatabase.getInstance();

        getUsers();


        return view;

    }

    private void getUsers() {
        DatabaseReference ref= firebaseDatabase.getReference("Users");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.clear();

                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);

                    assert user != null;
                    if(!user.id.equals(auth.getCurrentUser().getUid())){

                        if(user.profilePicUrl ==null){
                            user.profilePicUrl = "https://picsum.photos/id/237/200/300";
                        }
                        users.add(user);
                    }

                }

                if(users.size()!=0) {

                    userAdapter = new CallsAdapter(getContext(), users);

                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerView.setAdapter(userAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }






}
