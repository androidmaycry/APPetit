package com.mad.riders.ProfileManagment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mad.riders.R;

public class EditPassword extends AppCompatActivity {
    private String oldPsw, newPsw, confirmPsw, errMsg = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_password);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Updating password...");

        findViewById(R.id.text_psw_alert).setVisibility(View.INVISIBLE);
        findViewById(R.id.error_psw).setVisibility(View.INVISIBLE);

        findViewById(R.id.button).setOnClickListener(e -> {
            if(checkFields()){
                findViewById(R.id.text_psw_alert).setVisibility(View.INVISIBLE);
                findViewById(R.id.error_psw).setVisibility(View.INVISIBLE);

                progressDialog.show();

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String email = user.getEmail();

                AuthCredential credential = EmailAuthProvider.getCredential(email, oldPsw);
                user.reauthenticate(credential).addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        user.updatePassword(newPsw).addOnCompleteListener(task1 -> {
                            if(task1.isSuccessful()){
                                progressDialog.hide();
                                Toast.makeText(EditPassword.this, "Password successfully modified", Toast.LENGTH_LONG).show();
                                finish();
                            }
                            else{
                                progressDialog.hide();
                                Toast.makeText(EditPassword.this, "Something went wrong. Please try again later", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    else{
                        progressDialog.hide();
                        Toast.makeText(EditPassword.this, "Authentication Failed. Wrong password.", Toast.LENGTH_LONG).show();
                    }
                });
            }
            else
                Toast.makeText(EditPassword.this, errMsg, Toast.LENGTH_LONG).show();
        });
    }

    private boolean checkFields(){
        oldPsw = ((EditText)findViewById(R.id.old_psw)).getText().toString();
        newPsw = ((EditText)findViewById(R.id.new_password)).getText().toString();
        confirmPsw = ((EditText)findViewById(R.id.confirm_new_password)).getText().toString();

        if(oldPsw.trim().length() == 0){
            errMsg = "Please insert the old password";
            return false;
        }

        if(newPsw.trim().length() < 6){
            errMsg = "Password should be at least 6 characters";
            return false;
        }

        if(newPsw.trim().length() != confirmPsw.trim().length()){
            errMsg = "Passwords must be equal";
            findViewById(R.id.text_psw_alert).setVisibility(View.VISIBLE);
            findViewById(R.id.error_psw).setVisibility(View.VISIBLE);
            return false;
        }

        return true;
    }
}
