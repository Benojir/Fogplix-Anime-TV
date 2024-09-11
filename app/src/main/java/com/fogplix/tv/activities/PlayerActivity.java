package com.fogplix.tv.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.TrackGroup;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.HttpDataSource;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.dash.DashMediaSource;
import androidx.media3.exoplayer.drm.DefaultDrmSessionManager;
import androidx.media3.exoplayer.drm.DrmSessionManager;
import androidx.media3.exoplayer.drm.FrameworkMediaDrm;
import androidx.media3.exoplayer.drm.HttpMediaDrmCallback;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.TrackGroupArray;
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.trackselection.ExoTrackSelection;
import androidx.media3.exoplayer.trackselection.MappingTrackSelector;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;
import androidx.preference.PreferenceManager;

import com.fogplix.tv.BuildConfig;
import com.fogplix.tv.R;
import com.fogplix.tv.helpers.CustomMethods;
import com.fogplix.tv.helpers.DoubleClickListener;
import com.fogplix.tv.helpers.GenerateDirectLink;
import com.fogplix.tv.helpers.MyDatabaseHandler;
import com.fogplix.tv.model.LastEpisodeWatchedModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.UUID;

@UnstableApi
public class PlayerActivity extends AppCompatActivity {

    private static final String TAG = "MADARA";
    private LinearLayout apiLoadersContainer;
    private TextView episodeLoadingTV;
    private RelativeLayout playerComponentsContainer;
    private ProgressBar bufferingProgressBar;
    private PlayerView exoPlayerView;
    private String refererUrl = "";
    private Uri videoUri2;
    private ExoPlayer exoPlayer;
    private LinearLayout brightnessVolumeContainer;
    private ImageView volumeIcon, brightnessIcon;
    private TextView brightVolumeTV;
    private GestureDetectorCompat gestureDetectorCompat;
    private int brightness = 0;
    private int volume = 0;
    private AudioManager audioManager;
    private final int SHOW_MAX_BRIGHTNESS = 100;
    private final int SHOW_MAX_VOLUME = 50;
    private ImageButton qualityBtn, backButton, fitScreenBtn, backward10, forward10, previousEpisode, nextEpisode;
    private Button skipIntroOutroBtn;
    private TextView videoNameTV, episodeNumTV;
    private String episodeId;
    private String animeTitle;
    private String nextEpisodeId = "";
    private String previousEpisodeId = "";
    private DefaultTrackSelector defaultTrackSelector;
    private ArrayList<String> videoQualities;
    private int selectedQualityIndex = 0;
    private Button doubleTapSkipBackIcon, doubleTapSkipForwardIcon;
    private int touchPositionX;
    private boolean shouldShowController = true;
    private boolean playWhenReady = true;
    private long playbackPosition = C.TIME_UNSET;
    private SharedPreferences preferences;

