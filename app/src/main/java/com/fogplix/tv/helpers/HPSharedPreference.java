package com.fogplix.tv.helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class HPSharedPreference {

    private final SharedPreferences mPreference;
    private final SharedPreferences.Editor mPrefEditor;

    public HPSharedPreference(Context context) {

        String APP_PREFS_NAME = context.getPackageName();

        this.mPreference = context.getSharedPreferences(APP_PREFS_NAME, Context.MODE_PRIVATE);
        this.mPrefEditor = mPreference.edit();
    }

//    ----------------------------------------------------------------------------------------------
    public void savePlayableServersStatus(boolean... servers){

        int i = 1;

        for (boolean server : servers) {

            String serverName = "server_" + i;
            mPrefEditor.putBoolean(serverName, server);
            mPrefEditor.commit();
            i++;
        }
    }

    public boolean getPlayableServersStatus(String serverName){
        return  mPreference.getBoolean(serverName, true);
    }
//    ----------------------------------------------------------------------------------------------

    public void saveLastWatchedEpisode(String episodeId){
        mPrefEditor.putString("episodeId", episodeId);
        mPrefEditor.commit();
    }

    public void saveLastWatchedEpisodeServer(String server){
        mPrefEditor.putString("server", server);
        mPrefEditor.commit();
    }

    public String getLastWatchedEpisode(){
        return  mPreference.getString("episodeId", "");
    }

    public String getLastWatchedEpisodeServer(){
        return  mPreference.getString("server", "");
    }

    public void saveCurrentVideoPosOfLastPlayedVideo(long videoPos){
        mPrefEditor.putLong("lastVideoPosition", videoPos);
        mPrefEditor.commit();
    }

    public long getCurrentVideoPosOfLastPlayedVideo(){
        return mPreference.getLong("lastVideoPosition", 0);
    }
}
