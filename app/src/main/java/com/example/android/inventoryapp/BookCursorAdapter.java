package com.example.android.inventoryapp;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.BookContract.BookEntry;
import com.example.android.inventoryapp.data.BookDbHelper;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;


public class BookCursorAdapter extends CursorAdapter{

    public static final String LOG_TAG = BookCursorAdapter.class.getSimpleName();
    /**
     * Constructs a new {@link BookCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the book data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current book can be set on the title TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        TextView titleTextView = view.findViewById(R.id.book_title);
        TextView categoryTextView = view.findViewById(R.id.book_category);
        TextView priceTextView = view.findViewById(R.id.book_price);
        final TextView quantityTextView = view.findViewById(R.id.book_quantity);
        ImageView imageView = view.findViewById(R.id.sale_button);

        // Find the columns of book attributes that we're interested in
        int titleColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_TITLE);
        int categoryColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_CATEGORY);
        int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
        final int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);
        int rowIndex = cursor.getColumnIndex(BookEntry._ID);

        // Read the book attributes from the Cursor for the current book
        String bookTitle = cursor.getString(titleColumnIndex);
        String category = cursor.getString(categoryColumnIndex);
        double price = cursor.getDouble(priceColumnIndex);
        final String quantity = cursor.getString(quantityColumnIndex);
        final int rowId = cursor.getInt(rowIndex);

        imageView.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {

                BookDbHelper dbHelper = new BookDbHelper(context);
                SQLiteDatabase database = dbHelper.getWritableDatabase();

                int quantity = Integer.parseInt(quantityTextView.getText().toString());
                quantity = quantity - 1;

                ContentValues values = new ContentValues();
                values.put(BookEntry.COLUMN_BOOK_QUANTITY, quantity);
                String selection = BookEntry._ID + "=?";
                String[] selectionArgs = new String[] {String.valueOf(rowId)};
                if (quantity == -1) {
                    Toast.makeText(context, "No Stock Left ", Toast.LENGTH_SHORT).show();
                } else {
                    int rowsAffected = database.update(BookEntry.TABLE_NAME, values, selection, selectionArgs);
                    quantityTextView.setText(Integer.toString(quantity));
                }
            }
        });

        // Format the price number to have two digits to the right of the decimal point
        // so for example it will show '8.10' instead of '8'.
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMinimumFractionDigits(2);
        String priceFormatted = numberFormat.format(price);
        String currency = context.getString(R.string.price_currency) + priceFormatted;

        // Update the TextViews with the attributes for the current book
        titleTextView.setText(bookTitle);
        categoryTextView.setText(category);
        priceTextView.setText(currency);
        quantityTextView.setText(quantity);
    }
}
