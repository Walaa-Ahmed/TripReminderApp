package com.elsoudany.said.tripreminderapp.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.elsoudany.said.tripreminderapp.mainscreen.Drawer;
import com.elsoudany.said.tripreminderapp.R;
import com.elsoudany.said.tripreminderapp.room.AppDatabase;
import com.elsoudany.said.tripreminderapp.room.NoteDao;
import com.elsoudany.said.tripreminderapp.room.User;
import com.elsoudany.said.tripreminderapp.room.UserDAO;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUp extends AppCompatActivity {

    EditText editTextUserName,editTextEmail,editTextPassword,editTextConfirmPassword;
    Button buttonSignUp;
    TextView textViewSignIn;
    FirebaseAuth fAuth;
    SharedPreferencesConfig preferencesConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        editTextUserName=findViewById(R.id.inputUsername);
        editTextEmail=findViewById(R.id.inputEmail);
        editTextPassword=findViewById(R.id.inputPassword);
        editTextConfirmPassword=findViewById(R.id.inputConformPassword);
        buttonSignUp=findViewById(R.id.btnRegister);
        textViewSignIn=findViewById(R.id.alreadyHaveAccount);

        fAuth = FirebaseAuth.getInstance();

        preferencesConfig = new SharedPreferencesConfig(getApplicationContext());
        if (preferencesConfig.readUserLoginStatus()) {
            Intent intent = new Intent(SignUp.this, Drawer.class);
            startActivity(intent);
            finish();
        }

        textViewSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //signUp........
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if(getCurrentFocus() != null)
                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                String userName=editTextUserName.getText().toString().trim();
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                String confirmPassword=editTextConfirmPassword.getText().toString().trim();


                if (userName.isEmpty()) {
                    editTextUserName.setError("Please enter UserName");
                    editTextUserName.requestFocus();
                    return;
                }
                if (email.isEmpty()) {
                    editTextEmail.setError("Please enter email");
                    editTextEmail.requestFocus();
                    return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    editTextEmail.setError("enter correct email");
                    editTextEmail.requestFocus();
                    return;
                }
                if (password.isEmpty()) {
                    editTextPassword.setError("Please enter password");
                    editTextPassword.requestFocus();
                    return;
                }
                if (!(confirmPassword.equals(password))) {
                    editTextConfirmPassword.setError("Password is not matched");
                    editTextConfirmPassword.requestFocus();
                    return;
                }

                //create user
                fAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    editTextEmail.setText("");
                                    editTextUserName.setText("");
                                    editTextPassword.setText("");
                                    editTextConfirmPassword.setText("");

                                    //     Toast.makeText(SignUp.this, "Authentication failed." + task.getException(),Toast.LENGTH_SHORT).show();
                                    Toast.makeText(SignUp.this, "Authentication failed"+task.getException(),Toast.LENGTH_SHORT).show();

                                } else {

                                    Intent intent=new Intent(SignUp.this, Login.class);
//                                            preferencesConfig.writeUserLoginStatus(true);
                                    Toast.makeText(SignUp.this, "Email Verification is sent to your Email", Toast.LENGTH_SHORT).show();
                                    FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification();

                                    FirebaseAuth.getInstance().signOut();


//                                            AppDatabase db = Room.databaseBuilder(getApplicationContext(),AppDatabase.class,"DataBase-name").build();
//                                            UserDAO userDAO = db.userDAO();
//                                            User user1 = new User(uid);
//                                            userDAO.insertAll(user1);

                                    startActivity(intent);
                                    finish();
                                }


                            }

                        });

            }
        });
    }
}