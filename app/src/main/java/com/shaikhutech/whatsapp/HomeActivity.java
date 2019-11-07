package com.shaikhutech.whatsapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.support.design.widget.TabLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.shaikhutech.whatsapp.Adapters.ViewPagerAdapter;

public class HomeActivity extends AppCompatActivity {

    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);




        ViewPager viewPager = findViewById(R.id.viewPager);

        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));


        TabLayout tableLayout = findViewById(R.id.tableLayout);


        tableLayout.setupWithViewPager(viewPager);


        toolbar = findViewById(R.id.toolbar);


        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("WhatsApp");


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_home,menu);

        super.onCreateOptionsMenu(menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==R.id.menuSignout){
            FirebaseAuth.getInstance().signOut();

            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void status(final String status){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        FirebaseDatabase.getInstance().getReference("Users")
                .child(user.getUid()).child("status").setValue(status).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Log.e("online",status);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("online",status);
                    }
                });
    }

    @Override
    protected void onResume() {
        status("online");
        super.onResume();
    }


    @Override
    protected void onDestroy() {
        status("offline");
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        status("offline");
        super.onStop();
    }
}
