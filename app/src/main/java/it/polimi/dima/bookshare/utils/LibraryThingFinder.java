package it.polimi.dima.bookshare.utils;

import android.content.Context;

import com.squareup.picasso.Picasso;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.net.URL;

import it.polimi.dima.bookshare.tables.Book;

public class LibraryThingFinder {

    private Context context;
    private String BOOK_INFO = "http://www.librarything.com/services/rest/1.1/?method=librarything.ck.getwork&apikey=";
    private String DEV_KEY = "bc8ad7e68171c9d13936d8900fb32063";
    private String TYPE_ISBN = "&isbn=";

    private String BOOK_COVER = "http://covers.librarything.com/devkey/";
    private String TYPE_COVER_LARGE = "/large/isbn/";

    private String KEY_RESPONSE = "response";
    private String KEY_STAT = "stat";
    private String STATUS_FAIL = "fail";
    private String KEY_AUTHOR = "author";
    private String KEY_TITLE = "title";
    private String KEY_DESCRIPTION = "description";
    private String KEY_PUBLISHER = "originalpublicationdate";
    private String KEY_FACT = "fact";
    private String text;

    public LibraryThingFinder(Context context) {
        this.context = context;
    }

    public Book getBook(String ISBN) {

        Book LTbook = new Book();
        LTbook.setIsbn(ISBN);
        LTbook.setDescription("");
        LTbook.setPublishedDate("");

        try {

            URL url = new URL(BOOK_INFO + DEV_KEY + TYPE_ISBN + ISBN);

            System.out.println(url.toString());

            XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
            XmlPullParser xmlParser = xmlFactoryObject.newPullParser();
            xmlParser.setInput(url.openConnection().getInputStream(), null);

            int event = xmlParser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                String name = xmlParser.getName();
                String attValue = xmlParser.getAttributeValue(null, "name");
                if (attValue == null) attValue = "";
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if (name.equals(KEY_RESPONSE)) {

                            String stat = "";
                            try {
                                stat = xmlParser.getAttributeValue(null, KEY_STAT);
                            } catch (NullPointerException e) {
                            }

                            if (stat.equals(STATUS_FAIL))
                                return null;
                        }
                        if (attValue.equals(KEY_DESCRIPTION)) {

                            LTbook.setDescription("true");

                        } else if (attValue.equals(KEY_PUBLISHER)) {

                            LTbook.setPublishedDate("true");
                        }
                        break;

                    case XmlPullParser.TEXT:

                        text = xmlParser.getText();

                        break;

                    case XmlPullParser.END_TAG:
                        if (name.equals(KEY_AUTHOR)) {

                            LTbook.setAuthor(text);

                        } else if (name.equals(KEY_TITLE)) {

                            LTbook.setTitle(text);

                        } else if (name.equals(KEY_FACT)) {

                            if (LTbook.getDescription().equals("true")) {

                                text = text.replace("<![CDATA[", "");
                                text = text.replace("]]>", "");
                                LTbook.setDescription(text);

                            } else if (LTbook.getPublishedDate().equals("true")) {

                                LTbook.setPublishedDate(text);
                            }
                        }
                        break;
                }
                event = xmlParser.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        LTbook.setImgURL(getLTImgUrl(ISBN));

        return LTbook;
    }

    public String getLTImgUrl(String ISBN) {

        String path = BOOK_COVER + DEV_KEY + TYPE_COVER_LARGE + ISBN;

        try {
            if (Picasso.with(context).load(path).get().getHeight() < 5) return "";

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        return path;

    }
}
