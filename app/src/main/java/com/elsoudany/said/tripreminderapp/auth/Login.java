package com.elsoudany.said.tripreminderapp.auth;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.elsoudany.said.tripreminderapp.mainscreen.Drawer;
import com.elsoudany.said.tripreminderapp.R;
import com.elsoudany.said.tripreminderapp.reminderwork.ReminderWorker;
import com.elsoudany.said.tripreminderapp.room.AppDatabase;
import com.elsoudany.said.tripreminderapp.room.Note;
import com.elsoudany.said.tripreminderapp.room.NoteDao;
import com.elsoudany.said.tripreminderapp.room.Trip;
import com.elsoudany.said.tripreminderapp.room.TripDAO;
import com.elsoudany.said.tripreminderapp.room.User;
import com.elsoudany.said.tripreminderapp.room.UserDAO;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;


public class Login extends AppCompatActivity {

    EditText editTextEmail,editTextPassword;
    Button buttonLogin,buttonSignInWithGoogle;
    TextView textViewSignUp,textViewForgetPassword;;
    FirebaseAuth fAuth;
    GoogleSignInClient mGoogleSignInClient;
    static final int GOOGLE_SIGN_IN = 123;
    private static final String TAG = "GoogleActivity";
    SharedPreferencesConfig preferencesConfig;
    Snackbar bar;
    SyncHandler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        editTextEmail=findViewById(R.id.inputEmail);
        editTextPassword=findViewById(R.id.inputPassword);
        buttonLogin=findViewById(R.id.btnlogin);
        buttonSignInWithGoogle=findViewById(R.id.btnGoogle);
        textViewSignUp=findViewById(R.id.textViewSignUp);
        textViewForgetPassword=findViewById(R.id.forgotPassword);
        handler = new SyncHandler();
        getResources().getConfiguration().setLocale(Locale.US);
        // if forget password...
        textViewForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, ForgetPassword.class));
            }
        });

        preferencesConfig = new SharedPreferencesConfig(getApplicationContext());
        if (preferencesConfig.readUserLoginStatus()) {
            Intent intent = new Intent(Login.this, Drawer.class);
            startActivity(intent);
            finish();
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut();



        fAuth = FirebaseAuth.getInstance();


        textViewSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this,SignUp.class));
            }
        });



        //login......
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonLogin.setClickable(false);
                buttonSignInWithGoogle.setClickable(false);
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if(getCurrentFocus() != null)
                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                if (email.isEmpty()) {
                    editTextEmail.setError("Enter your email");
                    editTextEmail.requestFocus();
                    return;
                }
                if (password.isEmpty()) {
                    editTextPassword.setError("Enter your password");
                    editTextPassword.requestFocus();
                    return;
                }
                fAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            if(fAuth.getCurrentUser().isEmailVerified()){
                                getDataFromFireBase();
                            }
                            else {
                                fAuth.getCurrentUser().sendEmailVerification();
                                Toast.makeText(Login.this, "Email Verification is sent to your Email", Toast.LENGTH_SHORT).show();
                                buttonLogin.setClickable(true);
                                buttonSignInWithGoogle.setClickable(true);
                            }
                        }
                        else {
                            Toast.makeText(Login.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            editTextEmail.setText("");
                            editTextPassword.setText("");
                            buttonLogin.setClickable(true);
                            buttonSignInWithGoogle.setClickable(true);

                        }
                    }
                });

            }


        });



        buttonSignInWithGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonLogin.setClickable(false);
                buttonSignInWithGoogle.setClickable(false);
                SignInGoogle();
            }
        });

    }

    void SignInGoogle(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {

                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                buttonLogin.setClickable(true);
                buttonSignInWithGoogle.setClickable(true);
                Log.w(TAG, "Google sign in failed", e);

            }
        }
    }
    //Google Authentication
    private void firebaseAuthWithGoogle(String idToken) {

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        fAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            getDataFromFireBase();



                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(Login.this, "Authentication failed." + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                            buttonLogin.setClickable(true);
                            buttonSignInWithGoogle.setClickable(true);

                        }

                    }

                });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getDataFromFireBase() {
        LinearLayout linearLayout = findViewById(R.id.snackbar_layout);
        bar = Snackbar.make(linearLayout,"Logging in...", Snackbar.LENGTH_INDEFINITE);
        ViewGroup contentLay = (ViewGroup) bar.getView();
        ProgressBar progressBar = new ProgressBar(getApplicationContext());
        progressBar.setPadding(800,0,0,0);
        contentLay.addView(progressBar);

        bar.show();
        // Sync to firebase
        new Thread ()
        {
            @Override
            synchronized public void  run() {
                super.run();
                String uid= FirebaseAuth.getInstance().getCurrentUser().getUid();
                AppDatabase db = Room.databaseBuilder(getApplicationContext(),AppDatabase.class,"DataBase-name").build();
                DatabaseReference mDatabase;
                mDatabase = FirebaseDatabase.getInstance().getReference();
                TripDAO tripDAO = db.tripDAO();
                UserDAO userDAO = db.userDAO();
                NoteDao noteDao = db.noteDao();
                User user1 = new User(uid);
                userDAO.insertAll(user1);

                mDatabase.child("users").child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        new Thread(){
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            synchronized public void run() {
                                Log.i(TAG, "run: "+ task.getResult().getValue());
                                ArrayList<Trip> trips = parseTripData(task.getResult().getValue());
                                for(Trip trip : trips)
                                {
                                    WorkManager mWorkManger = WorkManager.getInstance(getApplicationContext());
                                    DateTimeFormatter formatter = null;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                                        LocalDateTime dateTime = LocalDateTime.parse(trip.date + " " + trip.time, formatter);
                                        Duration duration = Duration.between(LocalDateTime.now(), dateTime);

                                        Log.i(TAG, "onCreate: " + duration);
                                        if(!duration.isNegative()) {
                                            OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                                                    .setInputData(new Data.Builder().putLong("tripUid", trip.uid).
                                                            putString("tripName", trip.tripName)
                                                            .build())
                                                    .setInitialDelay(duration)
                                                    .build();

                                            mWorkManger.enqueueUniqueWork("" + trip.uid, ExistingWorkPolicy.REPLACE, oneTimeWorkRequest);
                                        }
                                        else {
                                            trip.status = "cancelled";
                                        }
                                        tripDAO.insert(trip);
                                    }
                                }
                                ArrayList<Note> notes = parseNoteData(task.getResult().getValue());
                                for(Note note : notes) {
                                    noteDao.insert(note);
                                }
                                handler.sendEmptyMessage(1);
                            }

                        }.start();
                    }
                });
            }
        }.start();

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private ArrayList<Trip> parseTripData(Object value) {
        if(value != null) {
            HashMap<String, Object> data = (HashMap<String, Object>) value;
            if(data.get("trips") != null) {
                Log.i(TAG, "parseTripData: "+ data.get("trips").getClass());
                ArrayList<Trip> trips = new ArrayList<>();
                if (data.get("trips").getClass() == HashMap.class) {
                    HashMap<String,HashMap<String,Object>> dataHashMap = (HashMap<String, HashMap<String, Object>>) data.get("trips");
                    dataHashMap.forEach((s, item) -> {
                        if (item != null) {
                            Trip trip = new Trip((String) item.get("tripName"), (String) item.get("startPoint"), (String) item.get("endPoint"), (String) item.get("date"), (String) item.get("time"), (String) item.get("userId"), (String) item.get("status"), (String) item.get("tripType"));
                            trip.uid = (Long) item.get("uid");
                            trips.add(trip);
                        }
                    });
                }
                else if(data.get("trips").getClass() == ArrayList.class){

                    ArrayList<HashMap<String, Object>> tripsData = (ArrayList<HashMap<String, Object>>) data.get("trips");
                    if (tripsData != null) {
                        for (HashMap<String, Object> item : tripsData) {
                            if (item != null) {
                                Trip trip = new Trip((String) item.get("tripName"), (String) item.get("startPoint"), (String) item.get("endPoint"), (String) item.get("date"), (String) item.get("time"), (String) item.get("userId"), (String) item.get("status"), (String) item.get("tripType"));
                                trip.uid = (Long) item.get("uid");
                                trips.add(trip);
                            }

                        }
                    }
                }
                return trips;
            }


        }
        return new ArrayList<>();
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    private ArrayList<Note> parseNoteData(Object value) {
        if(value != null) {
            HashMap<String, Object> data = (HashMap<String, Object>) value;
            ArrayList<Note> notes = new ArrayList<>();
            if(data.get("notes") != null) {
//                Log.i(TAG, "parseTripData: "+ data.get("trips").getClass());
                if (data.get("notes").getClass() == HashMap.class) {
                    HashMap<String, HashMap<String, Object>> dataHashMap = (HashMap<String, HashMap<String, Object>>) data.get("notes");
                    dataHashMap.forEach((s, item) -> {
                        if (item != null) {
                            Note note = new Note((String) item.get("noteBody"), (Long) item.get("tripUid"));
                            note.uid = (Long) item.get("uid");
                            notes.add(note);
                        }
                    });
                }

                else if(data.get("notes").getClass() == ArrayList.class){
                    ArrayList<HashMap<String, Object>> notesData = (ArrayList<HashMap<String, Object>>) data.get("notes");
                    for (HashMap<String, Object> item : notesData) {
                        if (item != null) {
                            Note note = new Note((String) item.get("noteBody"), (Long) item.get("tripUid"));
                            note.uid = (Long) item.get("uid");
                            notes.add(note);
                        }

                    }

                    return notes;
                }
            }
        }
        return new ArrayList<>();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(fAuth.getCurrentUser() != null)
        {
            finish();
        }
    }
    class SyncHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            bar.dismiss();
            Intent intent=new Intent(Login.this, Drawer.class);
            preferencesConfig.writeUserLoginStatus(true);
            startActivity(intent);
            finish();

        }
    }
}