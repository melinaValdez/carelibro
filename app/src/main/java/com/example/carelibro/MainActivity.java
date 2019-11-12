package com.example.carelibro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView postList;
    private Toolbar mToolBar;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private CircleImageView navProfileImage;
    private TextView navProfileUsername;

    private FirebaseAuth mAuth;
    private DatabaseReference usersReference, postsReference;
    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        postsReference = FirebaseDatabase.getInstance().getReference().child("Posts");
        currentUserId = mAuth.getCurrentUser().getUid();

        mToolBar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Home");

        drawerLayout = findViewById(R.id.drawableLayout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView = findViewById(R.id.navigationView);

        postList = (RecyclerView) findViewById(R.id.users_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        navProfileImage = (CircleImageView) navView.findViewById(R.id.profile_image);
        navProfileUsername = (TextView) navView.findViewById(R.id.txtUsername);

        usersReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("fullName"))
                    {
                        String fullname = dataSnapshot.child("fullName").getValue().toString();
                        navProfileUsername.setText(fullname);
                    }
                    if(dataSnapshot.hasChild("profilePic"))
                    {
                        String image = dataSnapshot.child("profilePic").getValue().toString();
                        Picasso.get().load(image).into(navProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                UserMenuSelector(menuItem);
                return false;
            }
        });

        displayAllUsersPosts();
    }

    @Override
    protected void onStart(){
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null){
            sendUserToLogin();
        }
        else{
            checkUserExistence();
        }
    }

    private void checkUserExistence(){
        final String currentUserId = mAuth.getCurrentUser().getUid();
        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(currentUserId)){
                    sendUserToSetupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendUserToSetupActivity(){
        Intent setUpActivity = new Intent(MainActivity.this, SetUpActivity.class);
        setUpActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setUpActivity);
        finish();
    }

    private void sendUserToLogin(){
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelector(MenuItem item){
        switch (item.getItemId()){
            case R.id.nav_profile:
                Toast.makeText(this, "Profile selected", Toast.LENGTH_LONG).show();
                break;

            case R.id.nav_friends:
                Toast.makeText(this, "Friends selected", Toast.LENGTH_LONG).show();
                break;

            case R.id.nav_home:
                Toast.makeText(this, "Home selected", Toast.LENGTH_LONG).show();
                break;

            case R.id.nav_find_friends:
                Toast.makeText(this, "Find friends selected", Toast.LENGTH_LONG).show();
                break;

            case R.id.nav_post:
                sendUserToNewPostActivity();
                break;

            case R.id.nav_settings:
                Toast.makeText(this, "Settings selected", Toast.LENGTH_LONG).show();
                break;

            case R.id.nav_logout:
                mAuth.signOut();
                sendUserToLogin();
                break;
        }

    }

    private void sendUserToNewPostActivity(){
        Intent addPostActivity = new Intent(MainActivity.this, PostActivity.class);
        startActivity(addPostActivity);
    }

    private void displayAllUsersPosts(){
        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, PostsViewHolder>
                        (
                                Posts.class,
                                R.layout.all_posts_layout,
                                PostsViewHolder.class,
                                postsReference
                        ) {
                    @Override
                    protected void populateViewHolder(PostsViewHolder postsViewHolder, Posts model, int position) {
                        final String postKey = getRef(position).getKey();

                        postsViewHolder.setFullName(model.getFullname());
                        postsViewHolder.setTime(model.getTime());
                        postsViewHolder.setDate(model.getDate());
                        postsViewHolder.setDescription(model.getDescription());
                        postsViewHolder.setProfilePic(model.getProfileimage());
                        postsViewHolder.setPostImage(model.getPostimage());

                        postsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent clickPostIntent = new Intent(MainActivity.this, ClickPostActivity.class);
                                clickPostIntent.putExtra("postKey", postKey);
                                startActivity(clickPostIntent);
                            }
                        });

                    }
                };
        postList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public PostsViewHolder (View itemView){
            super(itemView);
            mView = itemView;
        }

        public void setFullName(String fullName){
            TextView username = (TextView) mView.findViewById(R.id.txtPostUserName);
            username.setText(fullName);
        }

        public void setProfilePic(String profilePic){
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.postProfilePic);
            Picasso.get().load(profilePic).placeholder(R.drawable.profile).into(image);
        }

        public void setTime(String time){
            TextView postTime = (TextView) mView.findViewById(R.id.txtPostTime);
            postTime.setText("  " + time);
        }

        public void setDate(String date){
            TextView postDate = (TextView) mView.findViewById(R.id.txtPostDate);
            postDate.setText("               " + date);
        }

        public void setDescription(String description){
            TextView postDescription = (TextView) mView.findViewById(R.id.txtPostDescription);
            postDescription.setText(description);
        }

        public void setPostImage(String postImage){
            ImageView postPic = (ImageView) mView.findViewById(R.id.imgPostImage);
            Picasso.get().load(postImage).placeholder(R.drawable.add_post).into(postPic);
        }
    }
}
