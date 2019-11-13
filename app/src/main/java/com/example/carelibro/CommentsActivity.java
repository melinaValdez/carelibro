package com.example.carelibro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CommentsActivity extends AppCompatActivity {
    private ImageButton postCommentButton;
    private EditText commentImputText;
    private RecyclerView commentsList;
    private Toolbar mToolbar;

    private String postKey, currentUserId;

    private DatabaseReference usersReference, postReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        postKey = getIntent().getExtras().get("postKey").toString();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        postReference = FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey).child("Comments");

        mToolbar = (Toolbar) findViewById(R.id.updateComments_toolBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Comments");

        commentsList = findViewById(R.id.commentsList);
        commentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        commentsList.setLayoutManager(linearLayoutManager);

        commentImputText = findViewById(R.id.txtCommentInput);
        postCommentButton = findViewById(R.id.btnPostComment);

        postCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usersReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String userName = dataSnapshot.child("fullName").getValue().toString();
                            validateComment(userName);

                            commentImputText.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

    }

    private void validateComment(String userName) {
        String commentText = commentImputText.getText().toString();

        if (TextUtils.isEmpty(commentText)){
            Toast.makeText(CommentsActivity.this, "Please write your comment", Toast.LENGTH_LONG).show();
        }
        else{
            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMM-yy");
            final String saveCurrentDate = currentDate.format(calFordDate.getTime());

            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            final String saveCurrentTime = currentTime.format(calFordDate.getTime());

            final String randomKey = currentUserId + saveCurrentTime;

            HashMap commentsMap = new HashMap();
            commentsMap.put("uid", currentUserId);
            commentsMap.put("comment", commentText);
            commentsMap.put("date", saveCurrentDate);
            commentsMap.put("time", saveCurrentTime);
            commentsMap.put("fullNamel", userName);
            postReference.child(randomKey).updateChildren(commentsMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        Toast.makeText(CommentsActivity.this, "Comment updated succesfully", Toast.LENGTH_LONG).show();
                    }
                    else{
                        Toast.makeText(CommentsActivity.this, "Error ocurred. Please, try again.", Toast.LENGTH_LONG).show();
                    }
                }
            });

        }
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
        Intent mainIntent = new Intent(CommentsActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }
}
