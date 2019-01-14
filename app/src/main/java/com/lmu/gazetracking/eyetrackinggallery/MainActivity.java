package com.lmu.gazetracking.eyetrackinggallery;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.provider.MediaStore;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    public int[] IMAGES = {
        R.drawable.activity_3,
        R.drawable.activity_4,
        R.drawable.activity_6,
        R.drawable.activity_8,
        R.drawable.activity_9,
        R.drawable.friends_1,
        R.drawable.friends_3,
        R.drawable.friends_4,
        R.drawable.friends_9,
        R.drawable.friends_10,
        R.drawable.selfie_1,
        R.drawable.selfie_2,
        R.drawable.selfie_4,
        R.drawable.selfie_7,
        R.drawable.selfie_9,
        R.drawable.captioned_2,
        R.drawable.captioned_4,
        R.drawable.captioned_5,
        R.drawable.captioned_7,
        R.drawable.captioned_10,
        R.drawable.gadget_2,
        R.drawable.gadget_3,
        R.drawable.gadget_5,
        R.drawable.gadget_6,
        R.drawable.gadget_10,
        R.drawable.pet_2,
        R.drawable.pet_3,
        R.drawable.pet_4,
        R.drawable.pet_6,
        R.drawable.pet_8,
        R.drawable.fashion_2,
        R.drawable.fashion_5,
        R.drawable.fashion_6,
        R.drawable.fashion_7,
        R.drawable.fashion_10,
        R.drawable.food_3,
        R.drawable.food_4,
        R.drawable.food_6,
        R.drawable.food_7,
        R.drawable.food_10
    };

    String participant = "empty";
    String startTime = "no-date";
    private GestureDetectorCompat mDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            participant = bundle.getString("participant");
            startTime = bundle.getString("startTime");
        }

        if (hasEyetrackingStarted()) {
            writeFileOnInternalStorage(MainActivity.this, "log.txt", "Task started");

            //shuffle image array
            int index;
            List<Integer> randomIndices = new ArrayList();
            String initalIndexString = "activity_3, activity_4, activity_6, activity_8, activity_9, friends_1, friends_3, friends_4, friends_9, friends_10, selfie_1, selfie_2, selfie_4, selfie_7, selfie_9, captioned_2, captioned_4, captioned_5, captioned_7, captioned_10, gadget_2, gadget_3, gadget_5, gadget_6, gadget_10, pet_2, pet_3, pet_4, pet_6, pet_8, fashion_2, fashion_5, fashion_6, fashion_7, fashion_10, food_3, food_4, food_6, food_7, food_10";
            String randomIndexString = "";
            Random random = new Random();
            for (int i = IMAGES.length - 1; i > 0; i--) {
                index = random.nextInt(i + 1);
                randomIndices.add(index);
                randomIndexString = randomIndexString + ", " + String.valueOf(index);

                if (index != i) {
                    IMAGES[index] ^= IMAGES[i];
                    IMAGES[i] ^= IMAGES[index];
                    IMAGES[index] ^= IMAGES[i];
                }
            }

            writeFileOnInternalStorage(MainActivity.this, "imageorder.txt", initalIndexString);
            writeFileOnInternalStorage(MainActivity.this, "imageorder.txt", randomIndexString.substring(1)); //get rid of first char (,)

            //initialize headline of motionEvent log
            writeFileOnInternalStorage(MainActivity.this, "motionEvents.txt", "pointerID; eventTime; action; relativeX; relativeY; rawX; rawY; xPrecision; yPrecision; downTime; orientation; pressure; size; edgeFlags; actionButton; metaState; toolType; toolMajor; toolMinor;");

            ListView listView = findViewById(R.id.listView);
            FrameLayout footerButton = (FrameLayout) getLayoutInflater().inflate(R.layout.footer_btn, null);
            Button closeButton = footerButton.findViewById(R.id.closeButton);
            ListAdapter listAdapter = new ListAdapter();
            listView.setAdapter(listAdapter);
            listView.addFooterView(footerButton);

            listView.setOnTouchListener(onTouchListener);
            mDetector = new GestureDetectorCompat(this, this);
            mDetector.setOnDoubleTapListener(this);
            listView.setOnScrollListener(new AbsListView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    String visibleImage = String.valueOf(firstVisibleItem);
                    writeFileOnInternalStorage(MainActivity.this, "scrollInfo.txt", visibleImage);
                }
            });

            closeButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    writeFileOnInternalStorage(MainActivity.this, "log.txt", "Task finished");
                    Intent intent = new Intent(MainActivity.this, StartScreen.class);
                    startActivity(intent);
                }
            });
        } else {
            Intent intent = new Intent(MainActivity.this, StartScreen.class);
            startActivity(intent);
            Toast.makeText(
                    MainActivity.this,
                    "Recheck your ID and make sure that the eyetracking has started",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    class ListAdapter extends BaseAdapter{
        Picasso picasso = Picasso.with(MainActivity.this);
        Integer errorImage = R.drawable.error;
        int[] status = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

        @Override
        public int getCount() {
            return IMAGES.length;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.listlayout, null);
            final ImageView imageView = view.findViewById(R.id.imageView);

            Integer imageHeight = getImageHeight(IMAGES[i]);
            imageView.getLayoutParams().height = imageHeight * 2;

            picasso.load(IMAGES[i]).placeholder(R.color.colorPlaceholder).error(errorImage).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(imageView);
            final Button likeBtnEmpty = view.findViewById(R.id.likeButtonEmpty);
            final Button likeBtnActive = view.findViewById(R.id.likeButtonActive);

            likeBtnEmpty.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    likeBtnActive.setVisibility(View.VISIBLE);
                    likeBtnEmpty.setVisibility(View.GONE);
                    status[i] = 1;
                    String likedImage = String.valueOf(i);
                    Log.d("liked", likedImage);
                    writeFileOnInternalStorage(MainActivity.this, "likes.txt", likedImage + "; liked");
                }
            });

            likeBtnActive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    likeBtnEmpty.setVisibility(View.VISIBLE);
                    likeBtnActive.setVisibility(View.GONE);
                    status[i] = 0;
                    String unlikedImage = String.valueOf(i);
                    Log.d("unliked", unlikedImage);
                    writeFileOnInternalStorage(MainActivity.this,"likes.txt", unlikedImage + "; unliked");
                }
            });

            Log.d("tag", "getView: " + status[i]);
            if(status[i] == 1){
                likeBtnEmpty.setVisibility(View.GONE);
                likeBtnActive.setVisibility(View.VISIBLE);
            }
            return view;
        }

        public Integer getImageHeight(Integer image) {
            Integer height;
            switch (image) {
                case R.drawable.activity_4:     height = 405; break;
                case R.drawable.activity_6:     height = 405; break;
                case R.drawable.gadget_10:      height = 405; break;
                case R.drawable.captioned_7:    height = 405; break;
                case R.drawable.fashion_6:      height = 430; break;
                case R.drawable.food_10:        height = 430; break;
                case R.drawable.friends_1:      height = 450; break;
                case R.drawable.friends_3:      height = 510; break;
                case R.drawable.friends_10:     height = 510; break;
                case R.drawable.gadget_6:       height = 510; break;
                case R.drawable.pet_6:          height = 540; break;
                case R.drawable.captioned_2:    height = 560; break;
                case R.drawable.activity_8:     height = 925; break;
                case R.drawable.captioned_10:   height = 940; break;
                case R.drawable.selfie_7:       height = 960; break;
                case R.drawable.pet_4:          height = 1083;break;
                default:                        height = 480;
            }

            return height;
        }
    }

    View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            int historySize = event.getHistorySize();
            int pointerCount = event.getPointerCount();

            //Action Move events are batched together -> loop through historical data since last event trigger
            for (int h = 0; h < historySize; h++) {
                for (int p = 0; p < pointerCount; p++) {
                    try {
                        Integer pointerId = event.getPointerId(p);
                        Long eventTime = event.getHistoricalEventTime(h);
                        String action = MotionEvent.actionToString(event.getAction());
                        Float relativeX = event.getHistoricalY(p, h);
                        Float relativeY = event.getHistoricalX(p, h);
                        Float rawX = null;
                        Float rawY = null;
                        Float xPrecision = null;
                        Float yPrecision = null;
                        Long downTime = null;
                        Float orientation = event.getHistoricalOrientation(p,h);
                        Float pressure = event.getHistoricalPressure(p,h);
                        Float size = event.getHistoricalSize(p,h);
                        Integer edgeFlags = null;
                        Integer actionButton = null;
                        Integer metaState = null;
                        Integer toolType = null;
                        Float toolMajor = event.getHistoricalToolMajor(p, h);
                        Float toolMinor = event.getHistoricalToolMinor(p, h);

                        String log = String.format(Locale.GERMAN, "%d; %o; %s; %f; %f; %f; %f; %f; %f; %o; %f; %f; %f; %d; %d; %d; %d; %f; %f;",
                                pointerId,
                                eventTime,
                                action,
                                relativeX,
                                relativeY,
                                rawX,
                                rawY,
                                xPrecision,
                                yPrecision,
                                downTime,
                                orientation,
                                pressure,
                                size,
                                edgeFlags,
                                actionButton,
                                metaState,
                                toolType,
                                toolMajor,
                                toolMinor
                        );

                        writeFileOnInternalStorage(MainActivity.this, "motionEvents.txt", log);
                        writeFileOnInternalStorage(MainActivity.this, "rawHistoricalEvent.txt", event.toString());
                    } catch (Exception e) {
                        Log.e("Historical", "onTouch", e );
                    }
                }
            }

            //most current event data
            for (int p = 0; p < pointerCount; p++) {
                try {
                    Integer pointerId = event.getPointerId(p);
                    Long eventTime = event.getEventTime();
                    String action = MotionEvent.actionToString(event.getAction());
                    Float relativeX = event.getX(p);
                    Float relativeY = event.getY(p);
                    Float rawX = event.getRawX();
                    Float rawY = event.getRawY();
                    Float xPrecision = event.getXPrecision();
                    Float yPrecision = event.getYPrecision();
                    Long downTime = event.getDownTime();
                    Float orientation = event.getOrientation(p);
                    Float pressure = event.getPressure(p);
                    Float size = event.getSize(p);
                    Integer edgeFlags = event.getEdgeFlags();
                    Integer actionButton = event.getActionButton();
                    Integer metaState = event.getMetaState();
                    Integer toolType = event.getToolType(p);
                    Float toolMajor = event.getToolMajor(p);
                    Float toolMinor = event.getToolMinor(p);

                    String log = String.format(Locale.GERMAN, "%d; %o; %s; %f; %f; %f; %f; %f; %f; %o; %f; %f; %f; %d; %d; %d; %d; %f; %f;",
                            pointerId,
                            eventTime,
                            action,
                            relativeX,
                            relativeY,
                            rawX,
                            rawY,
                            xPrecision,
                            yPrecision,
                            downTime,
                            orientation,
                            pressure,
                            size,
                            edgeFlags,
                            actionButton,
                            metaState,
                            toolType,
                            toolMajor,
                            toolMinor
                    );

                    writeFileOnInternalStorage(MainActivity.this, "motionEvents.txt", log);
                    writeFileOnInternalStorage(MainActivity.this, "rawEvent.txt", event.toString());
                } catch (Exception e) {
                    Log.e("Historical", "onTouch", e );
                }
            }

            if (mDetector.onTouchEvent(event)) {
                return true;
            }
            return false;
        }
    };

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        writeFileOnInternalStorage(MainActivity.this, "gesture.txt", "onSingleTapConfirmed: " + event.toString());
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        writeFileOnInternalStorage(MainActivity.this, "gesture.txt", "onDoubleTap: " + event.toString());
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        writeFileOnInternalStorage(MainActivity.this, "gesture.txt", "onDoubleTapEvent: " + event.toString());
        return false;
    }

    @Override
    public boolean onDown(MotionEvent event) {
        writeFileOnInternalStorage(MainActivity.this, "gesture.txt", "onDown: " + event.toString());
        return false;
    }

    @Override
    public void onShowPress(MotionEvent event) {
        writeFileOnInternalStorage(MainActivity.this, "gesture.txt", "onShowPress: " + event.toString());

    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        writeFileOnInternalStorage(MainActivity.this, "gesture.txt", "onSingleTapUp: " + event.toString());
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {
        writeFileOnInternalStorage(MainActivity.this, "gesture.txt", "onScroll: " + event1.toString() + ", " + event2.toString() + ", " + distanceX + ", " + distanceY);
        return false;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        writeFileOnInternalStorage(MainActivity.this, "gesture.txt", "onLongPress: " + event.toString());
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
        writeFileOnInternalStorage(MainActivity.this, "gesture.txt", "onFling: " + event1.toString() + ", " + event2.toString() + ", " + velocityX + ", " + velocityY);
        return false;
    }

    public boolean hasEyetrackingStarted() {
        Log.d("HERE", "hasEyetrackingStarted: checking");
        File file = new File(MainActivity.this.getFilesDir(), participant + "_" + startTime);
        if(!file.exists()){
            return false;
        } else {
            return true;
        }
    }

    public void writeFileOnInternalStorage(Context mcoContext, String sFileName, String sBody) {
        SimpleDateFormat timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        File file = new File(mcoContext.getFilesDir(), participant + "_" + startTime);

        try {
            File outFile = new File(file, sFileName);
            FileWriter writer = new FileWriter(outFile, true);
            writer.append(timestamp.format(new Date()));
            writer.append("; ");
            writer.append(sBody);
            writer.append("\n");
            writer.flush();
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();

        }
    }
};

