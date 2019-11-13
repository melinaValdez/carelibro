package com.example.carelibro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {


    private Toolbar mToolbar;
    private TextView userfullName, userCity, userGender, userRelationship, userPhoneNumber, userPlaceOfStudy, userDoB;
    private CircleImageView userProfileImage;

    private Button sendFriendRequest, declaineFriendRequest;


    private DatabaseReference profileUsersReference,userRef;
    private FirebaseAuth mAuth;
    private String senderUserId,receiverUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mToolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Profile");

        userfullName = findViewById(R.id.tvPerdonProfileName);
        userCity = findViewById(R.id.tvPerdonProfileCity);
        userRelationship = findViewById(R.id.tvPerdonProfileRelationship);
        userPhoneNumber = findViewById(R.id.tvPerdonProfilePhone);
        userPlaceOfStudy = findViewById(R.id.tvPerdonProfilePlaceOfStudy);
        userDoB = findViewById(R.id.tvPerdonProfileDoB);
        userGender = findViewById(R.id.tvPerdonProfileGender);
        userProfileImage = findViewById(R.id.imgPerdonProfilePicture);
        sendFriendRequest = findViewById(R.id.btnPersonSend);
        declaineFriendRequest = findViewById(R.id.btnPersonDecline);

        mAuth = FirebaseAuth.getInstance();

        receiverUserId = getIntent().getExtras().get("visitUserId").toString();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        userRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
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
}
