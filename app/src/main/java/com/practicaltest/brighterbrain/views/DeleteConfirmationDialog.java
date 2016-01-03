package com.practicaltest.brighterbrain.views;

import android.app.Activity;
import android.app.Dialog;

/**
 * Created by alpesh.moradiya on 1/1/2016.
 */
public class DeleteConfirmationDialog extends Dialog{

    private Activity mContext;
    private String message;

    public DeleteConfirmationDialog(Activity context, String message) {
        super(context);
        this.mContext = context;
        this.message = message;
    }


}
