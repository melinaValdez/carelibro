package com.example.carelibro;

public class FindFriends {
    public String profilePic, fullName, city;

    public FindFriends(String profilePic, String fullName, String city) {
        this.profilePic = profilePic;
        this.fullName = fullName;
        this.city = city;
    }

    public FindFriends(){

    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
