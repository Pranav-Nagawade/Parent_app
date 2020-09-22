package com.example.prototype;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;

public class EditUserEmailPasswordDialog extends DialogFragment {

    private static final String TAG = "EditUserEmailPassword";

    private Button mSave;
    private TextView mResetPasswordLink;
    private Context mContext;
    private TextInputEditText mConfirmEmail, mConfirmPassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_edit_user_email_password, container, false);
        Log.d(TAG, "onCreate: started.");
        mConfirmEmail = (TextInputEditText) view.findViewById(R.id.input_email);
        mConfirmPassword = (TextInputEditText) view.findViewById(R.id.input_password);
        mSave = (Button) view.findViewById(R.id.btn_save);
        mResetPasswordLink = (TextView) view.findViewById(R.id.change_password);
        mContext = getActivity();



        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting to save settings.");

                //make sure email and current password fields are filled
                if (!isEmpty(mConfirmEmail.getText().toString())
                        && !isEmpty(mConfirmPassword.getText().toString())) {

                    /*
                    ------ Change Email Task -----
                     */
                    //if the current email doesn't equal what's in the EditText field then attempt
                    //to edit
                    if (!FirebaseAuth.getInstance().getCurrentUser().getEmail()
                            .equals(mConfirmEmail.getText().toString())) {

                           editUserEmail();

                    } else {
                        Toast.makeText(mContext, "no changes were made", Toast.LENGTH_SHORT).show();
                    }


                } else {
                    Toast.makeText(mContext, "Email and Current Password Fields Must be Filled to Save", Toast.LENGTH_SHORT).show();
                }
            }
        });


        mResetPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: sending password reset link");

                /*
                ------ Reset Password Link -----
                */
                sendResetPasswordLink();
            }
        });

        return view;
    }


    private void sendResetPasswordLink() {
        FirebaseAuth.getInstance().sendPasswordResetEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: Password Reset Email sent.");
                            Toast.makeText(mContext, "Sent Password Reset Link to Email",
                                    Toast.LENGTH_SHORT).show();
                            FirebaseAuth.getInstance().signOut();
                        } else {
                            Log.d(TAG, "onComplete: No user associated with that email.");

                            Toast.makeText(mContext, "No User Associated with that Email.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    private void editUserEmail() {
        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers,
        // such as GoogleAuthProvider or FacebookAuthProvider.


        AuthCredential credential = EmailAuthProvider
                .getCredential(FirebaseAuth.getInstance().getCurrentUser().getEmail(), mConfirmPassword.getText().toString());
        Log.d(TAG, "editUserEmail: reauthenticating with:  \n email " + FirebaseAuth.getInstance().getCurrentUser().getEmail()
                + " \n passowrd: " + mConfirmPassword.getText().toString());


        FirebaseAuth.getInstance().getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: reauthenticate success.");

                            ///////////////////now check to see if the email is not already present in the database
                            FirebaseAuth.getInstance().fetchSignInMethodsForEmail(mConfirmEmail.getText().toString()).addOnCompleteListener(
                                    new OnCompleteListener<SignInMethodQueryResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {

                                            if (task.isSuccessful()) {
                                                ///////// getProviders().size() will return size 1 if email ID is in use.

                                                Log.d(TAG, "onComplete: RESULT: " + task.getResult().getSignInMethods().size());
                                                if (task.getResult().getSignInMethods().size() == 1) {
                                                    Log.d(TAG, "onComplete: That email is already in use.");

                                                    Toast.makeText(mContext, "That email is already in use", Toast.LENGTH_SHORT).show();

                                                } else {
                                                    Log.d(TAG, "onComplete: That email is available.");

                                                    /////////////////////add new email
                                                    FirebaseAuth.getInstance().getCurrentUser().updateEmail(mConfirmEmail.getText().toString())
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        Log.d(TAG, "onComplete: User email address updated.");
                                                                        Toast.makeText(mContext, "Updated email", Toast.LENGTH_SHORT).show();
                                                                        sendVerificationEmail();
                                                                        FirebaseAuth.getInstance().signOut();
                                                                    } else {
                                                                        Log.d(TAG, "onComplete: Could not update email.");
                                                                        Toast.makeText(mContext, "unable to update email", Toast.LENGTH_SHORT).show();
                                                                    }

                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(mContext, "unable to update email", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });


                                                }

                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(mContext, "unable to update email", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Log.d(TAG, "onComplete: Incorrect Password");
                            Toast.makeText(mContext, "Incorrect Password", Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(mContext, "“unable to update email”", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(mContext, "Sent Verification Email", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(mContext, "Couldn't Verification Send Email", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }


    private boolean isEmpty(String string) {
        return string.equals("");
    }

}
