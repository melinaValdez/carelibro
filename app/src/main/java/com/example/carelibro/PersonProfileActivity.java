package com.example.carelibro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {


    private Toolbar mToolbar;
    private TextView userfullName, userCity, userGender, userRelationship, userPhoneNumber, userPlaceOfStudy, userDoB;
    private CircleImageView userProfileImage;

    private Button sendFriendRequest, declaineFriendRequest;


    private DatabaseReference friendRequestRef,userRef,friedRef;
    private FirebaseAuth mAuth;
    private String senderUserId,receiverUserId, CURRENT_STATE, saveCurrentDate;

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

        CURRENT_STATE = "Not Friends";

        mAuth = FirebaseAuth.getInstance();

        senderUserId = mAuth.getCurrentUser().getUid();

        receiverUserId = getIntent().getExtras().get("visitUserId").toString();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendsRequest");
        friedRef = FirebaseDatabase.getInstance().getReference().child("Friends");

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

                MaintanceOfButtons();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        declaineFriendRequest.setVisibility(View.INVISIBLE);
        declaineFriendRequest.setEnabled(false);

        if(!senderUserId.equals(receiverUserId)){
            sendFriendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendFriendRequest.setEnabled(false);
                    if(CURRENT_STATE.equals("Not Friends")){
                        sendFriendRequestToAPerson();
                    }
                    if(CURRENT_STATE.equals("request_send")){
                        cancelFriendRequest();
                    }
                    if(CURRENT_STATE.equals("request_received")){
                        acceptFrinedRequest();
                    }
                    if(CURRENT_STATE.equals("Friends")){
                        unfriendAFriend();
                    }
                }
            });
        }else {

            declaineFriendRequest.setVisibility(View.INVISIBLE);
            sendFriendRequest.setVisibility(View.INVISIBLE);
        }

    }

    private void unfriendAFriend(){
        friedRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            friedRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            sendFriendRequest.setEnabled(true);
                                            CURRENT_STATE = "Not Friends";
                                            sendFriendRequest.setText("Send Friend Request");

                                            declaineFriendRequest.setVisibility(View.INVISIBLE);
                                            declaineFriendRequest.setEnabled(false);
                                        }
                                    });
                        }
                    }
                });

    }
    private void acceptFrinedRequest(){

        Calendar calFordDate = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMM-yy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        friedRef.child(senderUserId).child(receiverUserId).child("date").setValue(saveCurrentDate)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    friedRef.child(receiverUserId).child(senderUserId).setValue(saveCurrentDate)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        friendRequestRef.child(senderUserId).child(receiverUserId)
                                                                .removeValue()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()){
                                                                            friendRequestRef.child(receiverUserId).child(senderUserId)
                                                                                    .removeValue()
                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            sendFriendRequest.setEnabled(true);
                                                                                            CURRENT_STATE = "Friends";
                                                                                            sendFriendRequest.setText("Unfriend this Person");

                                                                                            declaineFriendRequest.setVisibility(View.INVISIBLE);
                                                                                            declaineFriendRequest.setEnabled(false);
                                                                                        }
                                                                                    });
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                }
                                            });
                                }
                            }
                        });

    }

    private void cancelFriendRequest(){

        friendRequestRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            friendRequestRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            sendFriendRequest.setEnabled(true);
                                            CURRENT_STATE = "Not Friends";
                                            sendFriendRequest.setText("Send Friend Request");

                                            declaineFriendRequest.setVisibility(View.INVISIBLE);
                                            declaineFriendRequest.setEnabled(false);
                                        }
                                    });
                        }
                    }
                });

    }

    private void MaintanceOfButtons(){
        friendRequestRef.child(senderUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(receiverUserId)){
                            String request_type = dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();
                            if(request_type.equals("send")){
                                CURRENT_STATE = "request_send";
                                sendFriendRequest.setTag("Cancel Friend Request");

                                declaineFriendRequest.setVisibility(View.INVISIBLE);
                                declaineFriendRequest.setEnabled(false);
                            }else if(request_type.equals("received")){
                                CURRENT_STATE = "request_received";
                                sendFriendRequest.setText("Accept Friend Request");

                                declaineFriendRequest.setVisibility(View.VISIBLE);
                                declaineFriendRequest.setEnabled(true);

                                declaineFriendRequest.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        cancelFriendRequest();
                                    }
                                });
                            }
                        }else{
                            friedRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.exists()){
                                                CURRENT_STATE="Friends";
                                                sendFriendRequest.setText("Unfriend this Person");
                                                declaineFriendRequest.setVisibility(View.INVISIBLE);
                                                declaineFriendRequest.setEnabled(false);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void sendFriendRequestToAPerson() {
        friendRequestRef.child(senderUserId).child(receiverUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            friendRequestRef.child(receiverUserId).child(senderUserId)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            sendFriendRequest.setEnabled(true);
                                            CURRENT_STATE = "request_send";
                                            sendFriendRequest.setText("Cancel Friend Request");

                                            declaineFriendRequest.setVisibility(View.INVISIBLE);
                                            declaineFriendRequest.setEnabled(false);
                                        }
                                    });
                        }
                    }
                });
    }
}
