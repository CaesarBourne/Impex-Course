package com.bourne.caesar.impextutors.UI_Components.Activities;

import android.Manifest;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bourne.caesar.impextutors.MainActivity;
import com.bourne.caesar.impextutors.Models.CourseChaptersData;
import com.bourne.caesar.impextutors.Offline_Database.PayTable;
import com.bourne.caesar.impextutors.Offline_Database.ViewModel.PayViewModel;
import com.bourne.caesar.impextutors.R;
import com.bourne.caesar.impextutors.Utilities.Constants;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.ArrayList;

public class ChaptersAudioDetailsActivity extends AppCompatActivity implements Player.EventListener {
    public static final String EXTRA_CHAPTER_POSITION = "chapterposition";
    public static final String EXTRA_CHAPTER_ARRAY = "chapterARRAY";
    private ArrayList<CourseChaptersData> courseChaptersList;
    private PlayerView chapterVideoView;
    private int chapterPosition;
    private PayViewModel payViewModel;

    private static final String KEY_VIDEO_URI = "video_uri";


//    PlayerView chapterVideoView;

    ProgressBar spinnerVideoDetails;

    private DownloadManager downloadManager;
    private long refid;
    private Uri Download_Uri;

    ArrayList<Long> list = new ArrayList<>();

    private Button playChapterVideo, previousChapterButton, nextChapteButton,downloadCourseButton;
    android.widget.MediaController mediaController;
    TextView chapterTitleView, chapterDescription, chaptervideoDurationView, audioPlayerTitle;

