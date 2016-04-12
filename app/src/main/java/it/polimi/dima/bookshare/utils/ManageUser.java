package it.polimi.dima.bookshare.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import it.polimi.dima.bookshare.amazon.DynamoDBManager;
import it.polimi.dima.bookshare.tables.User;

public class ManageUser {

    private SharedPreferences sp;
    private DynamoDBManager DDBM;

    public ManageUser(Context context) {

        this.DDBM = new DynamoDBManager(context);
        this.sp = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void saveUser(User user) {

        sp.edit().putString("ID", user.getUserID()).apply();
        sp.edit().putString("user_name", user.getName()).apply();
        sp.edit().putString("user_surname", user.getSurname()).apply();
        sp.edit().putString("user_city", user.getCity()).apply();
        sp.edit().putString("user_country", user.getCountry()).apply();
        sp.edit().putString("user_lat", user.getLatitude() + "").apply();
        sp.edit().putString("user_lon", user.getLongitude() + "").apply();
        sp.edit().putString("user_img", user.getImgURL()).apply();
        sp.edit().putInt("user_credits", user.getCredits()).apply();
        sp.edit().putString("user_arn", user.getArn()).apply();
    }

    public User getUser() {

        User user = new User();

        if (sp.getString("ID", null) == null) {
            return null;
        }

        try {

            user.setUserID(sp.getString("ID", null));
            user.setName(sp.getString("user_name", null));
            user.setSurname(sp.getString("user_surname", null));
            user.setCity(sp.getString("user_city", null));
            user.setCountry(sp.getString("user_country", null));
            user.setLatitude(Double.parseDouble(sp.getString("user_lat", null)));
            user.setLongitude(Double.parseDouble(sp.getString("user_lon", null)));
            user.setImgURL(sp.getString("user_img", null));
            user.setCredits(sp.getInt("user_credits", 0));
            user.setArn(sp.getString("user_arn",null));

        } catch (NullPointerException e) {
            return null;
        }

        return user;
    }

    public boolean verifyRegistered() {

        try {

            return sp.getBoolean("registered", false);

        } catch (NullPointerException e) {
            e.printStackTrace();

            return false;
        }
    }

    public void setRegistered(Boolean registered) {

        sp.edit().putBoolean("registered", registered).apply();
    }

    public void updateLoc(String location) {

        sp.edit().putString("location", location).apply();
    }

    public int refreshBookCount(String userID) {

        int booksCount = 0;

        try {

            booksCount = DDBM.getBooksCount(userID);
            sp.edit().putInt("ownedBooks_count", booksCount).apply();

        } catch (Exception e) {
            return 0;
        }

        return booksCount;
    }

    public int getBooksCount() {

        try {

            return sp.getInt("ownedBooks_count", 0);

        } catch (NullPointerException e) {
            return 0;
        }

    }

    public void updateBooksCount(boolean increment) {

        if (increment) {

            sp.edit().putInt("ownedBooks_count", getBooksCount() + 1).apply();

        } else {

            sp.edit().putInt("ownedBooks_count", getBooksCount() - 1).apply();
        }
    }
}
