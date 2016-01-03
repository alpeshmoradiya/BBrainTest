package com.practicaltest.brighterbrain.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.practicaltest.brighterbrain.R;
import com.practicaltest.brighterbrain.comm.Constants;
import com.practicaltest.brighterbrain.database.DataBaseHelper;
import com.practicaltest.brighterbrain.model.ItemDetails;
import com.practicaltest.brighterbrain.utils.Utils;

import java.io.File;
import java.util.List;

/**
 * Created by apu on 03/01/16.
 */
public class SplashActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash);


        Constants.deviceId = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        File dbFile = this.getDatabasePath(DataBaseHelper.DB_NAME);
        if(!dbFile.exists()) {
            if(Utils.isConnected(this)){
                ParseQuery<ParseObject> queryUpdate = ParseQuery.getQuery("ItemCollection");
                queryUpdate.whereEqualTo("device_id", Constants.deviceId);
                queryUpdate.orderByAscending("item_id");
                queryUpdate.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        DataBaseHelper dbHalper = new DataBaseHelper(SplashActivity.this);
                        for (ParseObject nameObj : objects) {
                            ItemDetails details = new ItemDetails();
                            details.setItemName(nameObj.getString("item_name"));
                            details.setItemDiscription(nameObj.getString("item_description"));
                            details.setItemLocation(nameObj.getString("item_location"));
                            details.setItemCost(nameObj.getString("item_cost"));
     details.setItemImagePath(nameObj.getString("item_image_path"));
                       
                            dbHalper.addItem(details);
                        }
                        startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                        finish();
                    }
                });

            }else{
               findViewById(R.id.tv_message).setVisibility(View.VISIBLE);
            }
         }else{
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
    }
}
