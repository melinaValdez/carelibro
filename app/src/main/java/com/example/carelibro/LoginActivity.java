package com.example.carelibro;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;


import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.iid.FirebaseInstanceId;
import com.hannesdorfmann.mosby3.mvp.MvpBasePresenter;


import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements  GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = LoginActivity.class.getSimpleName();
    public static final int LOGIN_REQUEST_CODE = 10001;



    private Button loginButton;
    private EditText userEmail, userPassword;
    private TextView newAccountLink;
    private RelativeLayout googleSignInButton;

    private FirebaseAuth mAuth;

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
        googleSignInButton = findViewById(R.id.googleSignInButton);

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
        mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivity);
        finish();
    }

    private void sendUserToLogInAcitivty() {
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

/*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        newAccountLink = findViewById(R.id.lblCreateAccount);
        userEmail = findViewById(R.id.txtEmail);
        userPassword = findViewById(R.id.txtPassword);
        loginButton = findViewById(R.id.btnLogin);

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


        initGoogleSignIn();
        initFirebaseAuth();
    }

    private void initGoogleSignIn() {
        mGoogleApiClient = createGoogleApiClient(this);
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.googleSignInButton).setOnClickListener(view -> onGoogleSignInClick());
    }

    public void onGoogleSignInClick() {
        if (checkInternetConnection(null)) {
            signInWithGoogle();
        }
    }

    public boolean hasInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public boolean checkInternetConnection(@Nullable View anchorView) {
        boolean hasInternetConnection = hasInternetConnection();
        if (!hasInternetConnection) {
            if (anchorView != null) {
                Toast.makeText(this, R.string.internet_connection_failed, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, (R.string.internet_connection_failed), Toast.LENGTH_SHORT).show();
            }
        }

        return hasInternetConnection;
    }

    public static GoogleApiClient createGoogleApiClient(FragmentActivity fragmentActivity) {
        GoogleApiClient.OnConnectionFailedListener failedListener;

        if (fragmentActivity instanceof GoogleApiClient.OnConnectionFailedListener) {
            failedListener = (GoogleApiClient.OnConnectionFailedListener) fragmentActivity;
        } else {
            throw new IllegalArgumentException(fragmentActivity.getClass().getSimpleName() + " should implement OnConnectionFailedListener");
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(fragmentActivity.getResources().getString(R.string.google_web_client_id))
                .requestEmail()
                .build();

        return new GoogleApiClient.Builder(fragmentActivity)
                .enableAutoManage(fragmentActivity, failedListener)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void initFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance();

        //if (mAuth.getCurrentUser() != null) {
        //    signOut(mGoogleApiClient, this);
        //}

        mAuthListener = firebaseAuth -> {
            final FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // Profile is signed in
                Toast.makeText(this, ("onAuthStateChanged:signed_in:" + user.getUid()), Toast.LENGTH_SHORT).show();

                checkIsProfileExist(user.getUid());
                setResult(RESULT_OK);
            } else {
                // Profile is signed out
                Toast.makeText(this, "onAuthStateChanged:signed_out", Toast.LENGTH_SHORT).show();
            }
        };
    }

    public boolean checkIsProfileExist(final String userId) {

        Log.d("EXISTE!!!!!!!!!","EN SERIO!!!!!!!!!!!!!!!!!");
        Profile.getInstance(context).isProfileExist(userId, exist -> {
                if (!exist) {
                    sendUserToSetupAcitivty();

                    Log.d("EXISTE!!!!!!!!!","EN SERIO!!!!!!!!!!!!!!!!!");
                } else {
                    Log.d("EXISTE!!!!!!!!!","EN SERIO!!!!!!!!!!!!!!!!!");
                    PreferencesUtil.setProfileCreated(context, true);
                    Profile.getInstance(context.getApplicationContext())
                            .addRegistrationToken(FirebaseInstanceId.getInstance().getToken(), userId);
                }
        });
        return true;
    }

    @Override
    protected void onStart(){
        super.onStart();


        mAuth.addAuthStateListener(mAuthListener);
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.stopAutoManage(this);
            mGoogleApiClient.disconnect();
        }
    }

    private String buildGooglePhotoUrl(Uri photoUrl) {
        return String.format(String.valueOf(R.string.google_large_image_url_pattern), photoUrl, MAX_AVATAR_SIZE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == SIGN_IN_GOOGLE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        }
    }

    public void handleGoogleSignInResult(GoogleSignInResult result) {

        if(result.isSuccess()){

            GoogleSignInAccount account = result.getSignInAccount();
            profilePhotoUrlLarge = account.getPhotoUrl().toString();
            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            firebaseAuthWithCredentials(credential);
            Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());

        }else{
            Log.d("TAG","SIGN_IN_GOOGLE failed :" + result);
        }

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

    public void signInWithGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, SIGN_IN_GOOGLE);
        try {
            if (checkIsProfileExist(mAuth.getCurrentUser().getUid())) {

                sendUserToMainAcitivty();
            } else {
                sendUserToSetupAcitivty();

            }
        }catch (Exception e){
            Log.d("Error aiuda",e.getMessage());
        }

    }

    public void setProfilePhotoUrl(String url) {
            profilePhotoUrlLarge = url;

    }

    public void handleAuthError(Task<AuthResult> task) {
        Exception exception = task.getException();
        Toast.makeText(this, "signInWithCredential", Toast.LENGTH_SHORT).show();

        if(exception != null){
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, R.string.error_authentication, Toast.LENGTH_SHORT).show();
        }
    }

    public void firebaseAuthWithCredentials(AuthCredential credential) {

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    Toast.makeText(this, "signInWithCredential:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();


                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful()) {
                        handleAuthError(task);
                    }
                });

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Toast.makeText(this,"onConnectionFailed:" + connectionResult , Toast.LENGTH_SHORT).show();
        Toast.makeText(this, R.string.error_google_play_services, Toast.LENGTH_SHORT).show();
    }

    private void sendUserToRegisterActivity() {
        Intent registerActivity = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerActivity);
    }

    private void sendUserToMainAcitivty(){
        Intent mainActivity = new Intent(LoginActivity.this, MainActivity.class);
        mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivity);
        finish();
    }

    private void sendUserToSetupAcitivty(){
        Intent SetUpActivity = new Intent(LoginActivity.this, SetUpActivity.class);
        SetUpActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        SetUpActivity.putExtra("CreateProfileActivity.LARGE_IMAGE_URL_EXTRA_KEY", profilePhotoUrlLarge);
        startActivity(SetUpActivity);
        finish();
    }

 */

}