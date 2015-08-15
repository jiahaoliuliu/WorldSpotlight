package com.worldspotlightapp.android.ui;

import android.util.Log;

import java.util.Observer;

/**
 *
 * This class extends from {@link AbstractBaseFragment} bring all the modules availables. The main
 * function is use it as observer by implementing PostmanPattern for Fragment.
 *
 * @see <a href="http://www.jiahaoliuliu.com/2015/06/postman-pattern.html">Postman Pattern</a>
 *
 * @author jiahaoliuliu on 15/8/15.
 */
public abstract class AbstractBaseFragmentObserver extends AbstractBaseFragment implements Observer{

    private static final String TAG = "AbstractBaseFragmentObs";

    private boolean mIsInForeground;

    /**
     * method used to process possible data retrieved
     * from backend while this activity is in the background
     */
    protected abstract void processDataIfExists();

    // For the view pager, onResume is needed condition but not sufficient to make sure the
    // fragment is in foreground. This is because the view pager adapter loads the "next"
    // fragment by default. For the purpose of this pattern this is enough.
    @Override
    public void onResume() {
        mIsInForeground = true;
        super.onResume();
        Log.v(TAG, "On Resume. The fragment is in foreground");
        processDataIfExists();
    }

    @Override
    public void onPause() {
        mIsInForeground = false;
        super.onPause();
        Log.v(TAG, "On Pause. The fragmen is not in foreground");
    }

    public boolean isInForeground() {
        Log.v(TAG, "Checking if the fragment is in foreground or not " + mIsInForeground);
        return mIsInForeground;
    }

}
