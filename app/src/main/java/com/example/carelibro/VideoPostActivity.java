package com.example.carelibro;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

public class VideoPostActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ProgressDialog loadingBar;

    private Button UpdatePostButton;
    private EditText PostDescription;
    private EditText linkYT;

    private static final int Gallery_Pick = 1;
    private String Description;
    private String linkVideoYT;

    private DatabaseReference UsersRef, PostsRef;
    private FirebaseAuth mAuth;

    private String saveCurrentDate, saveCurrentTime, postRandomName, downloadUrl, current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        UpdatePostButton = (Button) findViewById(R.id.btnPostYT);
        PostDescription =(EditText) findViewById(R.id.txtDescriptionYT);
        linkYT = (EditText) findViewById(R.id.txtLinkYT);
        loadingBar = new ProgressDialog(this);


        mToolbar = (Toolbar) findViewById(R.id.updatePostPage_toolBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update post");




        UpdatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                ValidatePostInfo();
            }
        });
    }

    private void ValidatePostInfo()
    {
        Description = PostDescription.getText().toString();
        linkVideoYT = linkYT.getText().toString();
        loadingBar.setTitle("Update New Post");
        loadingBar.setMessage("Please, wait while we update your new post");
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(true);

        if (linkVideoYT.isEmpty()) {
            Toast.makeText(this, "Link is not exist", Toast.LENGTH_SHORT).show();
        }
        else{
            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMM-yy");
            saveCurrentDate = currentDate.format(calFordDate.getTime());

            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            saveCurrentTime = currentTime.format(calFordDate.getTime());

            postRandomName = current_user_id + saveCurrentTime;

            SavingPostInformationToDatabase();
        }
    }





    private void SavingPostInformationToDatabase()
    {
        UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    String userFullName = dataSnapshot.child("fullName").getValue().toString();
                    String userProfileImage = dataSnapshot.child("profilePic").getValue().toString();

                    HashMap postsMap = new HashMap();
                    postsMap.put("uid", current_user_id);
                    postsMap.put("date", saveCurrentDate);
                    postsMap.put("time", saveCurrentTime);
                    postsMap.put("description", Description);

                    postsMap.put("profileimage", userProfileImage);
                    postsMap.put("fullName", userFullName);

                    //dividir link de youtube
                    String llave = "";
                    String[] separated = linkVideoYT.split("/");

                    if(separated[2].equals("youtu.be")){
                        llave = separated[3];
                    }else if(separated[2].equals("www.youtube.com")){
                        String[] separateLlave = separated[3].split("=");
                        llave = separateLlave[1];
                    }
                    postsMap.put("videoUrl","https://www.youtube.com/embed/"+llave);

                    PostsRef.child(current_user_id + postRandomName).updateChildren(postsMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task)
                                {
                                    if(task.isSuccessful())
                                    {
                                        SendUserToMainActivity();
                                        Toast.makeText(VideoPostActivity.this, "New post updated successfully", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                    else
                                    {
                                        Toast.makeText(VideoPostActivity.this, "Error occured while updating your post", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

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
        Intent mainIntent = new Intent(VideoPostActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }
}
