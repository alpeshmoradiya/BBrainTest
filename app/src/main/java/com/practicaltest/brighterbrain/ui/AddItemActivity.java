package com.practicaltest.brighterbrain.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.practicaltest.brighterbrain.R;
import com.practicaltest.brighterbrain.comm.Constants;
import com.practicaltest.brighterbrain.database.DataBaseHelper;
import com.practicaltest.brighterbrain.model.ItemDetails;
import com.practicaltest.brighterbrain.service.DataSyncService;
import com.practicaltest.brighterbrain.utils.Utils;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by alpesh.moradiya on 01/01/2015.
 */
public class AddItemActivity extends BaseActivity implements View.OnClickListener{

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.etName)
    EditText edItemName;
    @Bind(R.id.etDiscription)
    EditText edItemDescription;
    @Bind(R.id.etLocation)
    EditText edItemLocation;
    @Bind(R.id.etCost)
    EditText edItemCost;
    @Bind(R.id.img_item_pic)
    ImageView imgItemPic;
    @Bind(R.id.ibtn_edit_profile)
    FloatingActionButton ibtnEditPic;
    @Bind(R.id.btn_add_item)
    Button btnAddItem;

    private Context mContext;
    private String mImageUri = null;
    private ItemDetails itemDetails = new ItemDetails();
    private boolean isEditMode = false;
    private DataBaseHelper dbHalper;

    public static void openWithPhotoUri(Context openingActivity, String photoUri, boolean isEditMode, ItemDetails itemDetails) {
        Intent intent = new Intent(openingActivity, AddItemActivity.class);
        intent.putExtra(Constants.IMAGE_PATH, photoUri);
        intent.putExtra(Constants.IS_EDIT_MODE, isEditMode);
        if(isEditMode){
            intent.putExtra(Constants.ITEM_DETAILS, itemDetails);
        }
        openingActivity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_item);

        initUI();

    }

    /**
     * Initialize all objects of HomeActivity.
     */
    private void initUI(){
        mContext = this;

        ButterKnife.bind(this);
        setupToolbar();

        dbHalper = new DataBaseHelper(mContext);

        btnAddItem.setOnClickListener(this);
        ibtnEditPic.setOnClickListener(this);

        Intent intent = getIntent();
        if(intent != null) {
            mImageUri = getIntent().getStringExtra(Constants.IMAGE_PATH);
            isEditMode = getIntent().getBooleanExtra(Constants.IS_EDIT_MODE, false);
            if(isEditMode) {
                itemDetails = (ItemDetails)getIntent().getSerializableExtra(Constants.ITEM_DETAILS);
                if(itemDetails != null)
                    feedItemData();
            }
        }

        Glide.with(this).load(new File(mImageUri)).placeholder(R.drawable.placeholder).into(imgItemPic);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_publish, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Initialize toolbar(Actionbar) components
     */
    protected void setupToolbar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            toolbar.setNavigationIcon(R.drawable.ic_nav_arrow_back);
            toolbar.setTitle("");
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Constants.RESULT_IMAGE) {
            if(resultCode == Activity.RESULT_OK){
                String result= data.getStringExtra(Constants.IMAGE_PATH);
                mImageUri = result;
                Glide.with(this).load(new File(mImageUri)).placeholder(R.drawable.placeholder).into(imgItemPic);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ibtn_edit_profile:
                int[] startingLocation = new int[2];
                ibtnEditPic.getLocationOnScreen(startingLocation);
                startingLocation[0] += ibtnEditPic.getWidth() / 2;
                TakePhotoActivity.startCameraFromLocation(startingLocation,AddItemActivity.this, Constants.RESULT_IMAGE);
                overridePendingTransition(0, 0);
                break;
            case R.id.btn_add_item:
                addOrUpdateItemInCollection();
                break;
        }
    }

    /**
     * perform action for add or update collection item.
     */
    private void addOrUpdateItemInCollection(){

        Utils.hideSoftKeyboard(this);
        if(checkvalidation()) {

            if (isEditMode) {
                itemDetails.setItemName(edItemName.getText().toString().trim());
                itemDetails.setItemDiscription(edItemDescription.getText().toString().trim());
                itemDetails.setItemLocation(edItemLocation.getText().toString().trim());
                itemDetails.setItemCost(edItemCost.getText().toString().trim());
                itemDetails.setItemImagePath(mImageUri.toString());
                itemDetails.setSyncStatus(Constants.DATA_NOT_SYNCKED_UPDATED);
                dbHalper.updateItemDeatils(itemDetails);
            } else {
                itemDetails.setItemName(edItemName.getText().toString().trim());
                itemDetails.setItemDiscription(edItemDescription.getText().toString().trim());
                itemDetails.setItemLocation(edItemLocation.getText().toString().trim());
                itemDetails.setItemCost(edItemCost.getText().toString().trim());
                itemDetails.setItemImagePath(mImageUri.toString());
                itemDetails.setSyncStatus(Constants.DATA_NOT_SYNCKED_ADDED);
                dbHalper.addItem(itemDetails);
            }
            requestBackup();


            if(!DataSyncService.IS_STATUS_RUNNING) {
                Intent intent = new Intent(Intent.ACTION_SYNC, null, this, DataSyncService.class);
                startService(intent);
            }


            finish();
        }else{
            showDialog(getResources().getString(R.string.app_name), getResources().getString(R.string.add_item_validation));
        }

    }

    /**
     * Fill up all input data of add item form in case of item is in edit mode.
     */
    private void feedItemData(){
        edItemName.setText(itemDetails.getItemName());
        edItemDescription.setText(itemDetails.getItemDiscription());
        edItemLocation.setText(itemDetails.getItemLocation());
        edItemCost.setText(itemDetails.getItemCost());
    }

    /**
     * Check all mandatory filed filled out or not
     * @return true if all filed values are proper otherwise false
     */
    private boolean checkvalidation(){
        if(mImageUri != null){
            if(edItemName.getText().toString().trim().length() > 0){
                if(edItemDescription.getText().toString().trim().length() > 0){
                    if(edItemLocation.getText().toString().trim().length() > 0){
                        if(edItemCost.getText().toString().trim().length() > 0){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
