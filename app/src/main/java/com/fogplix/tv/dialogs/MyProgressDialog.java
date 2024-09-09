package com.fogplix.tv.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.widget.TextView;

import com.fogplix.tv.R;

public class MyProgressDialog {

    private final Dialog dialog;
    private final TextView progressTV;

    public MyProgressDialog(Activity activity) {
        dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dialog_my_progress);
        progressTV = dialog.findViewById(R.id.progressTV);
    }

    public void setMessage(String text) {
        progressTV.setText(text);
    }

    public void setCancelable(boolean cancelable) {
        dialog.setCancelable(cancelable);
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }
}
