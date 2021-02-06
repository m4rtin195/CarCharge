package com.martin.carcharge;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.preference.PreferenceManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.martin.carcharge.databinding.ActivityLoginBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

public class LoginActivity extends BaseActivity
{
    private FirebaseAuth auth;
    private FirebaseUser user;
    
    ActivityLoginBinding binding;
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
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        root = binding.getRoot();
        setContentView(root);
        
        progressbar = binding.progressLogin;
        edit_username = binding.editUsername;
        edit_password = binding.editPassword;
        button_signin_mail = binding.buttonSigninMail;
            button_signin_mail.setOnClickListener(this::onLoginClick);
        button_signin_google = binding.buttonSigninGoogle;
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
    
    String downloadUserIcon() //and make it round
    {
        if(user.getPhotoUrl() != null)
        {
            AtomicReference<String> filename = new AtomicReference<>();
            
            Thread thr = new Thread(() ->
            {
                InputStream inStream = null; //data from internet
                OutputStream outStream = null; //file
                try
                {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inDensity = 24; options.inTargetDensity = 24; //todo preco nescaluje?
    
                    inStream = new URL(user.getPhotoUrl().toString()).openStream();
                    Bitmap raw = BitmapFactory.decodeStream(inStream, null, options);
                    assert raw != null; //todo over
                    
                    Bitmap out = Bitmap.createBitmap(raw.getWidth(), raw.getHeight(), Bitmap.Config.ARGB_8888);
    
                    Canvas canvas = new Canvas(out);
                    final Paint paint = new Paint();
                    final Rect rect = new Rect(0, 0, raw.getWidth(), raw.getHeight());
    
                    paint.setAntiAlias(true);
                    canvas.drawCircle(raw.getWidth()/2.0f, raw.getHeight()/2.0f, raw.getWidth()/2.0f, paint);
                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN)); //clip
                    canvas.drawBitmap(raw, rect, rect, paint);
    
                    File path = new File(getFilesDir().toString() + "/media");
                    if(!path.exists())
                        if(!path.mkdir())
                            throw new IOException();
                    
                    File file = File.createTempFile("usericon_", ".png", path);
                    outStream = new FileOutputStream(file);
                    out.compress(Bitmap.CompressFormat.PNG, 0, outStream);
                    
                    Log.i(G.tag, "filename: " + file.getName());
                    filename.set(file.getName());
                }
                catch(IOException e)
                {
                    Log.i(G.tag, "Streams fail");
                    e.printStackTrace();
                }
                finally
                {
                    try {inStream.close(); outStream.close();} //todo treba??
                    catch(NullPointerException | IOException e) {e.printStackTrace();}
                }
            });
            thr.start();
            try {thr.join();} catch(InterruptedException e) {e.printStackTrace();}
            
            return filename.get();
        }
        
        else return null;
    }
}