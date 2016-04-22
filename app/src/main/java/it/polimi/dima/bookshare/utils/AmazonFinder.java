package it.polimi.dima.bookshare.utils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import it.polimi.dima.bookshare.tables.Book;

public class AmazonFinder {

    private String AGILE_URL = "http://theagiledirector.com/getRest_v3.php?isbn=";
    private String KEY_LARGE_IMAGE = "LargeImage";
    private String KEY_URL = "URL";
    private String KEY_AUTHOR = "Author";
    private String KEY_TITLE = "Title";
    private String KEY_PUBLISHER = "Publisher";
    private String KEY_PUBL_DATE = "PublicationDate";
    private String KEY_FEATURE = "Feature";
    private String KEY_EAN = "EAN";
    private String text;

    public Book getBook(String ISBN) {

        Book amazonBook = new Book();
        boolean firstImage = false, alreadySetted = false;

        try {

            URL url = new URL(AGILE_URL + ISBN);

            XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
            XmlPullParser xmlParser = xmlFactoryObject.newPullParser();
            xmlParser.setInput(url.openConnection().getInputStream(), null);

            int event = xmlParser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {

                String name = xmlParser.getName();

                switch (event) {
                    case XmlPullParser.START_TAG:
                        //System.out.print("\n<"+name+">");
                        if (name.equals(KEY_LARGE_IMAGE) && !alreadySetted) {

                            firstImage = true;
                            alreadySetted = true;
                        }
                        break;

                    case XmlPullParser.TEXT:
                        //System.out.print(text);
                        text = xmlParser.getText();
                        break;

                    case XmlPullParser.END_TAG:
                        //System.out.print("<"+name+"\n>");

                        if (name.equals(KEY_URL) && firstImage) {

                            amazonBook.setImgURL(text);
                            firstImage = false;

                        } else if (name.equals(KEY_AUTHOR)) {

                            amazonBook.setAuthor(text);

                        } else if (name.equals(KEY_PUBLISHER)) {

                            amazonBook.setPublisher(text);

                        } else if (name.equals(KEY_PUBL_DATE)) {

                            amazonBook.setPublishedDate(text);

                        } else if (name.equals(KEY_FEATURE)) {

                            if (text.contains("Pagine:")) {

                                try {

                                    String pages = text.replace("Pagine:", "");
                                    pages = pages.replace(" ", "");
                                    amazonBook.setPageCount(Integer.parseInt(pages));

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else if (text.contains("Pages:")) {

                                try {

                                    String pages = text.replace("Pages:", "");
                                    pages = pages.replace(" ", "");
                                    amazonBook.setPageCount(Integer.parseInt(pages));

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        } else if (name.equals(KEY_TITLE)) {

                            amazonBook.setTitle(text);

                        } else if (name.equals(KEY_EAN)) {

                            amazonBook.setIsbn(text);
                        }

                        break;
                }
                event = xmlParser.next();
            }

        } catch (MalformedURLException murl) {
            return null;

        } catch (XmlPullParserException xec) {
            return null;

        } catch (IOException e) {
            return null;
        }

        try {
            if (amazonBook.getIsbn().length() > 0 && amazonBook.getTitle().length() > 0 && amazonBook.getAuthor().length() > 0)
                return amazonBook;

            return null;

        } catch (NullPointerException e) {
            return null;
        }

    }

}


