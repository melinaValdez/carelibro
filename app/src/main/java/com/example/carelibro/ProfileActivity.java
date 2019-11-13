package com.example.carelibro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private TextView userfullName, userCity, userGender, userRelationship, userPhoneNumber, userPlaceOfStudy, userDoB;
    private CircleImageView userProfileImage;

    private DatabaseReference profileUsersReference;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mToolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Profile");

        userfullName = findViewById(R.id.tvProfileName);
        userCity = findViewById(R.id.tvProfileCity);
        userRelationship = findViewById(R.id.tvProfileRelationship);
        userPhoneNumber = findViewById(R.id.tvProfilePhone);
        userPlaceOfStudy = findViewById(R.id.tvProfilePlaceOfStudy);
        userDoB = findViewById(R.id.tvProfileDoB);
        userGender = findViewById(R.id.tvProfileGender);
        userProfileImage = findViewById(R.id.imgProfilePicture);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        profileUsersReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);

        profileUsersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String myProfileImage = dataSnapshot.child("profilePic").getValue().toString();
                String myFullName = dataSnapshot.child("fullName").getValue().toString();
                String myCity = dataSnapshot.child("city").getValue().toString();
                String myGender = dataSnapshot.child("gender").getValue().toString();
                String myRelationship = dataSnapshot.child("relationshipStatus").getValue().toString();
                String myDoB = dataSnapshot.child("dateOfBirth").getValue().toString();
                String myPhoneNumber = dataSnapshot.child("phoneNumber").getValue().toString();
                String myPlaceOfStudy = dataSnapshot.child("placeOfStudy").getValue().toString();

                Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);

                userfullName.setText("Full name: " + myFullName);
                userCity.setText("City: "+ myCity);
                userGender.setText("Gender: " + myGender);
                userRelationship.setText("Relationship status: " + myRelationship);
                userPhoneNumber.setText("Phone number: " + myPhoneNumber);
                userPlaceOfStudy.setText("Place/s of study: " + myPlaceOfStudy);
                userDoB.setText("Date of birth: " + myDoB);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
        Intent mainIntent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }
}
