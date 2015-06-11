package com.worldspotlightapp.android.maincontroller.modules.notificationmodule;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

import com.worldspotlightapp.android.R;


public class NotificationModule implements INotificationModule {

    public static final boolean LOG_CONNECTIONS = true;

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
        loadingDialog.setCancelable(false);
        loadingDialog.show();
    }

    @Override
    public void dismissLoadingDialog() {
        if (isLoadingDialogShowing()) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }

    private boolean isLoadingDialogShowing() {
        return (loadingDialog != null && loadingDialog.isShowing());
    }

    @Override
    public void showAlertDialog() {
        // TODO Auto-generated method stub

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
