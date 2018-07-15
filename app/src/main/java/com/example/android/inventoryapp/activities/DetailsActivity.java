package com.example.android.inventoryapp.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.R;
import com.example.android.inventoryapp.data.BookContract.BookEntry;
import com.example.android.inventoryapp.data.BookDbHelper;

import static com.example.android.inventoryapp.data.BookContract.BookEntry.doubleToStringNoDecimal;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // Identifier for the book data loader
    private static final int EXISTING_BOOK_LOADER = 0;
    private int quantity;
    // Content URI for the existing book.
    private Uri currentBookUri;

    // The id for the selected book.
    private String rowId;

    // String for the supplier's Phone Number.
    private String supNumber;

    // TextViews that show the details of the selected book.
    private TextView titleDetails, priceDetails, isbnDetails, quantityDetails,
            categoryDetails, supNameDetails, supNumberDetails;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // get the data that was sent from the intent that launched this activity.
        Intent intent = getIntent();
        currentBookUri = intent.getData();

        // Initialize a loader to read the book data from the database
        // and display the current values.
        getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);

        // Find The Views in the hierarchy of the activity_details.xml
        titleDetails = findViewById(R.id.title_details);
        priceDetails = findViewById(R.id.price_details);
        isbnDetails = findViewById(R.id.isbn_details);
        quantityDetails = findViewById(R.id.quantity_details);
        categoryDetails = findViewById(R.id.category_details);
        supNameDetails = findViewById(R.id.supName_details);
        supNumberDetails = findViewById(R.id.supNumber_details);
        Button plusButtonDetails = findViewById(R.id.plus_button);
        Button minusButtonDetails = findViewById(R.id.minus_button);
        Button orderButtonDetails = findViewById(R.id.order_button);
        Button editButtonDetails = findViewById(R.id.edit_button_details);
        Button doneButtonDetails = findViewById(R.id.done_button_details);
        Button deleteButtonDetails = findViewById(R.id.delete_button_details);

        // Set up the click listener for the Plus Button.
        plusButtonDetails.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {

                BookDbHelper dbHelper = new BookDbHelper(getApplicationContext());
                final SQLiteDatabase database = dbHelper.getWritableDatabase();

                quantity += 1;

                ContentValues values = new ContentValues();
                values.put(BookEntry.COLUMN_BOOK_QUANTITY, quantity);
                String selection = BookEntry._ID + "=?";
                String[] selectionArgs = new String[]{String.valueOf(rowId)};
                database.update(BookEntry.TABLE_NAME, values, selection, selectionArgs);
                quantityDetails.setText(doubleToStringNoDecimal(quantity));
            }
        });

        // Set up the click listener for the Minus Button.
        minusButtonDetails.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {

                BookDbHelper dbHelper = new BookDbHelper(getApplicationContext());
                final SQLiteDatabase database = dbHelper.getWritableDatabase();

                quantity -= 1;

                ContentValues values = new ContentValues();
                values.put(BookEntry.COLUMN_BOOK_QUANTITY, quantity);

                String selection = BookEntry._ID + "=?";
                String[] selectionArgs = new String[]{String.valueOf(rowId)};
                if (quantity == -1) {
                    Toast.makeText(DetailsActivity.this, "No Stock Left ", Toast.LENGTH_SHORT).show();
                } else {
                    database.update(BookEntry.TABLE_NAME, values, selection, selectionArgs);
                }
                quantityDetails.setText(doubleToStringNoDecimal(quantity));
            }
        });

        // Set up the click listener for the Order Button.
        orderButtonDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Make an intent to open the Phone App and ring the supplier.
                Intent phoneIntent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", supNumber, null));
                startActivity(phoneIntent);
            }
        });

        // Set up the click listener for the Edit Button.
        // Opens the Edit Activity to edit the selected book.
        editButtonDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentEdit();
            }
        });

        // Set up the click listener for the Done Button.
        // Update the quantity and go back to Main Activity.
        doneButtonDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Set up the click listener for the Delete Button.
        // Delete the selected book from the books table.
        deleteButtonDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });
    }

    // This method is called when the Delete Button is clicked
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg_book);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteBook();
                onBackPressed();
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

    // This method is called to delete the selected book.
    private void deleteBook() {

        if (currentBookUri != null) {
            int rowsDeleted = getContentResolver().delete(currentBookUri, null, null);
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(DetailsActivity.this, getString(R.string.editor_delete_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(DetailsActivity.this, getString(R.string.editor_delete_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(DetailsActivity.this, "Klein", Toast.LENGTH_SHORT).show();
        }
    }

    // This method creates an intent to send the user the Edit Activity.
    private void intentEdit() {
        // Create new intentEdit to go to {@link EditActivity}
        Intent intent = new Intent(DetailsActivity.this, EditActivity.class);

        // Send the URI on the data field of the intentEdit.
        intent.setData(currentBookUri);

        // Launch the{@link EditActivity} to display the data for the current book.
        startActivity(intent);
    }

    // Method called when the back button is pressed.
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(DetailsActivity.this, MainActivity.class);
        startActivity(intent);
    }

    // This Method is called when the Up Button in the Action Bar is clicked.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all book attributes, define a projection that contains
        // all columns from the book table
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
        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                currentBookUri,         // Query the content URI for the current book
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (data == null || data.getCount() < 1) {
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (data.moveToFirst()) {
            // Find the columns of book attributes that we're interested in
            int titleColumnIndex = data.getColumnIndex(BookEntry.COLUMN_BOOK_TITLE);
            int priceColumnIndex = data.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
            int isbnColumnIndex = data.getColumnIndex(BookEntry.COLUMN_BOOK_ISBN);
            int quantityColumnIndex = data.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);
            int categoryColumnIndex = data.getColumnIndex(BookEntry.COLUMN_BOOK_CATEGORY);
            int supNameColumnIndex = data.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER_NAME);
            int supNumberColumnIndex = data.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER_NUMBER);
            int rowIndex = data.getColumnIndex(BookEntry._ID);

            // Extract out the value from the Cursor for the given column index
            String title = data.getString(titleColumnIndex);
            double price = data.getDouble(priceColumnIndex);
            long isbn = data.getLong(isbnColumnIndex);
            quantity = data.getInt(quantityColumnIndex);
            String category = data.getString(categoryColumnIndex);
            String supName = data.getString(supNameColumnIndex);
            supNumber = data.getString(supNumberColumnIndex);
            rowId = data.getString(rowIndex);

            // Update the views on the screen with the values from the database
            titleDetails.setText(title);
            priceDetails.setText(Double.toString(price));
            isbnDetails.setText(Long.toString(isbn));
            quantityDetails.setText(doubleToStringNoDecimal(quantity));
            categoryDetails.setText(category);
            supNameDetails.setText(supName);
            supNumberDetails.setText(supNumber);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
