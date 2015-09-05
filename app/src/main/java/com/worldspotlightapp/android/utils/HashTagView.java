package com.worldspotlightapp.android.utils;

import android.content.Context;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import com.worldspotlightapp.android.R;

/**
 * Created by jiahaoliuliu on 01/09/31.
 */
public class HashTagView extends ClickableSpan {

    private static final String TAG = "HashTag";
    private Context mContext;

    public HashTagView(Context context) {
        super();
        mContext = context;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setColor(mContext.getResources().getColor(R.color.primary));
    }

    @Override
    public void onClick(View widget) {
        throw new IllegalStateException("You must override onclick method");
    }
}
