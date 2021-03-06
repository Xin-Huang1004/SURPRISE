/*
 * Copyright 2017 CMPUT301W17T07
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cmput301w17t07.moody;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * The CreateMoodActivity is used for  handling the user interface
 * logic for when a user is creating a mood.
 */

public class CreateMoodActivity extends BarMenuActivity implements LocationListener{
    private ImageView mImageView;
    private String EmotionText = "";
    private String SocialSituation = "";
    private EditText Description;
    private String userName;
    private LocationManager locationManager;
    private double latitude = 0;
    private double longitude = 0;
    private String provider;
    private TextView locationText;
    private Location location;
    private String address;
    private Mood tempMood;
    private String moodMessage_text = "";
    private int pickLocation = 0;
    private static Achievements achievements;
    private Bitmap bitmap = null;
    //________________________________
    //parameters for datetimePicker
    AlertDialog TimeDialog;
    AlertDialog DateDialog;
    Calendar calendar = Calendar.getInstance();
    int currentYear = calendar.get(Calendar.YEAR);
    int currentMonth = calendar.get(Calendar.MONTH);
    int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
    int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
    int currentMinute = calendar.get(Calendar.MINUTE);
    String dateString;
    Date date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_mood);
        UserController userController = new UserController();
        userName = userController.readUsername(CreateMoodActivity.this).toString();
        setUpMenuBar(this);
        location = null;
        date = new Date();
        Intent intent = getIntent();
        locationText = (TextView) findViewById(R.id.locationText);
        Description = (EditText) findViewById(R.id.Description);
        mImageView = (ImageView) findViewById(R.id.editImageView);

        //try to get picklocation, if it is equal to 1 that means, user just back from map not
        //other activities
        try{
            pickLocation = (int) intent.getExtras().getInt("pickLocation");}
        catch(Exception e){e.printStackTrace();
        }
        if(pickLocation == 1){
            tempMood = (Mood) intent.getSerializableExtra("editMood");
            bitmap = (Bitmap) intent.getParcelableExtra("bitmapback");
            latitude = tempMood.getLatitude();
            longitude = tempMood.getLongitude();
            address = tempMood.getDisplayLocation();
            locationText.setText(address);
            Description.setText(tempMood.getMoodMessage());
            mImageView.setImageBitmap(bitmap);
            date = tempMood.getDate();
            displayAttributes();
        }



        /**
         * Spinner dropdown logic taken from http://stackoverflow.com/questions/13377361/how-to-create-a-drop-down-list <br>
         * Author: Nicolas Tyler, 2013/07/15 8:47 <br>
         * taken by Xin Huang 2017/03/10 <br>
         */
        //Spinner for emotion and socialsituatuion
        if(pickLocation == 0) {
        Spinner dropdown = (Spinner) findViewById(R.id.Emotion);

        String[] items = new String[]{"anger", "confusion", "disgust", "fear", "happiness", "sadness", "shame", "surprise"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                EmotionText = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(CreateMoodActivity.this, "Please pick a feeling!", Toast.LENGTH_SHORT).show();
            }
        });

            Spinner dropdown_SocialSituation = (Spinner) findViewById(R.id.SocialSituation);
            String[] item_SocialSituation = new String[]{"", "alone", "with one other person",
                    "with two people", "with several people", "with a crowd"};
            ArrayAdapter<String> adapter_SocialSituation = new ArrayAdapter<String>
                    (this, android.R.layout.simple_spinner_dropdown_item, item_SocialSituation);
            dropdown_SocialSituation.setAdapter(adapter_SocialSituation);

            dropdown_SocialSituation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int position, long id) {
                    SocialSituation = parent.getItemAtPosition(position).toString();
                    TextView sizeView = (TextView) findViewById(R.id.SocialText);
                    sizeView.setText("  " + SocialSituation);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        ImageButton chooseButton = (ImageButton) findViewById(R.id.Camera);

        ImageButton locationButton = (ImageButton) findViewById(R.id.location);

        ImageButton PickerButton = (ImageButton) findViewById(R.id.Picker);

        //click on PickerButton, call the datetimePicker
        PickerButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                innit();
                TimeDialog.show();
            }
        });

        chooseButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                try {
                    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                    startActivityForResult(intent, 1);
                } catch (Exception e) {
                    Intent intent = new Intent(getApplicationContext(), CreateMoodActivity.class);
                    startActivity(intent);
                }
            }
        });

        chooseButton.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
                try {
                    Intent intent = new Intent("android.intent.action.PICK");
                    intent.setType("image/*");
                    startActivityForResult(intent, 0);
                } catch (Exception e) {
                    Intent intent = new Intent(getApplicationContext(), CreateMoodActivity.class);
                    startActivity(intent);
                }
                return true;
            }
        });

        locationButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                //check available tools
                List<String> locationList = locationManager.getProviders(true);
                if (locationList.contains(LocationManager.GPS_PROVIDER)) {
                    provider = LocationManager.GPS_PROVIDER;
                } else if (locationList.contains(LocationManager.NETWORK_PROVIDER)) {
                    provider = LocationManager.NETWORK_PROVIDER;
                } else {
                    Toast.makeText(getApplicationContext(), "Please check application permissions", Toast.LENGTH_LONG).show();
                }

                //check the permission
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    Toast.makeText(getApplicationContext(), "Get location failed, Please check the Permission", Toast.LENGTH_SHORT).show();
                    return;
                }

                location = locationManager.getLastKnownLocation(provider);
                if (location == null) {
                    latitude = 0;
                    longitude = 0;
                } else {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }

                Geocoder gcd = new Geocoder(CreateMoodActivity.this, Locale.getDefault());
                try{
                    List<Address> addresses = gcd.getFromLocation(latitude, longitude, 1);
                    if (addresses.size() > 0)
                        address = "  " + addresses.get(0).getFeatureName() + " " +
                                addresses.get(0).getThoroughfare() + ", " +
                                addresses.get(0).getLocality() + ", " +
                                addresses.get(0).getAdminArea() + ", " +
                                addresses.get(0).getCountryCode();
                    locationText.setText(address);}
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        });

        //pass users' changes to map, will be passed back
        locationButton.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
                int fromCreate = 123;
                moodMessage_text = Description.getText().toString();
                tempMood = new Mood(EmotionText, userName,
                        moodMessage_text, latitude,longitude, null,
                        SocialSituation,date, address);
                Intent editLocation = new Intent(CreateMoodActivity.this, EditLocation.class);
                editLocation.putExtra("EditMood", tempMood);
                editLocation.putExtra("fromCreate",fromCreate);
                editLocation.putExtra("bitmap", compress(bitmap));
                startActivity(editLocation);
                return true;
            }
        });

        Button submitButton = (Button) findViewById(R.id.button5);
        submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                moodMessage_text = Description.getText().toString();
                MoodController moodController = new MoodController();

                // --------------------------- achievements -------------------------------------
                AchievementManager.initManager(CreateMoodActivity.this);
                AchievementController achievementController = new AchievementController();
                achievements = achievementController.getAchievements();
                achievements.moodCount += 1;
                achievementController.incrementMoodCounter(EmotionText);
                achievementController.saveAchievements();
                // ------------------------------------------------------------------------------
                if(location != null || pickLocation == 1){
                    //todo can remove these if/else statements that toast message too long. They could
                    // be handled in the controller
                if (!MoodController.createMood(EmotionText, userName,
                        moodMessage_text, latitude,longitude, bitmap,
                        SocialSituation,date, address, CreateMoodActivity.this)) {
                    Toast.makeText(CreateMoodActivity.this,
                            "Mood message length is too long. Please try again.", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(CreateMoodActivity.this, TimelineActivity.class);
                    startActivity(intent);
                    finish();
                }}
                else{
                    if (!MoodController.createMood(EmotionText, userName,
                            moodMessage_text, 0,0, bitmap, SocialSituation,date,address,
                            CreateMoodActivity.this)) {
                        Toast.makeText(CreateMoodActivity.this,
                                "Mood message length is too long. Please try again.", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(CreateMoodActivity.this, TimelineActivity.class);
                        startActivity(intent);
                        finish();
                    }}
            }
        });
    }


    /**
     * Method handles user interface response to when a user adds an image to their mood
     * from either their camera or their gallery. <br>
     *
     * Knowledge and logic of onActivityResult referenced and taken from <br>
     * link: http://blog.csdn.net/AndroidStudioo/article/details/52077597 <br>
     * author: AndroidStudio 2016-07-31 11:15 <br>
     * taken by Xin Huang 2017-03-04 15:30 <br>
     * @param requestCode          integer indicating the kind of action taken by the user <br>
     * @param resultCode <br>
     * @param data <br>
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            finish();   //no data return
        }
        if (requestCode == 0) {
            //get pic from local photo
            try {
                bitmap = data.getParcelableExtra("data");
                if (bitmap == null) {//if pic is not so big use original one
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(data.getData());
                        bitmap = BitmapFactory.decodeStream(inputStream);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            } catch (RuntimeException e) {
                Intent intent = new Intent(getApplicationContext(), CreateMoodActivity.class);
                startActivity(intent);
            }

        } else if (requestCode == 1) {
            try {
                bitmap = (Bitmap) data.getExtras().get("data");
            } catch (Exception e) {
                Intent intent = new Intent(getApplicationContext(), CreateMoodActivity.class);
                startActivity(intent);
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            try {
                Intent intent = new Intent(getApplicationContext(), CreateMoodActivity.class);
                startActivity(intent);
            } catch (RuntimeException e) {
            }

        }
        mImageView.setImageBitmap(bitmap);


    }

    @Override
    public void onLocationChanged(Location location) {
        locationText = (TextView) findViewById(R.id.locationText);
        locationText.setText("Latitude:" + location.getLatitude() + ",Longitude:" + location.getLongitude());

        Log.e("Map", "Location changed : Lat: " + location.getLatitude()
                + " Lng: " + location.getLongitude());

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude", "disable");

    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude", "disable");

    }

    /**
     * link: http://blog.csdn.net/hzflogo/article/details/62423240 <br>
     * author: hzflogo 2017-03-16 14:58 <br>
     * taken by Xin Huang 2017-03-29 21:42 <br>
     * get the datetimePicker <br>
     * @param provider <br>
     */
    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude", "disable");
    }

    private void innit() {
        final View dateView = View.inflate(getApplicationContext(), R.layout.datepicker, null);
        final View timeView = View.inflate(getApplicationContext(), R.layout.timepicker, null);
        TimePicker timePicker = (TimePicker) timeView.findViewById(R.id.time);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                currentHour = hourOfDay;
                currentMinute = minute;
            }
        });
        DatePicker datePicker = (DatePicker) dateView.findViewById(R.id.pick);
        datePicker.setDrawingCacheBackgroundColor(getResources().getColor(R.color.colorAccent));
        datePicker.init(currentYear, currentMonth, currentDay, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                currentYear = year;
                currentMonth = monthOfYear+1;
                currentDay = dayOfMonth;
            }
        });
        DateDialog = new AlertDialog.Builder(CreateMoodActivity.this)
                .setView(dateView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dateString = currentYear + "-" + currentMonth + "-" + currentDay + " " + currentHour+":"+currentMinute;
                        try {
                            java.text.SimpleDateFormat formatter = new SimpleDateFormat(
                                "yyyy-MM-dd HH:mm");
                        date = formatter.parse(dateString);}
                        catch (Exception e){e.printStackTrace();}
                        Toast.makeText(CreateMoodActivity.this, ""+date, Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DateDialog.dismiss();
                    }
                })
                .create();
        TimeDialog = new AlertDialog.Builder(CreateMoodActivity.this)
                .setView(timeView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DateDialog.show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TimeDialog.dismiss();
                    }
                })
                .create();

    }

    /**
     * Compression of image. From: http://blog.csdn.net/harryweasley/article/details/51955467 <br>
     * author: HarryWeasley 2016-07-20 15:26 <br>
     * taken by Xin Huang 2017-03-04 18:45 <br>
     * for compressing the image to meet the project storage requirements <br>
     * @param image <br>
     * @return Bitmap image <br>
     */
    public Bitmap compress(Bitmap image) {
        try {
            while (((image.getRowBytes() * image.getHeight()) / 8) > 65536) {
                BitmapFactory.Options options2 = new BitmapFactory.Options();
                options2.inPreferredConfig = Bitmap.Config.RGB_565;

                Matrix matrix = new Matrix();
                matrix.setScale(0.5f, 0.5f);
                image = Bitmap.createBitmap(image, 0, 0, image.getWidth(),
                        image.getHeight(), matrix, true);
            }
        } catch (Exception E) {

        }
        return image;
    }


    /**
     * displayAttributes method used when the user gets back from the "Map" display
     * and displays the changed spinner attributes.
     * <br>
     * Spinner dropdown logic taken from <br>
     * link: http://stackoverflow.com/questions/13377361/how-to-create-a-drop-down-listm <br>
     * Author: Nicolas Tyler, 2013/07/15 8:47 <br>
     * taken by Xin Huang 2017-03-04 15:30 (used and switch function written by Nick 2017/03/12 14:30) <br>
     */
    private void displayAttributes() {
        Spinner dropdown = (Spinner) findViewById(R.id.Emotion);
        String[] items = new String[]{"anger", "confusion", "disgust", "fear", "happiness", "sadness", "shame", "surprise"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

        switch (tempMood.getFeeling()) {
            case "anger":
                dropdown.setSelection(0);
                break;
            case "confusion":
                dropdown.setSelection(1);
                break;
            case "disgust":
                dropdown.setSelection(2);
                break;
            case "fear":
                dropdown.setSelection(3);
                break;
            case "happiness":
                dropdown.setSelection(4);
                break;
            case "sadness":
                dropdown.setSelection(5);
                break;
            case "shame":
                dropdown.setSelection(6);
                break;
            case "surprise":
                dropdown.setSelection(7);
                break;

        }
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                EmotionText = parent.getItemAtPosition(position).toString();
                tempMood.setFeeling(EmotionText);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(CreateMoodActivity.this, "Please pick a feeling!", Toast.LENGTH_SHORT).show();
            }
        });

        Spinner dropdown_SocialSituation = (Spinner) findViewById(R.id.SocialSituation);
        String[] item_SocialSituation = new String[]{"", "alone", "with one other person",
                "with two people", "with several people", "with a crowd"};
        ArrayAdapter<String> adapter_SocialSituation = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_dropdown_item, item_SocialSituation);
        dropdown_SocialSituation.setAdapter(adapter_SocialSituation);
        switch (tempMood.getSocialSituation()) {
            case "alone":
                dropdown_SocialSituation.setSelection(1);
                break;
            case "with one other person":
                dropdown_SocialSituation.setSelection(2);
                break;
            case "with two people":
                dropdown_SocialSituation.setSelection(3);
                break;
            case "with several people":
                dropdown_SocialSituation.setSelection(4);
                break;
            case "with a crowd":
                dropdown_SocialSituation.setSelection(5);
                break;

        }

        dropdown_SocialSituation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                SocialSituation = parent.getItemAtPosition(position).toString();
                tempMood.setSocialSituation(SocialSituation);
                TextView sizeView = (TextView) findViewById(R.id.SocialText);
                sizeView.setText("  " + SocialSituation);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(CreateMoodActivity.this, "Please pick a feeling!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
