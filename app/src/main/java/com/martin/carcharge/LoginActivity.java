package com.martin.carcharge;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

public class LoginActivity extends BaseActivity
{
    private FirebaseAuth auth;
    private FirebaseUser user;
    
    View root;
    ProgressBar progressbar;
    EditText edit_username, edit_password;
    Button button_signin_mail;
    SignInButton button_signin_google;
    
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if(user != null)
        {
            Log.i(G.tag, "already logged: " + user.getEmail());
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        
        super.onCreate(savedInstanceState);
        this.setupUI();
        setContentView(R.layout.activity_login);
    
        root = findViewById(R.id.layout_root_login);
        progressbar = findViewById(R.id.progress_login);
        edit_username = findViewById(R.id.edit_username);
        edit_password = findViewById(R.id.edit_password);
        button_signin_mail = findViewById(R.id.button_signin_mail);
            button_signin_mail.setOnClickListener(this::onLoginClick);
        button_signin_google = findViewById(R.id.button_signin_google);
            button_signin_google.setOnClickListener(this::onGoogleLoginClick);
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
    
        firebaseAuthWithEmailAndPassword(email, password);
    }
    
    public void onGoogleLoginClick(View view)
    {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
    
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    
        Intent intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent, G.RC_SIGN_IN);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        
        if(requestCode == G.RC_SIGN_IN && resultCode == RESULT_OK)
        {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try
            {   // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(G.tag, "Google sign in success: " + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            }
            catch(ApiException e)
            {
                Log.w(G.tag, "Google sign in failed", e);
            }
        }
        else
            Log.w(G.tag, "nonok, resultcode: " + resultCode);
    }
    
    private void firebaseAuthWithGoogle(String idToken)
    {
        progressbar.setVisibility(View.VISIBLE);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        
        auth.signInWithCredential(credential).addOnCompleteListener(this, task ->
        {
            if (task.isSuccessful())
            {
                Log.d(G.tag, "signInWithCredential: success");
                user = auth.getCurrentUser();
                //assert user != null;
    
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putString("user_nickname", user.getDisplayName())
                        .putString("user_email", user.getEmail())
                        .putString("user_icon", downloadUserIcon())
                        .apply();
                
                Toast.makeText(this, "Welcome back, " + user.getDisplayName() + "!", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
            else
            {
                Log.w(G.tag, "signInWithCredential: failure", task.getException());
                Snackbar.make(root, "Authentication failed.", Snackbar.LENGTH_LONG).show();
            }
            progressbar.setVisibility(View.INVISIBLE);
        });
    }
    
    private void firebaseAuthWithEmailAndPassword(String email, String password)
    {
        progressbar.setVisibility(View.VISIBLE);
    
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task ->
        {
            if(task.isSuccessful())
            {
                Log.i(G.tag, "signInWithEmail: success");
                user = auth.getCurrentUser();
                assert user != null;
            
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putString("user_nickname", user.getDisplayName())
                        .putString("user_email", user.getEmail())
                        .apply();
            
                Toast.makeText(this, "Welcome back, " + user.getDisplayName() + "!", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
            else
            {
                Log.w(G.tag, "signInWithEmail: failure", task.getException());
                Snackbar.make(root, "Authentication failed.", Snackbar.LENGTH_LONG).show();
            }
            progressbar.setVisibility(View.INVISIBLE);
        });
    }
    
    String downloadUserIcon()
    {
        if(user.getPhotoUrl() != null)
        {
            AtomicReference<String> localUri = new AtomicReference<>();
            Thread thr = new Thread(() ->
            {
                InputStream inStream = null; //data from internet
                OutputStream outStream = null; //file
                try
                {
                    inStream = new URL(user.getPhotoUrl().toString()).openStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inStream);
                    File storageDir = getExternalFilesDir("media");
                    Uri fileUri = null;
                    try
                    {
                        File file = File.createTempFile("icon_", ".jpg", storageDir);
                        fileUri = FileProvider.getUriForFile(this, "com.martin.carcharge.fileprovider", file); //fileProvider uri, Uri.fromFile dava file:///
                    }
                    catch(IOException e)
                    {
                        e.printStackTrace();
                    }
                    outStream = getContentResolver().openOutputStream(fileUri); //open stream from file uri
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outStream);
                    localUri.set(fileUri.toString());
                }
                catch(IOException e)
                {
                    Log.i("daco", "Streams fail");
                    e.printStackTrace();
                }
                finally
                {
                    try
                    {
                        inStream.close();
                        outStream.close();
                    }
                    catch(IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            });
            thr.start();
            try {thr.join();} catch(InterruptedException e) {e.printStackTrace();}
            
            return localUri.get();
        }
        
        else return null;
    }
}