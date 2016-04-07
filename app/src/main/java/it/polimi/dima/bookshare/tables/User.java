package it.polimi.dima.bookshare.tables;

import android.os.Parcel;
import android.os.Parcelable;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import it.polimi.dima.bookshare.amazon.Constants;

@DynamoDBTable(tableName = Constants.USER_TABLE_NAME)
public class User implements Parcelable {
    private String userID;
    private String name;
    private String surname;
    private String city;
    private String country;
    private double latitude;
    private double longitude;
    private String imgURL;
    private int credits;

    public User() {
    }

    @DynamoDBHashKey(attributeName = "UserID")
    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    @DynamoDBAttribute
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @DynamoDBIndexRangeKey(attributeName = "Surname")
    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    @DynamoDBAttribute(attributeName = "City")
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @DynamoDBAttribute(attributeName = "imgURL")
    public String getImgURL() {
        return imgURL;
    }

    public void setImgURL(String imgURL) {
        this.imgURL = imgURL;
    }

    @DynamoDBAttribute(attributeName = "Credits")
    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    @DynamoDBAttribute(attributeName = "Country")
    public String getCountry() {
        return country;
    }

    @DynamoDBAttribute(attributeName = "Latitude")
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @DynamoDBAttribute(attributeName = "Longitude")
    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // write your object's data to the passed-in Parcel
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(userID);
        out.writeString(name);
        out.writeString(surname);
        out.writeString(city);
        out.writeString(country);
        out.writeString(imgURL);
        out.writeInt(credits);
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public User(Parcel in) {
        this.userID = in.readString();
        this.name = in.readString();
        this.surname = in.readString();
        this.city = in.readString();
        this.country = in.readString();
        this.imgURL = in.readString();
        this.credits = in.readInt();

    }
}
