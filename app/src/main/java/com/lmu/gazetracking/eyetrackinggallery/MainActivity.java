package com.lmu.gazetracking.eyetrackinggallery;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    int[] IMAGES = {R.drawable.test_2, R.drawable.test_2, R.drawable.test_2, R.drawable.test_2};
    String participant = "empty";
    String startTime = "no-date";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null)
            participant = bundle.getString("participant");
            startTime = bundle.getString("startTime");

        ListView listView = findViewById(R.id.listView);
        ListAdapter listAdapter = new ListAdapter();
        listView.setAdapter(listAdapter);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                Log.d("OnScrollStateChanged", String.valueOf(scrollState));
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                String visibleImage = String.valueOf(firstVisibleItem);
                Log.d("onScroll", visibleImage);
                writeFileOnInternalStorage(MainActivity.this,"scrollInfo.txt", visibleImage);
            }
        });
    }

    class ListAdapter extends BaseAdapter{

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
            ImageView imageView = view.findViewById(R.id.imageView);
            imageView.setImageResource(IMAGES[i]);

            final Button likeBtnEmpty = view.findViewById(R.id.likeButtonEmpty);
            final Button likeBtnActive = view.findViewById(R.id.likeButtonActive);

            likeBtnEmpty.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    likeBtnActive.setVisibility(View.VISIBLE);
                    likeBtnEmpty.setVisibility(View.GONE);

                    String likedImage = String.valueOf(i);
                    Log.d("liked", likedImage);
                    writeFileOnInternalStorage(MainActivity.this, "likes.txt", likedImage);
                }
            });

            likeBtnActive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    likeBtnEmpty.setVisibility(View.VISIBLE);
                    likeBtnActive.setVisibility(View.GONE);

                    String unlikedImage = String.valueOf(i);
                    Log.d("unliked", unlikedImage);
                    writeFileOnInternalStorage(MainActivity.this,"likes.txt", unlikedImage);
                }
            });
            return view;
        }
    }

    public void writeFileOnInternalStorage(Context mcoContext, String sFileName, String sBody){
        SimpleDateFormat timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        File file = new File(mcoContext.getFilesDir(), participant + "_" + startTime);

        if(!file.exists()){
            file.mkdir();
        }

        try{
            File outFile = new File(file, sFileName);
            FileWriter writer = new FileWriter(outFile, true);
            writer.append(timestamp.format(new Date()));
            writer.append("; ");
            writer.append(sBody);
            writer.append("\n");
            writer.flush();
            writer.close();

        }catch (Exception e){
            e.printStackTrace();

        }
    };
};

