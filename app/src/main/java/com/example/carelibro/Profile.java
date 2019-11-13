/*
 * Copyright 2017 Rozdoum
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.example.carelibro;

import android.content.Context;
import android.util.Log;

import com.example.carelibro.listeners.OnObjectExistListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;

@IgnoreExtraProperties
public class Profile implements Serializable {

    private String id;
    private String username;
    private String email;
    private String photoUrl;
    private String gender;
    private String city;
    private String phone;
    private String birth;
    private long likesCount;
    private String registrationToken;
    private ItemType itemType;

    private static Profile instance;


    private DatabaseHelper databaseHelper;

    Context context;

    public Profile() {
        // Default constructor required for calls to DataSnapshot.getValue(Profile.class)
    }

    private Profile(Context context) {
        this.context = context;
        databaseHelper = ApplicationHelper.getDatabaseHelper();
    }

    public static Profile getInstance(Context pContext) {
        if (instance == null) {
            instance = new Profile(pContext);
        }

        return instance;
    }

    public Profile(String id) {
        this.id = id;
    }

    public Profile(ItemType load) {
        itemType = load;
    }

    public void isProfileExist(String id, final OnObjectExistListener<Profile> onObjectExistListener) {
        isProfileExistDB(id, onObjectExistListener);
    }

    public void isProfileExistDB(String id, final OnObjectExistListener<Profile> onObjectExistListener) {
        DatabaseReference databaseReference = databaseHelper.getDatabaseReference().child("Users").child(id);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                onObjectExistListener.onDataChanged(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void addRegistrationToken(String token, String userId) {
        Task<Void> task = databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.PROFILES_DB_KEY)
                .child(userId).child("notificationTokens")
                .child(token).setValue(true);
        task.addOnCompleteListener(task1 -> Log.d("Profile", "addRegistrationToken, success: " + task1.isSuccessful()));
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }



    public long getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(long likesCount) {
        this.likesCount = likesCount;
    }

    public String getRegistrationToken() {
        return registrationToken;
    }

    public void setRegistrationToken(String registrationToken) {
        this.registrationToken = registrationToken;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBirth() {
        return birth;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }


    public ItemType getItemType() {
        return itemType;
    }


    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }
}