    private static final CookieManager DEFAULT_COOKIE_MANAGER;

    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
    }

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );

        setContentView(R.layout.activity_player);

        ////////////////////////////////////////////////////////////////////////////////////////////

        hideSystemUI();
        initVars();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        MyDatabaseHandler dbHandler = new MyDatabaseHandler(this);

        ////////////////////////////////////////////////////////////////////////////////////////////

        episodeId = getIntent().getStringExtra("episodeId");

        if (episodeId == null) {
            CustomMethods.errorAlert(this, "Error", "Episode ID is null.", "OK", true);
            return;
        }

        //------------------------------------------------------------------------------------------

        episodeLoadingTV.setText("Loading episode " + CustomMethods.extractEpisodeNumberFromId(episodeId));

        GenerateDirectLink generateDirectLink = new GenerateDirectLink(this);

        generateDirectLink.generate(episodeId, new GenerateDirectLink.OnGenerateDirectLink() {
            @Override
            public void onComplete(JSONObject object) {
                try {
                    apiLoadersContainer.setVisibility(View.GONE);
                    playerComponentsContainer.setVisibility(View.VISIBLE);

                    animeTitle = object.getString("animeTitle");
                    refererUrl = object.getString("referer");
                    String animeId = object.getString("animeId");
                    String videoHLSUrl = object.getString("videoHLSUrl");
                    String videoHLSUrl2 = object.getString("videoHLSUrl2");
                    previousEpisodeId = object.getString("previousEpisodeId");
                    nextEpisodeId = object.getString("nextEpisodeId");

                    //-----------Below code for showing/hiding previous and next episode------------

                    if (previousEpisodeId.isEmpty()) {
                        previousEpisode.setVisibility(View.INVISIBLE);
                    } else {
                        previousEpisode.setVisibility(View.VISIBLE);
                    }

                    if (nextEpisodeId.isEmpty()) {
                        nextEpisode.setVisibility(View.INVISIBLE);
                    } else {
                        nextEpisode.setVisibility(View.VISIBLE);
                    }

                    //------------------------------------------------------------------------------

                    Uri videoUri = Uri.parse(videoHLSUrl);
                    videoUri2 = Uri.parse(videoHLSUrl2);

                    initializePlayer(videoUri, false);

                    //--------------------Below code for each anime last episode--------------------
                    LastEpisodeWatchedModel lastEpisodeWatchedModel = new LastEpisodeWatchedModel(animeId, episodeId);
                    dbHandler.addLastWatchedEpisode(lastEpisodeWatchedModel);

                } catch (JSONException e) {
                    CustomMethods.errorAlert(PlayerActivity.this, "Error", e.getMessage(), "OK", true);
                }
            }

            @Override
            public void onFailed(String error) {
                CustomMethods.errorAlert(PlayerActivity.this, "Error", error, "OK", true);
            }
        });


        //+++++++++++++++++++++++ Below section is handing button actions ++++++++++++++++++++++++++

        skipIntroOutroBtn.setOnClickListener(v -> {
            long currentVideoPosition = exoPlayer.getCurrentPosition();
            exoPlayer.seekTo(currentVideoPosition + 85000);
        });

        skipIntroOutroBtn.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                skipIntroOutroBtn.setTextColor(getColor(R.color.green));
            } else {
                skipIntroOutroBtn.setTextColor(getColor(R.color.white));
            }
        });

        previousEpisode.setOnClickListener(v -> {
            if (!previousEpisodeId.isEmpty()) {
                Intent intent = new Intent(PlayerActivity.this, PlayerActivity.class);
                intent.putExtra("episodeId", previousEpisodeId);
                startActivity(intent);
                finish();
            }
        });

        nextEpisode.setOnClickListener(v -> {
            if (!nextEpisodeId.isEmpty()) {
                Intent intent = new Intent(PlayerActivity.this, PlayerActivity.class);
                intent.putExtra("episodeId", nextEpisodeId);
                startActivity(intent);
                finish();
            }
        });

        backward10.setOnClickListener(view -> exoPlayer.seekTo(exoPlayer.getCurrentPosition() - 10000));
        forward10.setOnClickListener(view -> exoPlayer.seekTo(exoPlayer.getCurrentPosition() + 10000));

        qualityBtn.setOnClickListener(view -> {
            if (videoQualities != null) {
                if (!videoQualities.isEmpty()) {
                    getQualityChooserDialog(this, videoQualities);
                } else {
                    Toast.makeText(this, "No video quality found.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Wait until video start.", Toast.LENGTH_SHORT).show();
            }
        });

        fitScreenBtn.setOnClickListener(v -> {
            if (exoPlayerView.getResizeMode() == AspectRatioFrameLayout.RESIZE_MODE_FIT) {
                exoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
                fitScreenBtn.setImageResource(R.drawable.crop_5_4);
                Toast.makeText(this, "ZOOM", Toast.LENGTH_SHORT).show();
            } else if (exoPlayerView.getResizeMode() == AspectRatioFrameLayout.RESIZE_MODE_ZOOM) {
                exoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                fitScreenBtn.setImageResource(R.drawable.fit_screen);
                Toast.makeText(this, "FILL", Toast.LENGTH_SHORT).show();
            } else if (exoPlayerView.getResizeMode() == AspectRatioFrameLayout.RESIZE_MODE_FILL) {
                exoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                fitScreenBtn.setImageResource(R.drawable.crop_free);
                Toast.makeText(this, "FIT", Toast.LENGTH_SHORT).show();
            }

        });

        backButton.setOnClickListener(view -> {
            if (exoPlayer != null) {
                exoPlayer.stop();
                exoPlayer.release();
            }
            onBackPressed();
        });


        exoPlayerView.setOnTouchListener((view, motionEvent) -> {

            gestureDetectorCompat.onTouchEvent(motionEvent);

            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                brightnessVolumeContainer.setVisibility(View.GONE);

                if (!shouldShowController) {

                    exoPlayerView.setUseController(false);

                    new Handler().postDelayed(() -> {
                        shouldShowController = true;
                        exoPlayerView.setUseController(true);
                    }, 500);
                }
            }

            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                touchPositionX = (int) motionEvent.getX();
            }
            return false;
        });

        exoPlayerView.setOnClickListener(new DoubleClickListener(500, () -> {

            exoPlayerView.setUseController(false);
            new Handler().postDelayed(() -> exoPlayerView.setUseController(true), 500);

            int deviceWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

            if (touchPositionX < deviceWidth / 2) {
                doubleTapSkipBackIcon.setVisibility(View.VISIBLE);
                new Handler().postDelayed(() -> doubleTapSkipBackIcon.setVisibility(View.GONE), 500);
                exoPlayer.seekTo(exoPlayer.getCurrentPosition() - 10000);
            } else {
                doubleTapSkipForwardIcon.setVisibility(View.VISIBLE);
                new Handler().postDelayed(() -> doubleTapSkipForwardIcon.setVisibility(View.GONE), 500);
                exoPlayer.seekTo(exoPlayer.getCurrentPosition() + 10000);
            }
        }));

        //------------------------------------------------------------------------------------------

        gestureDetectorCompat = new GestureDetectorCompat(this, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(@NonNull MotionEvent motionEvent) {
                return false;
            }

            @Override
            public void onShowPress(@NonNull MotionEvent motionEvent) {

            }

            @Override
            public boolean onSingleTapUp(@NonNull MotionEvent motionEvent) {
                return false;
            }

            @Override
            public void onLongPress(@NonNull MotionEvent motionEvent) {

            }

            @Override
            public boolean onFling(MotionEvent motionEvent, @NonNull MotionEvent motionEvent1, float v, float v1) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent motionEvent, @NonNull MotionEvent motionEvent1, float distanceX, float distanceY) {

                int deviceWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

                if (Math.abs(distanceY) > Math.abs(distanceX)) {

                    if (motionEvent != null) {

                        brightnessVolumeContainer.setVisibility(View.VISIBLE);

                        shouldShowController = false;

                        if (motionEvent.getX() < deviceWidth / 2) {

                            volumeIcon.setVisibility(View.GONE);
                            brightnessIcon.setVisibility(View.VISIBLE);

                            boolean increase = distanceY > 0;

                            int newValue = (increase) ? brightness + 1 : brightness - 1;

                            if (newValue >= 0 && newValue <= SHOW_MAX_BRIGHTNESS) {
                                brightness = newValue;
                            }

                            brightVolumeTV.setText(String.valueOf(brightness));
                            setScreenBrightness(brightness);
                        } else {

                            if (audioManager != null) {

                                volumeIcon.setVisibility(View.VISIBLE);
                                brightnessIcon.setVisibility(View.GONE);

                                boolean increase = distanceY > 0;

                                int newValue = (increase) ? volume + 1 : volume - 1;

                                if (newValue >= 0 && newValue <= SHOW_MAX_VOLUME) {
                                    volume = newValue;
                                }

                                brightVolumeTV.setText(String.valueOf(volume));
                                setVolume(volume);
                            }
                        }
                    }
                }
                return true;
            }
        });

        //------------------------------------------------------------------------------------------

        /* This block of codes set the current device volume and brightness to the video on startup */
        brightness = (int) (getCurrentScreenBrightness() * 100);
        setVolumeVariable();
    }

    //--------------------------------------------------------------------------------------------------
    private void initializePlayer(Uri vUri, boolean isSecondSrc) {

        ExoTrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();

        defaultTrackSelector = new DefaultTrackSelector(this, videoTrackSelectionFactory);
        defaultTrackSelector.setParameters(defaultTrackSelector.getParameters().buildUpon()
                .setPreferredTextLanguage("en")
                .build());

        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this)
                .forceEnableMediaCodecAsynchronousQueueing()
                .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF);

        DefaultLoadControl loadControl = new DefaultLoadControl.Builder()
                .setBufferDurationsMs(DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
                        DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
                        DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                        DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS)
                .build();

        //*********************************************************************************

        exoPlayer = new ExoPlayer.Builder(this, renderersFactory)
                .setTrackSelector(defaultTrackSelector)
                .setLoadControl(loadControl)
                .build();

        if (vUri.toString().toLowerCase().contains(".m3u8") || vUri.toString().toLowerCase().contains(".m3u")) {
            MediaSource mediaSource = buildHlsMediaSource(vUri);
            exoPlayer.setMediaSource(mediaSource);
        } else if (vUri.toString().toLowerCase().contains(".mpd")) {
            MediaSource mediaSource = buildDashMediaSource(vUri);
            exoPlayer.setMediaSource(mediaSource);
        } else {
            exoPlayer.setMediaItem(MediaItem.fromUri(vUri));
        }

        exoPlayer.prepare();

        exoPlayer.setPlayWhenReady(playWhenReady);

        if (playbackPosition != C.TIME_UNSET) {
            exoPlayer.seekTo(playbackPosition);
        }

        exoPlayer.addListener(new Player.Listener() {

            @SuppressLint("SetTextI18n")
            @Override
            public void onPlaybackStateChanged(int playbackState) {

                if (playbackState == Player.STATE_BUFFERING) {
                    bufferingProgressBar.setVisibility(View.VISIBLE);
                }

                if (playbackState == Player.STATE_READY) {
                    skipIntroOutroBtn.setVisibility(View.VISIBLE);
                    exoPlayerView.setVisibility(View.VISIBLE);
                    bufferingProgressBar.setVisibility(View.GONE);
                    videoNameTV.setText(animeTitle.trim());
                    episodeNumTV.setText("Episode " + CustomMethods.extractEpisodeNumberFromId(episodeId));
                    videoQualities = getVideoQualitiesTracks();
                }

                if (playbackState == Player.STATE_ENDED) {
                    skipIntroOutroBtn.setVisibility(View.GONE);

                    if (preferences.getBoolean("auto_play_next_episode", false)) {
                        if (!nextEpisodeId.isEmpty()) {
                            Intent intent = new Intent(PlayerActivity.this, PlayerActivity.class);
                            intent.putExtra("episodeId", nextEpisodeId);
                            startActivity(intent);
                            finish();
                        }
                    }
                }
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {

                exoPlayerView.setVisibility(View.GONE);
                bufferingProgressBar.setVisibility(View.GONE);

                if (isSecondSrc) {

                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(PlayerActivity.this);
                    alertBuilder.setTitle("Error Playing");
                    alertBuilder.setMessage(getString(R.string.episode_playing_failed_message) + error.getErrorCodeName() + " " + error.getMessage());
                    alertBuilder.setCancelable(false);

                    alertBuilder.setPositiveButton("Try Web Version", (dialog, which) -> {
                        if (refererUrl.isEmpty()) {
                            Toast.makeText(PlayerActivity.this, "Cannot play this video.", Toast.LENGTH_SHORT).show();
                            onBackPressed();
                        } else {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(refererUrl));

                            if (intent.resolveActivity(getPackageManager()) != null) {
                                startActivity(intent);
                            } else {
                                Toast.makeText(PlayerActivity.this, "Cannot play this video.", Toast.LENGTH_SHORT).show();
                                onBackPressed();
                            }

                            new Handler().postDelayed(() -> finish(), 2000);
                        }
                    });

                    alertBuilder.setNeutralButton("Exit", (dialog, which) -> finish());

                    alertBuilder.setNegativeButton("Report Us", (dialog, which) -> {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + getString(R.string.feedback_email) + "?subject=" + getString(R.string.app_name) + " Playing Error v" + BuildConfig.VERSION_NAME + "&body=" + error.getMessage() + "\n" + error.getErrorCodeName() + "\n" + episodeId + "\n" + vUri)));
                        finish();
                    });

                    AlertDialog dialog = alertBuilder.create();
                    dialog.show();
                } else {
                    Toast.makeText(PlayerActivity.this, "Trying second server.", Toast.LENGTH_SHORT).show();
                    exoPlayer.stop();
                    exoPlayer.release();

                    bufferingProgressBar.setVisibility(View.VISIBLE);
                    initializePlayer(videoUri2, true);
                }
            }
        });

        exoPlayerView.setPlayer(exoPlayer);
        exoPlayerView.setShowNextButton(false);
        exoPlayerView.setShowPreviousButton(false);
        exoPlayerView.setControllerShowTimeoutMs(2500);
    }

    //--------------------------------------------------------------------------------------------------

    private DefaultDrmSessionManager buildDrmSessionManager(UUID uuid, String licenseUrl, String userAgent) {
        HttpDataSource.Factory licenseDataSourceFactory = new DefaultHttpDataSource.Factory().setUserAgent(userAgent);
        HttpMediaDrmCallback drmCallback = new HttpMediaDrmCallback(licenseUrl, true,
                licenseDataSourceFactory);
        return new DefaultDrmSessionManager.Builder()
                .setUuidAndExoMediaDrmProvider(uuid, FrameworkMediaDrm.DEFAULT_PROVIDER)
                .build(drmCallback);
    }


    private HlsMediaSource buildHlsMediaSource(Uri uri) {
        String drmLicenseUrl = "https://cwip-shaka-proxy.appspot.com/no_auth";
        String userAgent = getString(R.string.user_agent);
        UUID drmSchemeUuid = Util.getDrmUuid(C.WIDEVINE_UUID.toString());

        DrmSessionManager drmSessionManager = buildDrmSessionManager(drmSchemeUuid, drmLicenseUrl, userAgent);

        DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(this, new DefaultHttpDataSource.Factory()
                .setAllowCrossProtocolRedirects(true)
                .setUserAgent(userAgent));

        return new HlsMediaSource.Factory(dataSourceFactory)
                .setAllowChunklessPreparation(true)
                .setDrmSessionManagerProvider(unusedMediaItem -> drmSessionManager)
                .createMediaSource(
                        new MediaItem.Builder()
                                .setUri(uri)
                                .setMimeType(MimeTypes.APPLICATION_M3U8)
                                .build()
                );
    }

    private DashMediaSource buildDashMediaSource(Uri uri) {
        String drmLicenseUrl = "https://cwip-shaka-proxy.appspot.com/no_auth";
        String userAgent = getString(R.string.user_agent);
        UUID drmSchemeUuid = Util.getDrmUuid(C.WIDEVINE_UUID.toString());

        DrmSessionManager drmSessionManager = buildDrmSessionManager(drmSchemeUuid, drmLicenseUrl, userAgent);

        DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(this, new DefaultHttpDataSource.Factory()
                .setAllowCrossProtocolRedirects(true)
                .setUserAgent(userAgent));

        return new DashMediaSource.Factory(dataSourceFactory)
                .setDrmSessionManagerProvider(unusedMediaItem -> drmSessionManager)
                .createMediaSource(
                        new MediaItem.Builder()
                                .setUri(uri)
                                .setMimeType(MimeTypes.APPLICATION_MPD)
                                .build()
                );
    }

    //--------------------------------------------------------------------------------------------------
    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    //--------------------------------------------------------------------------------------------------
    private void getQualityChooserDialog(Context context, ArrayList<String> arrayList) {

        CharSequence[] charSequences = new CharSequence[arrayList.size() + 1];
        charSequences[0] = "Auto";

        for (int i = 0; i < arrayList.size(); i++) {
            charSequences[i + 1] = arrayList.get(i).split("x")[1] + "p";
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle("Select video quality:");
        builder.setSingleChoiceItems(charSequences, selectedQualityIndex, (dialogInterface, which) -> selectedQualityIndex = which);
        builder.setPositiveButton("OK", (dialogInterface, i) -> {

            if (selectedQualityIndex == 0) {
                Toast.makeText(context, context.getText(R.string.app_name) + " will choose video resolution automatically.", Toast.LENGTH_SHORT).show();
                defaultTrackSelector.setParameters(defaultTrackSelector.buildUponParameters().setMaxVideoSizeSd());
            } else {
                String[] videoQualityInfo = arrayList.get(selectedQualityIndex - 1).split("x");

                Toast.makeText(context, "Video will be played with " + videoQualityInfo[1] + "p resolution.", Toast.LENGTH_SHORT).show();

                int videoWidth = Integer.parseInt(videoQualityInfo[0]);
                int videoHeight = Integer.parseInt(videoQualityInfo[1]);

                defaultTrackSelector.setParameters(
                        defaultTrackSelector
                                .buildUponParameters()
                                .setMaxVideoSize(videoWidth, videoHeight)
                                .setMinVideoSize(videoWidth, videoHeight)
                );
            }
        });
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
        builder.show();
    }
//--------------------------------------------------------------------------------------------------

    private ArrayList<String> getVideoQualitiesTracks() {

        ArrayList<String> videoQualities = new ArrayList<>();

        MappingTrackSelector.MappedTrackInfo renderTrack = defaultTrackSelector.getCurrentMappedTrackInfo();
        assert renderTrack != null;
        int renderCount = renderTrack.getRendererCount();

        for (int rendererIndex = 0; rendererIndex < renderCount; rendererIndex++) {

            if (isSupportedFormat(renderTrack, rendererIndex)) {

                int trackGroupType = renderTrack.getRendererType(rendererIndex);
                TrackGroupArray trackGroups = renderTrack.getTrackGroups(rendererIndex);
                int trackGroupsCount = trackGroups.length;

                if (trackGroupType == C.TRACK_TYPE_VIDEO) {

                    for (int groupIndex = 0; groupIndex < trackGroupsCount; groupIndex++) {

                        int videoQualityTrackCount = trackGroups.get(groupIndex).length;

                        for (int trackIndex = 0; trackIndex < videoQualityTrackCount; trackIndex++) {

                            boolean isTrackSupported = renderTrack.getTrackSupport(rendererIndex, groupIndex, trackIndex) == C.FORMAT_HANDLED;

                            if (isTrackSupported) {

                                TrackGroup track = trackGroups.get(groupIndex);

                                int videoWidth = track.getFormat(trackIndex).width;
                                int videoHeight = track.getFormat(trackIndex).height;

                                String quality = videoWidth + "x" + videoHeight;
                                videoQualities.add(quality);
                            }
                        }
                    }
                }
            }
        }

        return videoQualities;
    }

    private boolean isSupportedFormat(MappingTrackSelector.MappedTrackInfo mappedTrackInfo, int rendererIndex) {

        TrackGroupArray trackGroupArray = mappedTrackInfo.getTrackGroups(rendererIndex);
        if (trackGroupArray.length == 0) {
            return false;
        } else {
            return mappedTrackInfo.getRendererType(rendererIndex) == C.TRACK_TYPE_VIDEO;
        }
    }
//--------------------------------------------------------------------------------------------------

    private void setScreenBrightness(int brightness1) {

        float d = 1.0f / SHOW_MAX_BRIGHTNESS;

        WindowManager.LayoutParams lp = getWindow().getAttributes();

        lp.screenBrightness = d * brightness1;

        getWindow().setAttributes(lp);
    }

    private float getCurrentScreenBrightness() {
        // Get the current screen brightness value
        int currentBrightness = 0;
        try {
            currentBrightness = Settings.System.getInt(
                    getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS
            );
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "getCurrentScreenBrightness: ", e);
        }

        // Get the maximum brightness value supported by the device's screen
        int maxBrightness = 255; // Default value; you can get the actual maximum brightness using system APIs

        // Calculate the brightness value in the range [0, 1.0]
        float brightnessValue = (float) currentBrightness / maxBrightness;

        // Clamp the brightnessValue to the range [0, 1.0]
        brightnessValue = Math.max(0f, Math.min(1.0f, brightnessValue));

        return brightnessValue;
    }
