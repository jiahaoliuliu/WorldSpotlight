package com.worldspotlightapp.android.maincontroller.modules.notificationmodule;

import android.content.Context;
import android.content.DialogInterface;

public interface INotificationModule {

    /**
     * Show a loading dialog which blocks the interaction with other elements of the UI.
     * 
     * @param context
     *            The context in which the loading dialog will be shown. It is important to enter
     *            the actual activity context. Otherwise the loading dialog won't be shown
     * 
     */
    public void showLoadingDialog(Context context);

    /**
     * Show a loading dialog which blocks the interaction with other elements of the UI. The text is
     * customized
     * 
     * @param context
     *            The context of the activity which this dialog will be shown.
     * @param message
     *            The customized message to be shown
     */
    public void showLoadingDialog(Context context, String message);

    /**
     * Show a loading dialog which blocks the interaction with other elements of the UI. The text is
     * customized
     * 
     * @param context
     *            The context of the activity which this dialog will be shown.
     * @param resId
     *            The customized message resource id to be shown
     */
    public void showLoadingDialog(Context context, int resId);

    /**
     * Dismiss the loading dialog which should be shown at the moment of call. If the loading dialog
     * is not shown, don't do anything
     */
    public void dismissLoadingDialog();

    /**
     * Show an alert dialog
     */
    public void showAlertDialog(
            Context context,
            String title,
            String message,
            String positiveButtonText,
            DialogInterface.OnClickListener positiveButtonOnClickListener,
            String negativeButtonText,
            DialogInterface.OnClickListener negativeButtonOnClickListener);

    /**
     * Show a normal toast
     * 
     * @param text
     *            The toast to be shown
     * @param longDuration
     *            If it has extra duration or not
     */
    public void showToast(String text, boolean longDuration);

    /**
     * Show a normal toast
     * 
     * @param resId
     *            The id of the string to be shown
     * @param longDuration
     *            If it has extra duration or not
     */
    public void showToast(int resId, boolean longDuration);

}
