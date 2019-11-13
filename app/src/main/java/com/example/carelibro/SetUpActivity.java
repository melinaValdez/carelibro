package com.example.carelibro;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

public class SetUpActivity extends AppCompatActivity {

    private EditText txtusername, txtfullName, txtcity, txtdateOfBirth, txtGender, txtPhone;
    private Button btnSaveInformation;
    private CircleImageView imgProfilePic;
    private ProgressDialog loadingDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference userReference;
    private String currentUserId;
    private StorageReference userProfilePicReference;

    final static int galleryPic = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);
        loadingDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        userProfilePicReference = FirebaseStorage.getInstance().getReference().child("Profile images");

        txtusername = findViewById(R.id.txtUsername);
        txtfullName = findViewById(R.id.txtFullName);
        txtGender = findViewById(R.id.txtGender);
        txtPhone = findViewById(R.id.txtPhone);
        txtcity = findViewById(R.id.txtCity);
        txtdateOfBirth = findViewById(R.id.txtDateOfBirth);
        btnSaveInformation = findViewById(R.id.btnSave);
        imgProfilePic = findViewById(R.id.imgProfilePic);


        btnSaveInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSetUpInformation();
            }
        });

        imgProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, galleryPic);
            }
        });

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.hasChild("profilePic")){
                        userProfilePicReference.child(currentUserId).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Picasso.get().load(uri).placeholder(R.drawable.profile).into((imgProfilePic));
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
                        Toast.makeText(SetUpActivity.this, "Profile image stored successfully", Toast.LENGTH_SHORT).show();
                        userProfilePicReference.child(currentUserId).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                userReference.child("profilePic").setValue(uri.toString())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Intent selfIntent = new Intent(SetUpActivity.this, SetUpActivity.class);
                                                    startActivity(selfIntent);
                                                    Toast.makeText(SetUpActivity.this, "Profile image saved", Toast.LENGTH_LONG).show();
                                                } else {
                                                    String message = task.getException().getMessage();
                                                    Toast.makeText(SetUpActivity.this, "Error occured: " + message, Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {
                                                String message = taskSnapshot.getError().getMessage().toString();
                                                Toast.makeText(SetUpActivity.this, "Error 1 occured: " + message, Toast.LENGTH_LONG).show();
                                            };
                                        });
                            }
                        });

                    }
                });

            } else {
                Toast.makeText(SetUpActivity.this, "Image couldn't be cropped. Please try again", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void sendUserToMainAcitivty(){
        Intent mainActivity = new Intent(SetUpActivity.this, MainActivity.class);
        mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivity);
        finish();
    }

    private void saveSetUpInformation(){
        String username = txtusername.getText().toString();
        String fullName = txtfullName.getText().toString();
        String city = txtcity.getText().toString();
        String dateOfBirth = txtdateOfBirth.getText().toString();
        String gender = txtGender.getText().toString();
        String phone = txtPhone.getText().toString();

        if (TextUtils.isEmpty(username)){
            Toast.makeText(this, "Please, write your username", Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(fullName)){
            Toast.makeText(this, "Please, write your full name", Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(city)){
            Toast.makeText(this, "Please, write your city", Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(dateOfBirth)){
            Toast.makeText(this, "Please, write your date of birth", Toast.LENGTH_LONG).show();
        }
        else{
            loadingDialog.setTitle("Saving your information");
            loadingDialog.setMessage("Please wait while we save your data.");
            loadingDialog.show();
            loadingDialog.setCanceledOnTouchOutside(true);
            HashMap userMap = new HashMap();
            userMap.put("username", username);
            userMap.put("fullName", fullName);
            userMap.put("city", city);
            userMap.put("dateOfBirth", dateOfBirth);
            userMap.put("gender", gender);
            userMap.put("relationshipStatus", "None");
            userMap.put("placeOfStudy", "None");
            userMap.put("phoneNumber", phone);

            userReference.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        Toast.makeText(SetUpActivity.this, "Profile updated succesfully", Toast.LENGTH_LONG).show();
                        sendUserToMainAcitivty();
                        loadingDialog.dismiss();
                        finish();
                    }
                    else{
                        String message = task.getException().getMessage();
                        Toast.makeText(SetUpActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                        loadingDialog.dismiss();
                    }
                }
            });

        }
    }
}
