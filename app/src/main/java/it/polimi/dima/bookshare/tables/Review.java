package it.polimi.dima.bookshare.tables;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import it.polimi.dima.bookshare.amazon.Constants;

@DynamoDBTable(tableName = Constants.REVIEW_TABLE_NAME)
public class Review {

    private String reviewerID;
    private String targetUserID;
    private String date;
    private String title;
    private String description;
    private int rating;

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
    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
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

}
