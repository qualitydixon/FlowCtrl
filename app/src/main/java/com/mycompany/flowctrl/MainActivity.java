package com.mycompany.flowctrl;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    private MySQLiteHelper mDbHelper;
    private SQLiteDatabase db;
    public static ArrayList<String> values;
    public static ArrayAdapter<String> adapter;
    public TextView status;
    public TextView currentOut;
    public int out = 0;
    public Button sO;
    public Button sI;
    private static final boolean VERBOSE = true;
    private static final String TAG = "SampleActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new MySQLiteHelper(this);
        setContentView(R.layout.activity_main);

        // Set action bar title
        setTitle("Flow Ctrl");

        // Inject view elements
        status = (TextView)this.findViewById(R.id.status);
        currentOut = (TextView)this.findViewById(R.id.currentOut);
        sO = (Button)this.findViewById(R.id.scanOut);
        sI = (Button)this.findViewById(R.id.scanIn);

        // Set font
        Typeface main_font = Typeface.createFromAsset(getAssets(),  "fonts/Montserrat-Regular.ttf");
        status.setTypeface(main_font);
        currentOut.setTypeface(main_font);
        sO.setTypeface(main_font);
        sI.setTypeface(main_font);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if(id == R.id.action_cleardb) {
            mDbHelper.onUpgrade(db, MySQLiteHelper.DATABASE_VERSION, MySQLiteHelper.DATABASE_VERSION + 1);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (VERBOSE) Log.v(TAG, "- ON PAUSE -");
    }

    public void scanIn(View view){
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan(0);
    }

    public void scanOut(View view){
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan(1);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            db = mDbHelper.getWritableDatabase();

            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT"); //this is the result
                System.out.println(requestCode + "   " + resultCode + "   " + DateFormat.getDateTimeInstance().format(new Date()));

                // Display success message
                status.setTextColor(getResources().getColor(R.color.holo_green));
                status.setText(R.string.success);

                // Increment count;
                out++;

                // Add contents to database
                ContentValues bc = new ContentValues();
                bc.put(MySQLiteHelper.COLUMN_BARCODE, contents);
                long newRowId;
                newRowId = db.insert(
                        MySQLiteHelper.TABLE_TITLE,
                        null,
                        bc);
            } else if (resultCode == RESULT_CANCELED) {
                // Handle cancel
            }
        }

        if (requestCode == 0) {
            db = mDbHelper.getReadableDatabase();

            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT"); //this is the result
                System.out.println(contents + DateFormat.getDateTimeInstance().format(new Date()));

                // Make query
                String q = "SELECT * FROM " + MySQLiteHelper.TABLE_TITLE + " WHERE " + MySQLiteHelper.COLUMN_BARCODE + " = '" + contents + "'";
                Cursor c = db.rawQuery(q, null);
                boolean isEmpty = c.getCount() < 1;
                if(isEmpty) {

                    // Display not found message
                    status.setTextColor(getResources().getColor(R.color.darkred));
                    status.setText(R.string.not_found);
                }
                else {

                    // Display found message
                    status.setTextColor(getResources().getColor(R.color.holo_green));
                    status.setText(R.string.found);

                    // Decrement count;

                    // Delete entry from database
                    String table = "beaconTable";
                    String whereClause =MySQLiteHelper.COLUMN_BARCODE + " = ? ";
                    String[] whereArgs = new String[] { contents };
                    db.delete(MySQLiteHelper.TABLE_TITLE, whereClause, whereArgs);
                }

                // Close cursor
                c.close();
            } else if (resultCode == RESULT_CANCELED) {
                // Handle cancel
            }
        }
    }
    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            db = mDbHelper.getWritableDatabase();

            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT"); //this is the result
                System.out.println(contents + " " + DateFormat.getDateTimeInstance().format(new Date()));

                // Display success message
                status.setTextColor(getResources().getColor(R.color.holo_green));
                status.setText(R.string.success);

                // Add contents to database
                ContentValues bc = new ContentValues();
                bc.put(MySQLiteHelper.COLUMN_BARCODE, contents);
                long newRowId;
                newRowId = db.insert(
                        MySQLiteHelper.TABLE_TITLE,
                        null,
                        bc);
            } else if (resultCode == RESULT_CANCELED) {
                // Handle cancel
            }
        }

        if (requestCode == 0) {
            db = mDbHelper.getReadableDatabase();

            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT"); //this is the result
                System.out.println(contents + DateFormat.getDateTimeInstance().format(new Date()));

                // Make query
                String q = "SELECT * FROM " + MySQLiteHelper.TABLE_TITLE + " WHERE " + MySQLiteHelper.COLUMN_BARCODE + " = '" + contents + "'";
                Cursor c = db.rawQuery(q, null);
                boolean isEmpty = c.getCount() < 1;
                if(isEmpty) {

                    // Display not found message
                    status.setTextColor(getResources().getColor(R.color.darkred));
                    status.setText(R.string.not_found);
                }
                else {

                    // Display found message
                    status.setTextColor(getResources().getColor(R.color.holo_green));
                    status.setText(R.string.found);

                    // Delete entry from database
                    String table = "beaconTable";
                    String whereClause =MySQLiteHelper.COLUMN_BARCODE + " = ? ";
                    String[] whereArgs = new String[] { contents };
                    db.delete(MySQLiteHelper.TABLE_TITLE, whereClause, whereArgs);
                }

                // Close cursor
                c.close();
            } else if (resultCode == RESULT_CANCELED) {
                // Handle cancel
            }
        }
    }*/
}
