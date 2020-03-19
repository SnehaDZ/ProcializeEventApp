package com.procialize.eventsapp.InnerDrawerActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.procialize.eventsapp.Adapter.SwipeImageAdapter;
import com.procialize.eventsapp.Adapter.SwipepagerAdapter;
import com.procialize.eventsapp.CustomTools.PicassoTrustAll;
import com.procialize.eventsapp.DbHelper.ConnectionDetector;
import com.procialize.eventsapp.GetterSetter.FirstLevelFilter;
import com.procialize.eventsapp.R;
import com.procialize.eventsapp.Utility.Util;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import cn.jzvd.JZVideoPlayer;

public class SwappingGalleryActivity extends AppCompatActivity implements SwipeImageAdapter.SwipeImageAdapterListner {

    public int rvposition = 0;
    String name;
    List<FirstLevelFilter> firstLevelFilters;
    SwipeImageAdapter swipeImageAdapter;
    SwipepagerAdapter swipepagerAdapter;
    RecyclerView recyclerView;
    ViewPager pager;
    ImageView right, left, backIv;
    ImageView headerlogoIv;
    String colorActive;
    String MY_PREFS_NAME = "ProcializeInfo";
    String img = "";
    private ConnectionDetector cd;
    RelativeLayout linear;

    static public void shareImage(String url, final Context context) {
        Picasso.with(context).load(url).into(new com.squareup.picasso.Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("image/*");
                i.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(bitmap, context));
                context.startActivity(Intent.createChooser(i, "Share Image"));
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        });
    }

    static public Uri getLocalBitmapUri(Bitmap bmp, Context context) {
        Uri bmpUri = null;
        try {
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
//            bmpUri = Uri.fromFile(file);
            bmpUri = FileProvider.getUriForFile(context, "com.procialize.eventsapp.android.fileprovider", file);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    @Override
    public void onContactSelected(FirstLevelFilter filtergallerylists) {

        indexset(filtergallerylists.getFileName());
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public void indexset(String name) {
        for (int j = 0; j < firstLevelFilters.size(); j++) {
            if (firstLevelFilters.get(j).getFileName().equalsIgnoreCase(name)) {
                pager.setCurrentItem(j);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swapping_gallery);
        cd = new ConnectionDetector(this);

        name = getIntent().getExtras().getString("url");
        firstLevelFilters = (List<FirstLevelFilter>) getIntent().getExtras().getSerializable("gallerylist");

        recyclerView = findViewById(R.id.listrecycler);
        pager = findViewById(R.id.pager);
        right = findViewById(R.id.right);
        left = findViewById(R.id.left);
        backIv = findViewById(R.id.backIv);
        linear = findViewById(R.id.linear);

        headerlogoIv = findViewById(R.id.headerlogoIv);
        Util.logomethod(this, headerlogoIv);
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        colorActive = prefs.getString("colorActive", "");

        backIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        try {

            File mypath = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "/Procialize/" + "background.jpg");
            Resources res = getResources();
            Bitmap bitmap = BitmapFactory.decodeFile(String.valueOf(mypath));
            BitmapDrawable bd = new BitmapDrawable(res, bitmap);
            linear.setBackgroundDrawable(bd);

            Log.e("PATH", String.valueOf(mypath));
        } catch (Exception e) {
            e.printStackTrace();
            linear.setBackgroundColor(Color.parseColor("#f1f1f1"));
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        swipeImageAdapter = new SwipeImageAdapter(this, firstLevelFilters, this);
        recyclerView.setAdapter(swipeImageAdapter);
        swipeImageAdapter.notifyDataSetChanged();


        swipepagerAdapter = new SwipepagerAdapter(this, firstLevelFilters);
        pager.setAdapter(swipepagerAdapter);
        swipepagerAdapter.notifyDataSetChanged();


        LinearLayout linMsg = findViewById(R.id.linMsg);
        LinearLayout linsave = findViewById(R.id.linsave);
        TextView savebtn = findViewById(R.id.savebtn);
        TextView sharebtn = findViewById(R.id.sharebtn);

        linsave.setBackgroundColor(Color.parseColor(colorActive));
        linMsg.setBackgroundColor(Color.parseColor(colorActive));
        savebtn.setBackgroundColor(Color.parseColor(colorActive));
        sharebtn.setBackgroundColor(Color.parseColor(colorActive));


        linMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cd.isConnectingToInternet()) {

                    try {
                        try {
                            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                            String imageFileName = "JPEG_" + timeStamp + "_";
                            img = imageFileName;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        if (SwappingGalleryActivity.this.checkSelfPermission(
                                "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
                            final String[] permissions = new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};
                            ActivityCompat.requestPermissions(SwappingGalleryActivity.this, permissions, 0);
                        } else if (SwappingGalleryActivity.this.checkSelfPermission(
                                "android.permission.READ_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
                            final String[] permissions = new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};
                            ActivityCompat.requestPermissions(SwappingGalleryActivity.this, permissions, 0);
                        } else {

                            // new myAsyncTask().execute();
                            PicassoTrustAll.getInstance(SwappingGalleryActivity.this)
                                    .load(firstLevelFilters.get(rvposition).getFileName())
                                    .into(new com.squareup.picasso.Target() {
                                              @Override
                                              public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                                  try {
                                                      String root = Environment.getExternalStorageDirectory().toString();
                                                      File myDir = new File(root + "/Procialize");

                                                      if (!myDir.exists()) {
                                                          myDir.mkdirs();
                                                      }
                                                      Toast.makeText(SwappingGalleryActivity.this,
                                                              "Download completed- check folder Procialize/Image",
                                                              Toast.LENGTH_SHORT).show();
                                                      String name = img + ".jpg";
                                                      myDir = new File(myDir, name);
                                                      FileOutputStream out = new FileOutputStream(myDir);
                                                      bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);

                                                      out.flush();
                                                      out.close();
                                                  } catch (Exception e) {
                                                      // some action
                                                  }
                                              }

                                              @Override
                                              public void onBitmapFailed(Drawable errorDrawable) {
                                              }

                                              @Override
                                              public void onPrepareLoad(Drawable placeHolderDrawable) {
                                              }
                                          }
                                    );

                        }


                    } else {
                        //new myAsyncTask().execute();
                        PicassoTrustAll.getInstance(SwappingGalleryActivity.this)
                                .load(firstLevelFilters.get(rvposition).getFileName())
                                .into(new com.squareup.picasso.Target() {
                                          @Override
                                          public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                              try {
                                                  String root = Environment.getExternalStorageDirectory().toString();
                                                  File myDir = new File(root + "/Procialize");

                                                  if (!myDir.exists()) {
                                                      myDir.mkdirs();
                                                  }
                                                  Toast.makeText(SwappingGalleryActivity.this,
                                                          "Download completed- check folder Procialize/Image",
                                                          Toast.LENGTH_SHORT).show();
                                                  String name = img + ".jpg";
                                                  myDir = new File(myDir, name);

                                                  FileOutputStream out = new FileOutputStream(myDir);
                                                  bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);

                                                  out.flush();
                                                  out.close();
                                              } catch (Exception e) {
                                                  // some action
                                              }
                                          }

                                          @Override
                                          public void onBitmapFailed(Drawable errorDrawable) {
                                          }

                                          @Override
                                          public void onPrepareLoad(Drawable placeHolderDrawable) {
                                          }
                                      }
                                );

                    }


                } else {
                    Toast.makeText(SwappingGalleryActivity.this, "No Internet Connection",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        linsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareImage(firstLevelFilters.get(rvposition).getFileName(), SwappingGalleryActivity.this);
            }
        });

        indexset(name);
        int colorInt = Color.parseColor(colorActive);
        ColorStateList csl = ColorStateList.valueOf(colorInt);
        Drawable drawable = DrawableCompat.wrap(right.getDrawable());
        DrawableCompat.setTintList(drawable, csl);
        right.setImageDrawable(drawable);

        Drawable drawable1 = DrawableCompat.wrap(left.getDrawable());
        DrawableCompat.setTintList(drawable1, csl);
        left.setImageDrawable(drawable1);

        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {

                    int position = pager.getCurrentItem() + 1;

                    if (position <= firstLevelFilters.size()) {
                        pager.setCurrentItem(position);
                        recyclerView.scrollToPosition(position);
//                        if(pager.getCurrentItem()==position) {
//                            recyclerView.setBackgroundColor(Color.parseColor(colorActive));
//                        }
                        recyclerView.getItemDecorationAt(position);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {

                    int position = pager.getCurrentItem() - 1;

                    if (position >= 0) {
                        pager.setCurrentItem(position);
                        recyclerView.scrollToPosition(position);

//                        if(pager.getCurrentItem()==position) {
//                            recyclerView.setBackgroundColor(Color.parseColor(colorActive));
//                        }
                        recyclerView.getItemDecorationAt(position);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });


        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                recyclerView.scrollToPosition(position);
//                recyclerView.getItemDecorationAt(position);
                rvposition = position;

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();

        JZVideoPlayer.releaseAllVideos();

    }
}
