package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class BookContract {

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";
    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.inventoryapp/books/ is a valid path for
     * looking at book data. content://com.example.android.inventoryapp/whatever/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "whatever".
     */
    public static final String PATH_BOOKS = "books";
    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private BookContract() {
    }

    /**
     * Inner class that defines constant values for the books database table.
     * Each entry in the table represents a single book.
     */
    public static class BookEntry implements BaseColumns {

        // The content URI to access the book data in the provider.
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKS);

        // Name of database table for books.
        public final static String TABLE_NAME = "books";

        // Unique ID number for the book (only for use in the database table).
        public final static String _ID = BaseColumns._ID;


        // Name of the book.
        public final static String COLUMN_BOOK_TITLE = "title";

        // Price of the book.
        public static final String COLUMN_BOOK_PRICE = "price";

        // ISBN code of the book.
        public static final String COLUMN_BOOK_ISBN = "isbn";

        // Quantity of the book.
        public static final String COLUMN_BOOK_QUANTITY = "quantity";

        // Category of the book.
        public static final String COLUMN_BOOK_CATEGORY = "category";

        // Supplier's Name of the book.
        public static final String COLUMN_BOOK_SUPPLIER_NAME = "supplierName";

        // Supplier's Phone number of the book.
        public static final String COLUMN_BOOK_SUPPLIER_NUMBER = "supplierNumber";

        // Possible values for the Category of the book.
        public static final String CATEGORY_UNKNOWN = "Un-Categorized";
        public static final String CATEGORY_FICTION = "Fiction";
        public static final String CATEGORY_CHILDREN_BOOKS = "Children's Books";
        public static final String CATEGORY_CRIME_THRILLER_MYSTERY = "Crime/Thriller/Mystery";
        public static final String CATEGORY_BIOGRAPHY = "Biography";
        public static final String CATEGORY_HISTORY = "History";
        public static final String CATEGORY_SCIENCE_FICTION = "Science Fiction";
        public static final String CATEGORY_HEALTH_FAMILY_LIFESTYLE = "Health/Family/Lifestyle";
        public static final String CATEGORY_FOOD_DRINK = "Food/Drink";
        public static final String CATEGORY_SCHOOL_BOOKS = "School Books";

        //The MIME type of the {@link #CONTENT_URI} for a list of books.
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        //The MIME type of the {@link #CONTENT_URI} for a single book.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        public static String doubleToStringNoDecimal(double d) {
            DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.UK);
            formatter.applyPattern("#,###");
            return formatter.format(d);
        }
    }
}












