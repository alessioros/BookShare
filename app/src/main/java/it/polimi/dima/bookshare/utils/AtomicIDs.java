package it.polimi.dima.bookshare.utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by matteo on 12/04/16.
 */
public class AtomicIDs {
    private final static AtomicInteger notificationID = new AtomicInteger(0);

    public static int getNotificationID(){
        return notificationID.incrementAndGet();
    }
}
