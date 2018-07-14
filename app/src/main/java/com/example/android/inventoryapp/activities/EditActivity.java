package com.example.android.inventoryapp.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.LoaderManager;
import android.content.Loader;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.inventoryapp.R;
import com.example.android.inventoryapp.data.BookContract.BookEntry;

import java.util.Objects;

public class EditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // Identifier for the book data loader.
    private static final int EXISTING_BOOK_LOADER = 0;

    // EditText fields to enter the book's details.
    private EditText titleEditText, priceEditText, isbnEditText, quantityEditText,
            supplierNameEditText, supplierNumberEditText;

    // Content URI for the existing book (null if its a new book).
    private Uri currentBookUri;

    // EditText field to enter the book's category.
    private Spinner categorySpinner;

    // Boolean flag that keeps track of weather the book has been edited (true) or not (false).
    private boolean bookHasChanged = false;

    // Category of the Book. The possible values are in the BookContract.java file.
    private String category = BookEntry.CATEGORY_UNKNOWN;

    // OnTouchListener that listens for any user touches on a View, implying that they are modifying
    // the view, and we change the bookHasChanged boolean to true.
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            bookHasChanged = true;
            return false;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Button deleteButton = findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
            }
        });

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new book or editing an existing one.
        Intent intent = getIntent();
        currentBookUri = intent.getData();

        // If the intent DOES NOT contain a book content URI, then we know that we are
        // creating a new book.
        if (currentBookUri == null) {
            // This is a new book, so change the app bar to say "Add a Book"
            setTitle(getString(R.string.edit_activity_new_book));
            deleteButton.setVisibility(View.GONE);
        } else {
            // Otherwise this is an existing book, so change app bar to say "Edit Book"
            setTitle(getString(R.string.edit_fragment_edit_book));

            // Initialize a loader to read the book data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        titleEditText = findViewById(R.id.title);
        priceEditText = findViewById(R.id.price);
        isbnEditText = findViewById(R.id.isbn);
        quantityEditText = findViewById(R.id.quantity);
        supplierNameEditText = findViewById(R.id.sup_name);
        supplierNumberEditText = findViewById(R.id.sup_number);
        categorySpinner = findViewById(R.id.spinner_category);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        titleEditText.setOnTouchListener(touchListener);
        priceEditText.setOnTouchListener(touchListener);
        isbnEditText.setOnTouchListener(touchListener);
        quantityEditText.setOnTouchListener(touchListener);
        supplierNameEditText.setOnTouchListener(touchListener);
        supplierNumberEditText.setOnTouchListener(touchListener);
        categorySpinner.setOnTouchListener(touchListener);

        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBook();
            }
        });
        setUpSpinner();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void setUpSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter categorySpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_category_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        categorySpinner.setAdapter(categorySpinnerAdapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!selection.isEmpty()) {
                    if (selection.equals(getString(R.string.category_fiction))) {
                        category = BookEntry.CATEGORY_FICTION;
                    } else if (selection.equals(getString(R.string.category_children_books))) {
                        category = BookEntry.CATEGORY_CHILDREN_BOOKS;
                    } else if (selection.equals(getString(R.string.category_crime_thriller_mystery))) {
                        category = BookEntry.CATEGORY_CRIME_THRILLER_MYSTERY;
                    } else if (selection.equals(getString(R.string.category_biography))) {
                        category = BookEntry.CATEGORY_BIOGRAPHY;
                    } else if (selection.equals(getString(R.string.category_history))) {
                        category = BookEntry.CATEGORY_HISTORY;
                    } else if (selection.equals(getString(R.string.category_science_fiction))) {
                        category = BookEntry.CATEGORY_SCIENCE_FICTION;
                    } else if (selection.equals(getString(R.string.category_health_family_lifestyle))) {
                        category = BookEntry.CATEGORY_HEALTH_FAMILY_LIFESTYLE;
                    } else if (selection.equals(getString(R.string.category_food_drink))) {
                        category = BookEntry.CATEGORY_FOOD_DRINK;
                    } else if (selection.equals(getString(R.string.category_school_books))) {
                        category = BookEntry.CATEGORY_SCHOOL_BOOKS;
                    } else {
                        category = BookEntry.CATEGORY_UNKNOWN;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                category = BookEntry.CATEGORY_UNKNOWN;
            }
        });
    }

    // Get user input from editor and save book into database.
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void saveBook() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String titleString = titleEditText.getText().toString().trim();
        String priceString = priceEditText.getText().toString().trim();
        String isbnString = isbnEditText.getText().toString().trim();
        String quantityString = quantityEditText.getText().toString().trim();
        String supNameString = supplierNameEditText.getText().toString().trim();
        String supNumberString = supplierNumberEditText.getText().toString().trim();

        if (TextUtils.isEmpty(titleString) && TextUtils.isEmpty(priceString)
                && TextUtils.isEmpty(isbnString) && TextUtils.isEmpty(quantityString)
                && TextUtils.isEmpty(supNameString) && TextUtils.isEmpty(supNumberString)) {
            Toast.makeText(EditActivity.this, R.string.fill_in_the_fields, Toast.LENGTH_SHORT).show();
            return;
        } else if (!TextUtils.isEmpty(titleString) && !TextUtils.isEmpty(priceString)
                && !TextUtils.isEmpty(isbnString) && !TextUtils.isEmpty(quantityString)
                && !TextUtils.isEmpty(supNameString) && !TextUtils.isEmpty(supNumberString)) {
            finish();
        }

        if (TextUtils.isEmpty(titleString)) {
            Toast.makeText(EditActivity.this, R.string.empty_title,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(EditActivity.this, R.string.empty_price,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(isbnString)) {
            Toast.makeText(EditActivity.this, R.string.empty_isbn,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(quantityString)) {
            Toast.makeText(EditActivity.this, R.string.empty_quantity,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(supNameString)) {
            Toast.makeText(EditActivity.this, R.string.empty_sup_name,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(supNumberString)) {
            Toast.makeText(EditActivity.this, R.string.empty_sup_number,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and book attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_BOOK_TITLE, titleString);
        values.put(BookEntry.COLUMN_BOOK_PRICE, priceString);
        values.put(BookEntry.COLUMN_BOOK_ISBN, isbnString);
        values.put(BookEntry.COLUMN_BOOK_QUANTITY, quantityString);
        values.put(BookEntry.COLUMN_BOOK_CATEGORY, category);
        values.put(BookEntry.COLUMN_BOOK_SUPPLIER_NAME, supNameString);
        values.put(BookEntry.COLUMN_BOOK_SUPPLIER_NUMBER, supNumberString);

        // Determine if this is a new or existing book by checking if currentBookUri is null or not
        if (currentBookUri == null) {
            // This is a NEW book, so insert a new book into the provider,
            // returning the content URI for the new book.
            Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, R.string.editor_insert_book_failed,
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, R.string.editor_insert_book_successful,
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING book, so update the book with content URI: currentBookUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because currentBookUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = Objects.requireNonNull(getContentResolver()
                    .update(currentBookUri, values, null, null));

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, R.string.editor_update_book_failed,
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, R.string.editor_update_book_successful,
                        Toast.LENGTH_SHORT).show();
            }
        }
        intentMain();
    }

    // Perform the deletion of the book in the database.
    private void deleteBook() {
        // Only perform the delete if this is an existing book.
        if (currentBookUri != null) {
            // Call the ContentResolver to delete the book at the given content URI.
            // Pass in null for the selection and selection args because the currentBookUri
            // content URI already identifies the book that we want.
            int rowsDeleted = getContentResolver().delete(currentBookUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        intentMain();
    }

    // This method makes an intent that sends the user back to the Main Activity.
    private void intentMain() {
        Intent intent = new Intent(EditActivity.this, MainActivity.class);
        startActivity(intent);
    }

    // This method is called when the back button is pressed.
    @Override
    public void onBackPressed() {
        // If the book hasn't changed, continue with handling back button press
        if (!bookHasChanged) {
            intentMain();
        } else {

            // Otherwise if there are unsaved changes, setup a dialog to warn the user.
            // Create a click listener to handle the user confirming that changes should be discarded.
            DialogInterface.OnClickListener discardButtonClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // User clicked "Discard" button, close the current activity.
                            intentMain();
                        }
                    };

            // Show dialog that there are unsaved changes
            showUnsavedChangesDialog(discardButtonClickListener);
        }
    }

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

    // Show a dialog that warns the user there are unsaved changes that will be lost
    // if they continue leaving the editor.
    // discardButtonClickListener is the click listener for what to do when
    // the user confirms they want to discard their changes
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg_book);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the book.
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the book.
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

            // Extract out the value from the Cursor for the given column index
            String title = data.getString(titleColumnIndex);
            double price = data.getDouble(priceColumnIndex);
            long isbn = data.getLong(isbnColumnIndex);
            int quantity = data.getInt(quantityColumnIndex);
            String category = data.getString(categoryColumnIndex);
            String supName = data.getString(supNameColumnIndex);
            long supNumber = data.getLong(supNumberColumnIndex);

            // Update the views on the screen with the values from the database
            titleEditText.setText(title);
            priceEditText.setText(Double.toString(price));
            isbnEditText.setText(Long.toString(isbn));
            quantityEditText.setText(Integer.toString(quantity));
            switch (category) {
                case BookEntry.CATEGORY_FICTION:
                    categorySpinner.setSelection(1);
                    break;
                case BookEntry.CATEGORY_CHILDREN_BOOKS:
                    categorySpinner.setSelection(2);
                    break;
                case BookEntry.CATEGORY_CRIME_THRILLER_MYSTERY:
                    categorySpinner.setSelection(3);
                    break;
                case BookEntry.CATEGORY_BIOGRAPHY:
                    categorySpinner.setSelection(4);
                    break;
                case BookEntry.CATEGORY_HISTORY:
                    categorySpinner.setSelection(5);
                    break;
                case BookEntry.CATEGORY_SCIENCE_FICTION:
                    categorySpinner.setSelection(6);
                    break;
                case BookEntry.CATEGORY_HEALTH_FAMILY_LIFESTYLE:
                    categorySpinner.setSelection(7);
                    break;
                case BookEntry.CATEGORY_FOOD_DRINK:
                    categorySpinner.setSelection(8);
                    break;
                case BookEntry.CATEGORY_SCHOOL_BOOKS:
                    categorySpinner.setSelection(9);
                    break;
                default:
                    categorySpinner.setSelection(0);
                    break;
            }
            supplierNameEditText.setText(supName);
            supplierNumberEditText.setText(Long.toString(supNumber));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        titleEditText.setText("");
        priceEditText.setText("");
        isbnEditText.setText("");
        quantityEditText.setText("");
        categorySpinner.setSelection(0);
        supplierNameEditText.setText("");
        supplierNumberEditText.setText("");
    }
}