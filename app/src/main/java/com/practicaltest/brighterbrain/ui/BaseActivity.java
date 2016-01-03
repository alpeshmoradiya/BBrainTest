package com.practicaltest.brighterbrain.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;


public class BaseActivity extends AppCompatActivity {

    ProgressDialog m_progressDialog;
    public static final Object sDataLock = new Object();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    public void showToast(int msgId) {
        Toast.makeText(this, msgId, Toast.LENGTH_LONG).show();
    }

    /**
     * Use this to display single button dialog with
     * title and message.
     *
     * @param title    Title of the Dialog
     * @param message  Message to display
     * @param btnTitle Single button title to keep (i.e. Ok, Exit, Close etc..)
     */
    public void showDialog(String title, String message, String btnTitle) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton(btnTitle, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create();
        builder.show();
    }

    /**
     * Use this to display single button dialog with title and
     * message. It will display "OK" as default button text.
     *
     * @param title   Title of the Dialog
     * @param message Message to display
     */
    public void showDialog(String title, String message) {
        showDialog(title, message, "Ok");
    }

    public void showProgressDialog(Context context, String msg) {
        if (m_progressDialog == null) m_progressDialog = new ProgressDialog(context);
        m_progressDialog.setMessage(msg);
        m_progressDialog.setCancelable(false);
        if (!m_progressDialog.isShowing()) m_progressDialog.show();
    }

    public void dismissProgressDialog() {
        try {
            if (m_progressDialog != null && m_progressDialog.isShowing())
                m_progressDialog.dismiss();
        } catch (final IllegalArgumentException e) {
            e.printStackTrace();
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            this.m_progressDialog = null;
        }
    }

    public void requestBackup() {
        BackupManager bm = new BackupManager(this);
        bm.dataChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        if (requestCode == Constants.SHARE_POST_REQUEST_CODE) {
//            if (!TextUtils.isEmpty(Constants.SHARE_IMAGE_PATH)) {
//                Utils.deleteFileNoThrow(Constants.SHARE_IMAGE_PATH);
//            }
//        }
    }
}