    String audioUri;
    SimpleExoPlayer player;
    Handler mHandler;
    Runnable mRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapters_audio_details);
        initialization();
        payViewModel = ViewModelProviders.of(this).get(PayViewModel.class);


        if (getIntent().getExtras() != null){
            courseChaptersList = (ArrayList<CourseChaptersData>) getIntent().getSerializableExtra(EXTRA_CHAPTER_ARRAY);
            chapterPosition = getIntent().getExtras().getInt(EXTRA_CHAPTER_POSITION);
            final int chapterListSize = courseChaptersList.size();

            setViews(chapterPosition);
//download manager

            downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);


            registerReceiver(downloadCompleteReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));




            nextChapteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    if (chapterPosition == 2){
//                        nextChapteButton.setEnabled(false);
//                    }
//                    else if (chapterPosition < chapterListSize ){
//                         chapterPosition = chapterPosition + 1;
//                        setViews(chapterPosition);
//                    }
                    if (chapterPosition == chapterListSize-1){
                        Toast.makeText(ChaptersAudioDetailsActivity.this, "End of chapters", Toast.LENGTH_SHORT).show();
                        return;

                    }
                    else if (chapterPosition < chapterListSize-1){
                        chapterPosition = chapterPosition +1;
                        setViews(chapterPosition);
                    }
                    else {
                        Toast.makeText(ChaptersAudioDetailsActivity.this, "End of chapters", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            previousChapterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (chapterPosition ==  0){
                        previousChapterButton.setVisibility(View.INVISIBLE);
                        return;
                    }
                    else if (chapterPosition < chapterListSize){
                        chapterPosition = chapterPosition - 1;
                        setViews(chapterPosition);

                    }

                    else {
                        Toast.makeText(ChaptersAudioDetailsActivity.this, "No more Previous Chapter" +
                                "", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            if(!isStoragePermissionGranted())
            {


            }



        }



    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(downloadCompleteReceiver);
        releasePlayer();
    }
    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }


    BroadcastReceiver downloadCompleteReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            long downloadTag = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            list.remove(downloadTag);
            if (list.isEmpty()){
                String chapterTitle = courseChaptersList.get(chapterPosition).CourseID;
                PayTable payTable = new PayTable(chapterTitle);
                payViewModel.InsertPayID(payTable);
                NotificationCompat.Builder notification = new NotificationCompat.Builder(ChaptersAudioDetailsActivity.this);
                Intent intentstart = new Intent(ChaptersAudioDetailsActivity.this, MainActivity.class);
                intent.putExtra(PaaymentSuccessfulActivity.TRANSACTION_ID, "Your program is downloaded you would be abale to view offline...");

                PendingIntent pendingIntent = PendingIntent.getActivity(ChaptersAudioDetailsActivity.this,0, intentstart, PendingIntent.FLAG_UPDATE_CURRENT);


                notification.setTicker("Download Complete");
                notification.setContentTitle("Impex Tutor");
                notification.setContentText("Doownload succesful ");
                notification.setWhen(System.currentTimeMillis());
                notification.setSmallIcon(R.drawable.impexlogo);
                notification.setLights(Color.BLUE, 500, 500);
                long[] pattern ={500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500};
                notification.setVibrate(pattern);
                notification.setStyle(new NotificationCompat.InboxStyle());
                Uri alarmsound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                notification.setSound(alarmsound);
                notification.setContentIntent(pendingIntent);
                notification.setAutoCancel(true);

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(7, notification.build());

            }
        }
    };

    private void setViews(final int chapterNewPosition) {
//        payViewModel.getAllPayment().observe(this, new Observer<List<PayTable>>() {
//            //onchanged is only called when activity is in the
//            // foreground if activity is destroyed the refernce is cleaned too
//            @Override
//            public void onChanged(@Nullable List<PayTable> payTables) {
//                for (int i =0; i<payTables.size(); i++) {
//                    if (payTables.get(i).getCourseName() == courseChaptersList.get(chapterNewPosition).CourseAudio) {
//                        downloadCourseButton.setVisibility(View.GONE);
//                    }
//                }
//            }
//        });
        audioUri =  courseChaptersList.get(chapterNewPosition).CourseVideoStream;
        Download_Uri = Uri.parse(audioUri);
        chapterTitleView.setText(String.valueOf(chapterNewPosition + 1)+ " .   " +
                courseChaptersList.get(chapterNewPosition).CourseTitle);
        chapterDescription.setText(courseChaptersList.get(chapterNewPosition).CourseDescription);
        audioPlayerTitle.setText(courseChaptersList.get(chapterNewPosition).CourseTitle);




        final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        playChapterVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String audioId = courseChaptersList.get(chapterPosition).CourseID + "audio";
                String path = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + File.separator +".Impex/Audio" +File.separator +
                        audioId.trim()+".mp4";

                File file =  new File(path);
                if (file.exists()){
                    Toast.makeText(ChaptersAudioDetailsActivity.this, "This chapter is playing from the Impex app" +
                            "as it has been downloaded", Toast.LENGTH_SHORT).show();
                    audioUri = path;
                    playChapterVideo.setBackgroundColor(getResources().getColor(R.color.green_500));
                    setUp();
                }
                else if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() ==
                        NetworkInfo.State.CONNECTED || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                        .getState() == NetworkInfo.State.CONNECTED){
                    playChapterVideo.setBackgroundColor(getResources().getColor(R.color.blue_500));
                    audioUri =  courseChaptersList.get(chapterNewPosition).CourseAudio;
                    setUp();
                    Toast.makeText(ChaptersAudioDetailsActivity.this, "Streaming from the Impex network", Toast.LENGTH_SHORT).show();

                }else{
                    Toast.makeText(ChaptersAudioDetailsActivity.this, "No network connection Try later", Toast.LENGTH_SHORT).show();
                    playChapterVideo.setBackgroundColor(getResources().getColor(R.color.red_500));
                }
            }
        });

        downloadCourseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list.clear();

                String audioId = courseChaptersList.get(chapterNewPosition).CourseID + "audio";
                String downloadCourseName = audioId.trim();
                DownloadManager.Request downloadRequest = new DownloadManager.Request(Download_Uri);
                downloadRequest.setDescription("downloading impex Audio.mp4.....");
                downloadRequest.setTitle("Impex Audio");

                downloadRequest.setDestinationInExternalFilesDir(ChaptersAudioDetailsActivity.this,Environment.DIRECTORY_DOWNLOADS,
                        "/.Impex/Audio/"+downloadCourseName+".mp4");
                ;

