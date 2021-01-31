package com.martin.carcharge;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.martin.carcharge.MainActivity;
import com.martin.carcharge.R;

public class LoginActivity extends BaseActivity
{
    private FirebaseAuth auth;
    
    ProgressBar progress_login;
    EditText edit_username, edit_password;
    Button button_signin_mail;
    
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser != null)
        {
            Log.i(G.tag, "already logged: " + currentUser.getEmail());
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        
        super.onCreate(savedInstanceState);
        this.setupUI();
        setContentView(R.layout.activity_login);
    
        progress_login = findViewById(R.id.progress_login);
        edit_username = findViewById(R.id.edit_username);
        edit_password = findViewById(R.id.edit_password);
        button_signin_mail= findViewById(R.id.button_signin_mail);
    
        button_signin_mail.setOnClickListener(this::onLoginClick);
    }
    
    public void onLoginClick(View view)
    {
        String email = edit_username.getText().toString();
        String password = edit_password.getText().toString();
        
        if(email.isEmpty())
            edit_username.setError(getString(R.string.login_error_hint));
        if(password.isEmpty())
            edit_password.setError(getString(R.string.login_error_hint));
        
        if(email.isEmpty() || password.isEmpty()) return;
    
        progress_login.setVisibility(View.VISIBLE);
        
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task ->
        {
            if(task.isSuccessful())
            {
                Log.i(G.tag, "signInWithEmail: success");
                //FirebaseUser user = auth.getCurrentUser();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
            else
            {
                Log.w(G.tag, "signInWithEmail: failure", task.getException());
                //Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                Snackbar.make(findViewById(R.id.layout_root_login), "Authentication failed.", Snackbar.LENGTH_LONG).show();
            }
            progress_login.setVisibility(View.INVISIBLE);
        });
    }
}