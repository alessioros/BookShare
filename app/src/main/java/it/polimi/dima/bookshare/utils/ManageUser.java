package it.polimi.dima.bookshare.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import it.polimi.dima.bookshare.tables.User;

/**
 * Created by alessiorossotti on 06/04/16.
 */
public class ManageUser {

    private SharedPreferences sp;

    public ManageUser(Context context) {

        this.sp = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void saveUser(User user) {

        sp.edit().putString("ID", user.getUserID()).apply();
        sp.edit().putString("user_name", user.getName()).apply();
        sp.edit().putString("user_surname", user.getSurname()).apply();
        sp.edit().putString("user_city", user.getCity()).apply();
        sp.edit().putString("user_country", user.getCountry()).apply();
        sp.edit().putString("user_img", user.getImgURL()).apply();
        sp.edit().putInt("user_credits", user.getCredits()).apply();
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
            user.setImgURL(sp.getString("user_img", null));
            user.setCredits(sp.getInt("user_credits", 0));

        } catch (NullPointerException e) {
            return null;
        }

        return user;
    }
}
