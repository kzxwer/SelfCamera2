package com.example.android.camera2basic;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.android.camera2basic.adapter.GalleryAdapter;
import com.example.android.camera2basic.anim.AnimFadeReveal;
import com.example.android.camera2basic.base.PickerFragment;
import com.example.android.camera2basic.encoder.MediaEncoder;
import com.example.android.camera2basic.encoder.MediaVideoEncoder;
import com.example.android.camera2basic.fileio.FileHandler;
import com.example.android.camera2basic.fileio.FilePathUtil;
import com.example.android.camera2basic.image.ImageEffectFragment;
import com.example.android.camera2basic.picker.MediaPickerOpts;
import com.example.android.camera2basic.tasks.LoadGLImageTask;
import com.example.android.camera2basic.tasks.LoadImageTask;
import com.example.android.camera2basic.tasks.SaveGLImageTask;
import com.example.android.camera2basic.utils.BitmapUtils;

import java.util.ArrayList;
import java.util.Timer;

import static android.app.Activity.RESULT_OK;
import static com.example.android.camera2basic.R.drawable.camera_btn_mode_down;
import static com.example.android.camera2basic.R.drawable.camera_btn_mode_up;
import static com.example.android.camera2basic.R.drawable.shutter;
import static com.example.android.camera2basic.R.drawable.shutter2;


public class CameraFragment extends PickerFragment implements OnClickListener {

    public final String TAG = "CameraFragment";

    private static final int REQUEST_GET_CONTENT = 1001;

    private static final String FRAGMENT_DIALOG = "dialog";

    private static final int DEF_VID_SIZE = 480;

    public static CameraFragment newInstance(@NonNull MediaPickerOpts opts) {
        Bundle args = new Bundle();
        args.putParcelable(MediaPickerOpts.INTENT_OPTS, opts);
        CameraFragment fragment = new CameraFragment();
        fragment.setArguments(args);
        return fragment;
    }


    /**
     * for camera preview display
     */
    private CameraGLView mCameraView;
    //private ImageView iv_flash;
    private ImageView mCameraSwitcher;
    //private TextView tv_gallery_effects;
    //private AppCompatImageView iv_none_c, iv_duo_py_c, iv_cross_c, iv_negative_c, iv_duo_bw_c, iv_lomo_c, iv_fillight_c, iv_bw_c, iv_sepia_c;

    //private RecyclerView recyclerView;
    private GalleryAdapter galleryAdapter;
   // private SelectedAdapter selectedAdapter;

    //private HorizontalScrollView hsv_effects;
    //private ImageView iv_back;
    private ImageButton iv_filter;
    //private View txt_gallery;
    //private ImageView iv_gallery;
    //private ImageView iv_vid_crop;
    //private TextView txtVideoDur;
    private View txt_done;

    private Timer timer;
    private Handler uiThreadHandler;

    /**
     * button for start/stop recording
     */
    private ImageView mRecordButton;
    FrameLayout function_layout;
    ImageButton arrow_button;
    /**
     * muxer for audio/video recording
     */
    //private MediaMuxerWrapper mMuxer;
    private MediaPickerOpts opts;
    //private MediaActionSound mediaActionSound;

    /**
     * callback methods from encoder
     */