//--------------------------------------------------------------------------------------------------

    private void setVolume(int volume1) {

        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        float d = (maxVolume * 1.0f) / SHOW_MAX_VOLUME;

        int newVolume = (int) (d * volume1);

//        Log.d("NUR ALAM", "setVolume1: newVolume=" + newVolume + " float=" + (d * value) + " value=" + value + " d=" + d + " maxVolume=" + maxVolume);

        if (newVolume > maxVolume) {
            newVolume = maxVolume;
        }
        if (volume1 == SHOW_MAX_VOLUME && newVolume < maxVolume) {
            newVolume = maxVolume;
        }

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);
    }
//--------------------------------------------------------------------------------------------------

    private void setVolumeVariable() {

        volume = (int) ((audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 1.0f) / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * SHOW_MAX_VOLUME);

        if (volume > SHOW_MAX_VOLUME) {
            volume = SHOW_MAX_VOLUME;
        }
    }

    //--------------------------------------------------------------------------------------------------

    /**
     * Save and Restore Playback State:
     * To maintain the playback state across different app states (minimized, restored),
     * you can save and restore the playback state. You can do this using the
     * onSaveInstanceState and onRestoreInstanceState methods. Here's an example:
     */

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("playWhenReady", exoPlayer.getPlayWhenReady());
        outState.putLong("playbackPosition", exoPlayer.getCurrentPosition());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        playWhenReady = savedInstanceState.getBoolean("playWhenReady");
        playbackPosition = savedInstanceState.getLong("playbackPosition", C.TIME_UNSET);
    }

    //______________________________________________________________________________________________
    @Override
    protected void onResume() {
        super.onResume();

        if (brightness > 0) {
            setScreenBrightness(brightness);
        }
        setVolume(volume);
    }

    @Override
    protected void onPause() {
        super.onPause();
        exoPlayer.setPlayWhenReady(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exoPlayer.release();
    }

    @Override
    public void onBackPressed() {
        exoPlayer.stop();
        exoPlayer.release();
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            setVolumeVariable();
        }
        return super.onKeyUp(keyCode, event);
    }

    //--------------------------------------------------------------------------------------------------
    private void initVars() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        apiLoadersContainer = findViewById(R.id.apiLoadersContainer);
        episodeLoadingTV = findViewById(R.id.episodeLoadingTV);
        playerComponentsContainer = findViewById(R.id.playerComponentsContainer);
        bufferingProgressBar = findViewById(R.id.bufferingProgressBar);
        exoPlayerView = findViewById(R.id.exoPlayerView);
        brightnessVolumeContainer = findViewById(R.id.brightness_volume_container);
        brightnessIcon = findViewById(R.id.brightness_icon);
        volumeIcon = findViewById(R.id.volume_icon);
        brightVolumeTV = findViewById(R.id.brightness_volume_tv);
        qualityBtn = exoPlayerView.findViewById(R.id.quality_selection_btn);
        fitScreenBtn = exoPlayerView.findViewById(R.id.fit_screen_btn);
        skipIntroOutroBtn = exoPlayerView.findViewById(R.id.skipIntroOutroBtn);
        videoNameTV = exoPlayerView.findViewById(R.id.animeNameTV);
        episodeNumTV = exoPlayerView.findViewById(R.id.episodeNumTV);
        backButton = exoPlayerView.findViewById(R.id.backButton);
        doubleTapSkipBackIcon = findViewById(R.id.doubleTapSkipBackIcon);
        doubleTapSkipForwardIcon = findViewById(R.id.doubleTapSkipForwardIcon);
        backward10 = findViewById(R.id.backward_10);
        forward10 = findViewById(R.id.forward_10);
        previousEpisode = findViewById(R.id.previous_episode);
        nextEpisode = findViewById(R.id.next_episode);

        doubleTapSkipBackIcon.setVisibility(View.GONE);
        doubleTapSkipForwardIcon.setVisibility(View.GONE);
    }

    //----------------------------------------------------------------------------------------------
}