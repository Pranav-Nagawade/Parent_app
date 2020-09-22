package com.example.prototype;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserProfile extends AppCompatActivity {

    private static final String TAG = "UserProfile";

    TextInputLayout fullName, email, phoneNo, password;
    TextView fullNameLabel;
    EditText usernameLabel;

    //    Global Variables to hold user data inside this activity
    String _USERNAME, _NAME, _EMAIL, _PASSWORD, _PHONENO, userId;

    //  Firebase
    DatabaseReference reference;
    private FirebaseAuth.AuthStateListener mAuthListner;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_user_profile);

        Log.d(TAG, "onCreate: started.");

        setupFirebaseAuth();

        reference = FirebaseDatabase.getInstance().getReference("users");
        mAuth = FirebaseAuth.getInstance();

        fullName = findViewById(R.id.full_name_profile);
        email = findViewById(R.id.email_profile);
        phoneNo = findViewById(R.id.phone_no_profile);
        password = findViewById(R.id.password_profile);
        fullNameLabel = findViewById(R.id.fullname_field);
        usernameLabel = findViewById(R.id.username_field);

//        ShowAll Data
        showAllUserData();


    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAuthenticationState();
    }

    private void checkAuthenticationState() {
        Log.d(TAG, "checkAuthenticationState: checking authentication state.");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Log.d(TAG, "checkAuthenticationState: user is null, navigating back to login screen.");

            Intent intent = new Intent(UserProfile.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Log.d(TAG, "checkAuthenticationState: user is authenticated.");
        }
    }

    private void showAllUserData() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = mAuth.getCurrentUser().getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(userId);

        if (user != null) {
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    _NAME = dataSnapshot.child("name").getValue(String.class);
                    _USERNAME = dataSnapshot.child("username").getValue(String.class);
                    _PHONENO = dataSnapshot.child("phoneNo").getValue(String.class);
                    _EMAIL = dataSnapshot.child("email").getValue(String.class);
                    _PASSWORD = dataSnapshot.child("password").getValue(String.class);


                    fullNameLabel.setText(_NAME);
                    usernameLabel.setText(_USERNAME);
                    fullName.getEditText().setText(_NAME);
                    email.getEditText().setText(_EMAIL);
                    phoneNo.getEditText().setText(_PHONENO);
                    password.getEditText().setText(_PASSWORD);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(UserProfile.this, "error", Toast.LENGTH_SHORT).show();
                }
            });


        }

    }

    public void update(View view) {

        if (isNameChanged() || isUserNameChanged() || isPhoneNoChanged()) {
            reference.child(userId).child("username").setValue(usernameLabel.getText().toString());
            reference.child(userId).child("name").setValue(fullName.getEditText().getText().toString());
            reference.child(userId).child("phoneNo").setValue(phoneNo.getEditText().getText().toString());

            Toast.makeText(this, "Data has been Updated", Toast.LENGTH_LONG).show();
        } else Toast.makeText(this, "Data is Same or Invalid", Toast.LENGTH_LONG).show();

    }

    private boolean isUserNameChanged() {

        if (!_USERNAME.equals(usernameLabel.getText().toString())) {

            String val = usernameLabel.getText().toString();
            String noWhiteSpace = "\\A\\w{4,20}\\z";

            if (val.isEmpty()) {
                usernameLabel.setError("Field cannot be empty");
                return false;
            } else if (val.length() >= 20) {
                usernameLabel.setError("Username too long");
                return false;
            } else if (!val.matches(noWhiteSpace)) {
                usernameLabel.setError("White Spaces are not allowed");
                return false;
            } else {
                usernameLabel.setError(null);
                return true;
            }
        } else {
            return false;
        }
    }

    private boolean isNameChanged() {
        if (!_NAME.equals(fullName.getEditText().getText().toString())) {
            String val = fullName.getEditText().getText().toString();
            if (val.isEmpty()) {
                fullName.setError("Field cannot be empty");
                return false;
            }else if (val.length() >= 30) {
                usernameLabel.setError("Username too long");
                return false;
            } else {
                fullName.setError(null);
                fullName.setErrorEnabled(false);
                return true;
            }
        } else {
            return false;
        }
    }

    private boolean isPhoneNoChanged() {
        if (!_PHONENO.equals(phoneNo.getEditText().getText().toString())) {
            String val = phoneNo.getEditText().getText().toString();

            if (val.isEmpty()) {
                phoneNo.setError("Field cannot be empty");
                return false;
            }else if (val.length() != 10) {
                phoneNo.setError("Invalid PhoneNo");
                return false;
            } else {
                phoneNo.setError(null);
                phoneNo.setErrorEnabled(false);
                return true;
            }
        } else {
            return false;
        }
    }

    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), Login.class));
        finish();
    }


//    User Details

    private void getUserDetails() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            String email = user.getEmail();
            String phoneNumber = user.getPhoneNumber();
        }
    }

//            ----------------------------- Firebase setup ---------------------------------

    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: started.");

        mAuthListner = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Intent intent = new Intent(UserProfile.this, Login.class);
                    Toast.makeText(UserProfile.this, "ppppppp", Toast.LENGTH_SHORT).show();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
                // ...
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListner);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListner != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListner);
        }
    }


    public void EditEmailandPassword(View view) {
        EditUserEmailPasswordDialog dialog = new EditUserEmailPasswordDialog();
        dialog.show(getSupportFragmentManager(),"dialog_edit_user_email_password");
    }
}
