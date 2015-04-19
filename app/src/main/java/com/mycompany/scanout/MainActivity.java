package com.mycompany.scanout;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends ActionBarActivity {

    private MySQLiteHelper mDbHelper;
    private SQLiteDatabase db;
    public static ArrayList<String> values;
    public static ArrayAdapter<String> adapter;
    public TextView t;
    private static final boolean VERBOSE = true;
    private static final String TAG = "SampleActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new MySQLiteHelper(this);
        setContentView(R.layout.activity_main);
        t = (TextView)this.findViewById(R.id.textView);
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
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        //intent.putExtra("SCAN_MODE", "PRODUCT_MODE");//for Qr code, its "QR_CODE_MODE" instead of "PRODUCT_MODE"
        intent.putExtra("SAVE_HISTORY", true);//this stops saving ur barcode in barcode scanner app's history
        startActivityForResult(intent, 0);
    }

    public void scanOut(View view){
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        //intent.putExtra("SCAN_MODE", "PRODUCT_MODE");//for Qr code, its "QR_CODE_MODE" instead of "PRODUCT_MODE"
        intent.putExtra("SAVE_HISTORY", true);//this stops saving ur barcode in barcode scanner app's history
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            db = mDbHelper.getWritableDatabase();

            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT"); //this is the result
                System.out.println(contents + " " + DateFormat.getDateTimeInstance().format(new Date()));
                t.setTextColor(getResources().getColor(R.color.green));
                t.setText(contents + "\n" + DateFormat.getDateTimeInstance().format(new Date()));
                //adapter.notifyDataSetChanged();
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
                String q = "SELECT * FROM " + MySQLiteHelper.TABLE_TITLE + " WHERE " + MySQLiteHelper.COLUMN_BARCODE + " = '" + contents + "'";
                Cursor c = db.rawQuery(q, null);
                boolean isEmpty = c.getCount() < 1;
                if(isEmpty) {
                    t.setTextColor(getResources().getColor(R.color.darkred));
                    t.setText("Barcode not found!!!");
                }
                else {
                    t.setTextColor(getResources().getColor(R.color.darkgreen));
                    t.setText("Barcode found!");
                }
                c.close();
            } else if (resultCode == RESULT_CANCELED) {
                // Handle cancel
            }
        }
    }
}