    private final MediaEncoder.MediaEncoderListener mMediaEncoderListener =
            new MediaEncoder.MediaEncoderListener() {
                @Override
                public void onPrepared(final MediaEncoder encoder) {
                    Log.d(TAG, "onPrepared:encoder=" + encoder);
                    if (encoder instanceof MediaVideoEncoder) {
                        mCameraView.setVideoEncoder((MediaVideoEncoder) encoder);
                    }
                }

                @Override
                public void onStopped(final MediaEncoder encoder) {
                    Log.d(TAG, "onStopped:encoder=" + encoder);
                    if (encoder instanceof MediaVideoEncoder) mCameraView.setVideoEncoder(null);
                }
            };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (opts == null) {
            //noinspection ConstantConditions
            opts = getArguments().getParcelable(MediaPickerOpts.INTENT_OPTS);
        }
        uiThreadHandler = new Handler();
    }

    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_camera2_basic, container, false);

        mCameraView = rootView.findViewById(R.id.cameraView);
        //iv_flash = rootView.findViewById(R.id.iv_flash);
        mCameraSwitcher = rootView.findViewById(R.id.iv_switch_camera);
        //recyclerView = rootView.findViewById(R.id.gallery_previews);
        //hsv_effects = rootView.findViewById(R.id.hsv_effects_capture);

        iv_filter = rootView.findViewById(R.id.filter_button);

        //iv_gallery = rootView.findViewById(R.id.iv_gallery);
       // txt_gallery = rootView.findViewById(R.id.txt_gallery);
        //iv_vid_crop = rootView.findViewById(R.id.iv_vid_crop);
        //tv_gallery_effects = rootView.findViewById(R.id.tv_gallery_effects);


        //txtVideoDur = rootView.findViewById(R.id.video_dur);
        mRecordButton = rootView.findViewById(R.id.picture);

        function_layout = rootView.findViewById(R.id.function_layout);
        arrow_button = rootView.findViewById(R.id.arrow_upward_button);
        //iv_back = rootView.findViewById(R.id.iv_back);
        //txt_done = rootView.findViewById(R.id.txt_done);


        //txt_done.setOnClickListener(this);
        //iv_back.setOnClickListener(this);

        mRecordButton.setOnClickListener(this);
        mCameraSwitcher.setOnClickListener(this);
        function_layout.setOnClickListener(this);
        arrow_button.setOnClickListener(this);
        iv_filter.setOnClickListener(this);
        rootView.findViewById(R.id.info).setOnClickListener(this);




        handleIntent();

        AnimFadeReveal.fadeIn(rootView);

        return rootView;
    }

    @SuppressWarnings("ConstantConditions")
    private void handleIntent() {
        mCameraView.init(opts.scaleType, opts.mediaType);
        updateScaleUI();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        //recyclerView.setLayoutManager(layoutManager);

        if (opts.scaleTypeChangeable) {
          //  iv_vid_crop.setOnClickListener(this);
        } else {
           // iv_vid_crop.setVisibility(View.INVISIBLE);
          //  iv_vid_crop.setOnClickListener(null);
        }



        mCameraView.setPreviewEnabled(opts.showFilters());
    }
/*
    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.finish();
                        }
                    })
                    .create();
        }

    }*/

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume:");
        mCameraView.onResume();

        mCameraView.setCameraSwitcher(mCameraSwitcher);
        mCameraView.setFrag(this);
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause:");
        //stopRecording();
        mCameraView.onPause();
       //cancelTimer();

        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isRemoving() && galleryAdapter != null) {
            galleryAdapter.changeCursor(null);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || requestCode != REQUEST_GET_CONTENT || data.getData() == null) {
            return;
        }

        Uri fileUri = data.getData();
        String filePath = FilePathUtil.getRealPath(getContext(), fileUri, opts);

        if (filePath == null) return;

        Log.d(TAG, "selectedPath: " + filePath);

        if (galleryAdapter != null) {
            galleryAdapter.addSelected(filePath);
        }

        checkIfMediaSelectionCompleted();
    }

    @Override
    public void onClick(final View view) {
        final int id = view.getId();

        if (id == R.id.filter_button) {
           // toggleShowFilters();
            mCameraView.touched();
        } else if (id == R.id.iv_switch_camera) {
            mCameraView.toggleCamera();
        } else if (id == R.id.picture) {
            if (opts.mediaType == MediaType.IMAGE) {
               // playSound(MediaActionSound.SHUTTER_CLICK);
                takePicture();
            } else {
                //stopRecording();
                //uiThreadHandler.postDelayed(() -> playSound(MediaActionSound.STOP_VIDEO_RECORDING), 500L);
            }
        }
        else if(id== R.id.arrow_upward_button){

                if(function_layout.getVisibility()==View.GONE) {
                    arrow_button.setImageResource(camera_btn_mode_down);
                    function_layout.setVisibility(View.VISIBLE);
                }
                else if(function_layout.getVisibility()==View.VISIBLE){
                    arrow_button.setImageResource(camera_btn_mode_up);
                    function_layout.setVisibility(View.GONE);
                }
            /*catch(Exception e){
                ErrorDialog.newInstance(e.getMessage())
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            }*/
        }

    }

    /*
    private void startTimer() {
        cancelTimer();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {

            private long startTime = System.currentTimeMillis() - 200L;

            @Override
            public void run() {
                long delta = (System.currentTimeMillis() - startTime);
                String parsed = TimeParseUtils.getFormattedTimeHHMMSS(delta);
                uiThreadHandler.post(() -> txtVideoDur.setText(parsed));
            }
        }, 1000L, 1000L);
    }

    private void cancelTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }*/
