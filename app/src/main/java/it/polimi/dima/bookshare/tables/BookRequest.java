package it.polimi.dima.bookshare.tables;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import it.polimi.dima.bookshare.amazon.Constants;

/**
 * Created by matteo on 05/04/16.
 */
@DynamoDBTable(tableName = Constants.BOOKREQUEST_TABLE_NAME)
public class BookRequest {
    private String ID;
    private String askerID;
    private String receiverID;
    private String bookISBN;

}
