package com.practicaltest.brighterbrain.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageButton;

import com.practicaltest.brighterbrain.R;
import com.practicaltest.brighterbrain.comm.Constants;
import com.practicaltest.brighterbrain.utils.Utils;
import com.commonsware.cwac.camera.CameraHost;
import com.commonsware.cwac.camera.CameraHostProvider;
import com.commonsware.cwac.camera.CameraView;
import com.commonsware.cwac.camera.PictureTransaction;
import com.commonsware.cwac.camera.SimpleCameraHost;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TakePhotoActivity extends BaseActivity implements CameraHostProvider {

    public static final String LOG_TAG = TakePhotoActivity.class.getName();
    private static final int ACTION_REQUEST_GALLERY = 99;
    private static final int EXTERNAL_STORAGE_UNAVAILABLE = 1;
    /**
     * Folder name on the sdcard where the images will be saved *
     */
    private static final String FOLDER_NAME = "Instacar";

    private static final Interpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.vShutter)
    View vShutter;
    @Bind(R.id.cameraView) CameraView cameraView;
    @Bind(R.id.btnTakePhoto)
    Button btnTakePhoto;
    @Bind(R.id.btnGallery)
    ImageButton btnGallery;

    String mOutputFilePath;
    File mGalleryFolder;
    Uri mImageUri;

    public static void startCameraFromLocation(int[] startingLocation, Activity startingActivity, int RETURN_ID) {
        Intent intent = new Intent(startingActivity, TakePhotoActivity.class);
        startingActivity.startActivityForResult(intent, RETURN_ID);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);
        ButterKnife.bind(this);

        setupToolbar();
        updateStatusBarColor();

        mGalleryFolder = createFolders();
    }

    private void setupToolbar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            toolbar.setNavigationIcon(R.drawable.ic_nav_cancel);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void updateStatusBarColor() {
        if (Utils.isAndroid5()) {
            getWindow().setStatusBarColor(0xff21262b);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.onPause();
    }

    @OnClick(R.id.btnTakePhoto)
    public void onTakePhotoClick() {
        btnTakePhoto.setEnabled(false);
        cameraView.takePicture(true, true);
        animateShutter();
    }

    @OnClick(R.id.btnGallery)
    public void onGalleryBtnClick() {
        pickFromGallery();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_take_photo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_front_camera) {

            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void animateShutter() {
        vShutter.setVisibility(View.VISIBLE);
        vShutter.setAlpha(0.f);

        ObjectAnimator alphaInAnim = ObjectAnimator.ofFloat(vShutter, "alpha", 0f, 0.8f);
        alphaInAnim.setDuration(100);
        alphaInAnim.setStartDelay(100);
        alphaInAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

        ObjectAnimator alphaOutAnim = ObjectAnimator.ofFloat(vShutter, "alpha", 0.8f, 0f);
        alphaOutAnim.setDuration(200);
        alphaOutAnim.setInterpolator(DECELERATE_INTERPOLATOR);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(alphaInAnim, alphaOutAnim);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                vShutter.setVisibility(View.GONE);
            }
        });
        animatorSet.start();
    }

    @Override
    public CameraHost getCameraHost() {
        SimpleCameraHost builder = new SimpleCameraHost.Builder(new MyCameraHost(this)).photoDirectory(createFolders()).build();
        return builder;

    }

    @SuppressWarnings("deprecation")
    class MyCameraHost extends SimpleCameraHost {

        private Camera.Size previewSize;

        public MyCameraHost(Context ctxt) {
            super(ctxt);
        }

        @Override
        public boolean useFullBleedPreview() {
            return true;
        }

        @Override
        public Camera.Size getPictureSize(PictureTransaction xact, Camera.Parameters parameters) {
            return previewSize;
        }

        @Override
        public Camera.Parameters adjustPreviewParameters(Camera.Parameters parameters) {
            Camera.Parameters parameters1 = super.adjustPreviewParameters(parameters);
            previewSize = parameters1.getPreviewSize();
            return parameters1;
        }

        @Override
        public void saveImage(PictureTransaction xact, final Bitmap bitmap) {

        }

        @Override
        public void saveImage(PictureTransaction xact, byte[] image) {
            super.saveImage(xact, image);
            mOutputFilePath = getPhotoPath().getAbsolutePath();
            mImageUri = Uri.fromFile(getPhotoPath());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btnTakePhoto.setEnabled(true);
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(Constants.IMAGE_PATH,mOutputFilePath);
                    setResult(Activity.RESULT_OK,returnIntent);
                    finish();
                   // PublishActivity.openWithPhotoUri(TakePhotoActivity.this, mImageUri);
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ACTION_REQUEST_GALLERY:
                    // user chose an image from the gallery
                    mImageUri = data.getData();
                    startPublish(data.getData());
                    break;
            }
        } else if (resultCode == RESULT_CANCELED) {
            switch (requestCode) {
                case ACTION_REQUEST_GALLERY:

                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mOutputFilePath = null;
    }

    /**
     * Return a new image file. Name is based on the current time. Parent folder
     * will be the one created with createFolders
     *
     * @return
     * @see #createFolders()
     */
    private File getNextFileName() {
        if (mGalleryFolder != null) {
            if (mGalleryFolder.exists()) {
                File file = new File(
                        mGalleryFolder, "Photo_"
                        + System.currentTimeMillis() + ".jpg");
                return file;
            }
        }
        return null;
    }

    /**
     * Once you've chosen an image you can start the publish activity
     *
     * @param uri
     */
    @SuppressWarnings("deprecation")
    private void startPublish(Uri uri) {
        Log.d(LOG_TAG, "uri: " + uri);

        // first check the external storage availability
        if (!isExternalStorageAvailable()) {
            showDialog(EXTERNAL_STORAGE_UNAVAILABLE);
            return;
        }

        try {
            File source = new File(Utils.getRealPathFromURI(this, uri));
            File dest = getNextFileName();
            if (source.exists()) {
                FileChannel src = new FileInputStream(Utils.getRealPathFromURI(this, uri)).getChannel();
                FileChannel dst = new FileOutputStream(dest).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            }
            Intent returnIntent = new Intent();
            returnIntent.putExtra(Constants.IMAGE_PATH,Utils.getRealPathFromURI(this, uri));
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
            //PublishActivity.openWithPhotoUri(this, Uri.fromFile(dest));
        } catch (Exception e) {
            System.out.println("Error :" + e.getMessage());
        }
    }

    /**
     * Check the external storage status
     *
     * @return
     */
    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Start the activity to pick an image from the user gallery
     */
    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Choose a Photo"), ACTION_REQUEST_GALLERY);

        /*Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image*//*");

        Intent chooser = Intent.createChooser(intent, "Choose a Picture");
        startActivityForResult(chooser, ACTION_REQUEST_GALLERY);*/
    }

    private File createFolders() {
        File baseDir;

        if (android.os.Build.VERSION.SDK_INT < 8) {
            baseDir = Environment.getExternalStorageDirectory();
        } else {
            baseDir = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        }

        if (baseDir == null) {
            return Environment.getExternalStorageDirectory();
        }

        Log.d(LOG_TAG, "Pictures folder: " + baseDir.getAbsolutePath());
        File instacarFolder = new File(baseDir, FOLDER_NAME);

        if (instacarFolder.exists()) {
            return instacarFolder;
        }
        if (instacarFolder.mkdirs()) {
            return instacarFolder;
        }

        return Environment.getExternalStorageDirectory();
    }
}