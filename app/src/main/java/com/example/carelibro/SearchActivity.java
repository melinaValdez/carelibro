package com.example.carelibro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ImageButton searchButton, searchPostButton;
    private EditText searchInput;
    private RecyclerView searchResultList;
    private DatabaseReference usersReference, postsReference;

    private DatabaseReference allUsersReferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mToolbar = (Toolbar) findViewById(R.id.search_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Search");

        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        postsReference = FirebaseDatabase.getInstance().getReference().child("Posts");

        allUsersReferences = FirebaseDatabase.getInstance().getReference().child("Users");
        searchPostButton = findViewById(R.id.btnSearchPosts);

        searchResultList = findViewById(R.id.searchList);
        searchResultList.setHasFixedSize(true);
        searchResultList.setLayoutManager(new LinearLayoutManager(this));

        searchButton = findViewById(R.id.btnSearchButton);
        searchInput = findViewById(R.id.txtSearchInput);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchBoxInput = searchInput.getText().toString();
                searchPeople(searchBoxInput);
            }
        });

        searchPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchBoxInput = searchInput.getText().toString();
                displayAllUsersPosts(searchBoxInput);
            }
        });


    }

    private void searchPeople(String searchBoxInput) {
        Query searchPeopleQuery = allUsersReferences.orderByChild("fullName")
        .startAt(searchBoxInput).endAt(searchBoxInput + "\uf8ff");

        FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>
                (
                        FindFriends.class,
                        R.layout.all_users_display_layout,
                        FindFriendsViewHolder.class,
                        searchPeopleQuery
                ) {
            @Override
            protected void populateViewHolder(FindFriendsViewHolder findFriendsViewHolder, FindFriends model,final int i) {
                findFriendsViewHolder.setFullName(model.getFullName());
                findFriendsViewHolder.setCity(model.getCity());
                findFriendsViewHolder.setProfilePic(model.getProfilePic());
                findFriendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_id = getRef(i).getKey();
                        Intent profileIntent = new Intent(SearchActivity.this,PersonProfileActivity.class);
                        profileIntent.putExtra("visitUserId",visit_user_id);
                        startActivity(profileIntent);
                    }
                });
            }
        };
        searchResultList.setAdapter(firebaseRecyclerAdapter);

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
        Intent mainIntent = new Intent(SearchActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }


    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mView = itemView;
        }

        public void setProfilePic(String profilePic) {
            CircleImageView myImage = mView.findViewById(R.id.allUsersProfilePic);
            Picasso.get().load(profilePic).placeholder(R.drawable.profile).into(myImage);
        }

        public void setFullName(String fullName) {
            TextView myName = mView.findViewById(R.id.txtAllUsersProfileName);
            myName.setText(fullName);
        }

        public void setCity(String city) {
            TextView myCity = mView.findViewById(R.id.txtAllUsersCity);
            myCity.setText(city);
        }
    }

    private void displayAllUsersPosts(String filterInput){
        Query orderedPostsReference = postsReference.orderByChild("description")
                .startAt(filterInput).endAt(filterInput + "\uf8ff");
        FirebaseRecyclerAdapter<Posts, MainActivity.PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, MainActivity.PostsViewHolder>
                        (
                                Posts.class,
                                R.layout.all_posts_layout,
                                MainActivity.PostsViewHolder.class,
                                orderedPostsReference
                        ) {
                    @Override
                    protected void populateViewHolder(final MainActivity.PostsViewHolder postsViewHolder, Posts model, final int position) {
                        final String postKey = getRef(position).getKey();

                        postsViewHolder.setFullName(model.getFullName());
                        postsViewHolder.setTime(model.getTime());
                        postsViewHolder.setDate(model.getDate());
                        postsViewHolder.setDescription(model.getDescription());
                        postsViewHolder.setProfilePic(model.getProfileimage());
                        if (model.getPostimage() != null){
                            postsViewHolder.setPostImage(model.getPostimage());
                        }
                        else{
                            postsViewHolder.setNoPostImage();
                        }

                        postsViewHolder.setLikeButtonStatus(postKey);

                        postsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent clickPostIntent = new Intent(SearchActivity.this, ClickPostActivity.class);
                                clickPostIntent.putExtra("postKey", postKey);
                                startActivity(clickPostIntent);
                            }
                        });


                        postsViewHolder.username.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                getRef(position).child("uid").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()){
                                            String visit_id = dataSnapshot.getValue().toString();
                                            Intent profileIntent = new Intent(SearchActivity.this,PersonProfileActivity.class);
                                            profileIntent.putExtra("visitUserId",visit_id);
                                            startActivity(profileIntent);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        });
                    }
                };
        searchResultList.setAdapter(firebaseRecyclerAdapter);
    }



    public static class PostsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        ImageButton likePostButton, commentPostButton;
        TextView username;
        int likesCount;
        String currentUserId;
        DatabaseReference likesReference;


        public PostsViewHolder (View itemView){
            super(itemView);
            mView = itemView;

            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }


        public void setFullName(final String fullName){
            username = (TextView) mView.findViewById(R.id.txtPostUserName);
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

        public void setNoPostImage(){
            ImageView postPic = (ImageView) mView.findViewById(R.id.imgPostImage);
            postPic.setVisibility(postPic.INVISIBLE);
        }
    }
}
