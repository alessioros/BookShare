package it.polimi.dima.bookshare.tables;

import android.os.Parcel;
import android.os.Parcelable;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import it.polimi.dima.bookshare.amazon.Constants;

@DynamoDBTable(tableName = Constants.REVIEW_TABLE_NAME)
public class Review implements Parcelable {

    private String reviewerID;
    private String targetUserID;
    private String date;
    private String title;
    private String description;
    private float rating;

    public Review() {
    }

    @DynamoDBHashKey(attributeName = "reviewerID")
    public String getReviewerID() {
        return reviewerID;
    }

    public void setReviewerID(String reviewerID) {
        this.reviewerID = reviewerID;
    }

    @DynamoDBRangeKey(attributeName = "Date")
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @DynamoDBAttribute(attributeName = "targetUserID")
    public String getTargetUserID() {
        return targetUserID;
    }

    public void setTargetUserID(String targetUserID) {
        this.targetUserID = targetUserID;
    }

    @DynamoDBAttribute(attributeName = "Rating")
    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    @DynamoDBAttribute(attributeName = "Title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @DynamoDBAttribute(attributeName = "Description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    protected Review(Parcel in) {
        reviewerID = in.readString();
        targetUserID = in.readString();
        date = in.readString();
        title = in.readString();
        description = in.readString();
        rating = in.readFloat();
    }

    public static final Creator<Review> CREATOR = new Creator<Review>() {
        @Override
        public Review createFromParcel(Parcel in) {
            return new Review(in);
        }

        @Override
        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(reviewerID);
        out.writeString(targetUserID);
        out.writeString(date);
        out.writeString(title);
        out.writeString(description);
        out.writeFloat(rating);
    }

    private void readFromParcel(Parcel in) {

        this.reviewerID = in.readString();
        this.targetUserID = in.readString();
        this.date = in.readString();
        this.title = in.readString();
        this.description = in.readString();
        this.rating = in.readFloat();
    }
}
