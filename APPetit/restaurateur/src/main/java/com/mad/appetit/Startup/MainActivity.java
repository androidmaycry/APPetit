package com.mad.appetit.Startup;

import static com.mad.mylibrary.SharedClass.ROOT_UID;
import static com.mad.mylibrary.SharedClass.SIGNUP;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;

import com.mad.appetit.FragmentManager;
import com.mad.appetit.R;

public class MainActivity extends AppCompatActivity {

    private String email, password, errMsg = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        if(auth.getCurrentUser() == null){
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Authenticating...");

            findViewById(R.id.sign_up).setOnClickListener(e -> {
                Intent login = new Intent(this, SignUp.class);
                startActivityForResult(login, SIGNUP);
            });

            findViewById(R.id.login).setOnClickListener(h -> {
                if(checkFields()){
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this, task -> {
                                if(task.isSuccessful()) {
                                    ROOT_UID = auth.getUid();

                                    progressDialog.dismiss();

                                    Intent fragment = new Intent(this, FragmentManager.class);
                                    startActivity(fragment);
                                    finish();
                                }
                                else {
                                    //Log.w("LOGIN", "signInWithCredential:failure", task.getException());
                                    progressDialog.dismiss();
                                    Snackbar.make(findViewById(R.id.email), "Authentication Failed. Try again.", Snackbar.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Snackbar.make(findViewById(R.id.email), "Authentication Failed. Try again.", Snackbar.LENGTH_SHORT).show();
                            });
                }
                else{
                    Snackbar.make(findViewById(R.id.email), errMsg, Snackbar.LENGTH_SHORT).show();
                }
            });
        }
        else{
            ROOT_UID = auth.getUid();

            Intent fragment = new Intent(this, FragmentManager.class);
            startActivity(fragment);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data != null && resultCode == SIGNUP){
            Intent fragment = new Intent(this, FragmentManager.class);
            startActivity(fragment);
            finish();
        }
    }

    public boolean checkFields(){
        email = ((EditText)findViewById(R.id.email)).getText().toString();
        password = ((EditText)findViewById(R.id.password)).getText().toString();

        if(email.trim().length() == 0 || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            errMsg = "Invalid Mail";
            return false;
        }

        if(password.trim().length() == 0){
            errMsg = "Fill password";
            return false;
        }

        return true;
    }
}