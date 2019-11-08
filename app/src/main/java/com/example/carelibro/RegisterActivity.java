package com.example.carelibro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    private EditText userEmail, userPassword, userConfirmPassword;
    private Button createAccount;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // my_child_toolbar is defined in the layout file
        Toolbar myChildToolbar =
                (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myChildToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        userEmail = findViewById(R.id.txtEmail);
        userPassword = findViewById(R.id.txtPassword);
        userConfirmPassword = findViewById(R.id.txtConfirmPass);
        createAccount = findViewById(R.id.btnCreateAccount);

        loadingDialog = new ProgressDialog(this);

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewAccount();
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null){
            sendUserToMainAcitivty();
        }
    }

    private void sendUserToMainAcitivty(){
        Intent mainActivity = new Intent(RegisterActivity.this, MainActivity.class);
        mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivity);
        finish();
    }

    private void createNewAccount() {
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();
        String confirmPass = userConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please, write your email", Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please, write your password", Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(confirmPass)) {
            Toast.makeText(this, "Please, confirm your password", Toast.LENGTH_LONG).show();
        }
        else{
            loadingDialog.setTitle("Creating new account");
            loadingDialog.setMessage("Please wait while we create your account.");
            loadingDialog.show();
            loadingDialog.setCanceledOnTouchOutside(true);
            //Check the password
            if (!password.equals(confirmPass)){
                Toast.makeText(this, "Error: Password do not match", Toast.LENGTH_LONG).show();
                loadingDialog.dismiss();
            }
            //Create account
            else{
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                                                                    if (task.isSuccessful()){
                                                                                                        loadingDialog.dismiss();
                                                                                                        sendUserToSetUpActivity();
                                                                                                    }
                                                                                                    else{
                                                                                                        String message = task.getException().getMessage();
                                                                                                        Toast.makeText(RegisterActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                                                                                                        loadingDialog.dismiss();
                                                                                                    }
                                                                                                }
                                                                                            }
                );
            }
        }
    }

    private void sendUserToSetUpActivity(){
        Intent setUpActivity = new Intent(RegisterActivity.this, SetUpActivity.class);
        startActivity(setUpActivity);
        finish();
    }
}
