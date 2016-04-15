package it.polimi.dima.bookshare.utils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.net.URL;

import it.polimi.dima.bookshare.tables.Book;

public class LibraryThingFinder {

    private String BOOK_INFO = "http://www.librarything.com/services/rest/1.1/?method=librarything.ck.getwork&apikey=";
    private String DEV_KEY = "bc8ad7e68171c9d13936d8900fb32063";
    private String TYPE_ISBN = "&isbn=";

    private String BOOK_COVER = "http://covers.librarything.com/devkey/";
    private String TYPE_COVER_LARGE = "/large/isbn/";

    private String KEY_ITEM = "response";
    private String KEY_AUTHOR = "author";
    private String KEY_TITLE = "title";
    private String KEY_DESCRIPTION = "description";

    public Book getBook(String ISBN) {

        Book LTbook = new Book();
        LTbook.setIsbn(ISBN);

        try {

            URL url = new URL(BOOK_INFO + DEV_KEY + TYPE_ISBN + ISBN);

            XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
            XmlPullParser xmlParser = xmlFactoryObject.newPullParser();
            xmlParser.setInput(url.openStream(), null);


            int event = xmlParser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                String name = xmlParser.getName();
                switch (event) {
                    case XmlPullParser.START_TAG:
                        break;

                    case XmlPullParser.END_TAG:
                        if (name.equals(KEY_AUTHOR)) {

                            LTbook.setAuthor(xmlParser.getText());
                        }
                        if (name.equals(KEY_TITLE)) {

                            LTbook.setTitle(xmlParser.getText());
                        }
                        /*if(name.equals(KEY_DESCRIPTION)){

                            LTbook.setDescription(xmlParser.getAttributeValue(null,"value"));
                            xmlParser.getAttributeValue()
                        }*/
                        break;
                }
                event = xmlParser.next();
            }

        } catch (Exception e) {


        }


        return LTbook;
    }


}
