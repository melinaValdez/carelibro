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
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ImageButton searchButton;
    private EditText searchInput;
    private RecyclerView searchResultList;

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

        allUsersReferences = FirebaseDatabase.getInstance().getReference().child("Users");

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
    }

    private void searchPeople(String searchBoxInput) {
        Toast.makeText(this, "Searching...", Toast.LENGTH_LONG).show();
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
            protected void populateViewHolder(FindFriendsViewHolder findFriendsViewHolder, FindFriends model, int i) {
                findFriendsViewHolder.setFullName(model.getFullName());
                findFriendsViewHolder.setCity(model.getCity());
                findFriendsViewHolder.setProfilePic(model.getProfilePic());
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
}
