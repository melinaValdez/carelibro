package com.example.carelibro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ClickPostActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ImageView postImage;
    private TextView postDescription;
    private Button editButton, deleteButton;

    private DatabaseReference clickPostReference;
    private FirebaseAuth mAuth;

    WebView videoWeb;

    private String postKey, currentUserId, databaseUserId, description, image, videoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        mToolbar = (Toolbar) findViewById(R.id.clickPost_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Post details");

        postImage = findViewById(R.id.imgPostPic);
        postDescription = findViewById(R.id.tvPostDescription);
        editButton = findViewById(R.id.btnEditPost);
        editButton.setVisibility(View.INVISIBLE);
        deleteButton = findViewById(R.id.btnDeletePost);
        deleteButton.setVisibility(View.INVISIBLE);



        videoWeb = findViewById(R.id.youtube_video);
        videoWeb.setWebChromeClient(new WebChromeClient());

        WebSettings ws = videoWeb.getSettings();
        ws.setJavaScriptEnabled(true);

        postKey = getIntent().getExtras().get("postKey").toString();

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        clickPostReference = FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey);
        clickPostReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    description = dataSnapshot.child("description").getValue().toString();
                    if (dataSnapshot.child("postimage").exists()){
                        image = dataSnapshot.child("postimage").getValue().toString();
                        Picasso.get().load(image).into(postImage);
                    }
                    else{
                        postImage.setVisibility(postImage.INVISIBLE);
                    }
                    databaseUserId = dataSnapshot.child("uid").getValue().toString();
                    if (dataSnapshot.child("videoUrl").exists()) {
                        videoUrl = dataSnapshot.child("videoUrl").getValue().toString();
                        videoWeb.loadUrl(videoUrl);
                    }
                    else {
                        videoWeb.setVisibility(postImage.INVISIBLE);
                    }
                    postDescription.setText(description);

                    if (currentUserId.equals(databaseUserId)){
                        deleteButton.setVisibility(View.VISIBLE);
                        editButton.setVisibility(View.VISIBLE);
                    }

                    editButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            editCurrentPost(description);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteCurrentPost();
            }
        });
    }


    @Override
    public void onBackPressed() {
        if(videoWeb.canGoBack()){
            videoWeb.goBack();
        }else{
            super.onBackPressed();
        }
    }

    private void editCurrentPost(String description) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit post");

        final EditText inputField = new EditText(ClickPostActivity.this);
        inputField.setText(description);
        builder.setView(inputField);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                clickPostReference.child("description").setValue(inputField.getText().toString());
                Toast.makeText(ClickPostActivity.this, "Post updated succesfully", Toast.LENGTH_LONG);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        Dialog dialog = builder.create();
        dialog.show();
    }

    private void deleteCurrentPost() {
        clickPostReference.removeValue();
        SendUserToMainActivity();
        Toast.makeText(this, "Post has been deleted", Toast.LENGTH_LONG);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if(id == android.R.id.home)
        {
            SendUserToMainActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(ClickPostActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }


}
