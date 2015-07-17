package com.worldspotlightapp.android.maincontroller.modules.notificationmodule;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.worldspotlightapp.android.R;


public class NotificationModule implements INotificationModule {

    private Context mContext;

    /**
     * Loading dialog
     */
    private ProgressDialog loadingDialog;

    public NotificationModule(Context context) {
        this.mContext = context;
    }

    @Override
    public void showLoadingDialog(Context context) {
        showLoadingDialog(context, R.string.dialog_loading_generic_message);
    }

    @Override
    public void showLoadingDialog(Context context, int resId) {
        showLoadingDialog(context, context.getString(resId));
    }

    @Override
    public void showLoadingDialog(Context context, String message) {
        // If it was already showing, don't do anything
        if (isLoadingDialogShowing()) {
            return;
        }

        // If the loading dialog has not been built, build it
        loadingDialog = new ProgressDialog(context);
        loadingDialog.setMessage(message);
        loadingDialog.setIndeterminate(true);
        loadingDialog.setCancelable(true);
        loadingDialog.show();
    }

    @Override

    public void dismissLoadingDialog() {
        if (isLoadingDialogShowing()) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }

    @Override
    public void showAlertDialog(
            Context context, String title, String message,
            String positiveButtonText, DialogInterface.OnClickListener positiveButtonOnClickListener,
            String negativeButtonText, DialogInterface.OnClickListener negativeButtonOnClickListener) {

        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButtonText, positiveButtonOnClickListener)
                .setNegativeButton(negativeButtonText, negativeButtonOnClickListener)
                .create()
                .show();
    }

    private boolean isLoadingDialogShowing() {
        return (loadingDialog != null && loadingDialog.isShowing());
    }


    @Override
    public void showToast(String text, boolean longDuration) {
        int duration = longDuration ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
        Toast.makeText(mContext, text, duration).show();
    }

    @Override
    public void showToast(int resId, boolean longDuration) {
        int duration = longDuration ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
        Toast.makeText(mContext, resId, duration).show();
    }

}
