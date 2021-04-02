package com.elsoudany.said.tripreminderapp.upcomingtrips;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.elsoudany.said.tripreminderapp.R;
import com.elsoudany.said.tripreminderapp.room.Trip;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class AddTripActivity extends AppCompatActivity
{
    private static final int BACK_PRESSED = 61;
    private static final int RESULT_RETRIP = 3698;

    private static final String TAG = "AddActivity";
    //radio buttons
    RadioButton oneDirectionRadio,roundedRadio;
    //date edit text
    EditText dateText;
    //time edit Text
    EditText timeText;
    //add trip button
    FloatingActionButton addTripButton;
    //trip name
    EditText tripName;
    //calendar instance
    Calendar calendar;
    //current date variables
    int currentYear,currentMonth,currentDay;
    //current time
    int currentHour,currentMinute;
    long tripUid = 0;

    //points
    EditText startPoint,endPoint;
    //api key
    final int AUTOCOMPLETE_REQUEST_CODE=100;
    //flag to know start or end point
    String point;
    //firebase reference to get user id
    FirebaseAuth firebaseAuth;
    String userId ;

    //position for editing trip
    int position;
    Boolean comingToEdit;
    Boolean comingToRetrip;
    //backButton
    ImageView backToTrips;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trip);

        //radio buttons
        oneDirectionRadio =findViewById(R.id.radio_oneDirection);
        roundedRadio=findViewById(R.id.radio_roundTrip);

        //set one direction as default
        oneDirectionRadio.setChecked(true);
        addTripButton =findViewById(R.id.btn_addTrip);
        dateText=findViewById(R.id.txt_date);
        timeText=findViewById(R.id.txt_time);
        tripName=findViewById(R.id.txt_tripName);
        startPoint=findViewById(R.id.txt_startPoint);
        endPoint=findViewById(R.id.txt_endPoint);
        backToTrips=findViewById(R.id.back_trips);
        //get userid from firebase
        firebaseAuth = FirebaseAuth.getInstance();
        userId = firebaseAuth.getCurrentUser().getUid();
        //instance from calendar to get current date and time
        calendar=Calendar.getInstance();
        //check if coming from edit Trip
        Intent intent = getIntent();
        comingToEdit = intent.getBooleanExtra("editTrip",false);
        comingToRetrip = intent.getBooleanExtra("retrip",false);
        if(comingToEdit)
        {
            Log.i(TAG, "onCreate: ");
            Trip editingTrip = (Trip) intent.getSerializableExtra("tripData");
            tripUid = editingTrip.uid;
            tripName.setText(editingTrip.tripName);
            startPoint.setText(editingTrip.startPoint);
            endPoint.setText(editingTrip.endPoint);
            dateText.setText(editingTrip.date);
            timeText.setText(editingTrip.time);
            if(editingTrip.tripType.equals("one"))
                oneDirectionRadio.setChecked(true);
            else
                roundedRadio.setChecked(true);
            position = intent.getIntExtra("position",0);

        }
        else if(comingToRetrip) {
            Trip reTrip = (Trip) intent.getSerializableExtra("tripData");
            tripUid = reTrip.uid;
            tripName.setText(reTrip.tripName);
            startPoint.setText(reTrip.startPoint);
            endPoint.setText(reTrip.endPoint);
        }
        /*-----------------------------------------start point --------------------------*/
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyCdXqSieoMfWeS3GunOh0FKQzKJnsCWIGM");
        }
        PlacesClient placesClient = Places.createClient(this);


        startPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                point="start";
                startGoogleAutoComplete();
            }
        });
        endPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                point="end";
                startGoogleAutoComplete();
            }
        });

        addTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                //check if user not fill all the data
                if(TextUtils.isEmpty(tripName.getText())||TextUtils.isEmpty(startPoint.getText())
                 ||TextUtils.isEmpty(endPoint.getText())||TextUtils.isEmpty(dateText.getText())
               || TextUtils.isEmpty(timeText.getText())){

                 Toast toast=   Toast.makeText(AddTripActivity.this, "PLEASE FILL ALL TRIP INFORMATION", Toast.LENGTH_SHORT);
                    // change the Background of Toast
//                    View viewToast = toast.getView();
//                    viewToast.setBackgroundColor(Color.BLACK);
//                    viewToast.setBackground(getResources().getDrawable(R.drawable.btn_bg));
//                    //Change toast text color
//                    TextView toastText = viewToast.findViewById(android.R.id.message);
//                    toastText.setTextColor(Color.WHITE);
                           toast.show();
                }
                else {
                    String radio = "";
                    if (oneDirectionRadio.isChecked()) {
                        radio = "one";
                    }
                    if (roundedRadio.isChecked()) {
                        radio = "round";

                    }
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("tripUid", tripUid);
                    returnIntent.putExtra("radio", radio);
                    returnIntent.putExtra("tripName", tripName.getText().toString());
                    returnIntent.putExtra("startPoint", startPoint.getText().toString());
                    returnIntent.putExtra("endPoint", endPoint.getText().toString());
                    returnIntent.putExtra("date", dateText.getText().toString());
                    returnIntent.putExtra("time", timeText.getText().toString());
                    returnIntent.putExtra("userId", userId);
                    returnIntent.putExtra("status", "processing");
                    returnIntent.putExtra("position", position);
                    if(comingToRetrip){
                        setResult(RESULT_RETRIP, returnIntent);

                    }else {
                        setResult(Activity.RESULT_OK, returnIntent);
                    }
                    finish();
                }
            }
        });

        /*-----------------------------------------date text --------------------------*/
        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                //get current date from calendar
                currentYear=calendar.get(Calendar.YEAR);
                currentMonth=calendar.get(Calendar.MONTH);
                currentDay=calendar.get(Calendar.DAY_OF_MONTH);

                //lunch datapicker
                DatePickerDialog datePickerDialog=new DatePickerDialog(AddTripActivity.this,
                        new DatePickerDialog.OnDateSetListener()
                        {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth)
                            {
                                //set choosen date to datetext
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
                                LocalDate date = LocalDate.parse(dayOfMonth+"/"+(month + 1)+"/"+year,formatter);
                                dateText.setText(date.toString());
                            }
                        },currentYear,currentMonth,currentDay);

                datePickerDialog.show();


            }
        });
        /*-----------------------------------------time text --------------------------*/
        timeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                //get current time from calendar
                currentHour=calendar.get(Calendar.HOUR_OF_DAY);
                currentMinute=calendar.get(Calendar.MINUTE);
                //lunch timepicker
                TimePickerDialog timePickerDialog=new TimePickerDialog(AddTripActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute)
                    {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:m");
                        LocalTime time = LocalTime.parse(hourOfDay+":"+minute,formatter);
                        timeText.setText(time.toString());

                    }
                },currentHour,currentMinute,false);

                timePickerDialog.show();
            }
        });

        // backToTrips........
        backToTrips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
//                        new UpcomingTripsFragment()).commit();
                if(comingToEdit) {
                    Intent returnIntent = getIntent();
                    setResult(BACK_PRESSED,returnIntent);
                    finish();
                }
                else{
                    finish();

                }
            }
        });
    }
    //start google autocomplete api
    public void startGoogleAutoComplete()
    {
        //initialize place field list
        List<Place.Field> fields = Arrays.asList(Place.Field.ADDRESS,Place.Field.LAT_LNG);
        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(AddTripActivity.this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }
    //result from autocomplete google api
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK)
            {
                Place place = Autocomplete.getPlaceFromIntent(data);
                //check points request
                switch(point){
                    case "start":startPoint.setText(place.getAddress());
                        break;
                    case "end":endPoint.setText(place.getAddress());
                        break;
                }

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if(comingToEdit) {
            Intent returnIntent = getIntent();
            setResult(BACK_PRESSED,returnIntent);
            finish();
        }
        else
            super.onBackPressed();
    }
}