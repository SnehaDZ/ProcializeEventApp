package com.procialize.eventsapp.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.firebase.iid.FirebaseInstanceId;
import com.procialize.eventsapp.Adapter.EventAdapter;
import com.procialize.eventsapp.ApiConstant.APIService;
import com.procialize.eventsapp.ApiConstant.ApiUtils;
import com.procialize.eventsapp.BuildConfig;
import com.procialize.eventsapp.DbHelper.ConnectionDetector;
import com.procialize.eventsapp.GetterSetter.ExhibitorList;
import com.procialize.eventsapp.GetterSetter.Login;
import com.procialize.eventsapp.R;
import com.procialize.eventsapp.Session.SessionManager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExhibitorCatListActivity extends AppCompatActivity {

    RecyclerView exhibitorrecycler;
    SwipeRefreshLayout eventrefresh;
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    static final String TAG = "GCMDemo";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    RecyclerView eventrecycler;
    EventAdapter eventAdapter;
    String MY_PREFS_NAME = "ProcializeInfo";
    String MY_PREFS_LOGIN = "ProcializeLogin";
    String emailid, password;
    String eventnamestr;
    EditText searchEt;
    ImageView img_logout, imageview;
    SessionManager session;
    String platform, device, os_version, app_version, accesstiken;

    String gcmRegID;
    ImageView headerlogoIv;
    private APIService mAPIService;
    private ProgressBar progressBar;
    private String logoImg = "", colorActive = "", background = "";
    private AssetManager assetManager;
    private ProgressDialog mProgressDialog;
    private AsyncTask mMyTask;
    LinearLayout linear;
    String imgname;
    ConnectionDetector cd;
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exhibitor_listing);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        exhibitorrecycler=findViewById(R.id.exhibitorrecycler);
        eventrefresh=findViewById(R.id.eventrefresh);

        emailid = getIntent().getExtras().getString("email");
        password = getIntent().getExtras().getString("password");
        accesstiken = getIntent().getExtras().getString("accesstiken");
        session = new SessionManager(getApplicationContext());

        cd = new ConnectionDetector(ExhibitorCatListActivity.this);

        platform = "android";
        device = Build.MODEL;
        os_version = Build.VERSION.RELEASE;
        app_version = "Version" + BuildConfig.VERSION_NAME;
        eventrecycler = findViewById(R.id.eventrecycler);
        eventrefresh = findViewById(R.id.eventrefresh);
        progressBar = findViewById(R.id.progressBar);
        searchEt = findViewById(R.id.searchEt);
        img_logout = findViewById(R.id.img_logout);
        linear = findViewById(R.id.linear);
        imageview = findViewById(R.id.imageview);
