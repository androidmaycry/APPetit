package com.mad.customer.UI;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.mad.customer.R;

import static com.mad.mylibrary.SharedClass.ROOT_UID;

public class MainActivity extends AppCompatActivity {
    private String email, password, errMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        if(auth.getCurrentUser()!= null){
            ROOT_UID = auth.getUid();

            Intent i = new Intent(MainActivity.this,NavApp.class);
            startActivity(i);

            finish();
        }

        findViewById(R.id.sign_up).setOnClickListener(e -> {
            Intent i = new Intent(this, SignUp.class);
            startActivityForResult(i,1);
        });

        findViewById(R.id.sign_in).setOnClickListener(e -> {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Authenticating...");
            if(checkFields()){

                progressDialog.show();

                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                ROOT_UID = auth.getUid();
                                Intent fragment = new Intent(this, NavApp.class);
                                startActivity(fragment);
                                progressDialog.dismiss();
                                finish();
                            } else {
                                Toast.makeText(MainActivity.this,"Wrong Username or Password", Toast.LENGTH_LONG).show();
                            }
                        });
            }
            else{
                Toast.makeText(MainActivity.this, errMsg, Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        });

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == 1){
            Intent fragment = new Intent(this, NavApp.class);
            startActivity(fragment);
            finish();
        }
    }
}