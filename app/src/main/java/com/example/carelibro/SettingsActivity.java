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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.carelibro.SetUpActivity.galleryPic;

public class SettingsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private EditText userfullName, userCity, userGender, userRelationship, userPhoneNumber, userPlaceOfStudy, userDoB;
    private Button updateInfoButton;
    private CircleImageView userProfilePic;

    private DatabaseReference settingsUserReference;
    private FirebaseAuth mAuth;
    private String currentUserId;

    final static int galleryPic = 1;

    private StorageReference userProfilePicReference;

    private ProgressDialog loadingDialog;

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
        userProfilePicReference = FirebaseStorage.getInstance().getReference().child("Profile images");

        loadingDialog = new ProgressDialog(this);

        userfullName = findViewById(R.id.settingsFullName);
        userCity = findViewById(R.id.settingsCity);
        userRelationship = findViewById(R.id.settingsRelationship);
        userPhoneNumber = findViewById(R.id.settingsPhoneNumber);
        userPlaceOfStudy = findViewById(R.id.settingsStudy);
        updateInfoButton = findViewById(R.id.btnUpdateData);
        userDoB = findViewById(R.id.settingsDateOfBirth);
        userProfilePic = findViewById(R.id.settingsProfileImage);
        userGender = findViewById(R.id.settingsGender);

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

        updateInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateAccountInfo();
            }
        });

        userProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, galleryPic);
            }
        });

    }

    private void validateAccountInfo() {
        if (TextUtils.isEmpty(userfullName.getText())){
            Toast.makeText(SettingsActivity.this,"Please. write your name",Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(userCity.getText())){
            Toast.makeText(SettingsActivity.this,"Please. write your city",Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(userDoB.getText())){
            Toast.makeText(SettingsActivity.this,"Please. write your date of birth",Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(userGender.getText())){
            Toast.makeText(SettingsActivity.this,"Please. write your gender",Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(userRelationship.getText())){
            Toast.makeText(SettingsActivity.this,"Please. write your relationship status",Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(userPhoneNumber.getText())){
            Toast.makeText(SettingsActivity.this,"Please. write your phone number",Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(userPlaceOfStudy.getText())){
            Toast.makeText(SettingsActivity.this,"Please. write your place of study",Toast.LENGTH_LONG).show();
        }
        else{
            HashMap userMap = new HashMap();
            userMap.put("fullName", userfullName.getText().toString());
            userMap.put("city", userCity.getText().toString());
            userMap.put("dateOfBirth", userDoB.getText().toString());
            userMap.put("gender", userGender.getText().toString());
            userMap.put("relationshipStatus", userRelationship.getText().toString());
            userMap.put("placeOfStudy", userPlaceOfStudy.getText().toString());
            userMap.put("phoneNumber", userPhoneNumber.getText().toString());
            settingsUserReference.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        Toast.makeText(SettingsActivity.this, "Acoount data updated succesfully", Toast.LENGTH_LONG).show();
                        sendUserToMainAcitivty();
                    }
                    else{
                        String message = task.getException().getMessage();
                        Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

    }

    private void sendUserToMainAcitivty(){
        Intent mainActivity = new Intent(SettingsActivity.this, MainActivity.class);
        mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivity);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == galleryPic && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                loadingDialog.setTitle("Profile Image");
                loadingDialog.setMessage("Please, wait while we update your profile image");
                loadingDialog.show();
                loadingDialog.setCanceledOnTouchOutside(true);

                Uri resultUri = result.getUri();
                StorageReference filePath = userProfilePicReference.child(currentUserId);
                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(SettingsActivity.this, "Profile image stored successfully", Toast.LENGTH_SHORT).show();
                        userProfilePicReference.child(currentUserId).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                settingsUserReference.child("profilePic").setValue(uri.toString())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Intent selfIntent = new Intent(SettingsActivity.this, SettingsActivity.class);
                                                    startActivity(selfIntent);
                                                    Toast.makeText(SettingsActivity.this, "Profile image saved", Toast.LENGTH_LONG).show();
                                                } else {
                                                    String message = task.getException().getMessage();
                                                    Toast.makeText(SettingsActivity.this, "Error occured: " + message, Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {
                                                String message = taskSnapshot.getError().getMessage().toString();
                                                Toast.makeText(SettingsActivity.this, "Error 1 occured: " + message, Toast.LENGTH_LONG).show();
                                            };
                                        });
                            }
                        });

                    }
                });

            } else {
                Toast.makeText(SettingsActivity.this, "Image couldn't be cropped. Please try again", Toast.LENGTH_LONG).show();
            }
        }
    }
}
