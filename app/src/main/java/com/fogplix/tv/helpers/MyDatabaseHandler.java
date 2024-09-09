package com.fogplix.tv.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.fogplix.tv.model.AnimeFavoriteListModel;
import com.fogplix.tv.model.LastEpisodeWatchedModel;
import com.fogplix.tv.params.DatabaseParams;

import java.util.ArrayList;
import java.util.List;

public class MyDatabaseHandler extends SQLiteOpenHelper {

    public MyDatabaseHandler(Context context) {
        super(context, DatabaseParams.DB_NAME, null, DatabaseParams.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String CREATE_FAVORITE_ANIME_TABLE = "CREATE TABLE " + DatabaseParams.FAVORITE_ANIME_TABLE +
                "("
                + DatabaseParams.KEY_ANIME_ID + " TEXT,"
                + DatabaseParams.KEY_ANIME_NAME + " TEXT,"
                + DatabaseParams.KEY_ANIME_IMAGE_URL + " TEXT,"
                + DatabaseParams.KEY_ANIME_SERVER + " TEXT"
                + ")";

        sqLiteDatabase.execSQL(CREATE_FAVORITE_ANIME_TABLE);

        String CREATE_LAST_WATCHED_EP_TABLE =
                "CREATE TABLE " + DatabaseParams.LAST_WATCHED_EPISODES_TABLE +
                "(" + DatabaseParams.KEY_ANIME_ID + " TEXT," + DatabaseParams.KEY_LAST_EPISODE_ID + " TEXT" + ")";

        sqLiteDatabase.execSQL(CREATE_LAST_WATCHED_EP_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        try {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DatabaseParams.FAVORITE_ANIME_TABLE);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DatabaseParams.LAST_WATCHED_EPISODES_TABLE);
            onCreate(sqLiteDatabase);

            Log.d("Nur Alam", "onUpgrade success");
        }
        catch (Exception e){
            Log.d("Nur Alam", "onUpgrade failed: " + e.getMessage());
        }
    }

//--------------------------------------------------------------------------------------------------

    public String getLastWatchedEpisodeId(String animeId){

        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + DatabaseParams.LAST_WATCHED_EPISODES_TABLE + " WHERE " + DatabaseParams.KEY_ANIME_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[] { animeId });

        String episodeId = "";

        if (cursor != null && cursor.moveToFirst()) {
            episodeId = cursor.getString(1);
            cursor.close();
        }
        return episodeId;
    }

    public void addLastWatchedEpisode(LastEpisodeWatchedModel lastEpisodeWatchedModel){

        SQLiteDatabase db = this.getWritableDatabase();

        if (checkIfLastWatchedAnimeAddedOrNot(lastEpisodeWatchedModel)){
            updateLastWatchedEpisode(lastEpisodeWatchedModel);
        }
        else{

            ContentValues values = new ContentValues();

            values.put(DatabaseParams.KEY_ANIME_ID, lastEpisodeWatchedModel.getAnimeId());
            values.put(DatabaseParams.KEY_LAST_EPISODE_ID, lastEpisodeWatchedModel.getEpisodeId());

            db.insert(DatabaseParams.LAST_WATCHED_EPISODES_TABLE, null, values);
            db.close();
        }
    }

    public void updateLastWatchedEpisode(LastEpisodeWatchedModel lastEpisodeWatchedModel){

        SQLiteDatabase db = this.getWritableDatabase();

        String animeId = lastEpisodeWatchedModel.getAnimeId();
        String episodeId = lastEpisodeWatchedModel.getEpisodeId();

        String query = "UPDATE " + DatabaseParams.LAST_WATCHED_EPISODES_TABLE + " SET "
                + DatabaseParams.KEY_LAST_EPISODE_ID + " = ? WHERE " + DatabaseParams.KEY_ANIME_ID + " = ?";

        db.execSQL(query, new String[] {episodeId, animeId});
    }

    public boolean checkIfLastWatchedAnimeAddedOrNot(LastEpisodeWatchedModel lastEpisodeWatchedModel){

        SQLiteDatabase db = this.getReadableDatabase();

        String animeId = lastEpisodeWatchedModel.getAnimeId();

        Cursor cursor = db.query(DatabaseParams.LAST_WATCHED_EPISODES_TABLE, new String[] {DatabaseParams.KEY_ANIME_ID,
                        DatabaseParams.KEY_LAST_EPISODE_ID}, DatabaseParams.KEY_ANIME_ID + "=?",
                new String[] { animeId }, null, null, null, null);

        if (cursor.moveToFirst()){
            cursor.close();
            return true;
        }
        else {
            cursor.close();
            return false;
        }
    }
//--------------------------------------------------------------------------------------------------

    public void addAnimeToFavorite(AnimeFavoriteListModel favoriteListModel){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(DatabaseParams.KEY_ANIME_ID, favoriteListModel.getAnimeId());
        values.put(DatabaseParams.KEY_ANIME_NAME, favoriteListModel.getAnimeName());
        values.put(DatabaseParams.KEY_ANIME_IMAGE_URL, favoriteListModel.getAnimeImageUrl());
        values.put(DatabaseParams.KEY_ANIME_SERVER, favoriteListModel.getAnimeServer());

        db.insert(DatabaseParams.FAVORITE_ANIME_TABLE, null, values);
        db.close();
    }

//--------------------------------------------------------------------------------------------------

    public AnimeFavoriteListModel getAnimeFromFavorite(String animeId){

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(DatabaseParams.FAVORITE_ANIME_TABLE, new String[] {DatabaseParams.KEY_ANIME_ID,
                        DatabaseParams.KEY_ANIME_NAME, DatabaseParams.KEY_ANIME_IMAGE_URL, DatabaseParams.KEY_ANIME_SERVER}, DatabaseParams.KEY_ANIME_ID + "=?",
                new String[] { animeId }, null, null, null, null);

        AnimeFavoriteListModel favoriteListModel = new AnimeFavoriteListModel();

        if (cursor != null && cursor.moveToFirst()){

            cursor.moveToFirst();

            favoriteListModel.setAnimeId(cursor.getString(0));
            favoriteListModel.setAnimeName(cursor.getString(1));
            favoriteListModel.setAnimeImageUrl(cursor.getString(2));

            cursor.close();
        }

        return favoriteListModel;
    }

//--------------------------------------------------------------------------------------------------

    public boolean deleteAnimeFromFavorite(String animeId){

        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(DatabaseParams.FAVORITE_ANIME_TABLE, DatabaseParams.KEY_ANIME_ID + "=?", new String[]{animeId}) > 0;
    }
//--------------------------------------------------------------------------------------------------

    public void deleteAllAnimeFromFavorite(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + DatabaseParams.FAVORITE_ANIME_TABLE);
    }
//--------------------------------------------------------------------------------------------------

    public void getAllFavoriteAnime(OnRetrievedAllAnimeListener listener) {

        List<AnimeFavoriteListModel> favoriteListModelList = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + DatabaseParams.FAVORITE_ANIME_TABLE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                AnimeFavoriteListModel favoriteAnimeModel = new AnimeFavoriteListModel(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
                favoriteListModelList.add(favoriteAnimeModel);

            } while (cursor.moveToNext());
        }

        cursor.close();

        listener.onComplete(favoriteListModelList);
    }

//    ----------------------------------------------------------------------------------------------

    public interface OnRetrievedAllAnimeListener {
        void onComplete(List<AnimeFavoriteListModel> allFavoriteAnime);
    }
}
