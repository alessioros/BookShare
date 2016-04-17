package it.polimi.dima.bookshare.amazon;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import it.polimi.dima.bookshare.tables.Book;
import it.polimi.dima.bookshare.tables.BookRequest;
import it.polimi.dima.bookshare.tables.User;

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

        AmazonDynamoDBClient ddb = clientManager.ddb();

        KeySchemaElement kseH = new KeySchemaElement().withAttributeName("ISBN").withKeyType(KeyType.HASH);
        KeySchemaElement kseR = new KeySchemaElement().withAttributeName("ownerID").withKeyType(KeyType.RANGE);

        AttributeDefinition isbn = new AttributeDefinition().withAttributeName("ISBN").withAttributeType(ScalarAttributeType.S);
        AttributeDefinition ownerID = new AttributeDefinition().withAttributeName("ownerID").withAttributeType(ScalarAttributeType.S);

        ProvisionedThroughput pt = new ProvisionedThroughput().withReadCapacityUnits(5l).withWriteCapacityUnits(5l);

        CreateTableRequest request = new CreateTableRequest()
                .withTableName(Constants.BOOK_TABLE_NAME)
                .withKeySchema(kseH,kseR)
                .withAttributeDefinitions(isbn,ownerID)
                .withProvisionedThroughput(pt);

        try {
            Log.d(TAG, "Sending Create table request");
            ddb.createTable(request);
            Log.d(TAG, "Create request response successfully received");
        } catch (AmazonServiceException ex) {
            Log.e(TAG, "Error sending create table request", ex);
            clientManager.wipeCredentialsOnAuthError(ex);
        }
    }

    /*
     * Retrieves the table description and returns the table status as a string.
     */
    public static String getBookTableStatus() {

        try {
            AmazonDynamoDBClient ddb = clientManager.ddb();

            DescribeTableRequest request = new DescribeTableRequest().withTableName(Constants.BOOK_TABLE_NAME);
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
        AmazonDynamoDBClient ddb = clientManager.ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {

            Log.d(TAG, "Inserting book");
            mapper.save(book);
            Log.d(TAG, "Book inserted");

        } catch (AmazonServiceException ex) {
            Log.e(TAG, "Error inserting book");
            clientManager.wipeCredentialsOnAuthError(ex);
        }
    }

    /*
     * Scans the table and returns the list of books.
     */
    public static ArrayList<Book> getBookList() {

        AmazonDynamoDBClient ddb = clientManager.ddb();
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
    public static Book getBook(String ISBN,String ownerID) {

        AmazonDynamoDBClient ddb = clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            Book book = mapper.load(Book.class, ISBN, ownerID);

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

        AmazonDynamoDBClient ddb = clientManager.ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            mapper.save(book);

        } catch (AmazonServiceException ex) {
            clientManager.wipeCredentialsOnAuthError(ex);
        }
    }

    /*
     * Deletes the specified book and all of its attribute/value pairs.
     */
    public static void deleteBook(Book book) {

        AmazonDynamoDBClient ddb = clientManager.ddb();
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

                Book book = mapAttributes(item);

                userBooks.add(book);
            }

            // If the response lastEvaluatedKey has contents,
            // that means there are more results
            lastEvaluatedKey = queryResult.getLastEvaluatedKey();

        } while (lastEvaluatedKey != null);

        return userBooks;
    }

    public static ArrayList<Book> getReceivedBooks(String receiverID) {

        ArrayList<Book> userBooks = new ArrayList<>();

        AmazonDynamoDBClient ddb = clientManager.ddb();

        // Create our map of values
        Map keyConditions = new HashMap();

        // Specify our key conditions (receiverId == "receiverID")
        Condition hashKeyCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue().withS(receiverID));
        keyConditions.put("receiverID", hashKeyCondition);

        Map lastEvaluatedKey = null;
        do {
            QueryRequest queryRequest = new QueryRequest()
                    .withTableName(Constants.BOOK_TABLE_NAME)
                    .withKeyConditions(keyConditions)
                    .withExclusiveStartKey(lastEvaluatedKey)
                    .withIndexName("receiverID-index");

            QueryResult queryResult = ddb.query(queryRequest);

            for (Map item : queryResult.getItems()) {

                Book book = mapAttributes(item);

                userBooks.add(book);
            }

            // If the response lastEvaluatedKey has contents,
            // that means there are more results
            lastEvaluatedKey = queryResult.getLastEvaluatedKey();

        } while (lastEvaluatedKey != null);

        return userBooks;
    }

    public static int getReceivedBooksCount(String receiverID) {

        ArrayList<Book> userBooks = new ArrayList<>();

        AmazonDynamoDBClient ddb = clientManager.ddb();

        // Create our map of values
        Map keyConditions = new HashMap();

        // Specify our key conditions (receiverId == "receiverID")
        Condition hashKeyCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue().withS(receiverID));
        keyConditions.put("receiverID", hashKeyCondition);

        Map lastEvaluatedKey = null;

        QueryRequest queryRequest = new QueryRequest()
                .withTableName(Constants.BOOK_TABLE_NAME)
                .withKeyConditions(keyConditions)
                .withExclusiveStartKey(lastEvaluatedKey)
                .withIndexName("receiverID-index");

        QueryResult queryResult = ddb.query(queryRequest);

        return queryResult.getCount();
    }


    public static int getBooksCount(String ownerID) {

        AmazonDynamoDBClient ddb = clientManager.ddb();

        // Create our map of values
        Map keyConditions = new HashMap();

        // Specify our key conditions (ownerId == "ownerID")
        Condition hashKeyCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue().withS(ownerID));
        keyConditions.put("ownerID", hashKeyCondition);

        QueryRequest queryRequest = new QueryRequest()
                .withTableName(Constants.BOOK_TABLE_NAME)
                .withKeyConditions(keyConditions)
                .withIndexName("ownerID-index");

        QueryResult queryResult = ddb.query(queryRequest);

        return queryResult.getCount();
    }

    /*
     * Scans the table and returns the list of books searched
     */
    public ArrayList<Book> getBookListSearch(String query) {

        AmazonDynamoDBClient ddb = clientManager.ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        try {
            PaginatedScanList<Book> result = mapper.scan(Book.class, scanExpression);

            ArrayList<Book> resultList = new ArrayList<Book>();
            for (Book book : result) {
                if(Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE).matcher(book.getTitle()).find()
                        && !book.getOwnerID().equals(PreferenceManager.getDefaultSharedPreferences(context).getString("ID",null))){
                    resultList.add(book);
                }
            }
            return resultList;

        } catch (AmazonServiceException ex) {
            clientManager.wipeCredentialsOnAuthError(ex);
        }

        return null;
    }

    /*
    FUNCTION NOT WORKING
    query that gets the book on GSI Title
     */
    public PaginatedQueryList<Book> getBookListSearchQueryTitle(String query) {

        AmazonDynamoDBClient ddb = clientManager.ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            final Book book = new Book();
            book.setTitle(query);
            final DynamoDBQueryExpression<Book> queryExpression = new DynamoDBQueryExpression<>();
            queryExpression.setHashKeyValues(book);
            queryExpression.setIndexName("Title");
            queryExpression.setConsistentRead(false);   // cannot use consistent read on GSI
            final PaginatedQueryList<Book> results = mapper.query(Book.class, queryExpression);
            return results;

        } catch (AmazonServiceException ex) {
            clientManager.wipeCredentialsOnAuthError(ex);
        }

        return null;
    }

    /*
    *                                     USERS
    * */

    public static void createTableUsers() {

        Log.d(TAG, "Create table called");

        AmazonDynamoDBClient ddb = clientManager.ddb();

        KeySchemaElement kse = new KeySchemaElement().withAttributeName("UserID").withKeyType(KeyType.HASH);

        AttributeDefinition ad = new AttributeDefinition().withAttributeName("UserID").withAttributeType(ScalarAttributeType.S);

        ProvisionedThroughput pt = new ProvisionedThroughput().withReadCapacityUnits(5l).withWriteCapacityUnits(5l);

        CreateTableRequest request = new CreateTableRequest()
                .withTableName(Constants.USER_TABLE_NAME)
                .withKeySchema(kse)
                .withAttributeDefinitions(ad)
                .withProvisionedThroughput(pt);

        try {
            Log.d(TAG, "Sending Create table request");
            ddb.createTable(request);
            Log.d(TAG, "Create request response successfully received");
        } catch (AmazonServiceException ex) {
            Log.e(TAG, "Error sending create table request", ex);
            clientManager.wipeCredentialsOnAuthError(ex);
        }
    }

    /*
     * Insert user in table
     */
    public static void insertUser(User user) {
        AmazonDynamoDBClient ddb = clientManager.ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {

            mapper.save(user);

        } catch (AmazonServiceException ex) {
            Log.e(TAG, "Error inserting user");
            clientManager
                    .wipeCredentialsOnAuthError(ex);
        }
    }

    /*
     * Retrieves all of the attribute/value pairs for the specified user.
     */
    public static User getUser(String userID) {

        AmazonDynamoDBClient ddb = clientManager.ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            User user = mapper.load(User.class, userID);

            return user;

        } catch (AmazonServiceException ex) {
            clientManager.wipeCredentialsOnAuthError(ex);
        }

        return null;
    }

    /*
        Insert new BookRequest in dynamo
     */
    public static void insertBookRequest(BookRequest bookRequest) {
        AmazonDynamoDBClient ddb = clientManager.ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {

            mapper.save(bookRequest);
            Log.d(TAG,"Request inserted");

        } catch (AmazonServiceException ex) {
            Log.e(TAG, "Error inserting request");
            clientManager.wipeCredentialsOnAuthError(ex);
        }
    }

    /*
    Get all book requested by you
     */
    public ArrayList<BookRequest> getMyBookRequests() {

        ArrayList<BookRequest> userRequests = new ArrayList<>();

        AmazonDynamoDBClient ddb = clientManager.ddb();

        // Create our map of values
        Map keyConditions = new HashMap();

        // Specify our key conditions (askerID == "AskerID")
        Condition hashKeyCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue().withS(PreferenceManager.getDefaultSharedPreferences(context).getString("ID",null)));
        keyConditions.put("AskerID", hashKeyCondition);

        Map lastEvaluatedKey = null;
        do {
            QueryRequest queryRequest = new QueryRequest()
                    .withTableName(Constants.BOOKREQUEST_TABLE_NAME)
                    .withKeyConditions(keyConditions)
                    .withExclusiveStartKey(lastEvaluatedKey);

            QueryResult queryResult = ddb.query(queryRequest);

            for (Map item : queryResult.getItems()) {

                BookRequest bookRequest=new BookRequest();

                AttributeValue attribute = (AttributeValue) item.get("AskerID");
                bookRequest.setAskerID(attribute.getS());

                attribute=(AttributeValue) item.get("ReceiverID");
                bookRequest.setReceiverID(attribute.getS());

                attribute=(AttributeValue) item.get("BookISBN");
                bookRequest.setBookISBN(attribute.getS());

                bookRequest.setID(0);


                userRequests.add(bookRequest);
            }

            // If the response lastEvaluatedKey has contents,
            // that means there are more results
            lastEvaluatedKey = queryResult.getLastEvaluatedKey();

        } while (lastEvaluatedKey != null);

        return userRequests;
    }

    /*
    Get all the bookRequests received by the user
     */
    public ArrayList<BookRequest> getReceivedRequests() {

        ArrayList<BookRequest> userReceivedRequests = new ArrayList<>();

        AmazonDynamoDBClient ddb = clientManager.ddb();

        // Create our map of values
        Map keyConditions = new HashMap();

        // Specify our key conditions (receiverId == "receiverID")
        Condition hashKeyCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue().withS(PreferenceManager.getDefaultSharedPreferences(context).getString("ID",null)));
        keyConditions.put("ReceiverID", hashKeyCondition);

        Map lastEvaluatedKey = null;
        do {
            QueryRequest queryRequest = new QueryRequest()
                    .withTableName(Constants.BOOKREQUEST_TABLE_NAME)
                    .withKeyConditions(keyConditions)
                    .withExclusiveStartKey(lastEvaluatedKey)
                    .withIndexName("ReceiverID-index");

            QueryResult queryResult = ddb.query(queryRequest);

            for (Map item : queryResult.getItems()) {
                BookRequest bookRequest=new BookRequest();

                AttributeValue attribute = (AttributeValue) item.get("AskerID");
                bookRequest.setAskerID(attribute.getS());

                attribute=(AttributeValue) item.get("ReceiverID");
                bookRequest.setReceiverID(attribute.getS());

                attribute=(AttributeValue) item.get("BookISBN");
                bookRequest.setBookISBN(attribute.getS());

                attribute=(AttributeValue) item.get("ID");
                bookRequest.setID(Integer.parseInt(attribute.getN()));

                attribute=(AttributeValue) item.get("Accepted");
                if(Integer.parseInt(attribute.getN())==1){
                    bookRequest.setAccepted(true);
                }else {
                    bookRequest.setAccepted(false);
                }

                userReceivedRequests.add(bookRequest);
            }

            // If the response lastEvaluatedKey has contents,
            // that means there are more results
            lastEvaluatedKey = queryResult.getLastEvaluatedKey();

        } while (lastEvaluatedKey != null);

        return userReceivedRequests;
    }

    public void updateBookRequest(BookRequest bookRequest) {

        AmazonDynamoDBClient ddb = clientManager.ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            mapper.save(bookRequest);

        } catch (AmazonServiceException ex) {
            clientManager.wipeCredentialsOnAuthError(ex);
        }
    }


    public static Book mapAttributes(Map item) {

        Book book = new Book();

        AttributeValue attribute = (AttributeValue) item.get("ISBN");
        book.setIsbn(attribute.getS());

        try {
            attribute = (AttributeValue) item.get("Description");
            book.setDescription(attribute.getS());
        } catch (NullPointerException e) {
        }

        attribute = (AttributeValue) item.get("Title");
        book.setTitle(attribute.getS());

        attribute = (AttributeValue) item.get("ownerID");
        book.setOwnerID(attribute.getS());

        try {
            attribute = (AttributeValue) item.get("PageCount");
            book.setPageCount(Integer.parseInt(attribute.getN()));
        } catch (NullPointerException e) {
        }

        try {
            attribute = (AttributeValue) item.get("Author");
            book.setAuthor(attribute.getS());

        } catch (NullPointerException e) {
        }

        try {
            attribute = (AttributeValue) item.get("imgURL");
            book.setImgURL(attribute.getS());

        } catch (NullPointerException e) {
        }

        try {
            attribute = (AttributeValue) item.get("Publisher");
            book.setPublisher(attribute.getS());

        } catch (NullPointerException e) {
        }

        try {
            attribute = (AttributeValue) item.get("PublishedDate");
            book.setPublishedDate(attribute.getS());

        } catch (NullPointerException e) {
        }

        return book;

    }

}

