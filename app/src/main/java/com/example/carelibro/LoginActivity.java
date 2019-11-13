package com.example.carelibro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.w3c.dom.Text;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private Button loginButton;
    private EditText userEmail, userPassword;
    private TextView newAccountLink, resetPassword;

    private SignInButton googleSignInButton;

    private FirebaseAuth mAuth;


    private static final String TAG = LoginActivity.class.getSimpleName();


    private GoogleApiClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        newAccountLink = findViewById(R.id.lblCreateAccount);
        userEmail = findViewById(R.id.txtEmail);
        userPassword = findViewById(R.id.txtPassword);
        loginButton = findViewById(R.id.btnLogin);
        resetPassword = findViewById(R.id.txtForgetPassword);

        googleSignInButton = findViewById(R.id.googleSignInButton);
        googleSignInButton.setSize(SignInButton.SIZE_STANDARD);

        newAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToRegisterActivity();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent forgotPassword = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(forgotPassword);
            }
        });

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                        Toast.makeText(LoginActivity.this, "Connection Google in failed...", Toast.LENGTH_SHORT).show();
                    }
                }).addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private void signIn() {

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()){
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Can´t get Auth Result", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            sendUserToMainAcitivty();

                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            String msj = task.getException().toString();
                            sendUserToLogInAcitivty();
                            Toast.makeText(LoginActivity.this, "Not Authenticaded: "+msj, Toast.LENGTH_SHORT).show();

                        }

                        // ...
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

    private void sendUserToRegisterActivity() {
        Intent registerActivity = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerActivity);
    }

    private void sendUserToMainAcitivty(){
        Intent mainActivity = new Intent(LoginActivity.this, MainActivity.class);
        mainActivity.putExtra("GoogleImgPerfil",mAuth.getCurrentUser().getPhotoUrl());
        mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivity);
        finish();
    }

    private void sendUserToLogInAcitivty(){
        Intent mainActivity = new Intent(LoginActivity.this, LoginActivity.class);
        mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivity);
        finish();
    }

    private void login(){
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please, write your email", Toast.LENGTH_LONG).show();
        }

        else if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please, write your password", Toast.LENGTH_LONG).show();
        }

        else{
            mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        sendUserToMainAcitivty();
                    }
                    else{
                        String message = Objects.requireNonNull(task.getException()).getMessage();
                        Toast.makeText(LoginActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}