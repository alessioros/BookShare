package it.polimi.dima.bookshare.amazon;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import it.polimi.dima.bookshare.tables.Book;
import it.polimi.dima.bookshare.tables.BookRequest;
import it.polimi.dima.bookshare.tables.User;
import it.polimi.dima.bookshare.utils.ManageUser;

public class DynamoDBManager {

    private static final String TAG = "DynamoDBManager";

    public static AmazonClientManager clientManager = null;
    private AmazonDynamoDBClient ddbClient;
    private DynamoDBMapper mapper;
    private Context context;

    public DynamoDBManager(Context context) {
        this.context = context;
        clientManager = new AmazonClientManager(context);
        ddbClient = clientManager.ddb();
        mapper = new DynamoDBMapper(ddbClient);
    }

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
                .withKeySchema(kseH, kseR)
                .withAttributeDefinitions(isbn, ownerID)
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
    public static Book getBook(String ISBN, String ownerID) {

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
    public void updateBook(Book book) {

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

        AmazonDynamoDBClient ddb = clientManager.ddb();

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

                Book book = mapBookAttributes(item);

                userBooks.add(book);
            }

            // If the response lastEvaluatedKey has contents,
            // that means there are more results
            lastEvaluatedKey = queryResult.getLastEvaluatedKey();

        } while (lastEvaluatedKey != null);

        return userBooks;
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

                Book book = mapBookAttributes(item);

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