/*
    private void toggleScaleType() {
        if (mCameraView.getScaleType() == ScaleType.SCALE_SQUARE) {
            mCameraView.setScaleType(ScaleType.SCALE_CROP_CENTER);
        } else {
            mCameraView.setScaleType(ScaleType.SCALE_SQUARE);
        }

        updateScaleUI();
    }*/

    private void updateScaleUI() {
        if (mCameraView.getScaleType() == ScaleType.SCALE_SQUARE) {
           // iv_vid_crop.setImageResource(crop_portrait);
        } else {
           // iv_vid_crop.setImageResource(crop_square);
        }
    }
/*
    private void openGallery() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);

        if (opts.mediaType == MediaType.IMAGE) {
            intent.setType("image/*");
            intent = Intent.createChooser(intent, "Select Image");
        } else {
            intent.setType("video/*");
            intent = Intent.createChooser(intent, "Select Video");
        }

        startActivityForResult(intent, REQUEST_GET_CONTENT);
    }
*/
    private void takePicture() {
        //mRecordButton.setVisibility(View.INVISIBLE);
        mRecordButton.setImageResource(shutter2);
        mRecordButton.setOnClickListener(null);
        mCameraView.queueEvent(() -> {
            GLDrawer2D drawer = mCameraView.getDrawer();
            int x = drawer.getStartX();
            int y = drawer.getStartY();
            int w = drawer.width();
            int h = drawer.height();

            int bitmapBuffer[] = BitmapUtils.readEGLBuffer(x, y, w, h);

            if (bitmapBuffer == null) return;

            if (opts.mediaType == MediaType.IMAGE && opts.cropEnabled) {
                ImageEffectFragment fragment = ImageEffectFragment.newInstance(opts);
                uiThreadHandler.post(() -> showFragment(fragment));
                new LoadGLImageTask(w, h, bitmapBuffer, fragment, opts).execute();
            } else {
                new SaveGLImageTask(w, h, bitmapBuffer, this, opts).execute();
            }
        });
    }

    public void onPictureSaved(String imagePath) {
        if (imagePath == null || !FileHandler.exists(imagePath)) {
            mRecordButton.setVisibility(View.VISIBLE);
            mRecordButton.setOnClickListener(this);
            return;
        }

        if (galleryAdapter != null) {
            galleryAdapter.addSelected(imagePath);
        }

        MediaScannerConnection.MediaScannerConnectionClient callBack = null;

        if (!checkIfMediaSelectionCompleted()) {
            mRecordButton.setVisibility(View.VISIBLE);
            mRecordButton.setOnClickListener(this);

            callBack = new MediaScannerConnection.MediaScannerConnectionClient() {
                @Override
                public void onMediaScannerConnected() {

                }

                @Override
                public void onScanCompleted(String path, Uri uri) {
                    uiThreadHandler.post(() -> refreshAdapters());
                }
            };
        }

        //noinspection ConstantConditions
        scanFile(imagePath, "image/jpg", callBack);
    }

    /**
     * start recording
     * This is a sample project and call this on UI thread to avoid being complicated
     * but basically this should be called on private thread because prepareing
     * of encoder is heavy work
     */
    /*
    private void startRecording() {
        Log.d(TAG, "startRecording:");

        try {
            File mediaPath = FileHandler.getTempFile(opts);

            mMuxer = new MediaMuxerWrapper(mediaPath.getPath());  // if you record audio only, ".m4a" is also OK.

            int outputVideoWidth;
            int outputVideoHeight;

            if (ScaleType.SCALE_SQUARE == mCameraView.getScaleType()) {
                outputVideoWidth = DEF_VID_SIZE;
                outputVideoHeight = DEF_VID_SIZE;
            } else {
                outputVideoWidth = mCameraView.getVideoWidth();
                outputVideoHeight = mCameraView.getVideoHeight();
            }

            Log.d(TAG, "output: width: " + outputVideoWidth + " height: " + outputVideoHeight);

            // for video capturing
            new MediaVideoEncoder(mMuxer, mMediaEncoderListener, outputVideoWidth,
                    outputVideoHeight, mCameraView.getDrawer());

            // for audio capturing
            new MediaAudioEncoder(mMuxer, mMediaEncoderListener);

            mMuxer.prepare();
            //delay is added as camera record sound gets recorded
            uiThreadHandler.postDelayed(() -> mMuxer.startRecording(), 500L);

        } catch (final IOException e) {
            Log.e(TAG, "startCapture:", e);
            return;
        }

        iv_vid_crop.setVisibility(View.GONE);
        iv_filter.setVisibility(View.GONE);

       // iv_gallery.setVisibility(View.GONE);
       // txt_gallery.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        mCameraSwitcher.setVisibility(View.GONE);
       // iv_back.setVisibility(View.GONE);

        mRecordButton.setImageResource(R.drawable.circle_ringed_red_white);
        //txtVideoDur.setText(R.string.video_start_time);
       // txtVideoDur.setVisibility(View.VISIBLE);

        startTimer();

        mCameraView.onRecordingStart();
    }*/

    /**
     * request stop recording
     *//*
    private void stopRecording() {
        Log.d(TAG, "stopRecording:mMuxer=" + mMuxer);

        txtVideoDur.setVisibility(View.INVISIBLE);
        iv_back.setVisibility(View.VISIBLE);

        cancelTimer();

        mCameraView.onRecordingStop();

        if (mMuxer == null) {
            return;
        }

        mMuxer.stopRecording();
        String mediaPath = mMuxer.getOutputPath();
        mMuxer = null;

        if (mediaPath == null || !FileHandler.exists(mediaPath)) return;

        Log.d(TAG, "recordedPath: " + mediaPath);

        mRecordButton.setImageResource(R.drawable.circle_done);

        if (galleryAdapter != null) {
            galleryAdapter.addSelected(mediaPath);
        } else if (selectedAdapter != null) {
            selectedAdapter.addSelected(mediaPath);
        }

        MediaScannerConnection.MediaScannerConnectionClient callBack = null;

        if (!checkIfMediaSelectionCompleted()) {
            if (opts.showFilters()) {
                iv_filter.setVisibility(View.VISIBLE);
            } else {
                iv_filter.setVisibility(View.INVISIBLE);
            }

            if (opts.galleryEnabled) {
                iv_gallery.setVisibility(View.VISIBLE);
                if (getListItemCount() == 0) {
                    txt_gallery.setVisibility(View.VISIBLE);
                }
            }

            if (opts.scaleTypeChangeable) {
                iv_vid_crop.setVisibility(View.VISIBLE);
            } else {
                iv_vid_crop.setVisibility(View.INVISIBLE);
            }

            recyclerView.setVisibility(View.VISIBLE);

            if (CameraHelper.isFrontCameraAvailable(getContext())) {
                mCameraSwitcher.setVisibility(View.VISIBLE);
            }

            iv_back.setVisibility(View.VISIBLE);
            txtVideoDur.setVisibility(View.GONE);

            callBack = new MediaScannerConnection.MediaScannerConnectionClient() {
                @Override
                public void onMediaScannerConnected() {

                }

                @Override
                public void onScanCompleted(String path, Uri uri) {
                    uiThreadHandler.post(() -> refreshAdapters());
                }
            };
        }

        scanFile(mediaPath, "video/mp4", callBack);
    }*/
