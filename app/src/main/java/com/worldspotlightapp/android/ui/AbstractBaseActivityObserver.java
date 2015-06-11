package com.worldspotlightapp.android.ui;

import java.util.Observer;

/**
 * Created by jiahaoliuliu on 2/21/15.
 * This class extends the {@link AbstractBaseActivity } to include the observer
 * patterns. Note this is still an abstract class, so the extended class still need to implements the method
 * update that comes from teh Observer.
 * The main purpose of this class is override the method onPause, making sure that when the activity is
 * paused, it id unregistered on all the modules.
 *
 * To implement this class the method buildLayout must be overrided. This method is called in onResume
 * to check if the actual layout has been built.
 */
public abstract class AbstractBaseActivityObserver extends AbstractBaseActivity implements Observer {

    protected boolean mIsInForeground;

    /**
     * method used to process possible data retrieved
     * from backend while this activity is in the background
     */
    protected abstract void processDataIfExists();

    @Override
    protected void onResume() {
        super.onResume();
        mIsInForeground = true;
        processDataIfExists();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsInForeground = false;
    }
}
