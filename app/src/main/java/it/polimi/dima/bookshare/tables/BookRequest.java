package it.polimi.dima.bookshare.tables;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import it.polimi.dima.bookshare.amazon.Constants;

/**
 * Created by matteo on 05/04/16.
 */
@DynamoDBTable(tableName = Constants.BOOKREQUEST_TABLE_NAME)
public class BookRequest {
    private int ID;
    private String askerID;
    private String receiverID;
    private String bookISBN;
    private Boolean accepted;
    private User user;
    private Book book;

    public BookRequest(){}

    @DynamoDBHashKey(attributeName = "AskerID")
    public String getAskerID() {
        return askerID;
    }

    public void setAskerID(String askerID) {
        this.askerID = askerID;
    }

    @DynamoDBRangeKey(attributeName = "ID")
    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    @DynamoDBAttribute(attributeName = "ReceiverID")
    public String getReceiverID() {
        return receiverID;
    }

    public void setReceiverID(String receiverID) {
        this.receiverID = receiverID;
    }

    @DynamoDBAttribute(attributeName = "BookISBN")
    public String getBookISBN() {
        return bookISBN;
    }

    public void setBookISBN(String bookISBN) {
        this.bookISBN = bookISBN;
    }

    @DynamoDBAttribute(attributeName = "Accepted")
    public Boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }
}