/*
    private int getListItemCount() {
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        return adapter != null ? adapter.getItemCount() : 0;
    }*/

    private void refreshAdapters() {
        if (galleryAdapter != null) {
            loadGalleryAdapter();
        }
    }

    public void loadGalleryAdapter() {
       // txt_gallery.setVisibility(View.VISIBLE);

        String[] projection;
        final String orderBy;
        Uri contentURI;

        if (opts.mediaType == MediaType.VIDEO) {
            projection = new String[]{
                    MediaStore.Video.Media._ID,
                    MediaStore.MediaColumns.DATA,
            };

            orderBy = MediaStore.Video.Media.DATE_TAKEN;
            contentURI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        } else {
            projection = new String[]{
                    MediaStore.Images.Media._ID,
                    MediaStore.MediaColumns.DATA,
            };

            orderBy = MediaStore.Images.Media.DATE_TAKEN;
            contentURI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        //noinspection ConstantConditions
        ContentResolver contentResolver = getContext().getContentResolver();

        Cursor cursor = contentResolver.query(contentURI,
                projection, null, null, orderBy + " DESC");

        if (cursor != null && cursor.moveToFirst()) {

            Log.d(TAG, "mediaCount: " + cursor.getCount());

            if (cursor.getCount() > 0) {
              //  txt_gallery.setVisibility(View.GONE);
            }

            if (galleryAdapter == null) {
                galleryAdapter = new GalleryAdapter(cursor, opts.maxSelection, opts.mediaType, this);
            } else {
                galleryAdapter.changeCursor(cursor);
            }

            //recyclerView.setAdapter(galleryAdapter);
        }
    }
/*
    private void loadSelectedAdapter() {
        if (selectedAdapter == null) {
            selectedAdapter = new SelectedAdapter(opts.maxSelection, this);
        }

        recyclerView.setAdapter(selectedAdapter);
    }*/

    public boolean checkIfMediaSelectionCompleted() {
        int selectionCount = 0;

        if (galleryAdapter != null) {
            selectionCount = galleryAdapter.getSelectionCount();
        }

        mRecordButton.setImageResource(shutter);

        if (selectionCount == opts.maxSelection) {
            Log.d(TAG, "onMaxSelection");
            uiThreadHandler.post(this::onClickDone);
            return true;
        }

        if (selectionCount == 0) {
           // txt_done.setVisibility(View.GONE);
        } else {
          //  txt_done.setVisibility(View.VISIBLE);
        }

        return false;
    }

    @SuppressWarnings("ConstantConditions")
    private void onClickDone() {
        ArrayList<String> items = new ArrayList<>();

        if (galleryAdapter != null) {
            galleryAdapter.fill(items);
            galleryAdapter.clearSelection();
        }

        //if (opts.mediaType == MediaType.IMAGE && opts.cropEnabled) { opts.cropEnabled not working
            if (opts.mediaType == MediaType.IMAGE) {
            ImageEffectFragment fragment = ImageEffectFragment.newInstance(opts);
                showFragment(fragment);
                new LoadImageTask(items.remove(0), fragment).execute();

                return;
        }

        if (opts.mediaType == MediaType.IMAGE && opts.imgSize > 0) {
            String imagePath = items.remove(0);
            String newPath = BitmapUtils.createCroppedBitmap(imagePath, opts);
            if (!imagePath.equals(newPath)) {
                scanFile(newPath, "image/jpg", null);
            }
            items.add(0, newPath);
        }

        Intent resultIntent = new Intent();
        resultIntent.putStringArrayListExtra(MediaPickerOpts.INTENT_RES, items);
        FragmentActivity activity = getActivity();
        activity.setResult(RESULT_OK, resultIntent);
        activity.supportFinishAfterTransition();
    }

    @SuppressWarnings("ConstantConditions")
    private void scanFile(String mediaPath, String mimeType, OnScanCompletedListener callback) {
        MediaScannerConnection.scanFile(getContext().getApplicationContext(), new String[]{
                mediaPath
        }, new String[]{
                mimeType
        }, callback);
    }
/*
    private void playSound(int soundId) {
        if (mediaActionSound == null) {
            mediaActionSound = new MediaActionSound();
        }

        mediaActionSound.play(soundId);
    }*/
}