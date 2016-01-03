package com.practicaltest.brighterbrain.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.practicaltest.brighterbrain.comm.Constants;
import com.practicaltest.brighterbrain.model.ItemDetails;
import com.practicaltest.brighterbrain.ui.BaseActivity;

import java.util.ArrayList;


public class DataBaseHelper extends SQLiteOpenHelper {
    public static String DB_NAME = "BrighterBrainTest.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_ITEM_DETAILS = "item_details";

    private static final String KEY_ITEM_ID = "item_id";
    private static final String KEY_ITEM_NAME = "item_name";
    private static final String KEY_ITEM_DESCRIPTION = "item_description";
    private static final String KEY_ITEM_LOCATION = "item_location";
    private static final String KEY_ITEM_COST = "item_cost";
    private static final String KEY_ITEM_IMAGE_PATH = "item_image_path";
    private static final String KEY_DATA_SYNC = "item_data_sync";

    private SQLiteDatabase myDataBase;
    private Context myContext;

    public DataBaseHelper(Context context) {

        super(context, DB_NAME, null, DATABASE_VERSION);
        this.myContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        String CREATE_ITEM_DETAILS_TABLE = "CREATE TABLE " + TABLE_ITEM_DETAILS + "("
                + KEY_ITEM_ID + " INTEGER PRIMARY KEY,"
                + KEY_ITEM_NAME + " TEXT,"
                + KEY_ITEM_DESCRIPTION + " TEXT,"
                + KEY_ITEM_LOCATION + " TEXT,"
                + KEY_ITEM_COST + " TEXT,"
                + KEY_ITEM_IMAGE_PATH + " TEXT,"
                + KEY_DATA_SYNC + " TEXT"
                + ")";
        db.execSQL(CREATE_ITEM_DETAILS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEM_DETAILS);
        // Create tables again
        onCreate(db);
    }

    @Override
    public synchronized void close() {

        if (myDataBase != null)
            myDataBase.close();

        super.close();
    }


    public int addItem(ItemDetails model) {
        int val = -1;
        try {
            synchronized (BaseActivity.sDataLock) {
                SQLiteDatabase db = this.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(KEY_ITEM_NAME, model.getItemName()); // Item Name
                values.put(KEY_ITEM_DESCRIPTION, model.getItemDiscription()); // Item Description
                values.put(KEY_ITEM_LOCATION, model.getItemLocation()); // Item Location
                values.put(KEY_ITEM_COST, model.getItemCost()); // Item Cost
                values.put(KEY_ITEM_IMAGE_PATH, model.getItemImagePath()); // Item Image Path
                values.put(KEY_DATA_SYNC, model.getSyncStatus()); // Item Image Path

                val = (int) db.insert(TABLE_ITEM_DETAILS, null, values);
                db.close(); // Closing database connection
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return val;
    }

    public int removeItem(int itemId) {
        try {
            synchronized (BaseActivity.sDataLock) {
                SQLiteDatabase db = this.getWritableDatabase();
                return db.delete(TABLE_ITEM_DETAILS, KEY_ITEM_ID + " = ?", new String[]{String.valueOf(itemId)});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }


    public ArrayList<ItemDetails> getAllItems() {
        ArrayList<ItemDetails> itemDeatilList = new ArrayList<ItemDetails>();
        try {
            synchronized (BaseActivity.sDataLock) {
        // Select All Query

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_ITEM_DETAILS, null, KEY_DATA_SYNC + " != ?", new String[]{Constants.DATA_DELETED+""}, null, null, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ItemDetails itemDetailsModel = new ItemDetails();
                itemDetailsModel.setItemId(Integer.parseInt(cursor.getString(0)));
                itemDetailsModel.setItemName(cursor.getString(1));
                itemDetailsModel.setItemDiscription(cursor.getString(2));
                itemDetailsModel.setItemLocation(cursor.getString(3));
                itemDetailsModel.setItemCost(cursor.getString(4));
                itemDetailsModel.setItemImagePath(cursor.getString(5));
                itemDetailsModel.setSyncStatus(cursor.getInt(6));
                itemDeatilList.add(itemDetailsModel);
            } while (cursor.moveToNext());
        }

        db.close();
        // return vehicle list
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return itemDeatilList;
    }

    public ArrayList<ItemDetails> getNotSyncedItems() {
        ArrayList<ItemDetails> itemDeatilList = new ArrayList<ItemDetails>();
        try {
            synchronized (BaseActivity.sDataLock) {
                // Select All Query

                SQLiteDatabase db = this.getWritableDatabase();
                Cursor cursor = db.query(TABLE_ITEM_DETAILS, null, KEY_DATA_SYNC + " != ?", new String[]{Constants.DATA_SYNCKED+""}, null, null, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        ItemDetails itemDetailsModel = new ItemDetails();
                        itemDetailsModel.setItemId(Integer.parseInt(cursor.getString(0)));
                        itemDetailsModel.setItemName(cursor.getString(1));
                        itemDetailsModel.setItemDiscription(cursor.getString(2));
                        itemDetailsModel.setItemLocation(cursor.getString(3));
                        itemDetailsModel.setItemCost(cursor.getString(4));
                        itemDetailsModel.setItemImagePath(cursor.getString(5));
                        itemDetailsModel.setSyncStatus(cursor.getInt(6));
                        itemDeatilList.add(itemDetailsModel);
                    } while (cursor.moveToNext());
                }

                db.close();
                // return vehicle list
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return itemDeatilList;
    }

    public int updateItemDeatils(ItemDetails itemDetails) {
        SQLiteDatabase db = this.getWritableDatabase();
        int val = 0;
        if (!isScriptAlreadyExists("" + itemDetails.getItemId())) {
            ContentValues values = new ContentValues();
            values.put(KEY_ITEM_NAME, itemDetails.getItemName());
            values.put(KEY_ITEM_DESCRIPTION, itemDetails.getItemDiscription());
            values.put(KEY_ITEM_LOCATION, itemDetails.getItemLocation());
            values.put(KEY_ITEM_COST, itemDetails.getItemCost());
            values.put(KEY_ITEM_IMAGE_PATH, itemDetails.getItemImagePath());
            values.put(KEY_DATA_SYNC, itemDetails.getSyncStatus());

            val = db.update(TABLE_ITEM_DETAILS, values, KEY_ITEM_ID + " = ?", new String[]{itemDetails.getItemId() + ""});
        }
        db.close();
        return val;
    }

    public int updateItemStatus(int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int val = 0;
        if (!isScriptAlreadyExists("" + itemId)) {
            ContentValues values = new ContentValues();
            values.put(KEY_DATA_SYNC, Constants.DATA_SYNCKED);
            val = db.update(TABLE_ITEM_DETAILS, values, KEY_ITEM_ID + " = ?", new String[]{itemId + ""});
        }
        db.close();
        return val;
    }

    public boolean isScriptAlreadyExists(String symbol) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_ITEM_DETAILS, null, KEY_ITEM_ID + "=? COLLATE NOCASE =? "
                , new String[]{symbol}, null, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        if (count > 0)
            return true;
        else
            return false;
    }

}