//                String downloadstore = ProgramFeaturesActivity.this,Environment.DIRECTORY_DOWNLOADS,
//                        "/.Impex/programnumber.mp4"
//                downloadRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/.Impex/programnumber.mp4");
                downloadRequest.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
                downloadCourseButton.setVisibility(View.GONE);
                long reference_download_ID = downloadManager.enqueue(downloadRequest);

                list.add(reference_download_ID);
            }
        });

//        playChapterVideo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Uri uri = Uri.parse("https://firebasestorage.googleapis.com/v0/b/impexcollege.appspot.com/o/TPDP%20Customer%20Service%20Lecture-2_SDLow.mp4?alt=media&token=60f1f158-beee-4900-9cc0-d6b0c8ea3ae5");
//                chapterVideoView.setVideoURI(uri);
//                mediaController = new MediaController(ChaptersDetailActivity.this);
////        mediaController.setAnchorView(videoView);
//                chapterVideoView.setMediaController(mediaController);
//                chapterVideoView.start();
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String path = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + File.separator +".Impex" +File.separator +
                courseChaptersList.get(chapterPosition).CourseID+".mp4";
        File file =  new File(path);

        if (file.exists()){
            downloadCourseButton.setVisibility(View.GONE);
        }
    }

    private void initialization() {
        chapterVideoView = findViewById(R.id.chapterVideoView);
        chapterTitleView = findViewById(R.id.chapterTitle);
        chapterDescription = findViewById(R.id.chapterDescription);

        playChapterVideo = findViewById(R.id.playChapterVideo);
        previousChapterButton = findViewById(R.id.chaapterPreviousButton);
        nextChapteButton = findViewById(R.id.chapterNextButton);
        downloadCourseButton = findViewById(R.id.downloadChapter);
        spinnerVideoDetails = findViewById(R.id.spinnerVideoDetails);
        audioPlayerTitle = findViewById(R.id.audioPlayerTitle);

    }


    private void setUp() {
        initializePlayer();
        if (audioUri == null) {
            Toast.makeText(this, "Video Link is broken or empty", Toast.LENGTH_SHORT).show();
            return;
        }
        spinnerVideoDetails.setVisibility(View.VISIBLE);
        buildMediaSource(Uri.parse(audioUri));
    }

//    @OnClick(R.id.imageViewExit)
//    public void onViewClicked() {
//        finish();
//    }

    private void initializePlayer() {
        if (player == null) {
            // 1. Create a default TrackSelector
            LoadControl loadControl = new DefaultLoadControl(
                    new DefaultAllocator(true, 16),
                    Constants.MIN_BUFFER_DURATION,
                    Constants.MAX_BUFFER_DURATION,
                    Constants.MIN_PLAYBACK_START_BUFFER,
                    Constants.MIN_PLAYBACK_RESUME_BUFFER, -1, true);

            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            TrackSelection.Factory videoTrackSelectionFactory =
                    new AdaptiveTrackSelection.Factory(bandwidthMeter);
            TrackSelector trackSelector =
                    new DefaultTrackSelector(videoTrackSelectionFactory);
            // 2. Create the player
            player = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(this), trackSelector, loadControl);
            chapterVideoView.setPlayer(player);

        }


    }

    private void buildMediaSource(Uri mUri) {
        // Measures bandwidth during playback. Can be null if not required.
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, getString(R.string.app_name)), bandwidthMeter);
        // This is the MediaSource representing the media to be played.
        MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mUri);
        // Prepare the player with the source.
        player.prepare(videoSource);
        player.setPlayWhenReady(true);
        player.addListener(this);
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    private void pausePlayer() {
        if (player != null) {
            player.setPlayWhenReady(false);
            player.getPlaybackState();
        }
    }

    private void resumePlayer() {
        if (player != null) {
            player.setPlayWhenReady(true);
            player.getPlaybackState();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pausePlayer();
        if (mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        resumePlayer();
    }



    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {

            case Player.STATE_BUFFERING:
                spinnerVideoDetails.setVisibility(View.VISIBLE);
                break;
            case Player.STATE_ENDED:
                // Activate the force enable
                break;
            case Player.STATE_IDLE:

                break;
            case Player.STATE_READY:
                spinnerVideoDetails.setVisibility(View.GONE);

                break;
            default:
                // status = PlaybackStatus.IDLE;
                break;
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }
}
