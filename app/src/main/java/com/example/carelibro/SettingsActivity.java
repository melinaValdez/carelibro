package com.example.carelibro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private EditText userfullName, userCity, userGender, userRelationship, userPhoneNumber, userPlaceOfStudy, userDoB;
    private Button updateInfoButton;
    private CircleImageView userProfilePic;

    private DatabaseReference settingsUserReference;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mToolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Accounts settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        settingsUserReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);

        userfullName = findViewById(R.id.settingsFullName);
        userCity = findViewById(R.id.settingsCity);
        userRelationship = findViewById(R.id.settingsRelationship);
        userPhoneNumber = findViewById(R.id.settingsPhoneNumber);
        userPlaceOfStudy = findViewById(R.id.settingsStudy);
        updateInfoButton = findViewById(R.id.btnUpdateData);
        userDoB = findViewById(R.id.settingsDateOfBirth);
        userProfilePic = findViewById(R.id.settingsProfileImage);

        settingsUserReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String myProfileImage = dataSnapshot.child("profilePic").getValue().toString();
                    String myFullName = dataSnapshot.child("fullName").getValue().toString();
                    String myCity = dataSnapshot.child("city").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();
                    String myRelationship = dataSnapshot.child("relationshipStatus").getValue().toString();
                    String myDoB = dataSnapshot.child("dateOfBirth").getValue().toString();
                    String myPhoneNumber = dataSnapshot.child("phoneNumber").getValue().toString();
                    String myPlaceOfStudy = dataSnapshot.child("placeOfStudy").getValue().toString();

                    Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfilePic);
                    userfullName.setText(myFullName);
                    userCity.setText(myCity);
                    userGender.setText(myGender);
                    userRelationship.setText(myRelationship);
                    userPhoneNumber.setText(myPhoneNumber);
                    userPlaceOfStudy.setText(myPlaceOfStudy);
                    userDoB.setText(myDoB);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
