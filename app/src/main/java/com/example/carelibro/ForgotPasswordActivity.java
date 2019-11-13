package com.example.carelibro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    private Button btnSendEmail;
    private EditText txtEmailInput;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // my_child_toolbar is defined in the layout file
        Toolbar myChildToolbar =
                (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myChildToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("Forgot password");

        mAuth = FirebaseAuth.getInstance();

        btnSendEmail = (Button) findViewById(R.id.btnReset);
        txtEmailInput = (EditText) findViewById(R.id.txtEmail);

        btnSendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userEmail = txtEmailInput.getText().toString();

                if (TextUtils.isEmpty(userEmail)){
                    Toast.makeText(ForgotPasswordActivity.this, "Please, write your email", Toast.LENGTH_LONG).show();
                }
                else{
                    mAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(ForgotPasswordActivity.this, "Check your email to restore your password", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                            }
                            else{
                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(ForgotPasswordActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();

                            }
                        }
                    });
                }
            }
        });
    }
}
