package com.fogplix.tv.helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.content.FileProvider;

import com.fogplix.tv.BuildConfig;
import com.fogplix.tv.R;
import com.fogplix.tv.params.Statics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CustomMethods {

    private static final String TAG = "MADARA";

    public static String timeFormatterFromSeconds(int seconds) {
        return LocalTime.ofSecondOfDay(seconds) + "";
    }

    //----------------------------------------------------------------------------------------------
    public static String getDateTime(){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyy_hhmmss");

        return formatter.format(now);
    }
    //----------------------------------------------------------------------------------------------
    public static String extractEpisodeNumberFromId(String episodeId) {
        String[] parts = episodeId.split("-");
        return parts[parts.length - 1];
    }
//--------------------------------------------------------------------------------------------------

    public static String getVersionName(Context context) {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        return packageInfo.versionName;
    }

    public static int getVersionCode(Context context) {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        return packageInfo.versionCode;
    }
//--------------------------------------------------------------------------------------------------

    public static JSONArray mergeMultiJsonArray(JSONArray... arrays) {

        JSONArray outArray = new JSONArray();

        for (JSONArray array : arrays) {
            for (int i = 0; i < array.length(); i++) {
                outArray.put(array.optJSONObject(i));
            }
        }
        return outArray;
    }

    public static void mergeTwoJsonArray(JSONArray oldArray, JSONArray newArray) throws JSONException {

        for (int i = 0; i < newArray.length(); i++) {
            oldArray.put(newArray.getJSONObject(i));
        }
    }

    //--------------------------------------------------------------------------------------------------

    public static String capitalize(String sentence) {
        if (sentence == null){
            return "";
        } else {
            // Split the sentence into words
            String[] words = sentence.split(" ");

            // Capitalize the first letter of each word
            StringBuilder capitalizedSentence = new StringBuilder();
            for (String word : words) {
                if (!word.isEmpty()) {
                    char firstLetter = Character.toUpperCase(word.charAt(0));
                    String restOfWord = word.substring(1);
                    capitalizedSentence.append(firstLetter).append(restOfWord).append(" ");
                }
            }

            // Remove the trailing space
            capitalizedSentence.deleteCharAt(capitalizedSentence.length() - 1);

            return capitalizedSentence.toString();
        }
    }

    //--------------------------------------------------------------------------------------------------
    public static boolean isInternetOn(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();
        return nInfo != null && nInfo.isConnectedOrConnecting();
    }

    //--------------------------------------------------------------------------------------------------
    public static void errorAlert(Activity activity, String errorTitle, String errorBody, String actionButton, boolean shouldGoBack) {

        if (!activity.isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(errorTitle);
            builder.setMessage(errorBody);
            builder.setIcon(R.drawable.warning);
            builder.setPositiveButton(actionButton, (dialogInterface, i) -> {
                if (shouldGoBack) {
                    activity.finish();
                } else {
                    dialogInterface.dismiss();
                }
            });
            builder.setNegativeButton("Report", (dialog, which) -> {
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + activity.getString(R.string.feedback_email) + "?subject= FogPlix Error v" + BuildConfig.VERSION_NAME + "&body=" + errorBody)));
                activity.finish();
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
//--------------------------------------------------------------------------------------------------

    public static void showKeyBoard(Activity activity, EditText editText) {
        if (editText.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            }
            activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    public static void hideKeyboard(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    //--------------------------------------------------------------------------------------------------
    public static void checkPlayableServersStatus(Context context) {

        new Thread(() -> {

            try {
                String playableServers =
                        Jsoup
                                .connect(context.getString(R.string.playable_servers_status_json_link))
                                .timeout(30000)
                                .ignoreContentType(true)
                                .execute().body();

                if (!playableServers.equalsIgnoreCase("")) {

                    JSONObject object = new JSONObject(playableServers);

                    boolean server_1 = object.getBoolean("server-1");
                    boolean server_2 = object.getBoolean("server-2");
                    boolean server_3 = object.getBoolean("server-3");

                    HPSharedPreference hpSharedPreference = new HPSharedPreference(context);
                    hpSharedPreference.savePlayableServersStatus(server_1, server_2, server_3);
                }
            } catch (Exception e) {
                Log.e(TAG, "checkPlayableServersStatus: ", e);
            }
        }).start();
    }

//--------------------------------------------------------------------------------------------------

    public static void checkNewNotice(Context context, TextView textView) {

        int currentVersionCode = BuildConfig.VERSION_CODE;

        new Thread(() -> {

            try {
                String newNoticeJSON =
                        Jsoup
                                .connect(context.getString(R.string.new_notice_json_link))
                                .timeout(30000)
                                .ignoreContentType(true)
                                .execute().body();

                if (!newNoticeJSON.equalsIgnoreCase("")) {

                    JSONObject object = new JSONObject(newNoticeJSON);

                    int maxVersionCode = object.getInt("maxVersionCode");

                    boolean shouldShow = object.getBoolean("shouldShow");

                    String noticeMessage = object.getString("message");

                    if (maxVersionCode >= currentVersionCode) {

                        new Handler(Looper.getMainLooper()).post(() -> {

                            try {
                                if (shouldShow) {
                                    textView.setVisibility(View.VISIBLE);
                                    textView.setText(noticeMessage);
                                    textView.setOnClickListener(v -> v.setVisibility(View.GONE));
                                } else {
                                    textView.setVisibility(View.GONE);
                                }
                            } catch (Exception e) {
                                Log.d(TAG, "checkNewNotice Error 2: " + e.getMessage());
                            }
                        });
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "checkNewNotice: ", e);
            }
        }).start();
    }
//--------------------------------------------------------------------------------------------------

    @SuppressLint("UseCompatLoadingForDrawables")
    public static void checkForUpdateOnStartApp(Activity activity) {

        int currentVersionCode = BuildConfig.VERSION_CODE;

        new Thread(() -> {

            try {
                String versionInfoJSON =
                        Jsoup.connect(activity.getString(R.string.version_page_link))
                                .timeout(30000)
                                .ignoreContentType(true)
                                .execute().body();

                if (!versionInfoJSON.equalsIgnoreCase("")) {

                    JSONObject object = new JSONObject(versionInfoJSON);

                    String downloadLink = object.getString("download-link");
                    String directDownloadLink = object.getString("direct-download-link");
                    String versionName = object.getString("version-name");
                    String forceUpdate = object.getString("force-update");
                    int updatedVersionCode = Integer.parseInt(object.getString("version-code"));

                    if (updatedVersionCode > currentVersionCode) {

                        new Handler(Looper.getMainLooper()).post(() -> {

                            try {
                                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                builder.setCancelable(false);
                                builder.setTitle("Update Available (v" + versionName + ")");
                                builder.setIcon(R.drawable.update);
                                builder.setMessage(object.getString("changes-log"));

                                if (forceUpdate.equalsIgnoreCase("no")){
                                    builder.setNeutralButton("Cancel", ((dialog, which) -> dialog.dismiss()));
                                }

                                builder.setNegativeButton("Website", (dialogInterface, i) -> {
                                    dialogInterface.dismiss();
                                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(downloadLink)));
                                });

                                builder.setPositiveButton("Update", (dialogInterface, i) -> {
                                    dialogInterface.dismiss();

                                    String downloadPath = Objects.requireNonNull(activity.getExternalFilesDir(null)) + "/fogplix_v" + versionName + "_t" + getDateTime() + ".apk";

                                    ProgressDialog pd = new ProgressDialog(activity);
                                    pd.setCancelable(false);
                                    pd.setMessage("Don't close the app. \nDownloading 0%");
                                    pd.show();

                                    new Thread(() -> {

                                        try {
                                            URL url = new URL(directDownloadLink);
                                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                                            connection.setInstanceFollowRedirects(true);
                                            connection.connect();

                                            int totalSize = connection.getContentLength();

                                            InputStream input = connection.getInputStream();

                                            FileOutputStream output = new FileOutputStream(downloadPath);
                                            byte[] data = new byte[1024];
                                            int count;
                                            int downloadedSize = 0;

                                            while ((count = input.read(data)) != -1) {
                                                downloadedSize += count;
                                                int percent = (downloadedSize * 100) / totalSize;
                                                output.write(data, 0, count);
                                                new Handler(Looper.getMainLooper()).post(() -> pd.setMessage("Don't close the app. \nDownloading " + percent + "%"));
                                            }

                                            output.flush();
                                            output.close();
                                            input.close();

                                            new Handler(Looper.getMainLooper()).post(pd::dismiss);

                                            installApk(activity, downloadPath);

                                        } catch (Exception e) {
                                            Log.e(TAG, "checkForUpdateOnStartApp: ", e);
                                        }
                                    }).start();
                                });

                                builder.show();

                            } catch (Exception e) {
                                Log.d(TAG, "checkForUpdateOnStartApp Error 2: " + e.getMessage());
                            }
                        });
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "checkForUpdateOnStartApp: ", e);
            }
        }).start();
    }

    private static void installApk(Activity activity, String filePath) {

        File file = new File(filePath);

        if (file.exists()) {

            Uri fileUri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".provider", file);

            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            intent.setData(fileUri);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            activity.startActivity(intent);
        }
    }


    public static String encryptStringAES(String data, String key, String iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));

        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

        byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decryptStringAES(String encryptedData, String key, String iv) throws Exception {

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));

        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

        byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);

        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }


    public static String generateEncryptAjaxParameters(Document html, String id) throws Exception {

        String encryptedId = encryptStringAES(id, Statics.firstKey, Statics.iv);

        Element scriptTag = html.select("script[data-name=episode]").first();

        assert scriptTag != null;
        String encryptedToken = scriptTag.attr("data-value");

        String token = decryptStringAES(encryptedToken, Statics.firstKey, Statics.iv);

//        return "id=" + encryptedId + "&alias=" + token;
        return "id=" + encryptedId + "&alias=" + id + "&" + token;
    }

    public static JSONObject decryptEncryptAjaxResponse(String obj) throws Exception {

        String decrypted = decryptStringAES(obj, Statics.secondKey, Statics.iv);

        return new JSONObject(decrypted);
    }


    public static String getIdFromQuery(String query) {
        String idKey = "id=";
        int startIndex = query.indexOf(idKey);
        if (startIndex == -1) {
            return null; // or throw an exception if id is not found
        }
        startIndex += idKey.length();
        int endIndex = query.indexOf("&", startIndex);
        if (endIndex == -1) {
            endIndex = query.length(); // id is at the end of the query string
        }
        return query.substring(startIndex, endIndex);
    }


}