    public ArrayList<Book> getNearbyBooks(float maxDistance) {

        ArrayList<Book> nearbyBooks = new ArrayList<>();
        final int NUM_BOOKS = 30, BOOKS_PER_USER = 5;

        // retrieve user's location
        ManageUser manageUser = new ManageUser(context);
        String ownerID = manageUser.getUser().getUserID();
        Location myLoc = new Location(LocationManager.GPS_PROVIDER);
        myLoc.setLatitude(manageUser.getUser().getLatitude());
        myLoc.setLongitude(manageUser.getUser().getLongitude());

        ArrayList<User> BSUsers = getAllUsersExceptMe();

        float[] usersLocations = new float[BSUsers.size()];
        String[] usersIDs = new String[BSUsers.size()];

        int i = 0;
        for (User user : BSUsers) {

            Location userLoc = new Location(LocationManager.GPS_PROVIDER);
            userLoc.setLatitude(user.getLatitude());
            userLoc.setLongitude(user.getLongitude());

            if (userLoc.distanceTo(myLoc) < maxDistance) {

                usersIDs[i] = user.getUserID();
                usersLocations[i] = userLoc.distanceTo(myLoc);
                i++;
            }
        }

        // order users based on distance
        for (int j = 0; j < i; j++) {

            for (int k = j; k < i; k++) {

                if (usersLocations[j] > usersLocations[k]) {

                    float tmpLoc;
                    String tmpID;

                    tmpID = usersIDs[j];
                    tmpLoc = usersLocations[j];

                    usersLocations[j] = usersLocations[k];
                    usersIDs[j] = usersIDs[k];

                    usersIDs[k] = tmpID;
                    usersLocations[k] = tmpLoc;

                }
            }
        }


        for (int k = 0; k < i; k++) {

            if (nearbyBooks.size() <= NUM_BOOKS) {

                List<Book> books = getBooks(usersIDs[k]);
                nearbyBooks.addAll(books.subList(0, BOOKS_PER_USER));

            } else {

                k = i;
            }
        }

        return nearbyBooks;

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
                if (Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE).matcher(book.getTitle()).find()
                        && !book.getOwnerID().equals(PreferenceManager.getDefaultSharedPreferences(context).getString("ID", null))
                        && book.getReceiverID()==null) {
                    resultList.add(book);
                }
            }

            return resultList;

        } catch (AmazonServiceException ex) {
            clientManager.wipeCredentialsOnAuthError(ex);
        }

        return null;
    }

    public ArrayList<Book> searchByTitle(String query) {

        ArrayList<Book> books = new ArrayList<>();

        AmazonDynamoDBClient ddb = clientManager.ddb();

        // Create our map of values
        Map keyConditions = new HashMap();

        Condition hashKeyCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue().withS(query));
        keyConditions.put("Title", hashKeyCondition);

        Map lastEvaluatedKey = null;
        do {
            QueryRequest queryRequest = new QueryRequest()
                    .withTableName(Constants.BOOK_TABLE_NAME)
                    .withKeyConditions(keyConditions)
                    .withExclusiveStartKey(lastEvaluatedKey)
                    .withIndexName("Title-index");

            QueryResult queryResult = ddb.query(queryRequest);

            for (Map item : queryResult.getItems()) {

                Book book = mapBookAttributes(item);

                books.add(book);
            }

            // If the response lastEvaluatedKey has contents,
            // that means there are more results
            lastEvaluatedKey = queryResult.getLastEvaluatedKey();

        } while (lastEvaluatedKey != null);

        return books;

    }

    public ArrayList<Book> searchByAuthor(String query) {
        ArrayList<Book> books = new ArrayList<>();

        AmazonDynamoDBClient ddb = clientManager.ddb();

        // Create our map of values
        Map keyConditions = new HashMap();

        Condition hashKeyCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue().withS(query));
        keyConditions.put("Author", hashKeyCondition);

        Map lastEvaluatedKey = null;
        do {
            QueryRequest queryRequest = new QueryRequest()
                    .withTableName(Constants.BOOK_TABLE_NAME)
                    .withKeyConditions(keyConditions)
                    .withExclusiveStartKey(lastEvaluatedKey)
                    .withIndexName("Author-index");

            QueryResult queryResult = ddb.query(queryRequest);

            for (Map item : queryResult.getItems()) {

                Book book = mapBookAttributes(item);

                books.add(book);
            }

            // If the response lastEvaluatedKey has contents,
            // that means there are more results
            lastEvaluatedKey = queryResult.getLastEvaluatedKey();

        } while (lastEvaluatedKey != null);

        return books;
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
     * Updates one attribute/value pair for the specified user.
     */
    public void updateUser(User user) {

        AmazonDynamoDBClient ddb = clientManager.ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            mapper.save(user);

        } catch (AmazonServiceException ex) {
            clientManager.wipeCredentialsOnAuthError(ex);
        }
    }

    public void updateUserByID(String ID,int credits) {

        AmazonDynamoDBClient ddb = clientManager.ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {

            User user = mapper.load(User.class, ID);
            user.setCredits(user.getCredits()-credits);
            mapper.save(user);

        } catch (AmazonServiceException ex) {
            clientManager.wipeCredentialsOnAuthError(ex);
        }
    }

    public void confirmExchange(BookRequest bookRequest,Book book,User user, User user2){

        AmazonDynamoDBClient ddb = clientManager.ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {

            mapper.save(bookRequest);
            mapper.save(book);
            mapper.save(user);
            mapper.save(user2);

        } catch (AmazonServiceException ex) {
            clientManager.wipeCredentialsOnAuthError(ex);
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

    public ArrayList<User> getAllUsersExceptMe() {

        ArrayList<User> allUsers = new ArrayList<>();
        String myID = new ManageUser(context).getUser().getUserID();

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        PaginatedScanList<User> result = mapper.scan(User.class, scanExpression);

        Iterator allUsersIterator = result.iterator();
        while (allUsersIterator.hasNext()) {

            User nextUser = (User) allUsersIterator.next();

            if (!nextUser.getUserID().equals(myID))
                allUsers.add(nextUser);
        }

        return allUsers;
    }


    /*                                                          BOOKREQUESTS
        Insert new BookRequest in dynamo
     */
    public void insertBookRequest(BookRequest bookRequest) {
        AmazonDynamoDBClient ddb = clientManager.ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            mapper.save(bookRequest);
            Log.d(TAG, "Request inserted");

        } catch (AmazonServiceException ex) {
            Log.e(TAG, "Error inserting request");
            clientManager.wipeCredentialsOnAuthError(ex);
        }
    }

    /*
     * Deletes the specified bookRequest and all of its attribute/value pairs.
     */
    public static void deleteBookRequest(BookRequest bookRequest) {

        AmazonDynamoDBClient ddb = clientManager.ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            mapper.delete(bookRequest);

        } catch (AmazonServiceException ex) {
            clientManager
                    .wipeCredentialsOnAuthError(ex);
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
                .withAttributeValueList(new AttributeValue().withS(PreferenceManager.getDefaultSharedPreferences(context).getString("ID", null)));
        keyConditions.put("AskerID", hashKeyCondition);

        Map lastEvaluatedKey = null;
        do {
            QueryRequest queryRequest = new QueryRequest()
                    .withTableName(Constants.BOOKREQUEST_TABLE_NAME)
                    .withKeyConditions(keyConditions)
                    .withExclusiveStartKey(lastEvaluatedKey);

            QueryResult queryResult = ddb.query(queryRequest);

            for (Map item : queryResult.getItems()) {

                userRequests.add(mapBookRequestAttributes(item));
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
                .withAttributeValueList(new AttributeValue().withS(PreferenceManager.getDefaultSharedPreferences(context).getString("ID", null)));
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

                userReceivedRequests.add(mapBookRequestAttributes(item));
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


    public static Book mapBookAttributes(Map item) {

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
            attribute = (AttributeValue) item.get("receiverID");
            book.setReceiverID(attribute.getS());

        } catch (NullPointerException e) {

            book.setReceiverID("");
        }

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

    public static BookRequest mapBookRequestAttributes(Map item) {

        BookRequest bookRequest = new BookRequest();

        AttributeValue attribute = (AttributeValue) item.get("AskerID");
        bookRequest.setAskerID(attribute.getS());

        attribute = (AttributeValue) item.get("ReceiverID");
        bookRequest.setReceiverID(attribute.getS());

        attribute = (AttributeValue) item.get("BookISBN");
        bookRequest.setBookISBN(attribute.getS());

        attribute = (AttributeValue) item.get("ID");
        bookRequest.setID(Integer.parseInt(attribute.getN()));

        attribute = (AttributeValue) item.get("Accepted");
        bookRequest.setAccepted(Integer.parseInt(attribute.getN()));

        attribute = (AttributeValue) item.get("Time");
        bookRequest.setTime(attribute.getS());

        return bookRequest;
    }

}

