package com.fogplix.tv.helpers;

import android.view.View;

public class DoubleClickListener implements View.OnClickListener {

    private final long doubleClickTimeLimitMills;
    private final DoubleClickCallback callback;

    private long lastClicked = -1L;

    public DoubleClickListener(long doubleClickTimeLimitMills, DoubleClickCallback callback){

        this.doubleClickTimeLimitMills = doubleClickTimeLimitMills;
        this.callback = callback;
    }

    @Override
    public void onClick(View view) {

        if (lastClicked == -1L){
            lastClicked = System.currentTimeMillis();
        }
        else if (isDoubleClicked()){
            callback.doubleClicked();
            lastClicked = -1L;
        }
        else{
            lastClicked = System.currentTimeMillis();
        }
    }


    private long getTimeDiff(long from, long to) {
        return to - from;
    }

    private boolean isDoubleClicked(){
        return getTimeDiff(lastClicked, System.currentTimeMillis()) <= doubleClickTimeLimitMills;
    }

    public interface DoubleClickCallback {
        void doubleClicked();
    }
}
