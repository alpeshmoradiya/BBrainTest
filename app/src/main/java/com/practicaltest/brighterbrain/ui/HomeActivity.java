package com.practicaltest.brighterbrain.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.practicaltest.brighterbrain.R;
import com.practicaltest.brighterbrain.adapter.ItemAdapter;
import com.practicaltest.brighterbrain.comm.Constants;
import com.practicaltest.brighterbrain.database.DataBaseHelper;
import com.practicaltest.brighterbrain.interfaces.DeleteDailogClickListener;
import com.practicaltest.brighterbrain.model.ItemDetails;
import com.practicaltest.brighterbrain.progressactivity.ProgressActivity;
import com.practicaltest.brighterbrain.service.DataSyncService;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.Bind;
import butterknife.ButterKnife;

public class HomeActivity extends BaseActivity implements android.support.v7.widget.SearchView.OnQueryTextListener, DeleteDailogClickListener, View.OnClickListener{

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.ivLogo)
    TextView ivToolbarLogo;
    @Bind(R.id.fab)
    FloatingActionButton fab;
    @Bind(R.id.rvFeed)
    RecyclerView rvFeed;
    @Bind(R.id.progress)
    ProgressActivity progressLayout;

    private Context mContext;
    private ItemAdapter itemAdapter;
    private DataBaseHelper dbHalper;

    private ArrayList<ItemDetails> itemDetails = new ArrayList<ItemDetails>();
    private ArrayList<ItemDetails> baseList = new ArrayList<ItemDetails>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initUI();


    }

    /**
     * Initialize all objects of HomeActivity.
     */
    private void initUI(){
        mContext = this;
        ButterKnife.bind(this);

        setupToolbar();
        setTitleLogo();

        dbHalper = new DataBaseHelper(this);

        fab.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        itemDetails.clear();
        setupSingleDataFeed();
        super.onResume();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        android.support.v7.widget.SearchView searchView = (android.support.v7.widget.SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab:
                int[] startingLocation = new int[2];
                fab.getLocationOnScreen(startingLocation);
                startingLocation[0] += fab.getWidth() / 2;
                TakePhotoActivity.startCameraFromLocation(startingLocation, HomeActivity.this, Constants.RESULT_IMAGE);
                overridePendingTransition(0, 0);
                break;
        }
    }

    public boolean onQueryTextChange(String newText) {
        itemDetails.clear();
        if(newText.trim().length() > 0) {
            for (int i = 0; i < baseList.size(); i++) {
                if (baseList.get(i).getItemName().toLowerCase().contains(newText.toLowerCase())) {
                    itemDetails.add(baseList.get(i));
                }
            }
        }else{
            itemDetails.addAll(baseList);
        }
        itemAdapter.notifyDataSetChanged();
        setEmptyView(itemDetails);

        return true;
    }

    public boolean      onQueryTextSubmit      (String query) {
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.RESULT_IMAGE) {
            if(resultCode == RESULT_OK) {
                String result= data.getStringExtra(Constants.IMAGE_PATH);
                AddItemActivity.openWithPhotoUri(HomeActivity.this, result, false, null);
            }
            if (resultCode == RESULT_CANCELED) {
            }
        }

    }

    /**
     * Initialize toolbar(Actionbar) components
     */
    private void setupToolbar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            toolbar.setNavigationIcon(R.drawable.ic_action_view_quilt);
            setTitleLogo();
        }
    }

    private void setTitleLogo() {
        ivToolbarLogo.setText(getResources().getString(R.string.app_name1));
    }

    /**
     * Delete Action for collection list item.
     * @param position Collection item position which is going to delete
     */
    @Override
    public void onDeleteAction(int position) {
        itemDetails.get(position).setSyncStatus(Constants.DATA_DELETED);
        dbHalper.updateItemDeatils(itemDetails.get(position));
        requestBackup();
        itemDetails.remove(position);
        itemAdapter.notifyDataSetChanged();
        setEmptyView(itemDetails);

        if(!DataSyncService.IS_STATUS_RUNNING) {
            Intent intent = new Intent(Intent.ACTION_SYNC, null, this, DataSyncService.class);
            startService(intent);
        }
    }

    /**
     * Bind list item collections.
     */
    private void setupSingleDataFeed() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvFeed.setLayoutManager(linearLayoutManager);
        itemDetails = dbHalper.getAllItems();
        Collections.reverse(itemDetails);
        baseList.addAll(itemDetails);
        itemAdapter = new ItemAdapter(this, itemDetails);
        itemAdapter.setDeleteDailogClickListener(this);
        rvFeed.setAdapter(itemAdapter);
        setEmptyView(itemDetails);
    }

    private void setEmptyView(ArrayList<ItemDetails> post_details) {
        if (post_details != null && !post_details.isEmpty()) {
            if (!progressLayout.isContent()) {
                progressLayout.showContent();
            }
        } else {
            if (!progressLayout.isError()) {
                progressLayout.showError(getResources().getDrawable(R.drawable.ic_image_photo_camera),
                        "No Item!", "No item available. Please add item in collection.", "Add item in collection", errorClickListener, Collections.<Integer>emptyList());
            }
        }
    }

    private View.OnClickListener errorClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        }
    };

}