//        searchEt.setFocusable(false);


        img_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                session.logoutUser();
                try {
                    File mypath = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "/Procialize/" + "background.jpg");
                    mypath.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(ExhibitorCatListActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        eventrecycler.setLayoutManager(mLayoutManager);



        mAPIService = ApiUtils.getAPIService();


        sendEventList(emailid, password);

        eventrefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                sendEventList(emailid, password);
            }
        });


        searchEt.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                eventAdapter.getFilter().filter(s.toString());
            }
        });

        if (checkPlayServices()) {

            // gcmRegID = getRegistrationId(this);

            gcmRegID = session.getGcmID();

            if (gcmRegID.isEmpty()) {
                // storeRegistrationId(this, gcmRegID);
                new getGCMRegId().execute();
            }

        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }


    }


    public void sendEventList(String email, String password) {
        mAPIService.ExhibitorFetch(email, password).enqueue(new Callback<ExhibitorList>() {
            @Override
            public void onResponse(Call<ExhibitorList> call, Response<ExhibitorList> response) {

                if (response.isSuccessful()) {
                    Log.i("hit", "post submitted to API." + response.body().toString());
                    dismissProgress();
                    if (response.body().getMsg().equalsIgnoreCase("Invalid Token!")) {
                        Intent main = new Intent(ExhibitorCatListActivity.this, LoginActivity.class);
                        startActivity(main);
                        finish();

                    } else {
                        showResponse(response);
                    }
                } else {
                    dismissProgress();
                    Toast.makeText(getApplicationContext(), response.message(), Toast.LENGTH_SHORT).show();
                    Log.i("hit", "post submitted to API Wrong.");
                }
            }

            @Override
            public void onFailure(Call<ExhibitorList> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Low network or no network", Toast.LENGTH_SHORT).show();
                dismissProgress();
            }
        });
    }

    public void showResponse(Response<ExhibitorList> response) {

        if (response.body().getStatus().equals("success")) {

//            eventAdapter = new EventAdapter(ExhibitorCatListActivity.this, response.body().getUserEventList(), this);
//            eventAdapter.notifyDataSetChanged();
//            eventrecycler.setAdapter(eventAdapter);
//            eventrecycler.scheduleLayoutAnimation();


            if (eventrefresh.isRefreshing()) {
                eventrefresh.setRefreshing(false);
            }

        } else {
            Toast.makeText(this, response.body().getMsg(), Toast.LENGTH_SHORT).show();
            if (eventrefresh.isRefreshing()) {
                eventrefresh.setRefreshing(false);
            }
        }
    }

    public void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void dismissProgress() {
        if (progressBar.getVisibility() == View.VISIBLE) {
            progressBar.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onResume() {
        //  overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        super.onResume();
    }

//    @Override
//    public void onContactSelected(UserEventList eventList) {
//
//        try {
//            File mypath = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "/Procialize/" + "background.jpg");
//            mypath.delete();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//        String eventid = eventList.getEventId();
//        eventnamestr = eventList.getName();
//        logoImg = eventList.getHeader_logo();
//        colorActive = eventList.getPrimary_color_code();
//        background = eventList.getBackground_image();
//
////        mProgressDialog = new ProgressDialog(ExhibitorCatListActivity.this);
////        mProgressDialog.setIndeterminate(true);
////        // Progress dialog horizontal style
////        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
////        // Progress dialog title
////        mProgressDialog.setTitle("AsyncTask");
////        // Progress dialog message
////        mProgressDialog.setMessage("Please wait, we are downloading your image file...");
//
//        // Initialize a new click listener for positive button widget
//        url = ApiConstant.eventpic + eventList.getBackground_image();
//        Glide.with(this).load(url)
//                .apply(RequestOptions.skipMemoryCacheOf(true))
//                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE)).listener(new RequestListener<Drawable>() {
//            @Override
//            public boolean onLoadFailed(@Nullable GlideException e, Object model, com.bumptech.glide.request.target.Target<Drawable> target, boolean isFirstResource) {
////                progressBar.setVisibility(View.GONE);
//                e.printStackTrace();
//                return false;
//            }
//
//            @Override
//            public boolean onResourceReady(Drawable resource, Object model, com.bumptech.glide.request.target.Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
////                progressBar.setVisibility(View.GONE);
//                return false;
//            }
//        }).into(imageview);
//
//
//        imgname = "background";//url.substring(58, 60);
//
//        if (cd.isConnectingToInternet()) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//
//                if (ExhibitorCatListActivity.this.checkSelfPermission(
//                        "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
//                    final String[] permissions = new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};
//                    ActivityCompat.requestPermissions(ExhibitorCatListActivity.this, permissions, 0);
//                } else if (ExhibitorCatListActivity.this.checkSelfPermission(
//                        "android.permission.READ_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
//                    final String[] permissions = new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};
//                    ActivityCompat.requestPermissions(ExhibitorCatListActivity.this, permissions, 0);
//                } else {
//
//                    PicassoTrustAll.getInstance(ExhibitorCatListActivity.this)
//                            .load(ApiConstant.eventpic + eventList.getBackground_image())
//                            .into(new Target() {
//                                @Override
//                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                                    try {
//                                        String root = Environment.getExternalStorageDirectory().toString();
//                                        File myDir = new File(root + "/Procialize");
////                                            Toast.makeText(ExhibitorCatListActivity.this,
////                                                    "Download completed- check folder Procialize/Image",
////                                                    Toast.LENGTH_SHORT).show();
//                                        if (!myDir.exists()) {
//                                            myDir.mkdirs();
//                                        }
//
//                                        String name = imgname + ".jpg";
//                                        myDir = new File(myDir, name);
//                                        FileOutputStream out = new FileOutputStream(myDir);
//                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
//
//                                        out.flush();
//                                        out.close();
//                                    } catch (Exception e) {
//                                        // some action
//                                    }
//                                }
//
//                                @Override
//                                public void onBitmapFailed(Drawable errorDrawable) {
//                                }
//
//                                @Override
//                                public void onPrepareLoad(Drawable placeHolderDrawable) {
//                                }
//                            });
//
//                }
//            } else {
//
//                PicassoTrustAll.getInstance(ExhibitorCatListActivity.this)
//                        .load(ApiConstant.eventpic + eventList.getBackground_image())
//                        .into(new Target() {
//                            @Override
//                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                                try {
//                                    String root = Environment.getExternalStorageDirectory().toString();
//                                    File myDir = new File(root + "/Procialize");
////                                        Toast.makeText(ExhibitorCatListActivity.this,
////                                                "Download completed- check folder Procialize/Image",
////                                                Toast.LENGTH_SHORT).show();
//                                    if (!myDir.exists()) {
//                                        myDir.mkdirs();
//                                    }
//
//                                    String name = imgname + ".jpg";
//                                    myDir = new File(myDir, name);
//                                    FileOutputStream out = new FileOutputStream(myDir);
//                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
//
//                                    out.flush();
//                                    out.close();
//                                } catch (Exception e) {
//                                    // some action
//                                }
//                            }
//
//                            @Override
//                            public void onBitmapFailed(Drawable errorDrawable) {
//                            }
//
//                            @Override
//                            public void onPrepareLoad(Drawable placeHolderDrawable) {
//                            }
//                        });
//
//            }
//
//
//        } else {
//            Toast.makeText(ExhibitorCatListActivity.this, "No Intrnet Connection", Toast.LENGTH_SHORT).show();
//        }
//        sendLogin(emailid, password, eventid, gcmRegID, platform, device, os_version, app_version);
//    }


    public void sendLogin(String email, String password, final String eventid, String registration_id, String platform, String device, String os_version, String app_version) {
        mAPIService.LoginPost(email, password, eventid, registration_id, platform, device, os_version, accesstiken,app_version).enqueue(new Callback<Login>() {
            @Override
            public void onResponse(Call<Login> call, Response<Login> response) {

                if (response.isSuccessful()) {
                    Log.i("hit", "post submitted to API." + response.body().toString());
                    dismissProgress();
                    showResponseLogin(response, eventid);
                } else {
                    dismissProgress();
                    Toast.makeText(getApplicationContext(), response.message(), Toast.LENGTH_SHORT).show();
                    Log.i("hit", "post submitted to API Wrong.");
                }
            }

            @Override
            public void onFailure(Call<Login> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Low network or no network", Toast.LENGTH_SHORT).show();
                dismissProgress();
            }
        });
    }

    public void showResponseLogin(Response<Login> response, String eventid) {

        if (response.body().getStatus().equals("success")) {


            SessionManager sessionManager = new SessionManager(this);

            String firstname = response.body().getUserData().getFirstName();
            String lastname = response.body().getUserData().getLastName();
            String email = response.body().getUserData().getEmail();
            String mobile = response.body().getUserData().getMobile();
            String designation = response.body().getUserData().getDesignation();
            String company = response.body().getUserData().getCompanyName();
            String token = response.body().getUserData().getApiAccessToken();
            String desc = response.body().getUserData().getDescription();
            String city = response.body().getUserData().getCity();
            String country = response.body().getUserData().getCountry();
            String pic = response.body().getUserData().getProfilePic();
            String id = response.body().getUserData().getAttendeeId();
            String attendee_status = response.body().getUserData().getAttendee_status();
            String exhibitor_status = response.body().getUserData().getExhibitor_status();
            String exhibitor_id = response.body().getUserData().getExhibitor_id();

            sessionManager.createLoginSession(firstname, lastname, email, mobile, company, designation, token, desc, city, country, pic, id, emailid, password, "1", attendee_status,exhibitor_id,exhibitor_status);
            SessionManager.saveSharedPreferencesEventList(response.body().getEventSettingList());
            SessionManager.saveSharedPreferencesMenuEventList(response.body().getEventMenuSettingList());


            SharedPreferences prefs = getSharedPreferences(MY_PREFS_LOGIN, MODE_PRIVATE);
            String login = prefs.getString("loginfirst", "");


            if (login.equalsIgnoreCase("1")) {
                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString("eventid", eventid).commit();
                editor.putString("eventnamestr", eventnamestr).commit();
                editor.putString("logoImg", logoImg).commit();
                editor.putString("colorActive", colorActive).commit();
                editor.putString("eventback", background).commit();


//              Company company1 = new Company.Builder().withName(company).build();

//                UserAttributes userAttributes = new UserAttributes.Builder()
//                        .withName(firstname + " " + lastname)
//                        .withEmail(email)
//                        .withPhone(mobile)
////                      .withCompany(company1)
//                        .withUserId(id)
//                        .build();
//
//                Registration registration = Registration.create().withUserAttributes(userAttributes);
//                Intercom.client().registerIdentifiedUser(registration);

//                editor.putString("loginfirst","1");
                editor.apply();
                Intent home = new Intent(getApplicationContext(), ProfileActivity.class);
                home.putExtra("back", eventid);
//                home.putExtra("eventnamestr", eventnamestr);
                startActivity(home);
                finish();
            } else {
                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString("eventid", eventid).commit();
                editor.putString("eventnamestr", eventnamestr).commit();
                editor.putString("logoImg", logoImg).commit();
                editor.putString("colorActive", colorActive).commit();

//              editor.putString("loginfirst","1");
                editor.apply();

                Intent home = new Intent(getApplicationContext(), ProfileActivity.class);
                home.putExtra("back", eventid);
//              home.putExtra("eventnamestr", eventnamestr);
                startActivity(home);
                finish();
            }

        } else {
            Toast.makeText(this, response.body().getMsg(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private class getGCMRegId extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... arg0) {


            try {
                //gcmRegID = gcmRegistrationHelper.GCMRegister(REG_ID);
                String token = FirebaseInstanceId.getInstance().getToken();
                Log.d("MYTAG", "This is your Firebase token" + token);

                gcmRegID = token;

                session.storeGcmID(gcmRegID);

                // storeRegistrationId(getApplicationContext(), gcmRegID);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // session = new SessionManagement(getApplicationContext());
            // if (session.isLoggedIn()) {
            // Update GCM ID to Server
            // new updateGCMRegId().execute();
            // }

        }
    }

    private class DownloadTask extends AsyncTask<URL, Void, Bitmap> {
        // Before the tasks execution
        protected void onPreExecute() {
            // Display the progress dialog on async task start
            mProgressDialog.show();
        }

        // Do the task in background/non UI thread
        protected Bitmap doInBackground(URL... urls) {
            URL url = urls[0];
            HttpURLConnection connection = null;

            try {
                // Initialize a new http url connection
                connection = (HttpURLConnection) url.openConnection();

                // Connect the http url connection
                connection.connect();

                // Get the input stream from http url connection
                InputStream inputStream = connection.getInputStream();

                /*
                    BufferedInputStream
                        A BufferedInputStream adds functionality to another input stream-namely,
                        the ability to buffer the input and to support the mark and reset methods.
                */
                /*
                    BufferedInputStream(InputStream in)
                        Creates a BufferedInputStream and saves its argument,
                        the input stream in, for later use.
                */
                // Initialize a new BufferedInputStream from InputStream
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

                /*
                    decodeStream
                        Bitmap decodeStream (InputStream is)
                            Decode an input stream into a bitmap. If the input stream is null, or
                            cannot be used to decode a bitmap, the function returns null. The stream's
                            position will be where ever it was after the encoded data was read.

                        Parameters
                            is InputStream : The input stream that holds the raw data
                                              to be decoded into a bitmap.
                        Returns
                            Bitmap : The decoded bitmap, or null if the image data could not be decoded.
                */
                // Convert BufferedInputStream to Bitmap object
                Bitmap bmp = BitmapFactory.decodeStream(bufferedInputStream);

                // Return the downloaded bitmap
                return bmp;

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Disconnect the http url connection
                connection.disconnect();
            }
            return null;
        }

        // When all async task done
        protected void onPostExecute(Bitmap result) {
            // Hide the progress dialog
            mProgressDialog.dismiss();

            if (result != null) {
                // Display the downloaded image into ImageView
//                mImageView.setImageBitmap(result);

                // Save bitmap to internal storage
                Uri imageInternalUri = saveImageToInternalStorage(result);
                // Set the ImageView image from internal storage
//                mImageViewInternal.setImageURI(imageInternalUri);
            } else {
                // Notify user that an error occurred while downloading image
                Snackbar.make(linear, "Error", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    // Custom method to convert string to url
    protected URL stringToURL(String urlString) {
        try {
            URL url = new URL(urlString);
            return url;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected Uri saveImageToInternalStorage(Bitmap bitmap) {
        // Initialize ContextWrapper
        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());

        // Initializing a new file
        // The bellow line return a directory in internal storage
        File file = wrapper.getDir("Images", MODE_PRIVATE);

        // Create a file to save the image
        file = new File(file, "UniqueFileName" + ".jpg");

        try {
            // Initialize a new OutputStream
            OutputStream stream = null;

            // If the output file exists, it can be replaced or appended to it
            stream = new FileOutputStream(file);

            // Compress the bitmap
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

            // Flushes the stream
            stream.flush();

            // Closes the stream
            stream.close();

        } catch (IOException e) // Catch the exception
        {
            e.printStackTrace();
        }

        // Parse the gallery image url to uri
        Uri savedImageURI = Uri.parse(file.getAbsolutePath());

        // Return the saved image Uri
        return savedImageURI;
    }

    private String saveToInternalStorage(Bitmap bitmapImage) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, "profile.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }
}



