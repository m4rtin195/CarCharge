package com.martin.carcharge;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;

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
import com.martin.carcharge.databinding.ActivityLoginBinding;
import com.martin.carcharge.models.User;
import com.martin.carcharge.storage.FileStorage;
import com.martin.carcharge.storage.FirestoreDb;

public class LoginActivity extends BaseActivity
{
    private SharedPreferences pref;
    private FirebaseAuth auth;
    private FirestoreDb fdb;
    
    ActivityLoginBinding binding;
    View root;
    ProgressBar progressbar;
    EditText edit_username, edit_password;
    Button button_signin_mail;
    SignInButton button_signin_google;
    
    ActivityResultLauncher<Intent> googleLoginLauncher;
    
    
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        pref = App.getPreferences();
        auth = FirebaseAuth.getInstance();
        fdb = App.getFirestoreDb();
        
        googleLoginLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), googleLoginCallback);
        
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if(firebaseUser != null) //already logged in
        {
            Log.i(G.tag, "Already logged-in: " + firebaseUser.getEmail() + " [" + firebaseUser.getUid() + "]");
            App.getViewModel().init();
            
            User user = prepareUser(firebaseUser, false);
            Intent intent = new Intent(this, MainActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable(G.EXTRA_USER, user);
            intent.putExtras(bundle);
            
            fdb.fetchFirestoreProfile((success) ->  //this also fetches CloudStorage images
            {
                Log.i(G.tag, "Starting MainActivity");
                this.startActivity(intent);
                this.finish();
            });
        }
        
        else //not logged in
        {
            this.setupUI();
            binding = ActivityLoginBinding.inflate(getLayoutInflater());
            root = binding.getRoot();
            setContentView(root);
            
            progressbar = binding.progressbarLogin;
            edit_username = binding.editUsername;
            edit_password = binding.editPassword;
            button_signin_mail = binding.buttonSigninMail;
                button_signin_mail.setOnClickListener(this::onLoginClick);
            button_signin_google = binding.buttonSigninGoogle;
                button_signin_google.setOnClickListener(this::onGoogleLoginClick);
        }
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
                .requestIdToken(getString(R.string.default_web_client_id)) //false warning
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    
        Intent intent = mGoogleSignInClient.getSignInIntent();
        //startActivityForResult(intent, G.RC_SIGN_IN);
        googleLoginLauncher.launch(intent);
    }
    
    //public void onActivityResult(int requestCode, int resultCode, Intent data)
    ActivityResultCallback<ActivityResult> googleLoginCallback = new ActivityResultCallback<ActivityResult>()
    {
        @Override
        public void onActivityResult(ActivityResult result)
        {
            if(result.getResultCode() == RESULT_OK)
            {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try
                {   // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    assert account != null;
                    Log.d(G.tag, "Google sign-in success: " + account.getId());
                    firebaseAuthWithGoogle(account.getIdToken());
                }
                catch(ApiException e)
                {
                    Log.w(G.tag, "Google sign-in failed", e);
                }
            } else
                Log.w(G.tag, "Google sign-in intent not-ok, resultcode: " + result.getResultCode());
        }
    };
    
    private void firebaseAuthWithGoogle(String idToken)
    {
        progressbar.setVisibility(View.VISIBLE);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential).addOnCompleteListener(this, onSignInCompleteListener);
    }
    
    private void firebaseAuthWithEmailAndPassword(String email, String password)
    {
        progressbar.setVisibility(View.VISIBLE);
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, onSignInCompleteListener);
    }
    
    
    OnCompleteListener<AuthResult> onSignInCompleteListener = task ->
    {
        if(task.isSuccessful())
        {
            Log.i(G.tag, "signIn: success");
            App.getViewModel().init();
            App.getFirestoreDb().resetAuthUid();
            App.getCloudStorage().resetAuthUid();
            App.getApiClient().resetAuthUid();
            
            FirebaseUser firebaseUser = auth.getCurrentUser();
            assert firebaseUser != null;
            User user = prepareUser(firebaseUser, true);
            
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable(G.EXTRA_USER, user);
            bundle.putBoolean(G.EXTRA_USER_JUST_LOGGEDIN, true);
            intent.putExtras(bundle);
            //intent.putExtra(G.EXTRA_USER, user); //if just one extra
            
            fdb.fetchFirestoreProfile((success) ->
            {
                progressbar.setVisibility(View.INVISIBLE);
                Log.i(G.tag, "Starting MainActivity");
                this.startActivity(intent);
                this.finish();
            });
        }
        else //Authentication sign-in task unsuccessful
        {
            progressbar.setVisibility(View.INVISIBLE);
            Log.w(G.tag, "signIn: failure", task.getException());
            Snackbar.make(root, "Authentication failed.", Snackbar.LENGTH_LONG).show();
        }
    };
    
    
     // Prepare icon and convert FirebaseUser to local User
    private User prepareUser(FirebaseUser u, boolean isNew)
    {
        if(isNew)
        {
            String filename = FileStorage.downloadUserImage(this, u);
            pref.edit().putString(G.PREF_USER_ICON, filename).apply();
        }
        
        User user = new User();
        user.setUid(u.getUid());
        user.setNickname(u.getDisplayName());
        user.setEmail(u.getEmail());
        user.setIcon(FileStorage.loadCachedUserImage(this, pref.getString(G.PREF_USER_ICON, "")));
        
        return user;
    }
}