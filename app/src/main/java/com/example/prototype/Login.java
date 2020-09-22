package com.example.prototype;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import HelperClasses.HomeAdapter.UserHelperClass;

public class Login extends AppCompatActivity {

    Button callSignUp, login_btn;
    ImageView image;
    TextView logoText, sloganText;
    TextInputLayout password, email;
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    String userId;

    private static final String TAG = "Login";
    private FirebaseAuth.AuthStateListener mAuthListner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        //Hooks
        callSignUp = findViewById(R.id.signup_screen);
        image = findViewById(R.id.logo_image);
        logoText = findViewById(R.id.logo_name);
        sloganText = findViewById(R.id.slogan_name);
        password = findViewById(R.id.password);
        email = findViewById(R.id.email);
        login_btn = findViewById(R.id.login_btn);
        progressBar = findViewById(R.id.progress_bar);

        mAuth = FirebaseAuth.getInstance();

        setupFirebaseAuth();

    }


    private Boolean validatePassword() {
        String val = password.getEditText().getText().toString();
        if (val.isEmpty()) {
            password.setError("Field cannot be empty");
            return false;
        } else {
            password.setError(null);
            password.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validateEmail() {
        String val = email.getEditText().getText().toString();
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if (val.isEmpty()) {
            email.setError("Field cannot be empty");
            return false;
        } else if (!val.matches(emailPattern)) {
            email.setError("Invalid email address");
            return false;
        } else {
            email.setError(null);
            email.setErrorEnabled(false);
            return true;
        }
    }


    public void loginUser(View view) {
        //Validate Login Info 
        if (!validatePassword() | !validateEmail()) {
            return;
        } else {
            isUser();
        }
    }


    private void isUser() {
        String mail = email.getEditText().getText().toString();
        final String spassword = password.getEditText().getText().toString();
        UserHelperClass helperClass = new UserHelperClass(mail, spassword);


        mAuth.signInWithEmailAndPassword(mail, spassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    userId = mAuth.getCurrentUser().getUid();
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
                    reference.child(userId).child("password").setValue(spassword);
                } else {
                    Toast.makeText(Login.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    public void callSignUpScreen(View view) {
        Intent intent = new Intent(Login.this, SignUp.class);
        Pair[] pairs = new Pair[7];
        pairs[0] = new Pair<View, String>(image, "logo_image");
        pairs[1] = new Pair<View, String>(logoText, "logo_text");
        pairs[2] = new Pair<View, String>(sloganText, "logo_desc");
        pairs[3] = new Pair<View, String>(password, "password_tran");
        pairs[4] = new Pair<View, String>(email, "email_trans");
        pairs[5] = new Pair<View, String>(login_btn, "button_tran");
        pairs[6] = new Pair<View, String>(callSignUp, "login_signup_tran");

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(Login.this, pairs);
        startActivity(intent, options.toBundle());
    }


    //    Firebase Setup
    private void setupFirebaseAuth() {
        mAuthListner = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    if (user.isEmailVerified()) {
                        Log.d(TAG, "onAuthStateChanged: siged_in: " + user.getUid());
                        Toast.makeText(Login.this, "Authenticated with: " + user.getEmail(), Toast.LENGTH_SHORT).show();
                        userId = mAuth.getCurrentUser().getUid();
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
                        String Email = user.getEmail();
                        reference.child(userId).child("email").setValue(Email);

                        Intent intent = new Intent(Login.this, UserDashboard.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(Login.this, "Check Your Email Inbox for Verification Link", Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();
                    }
                } else {
                    Log.d(TAG, "onAuthStateChanged: siged_out: ");
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListner);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListner != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListner);
        }
    }

    public void ForgotPassword(View view) {
        PasswordResetDialog dialog = new PasswordResetDialog();
        dialog.show(getSupportFragmentManager(), "dialog_password_reset");
    }

    public void ResendEmailVerification(View view) {
        ResendVerificationDialog dialog = new ResendVerificationDialog();
        dialog.show(getSupportFragmentManager(), "dialog_resend_email_verification");
    }
}
