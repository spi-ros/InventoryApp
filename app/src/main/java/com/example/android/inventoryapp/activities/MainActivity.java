package com.example.android.inventoryapp.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.LoaderManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.BookCursorAdapter;
import com.example.android.inventoryapp.R;

import com.example.android.inventoryapp.data.BookContract.BookEntry;
import com.example.android.inventoryapp.data.BookDbHelper;

import java.text.NumberFormat;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int BOOK_LOADER = 0;

    BookCursorAdapter bookCursorAdapter;

    View emptyView;

    ListView bookListView;

    TextView titleDetails, priceDetails,
            isbnDetails, quantityDetails,
            categoryDetails, supNameDetails,
            supNumberDetails;

    Button plusButtonDetails, minusButtonDetails, orderButtonDetails,
            editButtonDetails, doneButtonDetails, deleteButtonDetails;

    FloatingActionButton fab;

    RelativeLayout backgroundLayout;

    ScrollView hiddenScrollView;

    // Content URI for the existing book (null if its a new book).
    private Uri currentBookUri;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open EditorActivity
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView object in the view hierarchy of the Activity.
        bookListView = findViewById(R.id.list_view);

        // Find the ScrollView object in the view hierarchy of the Activity.
        hiddenScrollView = findViewById(R.id.hidden_scroll_view);

        // Find The Views in the hierarchy of the activity_main.xml
        titleDetails = findViewById(R.id.title_details);
        priceDetails = findViewById(R.id.price_details);
        isbnDetails = findViewById(R.id.isbn_details);
        quantityDetails = findViewById(R.id.quantity_details);
        categoryDetails = findViewById(R.id.category_details);
        supNameDetails = findViewById(R.id.supName_details);
        supNumberDetails = findViewById(R.id.supNumber_details);
        backgroundLayout = findViewById(R.id.background_relative);
        plusButtonDetails = findViewById(R.id.plus_button);
        minusButtonDetails = findViewById(R.id.minus_button);
        orderButtonDetails = findViewById(R.id.order_button);
        editButtonDetails = findViewById(R.id.edit_button_details);
        doneButtonDetails = findViewById(R.id.done_button_details);
        deleteButtonDetails = findViewById(R.id.delete_button_details);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        emptyView = findViewById(R.id.empty_view);
        bookListView.setEmptyView(emptyView);

        // Create a BookCursorAdapter.
        bookCursorAdapter = new BookCursorAdapter(this, null);

        // Make the ListView use the BookCursorAdapter created above, so that the
        // ListView will display list items.
        bookListView.setAdapter(bookCursorAdapter);

        // Setup the item click listener.
        bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onItemClick(final AdapterView<?> parent, View view, int position, final long id) {

                // Form the content URI that represents the specific book that was clicked on,
                // by appending the"id" (passed as input to this method) onto the
                // {@link BookEntry#CONTENT_URI}.
                // FOr example, the URI would be "content://"com.example.android.inventoryapp/books/2"
                // If the set with ID 2 was clicked on.
                currentBookUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, id);

                hideListView();

                // Find the columns of book attributes that we're interested in
                int titleColumnIndex = bookCursorAdapter.getCursor().getColumnIndex(BookEntry.COLUMN_BOOK_TITLE);
                int priceColumnIndex = bookCursorAdapter.getCursor().getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
                int isbnColumnIndex = bookCursorAdapter.getCursor().getColumnIndex(BookEntry.COLUMN_BOOK_ISBN);
                final int quantityColumnIndex = bookCursorAdapter.getCursor().getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);
                int categoryColumnIndex = bookCursorAdapter.getCursor().getColumnIndex(BookEntry.COLUMN_BOOK_CATEGORY);
                int supNameColumnIndex = bookCursorAdapter.getCursor().getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER_NAME);
                int supNumberColumnIndex = bookCursorAdapter.getCursor().getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER_NUMBER);
                int rowIndex = bookCursorAdapter.getCursor().getColumnIndex(BookEntry._ID);

                // Read the book attributes from the Cursor for the current book
                String title = bookCursorAdapter.getCursor().getString(titleColumnIndex);
                double price = bookCursorAdapter.getCursor().getDouble(priceColumnIndex);
                String isbn = bookCursorAdapter.getCursor().getString(isbnColumnIndex);
                final String quantity = bookCursorAdapter.getCursor().getString(quantityColumnIndex);
                String category = bookCursorAdapter.getCursor().getString(categoryColumnIndex);
                final String rowId = bookCursorAdapter.getCursor().getString(rowIndex);
                String supName = bookCursorAdapter.getCursor().getString(supNameColumnIndex);
                final String supNumber = bookCursorAdapter.getCursor().getString(supNumberColumnIndex);

                // Format the price number to have two digits to the right of the decimal point
                // so for example it will show '8.10' instead of '8'.
                NumberFormat numberFormat = NumberFormat.getInstance();
                numberFormat.setMinimumFractionDigits(2);
                String priceFormatted = numberFormat.format(price);
                String currency = getString(R.string.price_currency) + priceFormatted;

                titleDetails.setText(title);
                priceDetails.setText(currency);
                isbnDetails.setText(isbn);
                quantityDetails.setText(quantity);
                categoryDetails.setText(category);
                supNameDetails.setText(supName);
                supNumberDetails.setText(supNumber);

                BookDbHelper dbHelper = new BookDbHelper(getApplicationContext());
                final SQLiteDatabase database = dbHelper.getWritableDatabase();
                final ContentValues values = new ContentValues();

                plusButtonDetails.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(View v) {

                        int quantityUpdate = Integer.parseInt(quantityDetails.getText().toString());

                        quantityUpdate = quantityUpdate + 1;

                        values.put(BookEntry.COLUMN_BOOK_QUANTITY, quantityUpdate);
                        String selection = BookEntry._ID + "=?";
                        String[] selectionArgs = new String[]{String.valueOf(rowId)};
                        int rowsAffected = database.update(BookEntry.TABLE_NAME, values, selection, selectionArgs);
                        quantityDetails.setText(Integer.toString(quantityUpdate));
                    }
                });

                minusButtonDetails.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(View v) {

                        int quantityUpdate = Integer.parseInt(quantityDetails.getText().toString());

                        quantityUpdate = quantityUpdate - 1;

                        values.put(BookEntry.COLUMN_BOOK_QUANTITY, quantityUpdate);

                        String selection = BookEntry._ID + "=?";
                        String[] selectionArgs = new String[]{String.valueOf(rowId)};
                        if (quantityUpdate == -1) {
                            Toast.makeText(MainActivity.this, "No Stock Left ", Toast.LENGTH_SHORT).show();
                        } else {
                            int rowsAffected = database.update(BookEntry.TABLE_NAME, values, selection, selectionArgs);
                            quantityDetails.setText(Integer.toString(quantityUpdate));
                        }
                    }
                });

                orderButtonDetails.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent phoneIntent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", supNumber, null));
                        startActivity(phoneIntent);
                    }
                });

                editButtonDetails.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showListView();
                        intentEdit();
                    }
                });

                doneButtonDetails.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showListView();
                        getLoaderManager().restartLoader(BOOK_LOADER, null, MainActivity.this);
                    }
                });

                deleteButtonDetails.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDeleteConfirmationDialog();
                        showListView();
                    }
                });
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(BOOK_LOADER, null, this);
    }

    private void showListView() {
        hiddenScrollView.setVisibility(View.GONE);
        backgroundLayout.setVisibility(View.GONE);
        fab.setVisibility(View.VISIBLE);
        setTitle(R.string.inventory_app);
    }

    private void hideListView() {
        hiddenScrollView.setVisibility(View.VISIBLE);
        hiddenScrollView.setClickable(true);
        backgroundLayout.setVisibility(View.VISIBLE);
        backgroundLayout.setClickable(true);
        fab.setVisibility(View.GONE);
        setTitle(getString(R.string.book_details));
    }

    private void intentEdit() {
        // Create new intentEdit to go to {@link EditActivity}
        Intent intent = new Intent(MainActivity.this, EditActivity.class);

        // Send the URI on the data field of the intentEdit.
        intent.setData(currentBookUri);

        // Launch the{@link EditActivity} to display the data for the current book.
        startActivity(intent);
    }

    private void deleteBook() {

        if (currentBookUri != null) {
            int rowsDeleted = getContentResolver().delete(currentBookUri, null, null);
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(MainActivity.this, getString(R.string.editor_delete_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(MainActivity.this, getString(R.string.editor_delete_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "Klein", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteAllBooks() {
        int rowsDeleted = getContentResolver().delete(BookEntry.CONTENT_URI, null, null);
        if (rowsDeleted == 0) {
            // If no rows were deleted, then there was an error with the delete.
            Toast.makeText(this, getString(R.string.delete_books_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the delete was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.delete_books_successful),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_main_activity.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg_all);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteAllBooks();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg_book);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_BOOK_TITLE,
                BookEntry.COLUMN_BOOK_PRICE,
                BookEntry.COLUMN_BOOK_ISBN,
                BookEntry.COLUMN_BOOK_QUANTITY,
                BookEntry.COLUMN_BOOK_CATEGORY,
                BookEntry.COLUMN_BOOK_SUPPLIER_NAME,
                BookEntry.COLUMN_BOOK_SUPPLIER_NUMBER
        };
        return new CursorLoader(this,
                BookEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link BookCursorAdapter} with this new cursor containing updated data
        bookCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback call when the data needs to be deleted
        bookCursorAdapter.swapCursor(null);
    }
}
