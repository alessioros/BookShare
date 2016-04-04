package it.polimi.dima.bookshare.utils;

/**
 * Created by alessiorossotti on 04/04/16.
 */

public class AmazonISBNLookup {

    String ISBN = "";
    String AWS_ACCESSKEY_ID = "[Your_AWSAccessKeyID]";
    String ASSOCIATE_TAG = "";
    String REQUEST_SIGNATURE = "";
    String TIME_STAMP = "[YYYY-MM-DDThh:mm:ssZ]";

    public AmazonISBNLookup() {

        String request = "http://webservices.amazon.com/onca/xml?" +
                "Service=AWSECommerceService" +
                "&Operation=ItemLookup" +
                "&ResponseGroup=Large" +
                "&SearchIndex=Books" +
                "&IdType=ISBN" +
                "&ItemId=" + ISBN +
                "&AWSAccessKeyId=" + AWS_ACCESSKEY_ID +
                "&AssociateTag=" + ASSOCIATE_TAG +
                "&Timestamp=" + TIME_STAMP +
                "&Signature=" + REQUEST_SIGNATURE;

        /*
        import com.ECS.client.jax.AWSECommerceService;
        import com.ECS.client.jax.AWSECommerceServicePortType;
        import com.ECS.client.jax.ItemLookup;
        import com.ECS.client.jax.ItemLookupResponse;
        import com.ECS.client.jax.ItemLookupRequest;

        compile 'de.malkusch.amazon.product-advertising-api:amazon-ecs-stub:1.0.1'

        AWSECommerceService service = new AWSECommerceService();
        AWSECommerceServicePortType port = service.getAWSECommerceServicePort();

        ItemLookupRequest itemLookup = new ItemLookupRequest();
        itemLookup.setIdType("ISBN");
        itemLookup.getItemId().add(ISBN);

        ItemLookup lookup = new ItemLookup();
        lookup.setAWSAccessKeyId(AWS_ACCESSKEY_ID); // important
        lookup.setMarketplaceDomain("");
        lookup.getRequest().add(itemLookup);


        ItemLookupResponse response = port.itemLookup("",AWS_ACCESSKEY_ID,ASSOCIATE_TAG,"","",itemLookup,itemLookup,lookup);

        String r = response.toString();
        System.out.println("response: " + r);
        System.out.println("API Test stopped");
        */
    }

}
