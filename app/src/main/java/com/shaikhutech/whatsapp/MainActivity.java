package com.shaikhutech.whatsapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.shaikhutech.whatsapp.Model.User;
import com.sinch.android.rtc.SinchError;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends BaseActivity implements SinchService.StartFailedListener {

    boolean flag = false;

    EditText phoneNumber, verificationCode, userName;

    CircleImageView profileImage;


    String imageUrl;

    final int requestCode = 1;

    final int pickImageRequest = 2;


    Button sendSMS, btnVerify,btnSave;
    String TAG = "MainActivity";

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;

    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;


    Uri imagePath;


    private FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phoneNumber = findViewById(R.id.editTextPhone);

        verificationCode = findViewById(R.id.editTextVerificationCode);


        userName = findViewById(R.id.txtUserName);

        profileImage = findViewById(R.id.profile_image);

        sendSMS = findViewById(R.id.btnSendSMS);

        btnVerify = findViewById(R.id.btnVerify);

        btnSave = findViewById(R.id.btnSave);



        mAuth = FirebaseAuth.getInstance();







        if(mAuth.getCurrentUser()!=null){

            flag = true;

        }


        firebaseDatabase = FirebaseDatabase.getInstance();


        applyCallBacks();

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPermissionGranted()) {
                    chooseImage();
                } else {
                    requestPermission();
                }

                try {
                    if(imagePath!=null) {


                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagePath);

                        profileImage.setImageBitmap(bitmap);
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });


    }


    public void verifyCode(View view) {
        try {

            String code = verificationCode.getText().toString();

            if (code == null) {
                return;
            }


            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);

            signInWithPhoneAuthCredential(credential);
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    @Override
    protected void onServiceConnected() {

        super.onServiceConnected();
        Log.e("service","sinch connected");

        if (flag && getSinchServiceInterface()!=null&&!getSinchServiceInterface().isStarted()) {
            String id = mAuth.getCurrentUser().getUid();

            Log.e("id",id);

            getSinchServiceInterface().startClient(id);
            Log.e("activity","starting");
            startActivityNow();
        }

        getSinchServiceInterface().setStartListener(this);

    }


    public  void checkSinchStatus(){
        if (!getSinchServiceInterface().isStarted()) {
            getSinchServiceInterface().startClient(FirebaseAuth.getInstance().getUid());
        }
    }

    public void sendSMS(View view) {
        String phone = phoneNumber.getText().toString();


        if (phone.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please Enter Phone Number", Toast.LENGTH_SHORT).show();
            return;
        }

        sendSMS.setVisibility(View.GONE);
        phoneNumber.setVisibility(View.GONE);

        btnVerify.setVisibility(View.VISIBLE);

        verificationCode.setVisibility(View.VISIBLE);


        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phone,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks


    }


    public void applyCallBacks() {
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                //Log.d(TAG, "onVerificationCompleted:" + credential);


                Toast.makeText(getApplicationContext(), "completed", Toast.LENGTH_SHORT).show();
                signInWithPhoneAuthCredential(credential);


            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);

                Toast.makeText(getApplicationContext(), "failed", Toast.LENGTH_SHORT).show();

                if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }

                // Show a message and update the UI
                // ...
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                Toast.makeText(MainActivity.this, "Code Sent", Toast.LENGTH_SHORT).show();

                // ...
            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            Toast.makeText(MainActivity.this, "User Signed In Successfully ...", Toast.LENGTH_SHORT).show();

                            verificationCode.setVisibility(View.GONE);

                            btnVerify.setVisibility(View.GONE);

                            profileImage.setVisibility(View.VISIBLE);

                            userName.setVisibility(View.VISIBLE);

                            btnSave.setVisibility(View.VISIBLE);

                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            task.getException();


                        }
                    }
                });
    }

    public void saveInfo(View view) {


        DatabaseReference ref = firebaseDatabase.getReference("Users");

        FirebaseUser user1 = mAuth.getCurrentUser();

        String user_name = userName.getText().toString();

        uploadImage();


        User user = new User(user_name, user1.getUid().toString(), imageUrl, "offline", user1.getPhoneNumber().toString());

        ref.child(user.id).setValue(user).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "failed to create user ", Toast.LENGTH_SHORT).show();
            }
        })

                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        if (!getSinchServiceInterface().isStarted()) {
                            getSinchServiceInterface().startClient(FirebaseAuth.getInstance().getUid());
                        }

                       flag = true;
                    }
                });
    }


    public void uploadImage() {
        if (imagePath != null) {
            final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

            final StorageReference reference = firebaseStorage.getReference("images/" + UUID.randomUUID().toString());

            reference.putFile(imagePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(getApplicationContext(),"profile pic uploaded",Toast.LENGTH_SHORT).show();

                            imageUrl = reference.getName().toString();


                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Log.e("imageUrl", String.valueOf(uri));


                                    imageUrl =  String.valueOf(uri);

                                    DatabaseReference reference1 = firebaseDatabase.getReference("Users");

                                    reference1.child(mAuth.getCurrentUser().getUid()).child("profilePicUrl").setValue(imageUrl);
                                }
                            });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });


        }
    }

    public boolean isPermissionGranted() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.e("permission", "Permission granted");
            chooseImage();

        }
    }

    public void chooseImage() {
        Intent intent = new Intent();

        intent.setType("image/*");

        intent.setAction(Intent.ACTION_GET_CONTENT);


        startActivityForResult(intent.createChooser(intent, "Select Picture"), pickImageRequest);
    }

    public  void startActivityNow(){
        Intent intent = new Intent(MainActivity.this,HomeActivity.class);

        startActivity(intent);

        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == pickImageRequest && resultCode == RESULT_OK) {
            imagePath = data.getData();
        }


    }

    private void status(final String status){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user==null){
            return;
        }


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
    public void onStartFailed(SinchError error) {
        Log.e("service","failed to start");
    }

    @Override
    public void onStarted() {
        Log.e("started ","yes");
    }

}
