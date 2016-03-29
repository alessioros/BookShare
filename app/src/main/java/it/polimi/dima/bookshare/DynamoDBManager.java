package it.polimi.dima.bookshare;

import android.content.Context;
import android.renderscript.Sampler;
import android.util.Log;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapperConfig;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.facebook.Profile;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by matteo on 25/03/16.
 */
public class DynamoDBManager {

    private static final String TAG = "DynamoDBManager";

    public static AmazonClientManager clientManager = null;
    private Context context;

    public DynamoDBManager(Context context) {
        this.context = context;
        clientManager = new AmazonClientManager(context);
    }

    /*
         * Creates a table with the following attributes: Table name: testTableName
         * Hash key: userNo type N Read Capacity Units: 10 Write Capacity Units: 5
         */
    public static void createTable() {

        Log.d(TAG, "Create table called");

        AmazonDynamoDBClient ddb = clientManager
                .ddb();

        KeySchemaElement kse = new KeySchemaElement().withAttributeName("ISBN").withKeyType(KeyType.HASH);

        AttributeDefinition ad = new AttributeDefinition().withAttributeName("ISBN").withAttributeType(ScalarAttributeType.S);

        ProvisionedThroughput pt = new ProvisionedThroughput().withReadCapacityUnits(5l).withWriteCapacityUnits(5l);

        CreateTableRequest request = new CreateTableRequest()
                .withTableName(Constants.BOOK_TABLE_NAME)
                .withKeySchema(kse)
                .withAttributeDefinitions(ad)
                .withProvisionedThroughput(pt);

        try {
            Log.d(TAG, "Sending Create table request");
            ddb.createTable(request);
            Log.d(TAG, "Create request response successfully received");
        } catch (AmazonServiceException ex) {
            Log.e(TAG, "Error sending create table request", ex);
            clientManager
                    .wipeCredentialsOnAuthError(ex);
        }
    }

    /*
     * Retrieves the table description and returns the table status as a string.
     */
    public static String getBookTableStatus() {

        try {
            AmazonDynamoDBClient ddb = clientManager
                    .ddb();

            DescribeTableRequest request = new DescribeTableRequest()
                    .withTableName(Constants.BOOK_TABLE_NAME);
            DescribeTableResult result = ddb.describeTable(request);

            String status = result.getTable().getTableStatus();
            return status == null ? "" : status;

        } catch (ResourceNotFoundException e) {
        } catch (AmazonServiceException ex) {
            clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return "";
    }

    /*
     * Insert book in table
     */
    public static void insertBook(Book book) {
        AmazonDynamoDBClient ddb = clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {

            Log.d(TAG, "Inserting book");
            mapper.save(book);
            Log.d(TAG, "Book inserted");

        } catch (AmazonServiceException ex) {
            Log.e(TAG, "Error inserting book");
            clientManager
                    .wipeCredentialsOnAuthError(ex);
        }
    }

    /*
     * Scans the table and returns the list of books.
     */
    public static ArrayList<Book> getBookList() {

        AmazonDynamoDBClient ddb = clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        try {
            PaginatedScanList<Book> result = mapper.scan(Book.class, scanExpression);

            ArrayList<Book> resultList = new ArrayList<Book>();
            for (Book book : result) {
                resultList.add(book);
            }
            return resultList;

        } catch (AmazonServiceException ex) {
            clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return null;
    }

    /*
     * Retrieves all of the attribute/value pairs for the specified book.
     */
    public static Book getBook(String ISBN) {

        AmazonDynamoDBClient ddb = clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            Book book = mapper.load(Book.class,
                    ISBN);

            return book;

        } catch (AmazonServiceException ex) {
            clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return null;
    }

    /*
     * Updates one attribute/value pair for the specified book.
     */
    public static void updateBook(Book book) {

        AmazonDynamoDBClient ddb = clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            mapper.save(book);

        } catch (AmazonServiceException ex) {
            clientManager
                    .wipeCredentialsOnAuthError(ex);
        }
    }

    /*
     * Deletes the specified book and all of its attribute/value pairs.
     */
    public static void deleteUser(Book book) {

        AmazonDynamoDBClient ddb = clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            mapper.delete(book);

        } catch (AmazonServiceException ex) {
            clientManager
                    .wipeCredentialsOnAuthError(ex);
        }
    }

    /*
     * Deletes the book table and all of its books and their attribute/value
     * pairs.
     */
    public static void cleanUp() {

        AmazonDynamoDBClient ddb = clientManager
                .ddb();

        DeleteTableRequest request = new DeleteTableRequest()
                .withTableName(Constants.BOOK_TABLE_NAME);
        try {
            ddb.deleteTable(request);

        } catch (AmazonServiceException ex) {
            clientManager
                    .wipeCredentialsOnAuthError(ex);
        }
    }

    public static ArrayList<Book> getBooks(String ownerID) {

        ArrayList<Book> userBooks = new ArrayList<>();

        AmazonDynamoDBClient ddb = clientManager
                .ddb();

        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        // Create our map of values
        Map keyConditions = new HashMap();

        // Specify our key conditions (ownerId == "ownerID")
        Condition hashKeyCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue().withS(ownerID));
        keyConditions.put("ownerID", hashKeyCondition);

        Map lastEvaluatedKey = null;
        do {
            QueryRequest queryRequest = new QueryRequest()
                    .withTableName(Constants.BOOK_TABLE_NAME)
                    .withKeyConditions(keyConditions)
                    .withExclusiveStartKey(lastEvaluatedKey)
                    .withIndexName("ownerID-index");

            QueryResult queryResult = ddb.query(queryRequest);

            for (Map item : queryResult.getItems()) {

                Book book;
                Log.i(TAG,item.toString());
                book = mapper.load(Book.class, item.get("ISBN").toString());

                book.setIsbn((item.get("ISBN").toString()));
                book.setDescription(item.get("Description").toString());
                book.setTitle(item.get("Title").toString());
                book.setOwnerID(item.get("ownerID").toString());
                //book.setPageCount(Integer.parseInt(item.get("pageCount").toString()));
                book.setAuthor(item.get("Author").toString());
                AttributeValue attribute = (AttributeValue) item.get("ISBN");
                book.setIsbn(attribute.getS());

                attribute = (AttributeValue) item.get("Description");
                book.setDescription(attribute.getS());

                attribute = (AttributeValue) item.get("Title");
                book.setTitle(attribute.getS());

                attribute = (AttributeValue) item.get("ownerID");
                book.setOwnerID(attribute.getS());

                attribute = (AttributeValue) item.get("pageCount");
                book.setPageCount(Integer.parseInt(attribute.getN()));

                attribute = (AttributeValue) item.get("Author");
                book.setAuthor(attribute.getS());

                try {

                    book.setImgURL(item.get("imgURL").toString());

                } catch (Exception e) {
                    e.printStackTrace();
                }

                userBooks.add(book);
            }

            // If the response lastEvaluatedKey has contents,
            // that means there are more results
            lastEvaluatedKey = queryResult.getLastEvaluatedKey();

        } while (lastEvaluatedKey != null);

        return userBooks;
    }

}

