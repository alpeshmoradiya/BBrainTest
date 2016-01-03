package com.practicaltest.brighterbrain.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.practicaltest.brighterbrain.comm.Constants;
import com.practicaltest.brighterbrain.database.DataBaseHelper;
import com.practicaltest.brighterbrain.model.ItemDetails;
import com.practicaltest.brighterbrain.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by apu on 03/01/16.
 */
public class DataSyncService  extends IntentService {

    public static boolean IS_STATUS_RUNNING = false;
    private static final String TAG = "DBSyncService";
    private ArrayList<ItemDetails> itemDeatilList = new ArrayList<ItemDetails>();
    private DataBaseHelper dbHalper;

    public DataSyncService() {
        super(DataSyncService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        dbHalper = new DataBaseHelper(this);
        Log.d(TAG, "Service Started!");
        IS_STATUS_RUNNING = true;

        if(Utils.isConnected(this)) {
            itemDeatilList = dbHalper.getNotSyncedItems();
            for (int i = 0; i < itemDeatilList.size(); i++) {
                final int pos = i;
                switch (itemDeatilList.get(i).getSyncStatus()) {
                    case 0:
                        ParseObject itemObjectAdd = new ParseObject("ItemCollection");
                        itemObjectAdd.put("item_name", itemDeatilList.get(i).getItemName());
                        itemObjectAdd.put("item_location", itemDeatilList.get(i).getItemLocation());
                        itemObjectAdd.put("item_description", itemDeatilList.get(i).getItemDiscription());
                        itemObjectAdd.put("item_cost", itemDeatilList.get(i).getItemCost());
                        itemObjectAdd.put("item_id", itemDeatilList.get(i).getItemId());
                        itemObjectAdd.put("item_image_path", itemDeatilList.get(i).getItemImagePath());
                        itemObjectAdd.put("device_id", Constants.deviceId);
                        itemObjectAdd.saveInBackground();
                        dbHalper.updateItemStatus(itemDeatilList.get(i).getItemId());
                        break;
                    case 1:
                        ParseQuery<ParseObject> queryUpdate = ParseQuery.getQuery("ItemCollection");
                        queryUpdate.whereEqualTo("device_id", Constants.deviceId);
                        queryUpdate.whereEqualTo("item_id", itemDeatilList.get(i).getItemId());
                        queryUpdate.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> objects, ParseException e) {
                                for (ParseObject nameObj : objects) {
                                    nameObj.put("item_name", itemDeatilList.get(pos).getItemName());
                                    nameObj.put("item_location", itemDeatilList.get(pos).getItemLocation());
                                    nameObj.put("item_description", itemDeatilList.get(pos).getItemDiscription());
                                    nameObj.put("item_cost", itemDeatilList.get(pos).getItemCost());
                                    nameObj.put("item_image_path", itemDeatilList.get(pos).getItemImagePath());
                                    nameObj.saveInBackground();
                                }
                                dbHalper.updateItemStatus(itemDeatilList.get(pos).getItemId());

                            }
                        });
                        break;
                    case 2:
                        ParseQuery<ParseObject> queryDelete = ParseQuery.getQuery("ItemCollection");
                        queryDelete.whereEqualTo("device_id", Constants.deviceId);
                        queryDelete.whereEqualTo("item_id", itemDeatilList.get(i).getItemId());
                        queryDelete.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> objects, ParseException e) {
                                for (ParseObject delete : objects) {
                                    delete.deleteInBackground();
                                    dbHalper.removeItem(itemDeatilList.get(pos).getItemId());
                                }
                                dbHalper.updateItemStatus(itemDeatilList.get(pos).getItemId());

                            }
                        });
                        break;

                }
            }
        }

        this.stopSelf();
    }

    @Override
    public void onDestroy() {
        IS_STATUS_RUNNING = false;
        super.onDestroy();
    }
}
