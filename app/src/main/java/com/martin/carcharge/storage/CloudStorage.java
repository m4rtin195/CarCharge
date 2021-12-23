package com.martin.carcharge.storage;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.martin.carcharge.G;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CloudStorage
{
    private final Context context;
    private final FirebaseStorage storage;
    
    private final ExecutorService executor;
    private StorageReference ref;
    private String uid;
    
    
    public CloudStorage(Context context)
    {
        this.context = context.getApplicationContext();
        storage = FirebaseStorage.getInstance();
        uid = FirebaseAuth.getInstance().getUid();
        ref = storage.getReference("/user_files/" + uid);
        executor = Executors.newCachedThreadPool();
    }
    
    public void resetAuthUid()
    {
        uid = FirebaseAuth.getInstance().getUid();
        ref = storage.getReference("/user_files/" + uid);
    }
    
    public CompletableFuture<Boolean> downloadVehicleImage(String filename)
    {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        File localFile = new File(context.getFilesDir().toString() + "/media/" + filename);
        try
        {
            localFile.createNewFile();
        }
        catch(IOException e)
        {
            Log.e(G.tag, "Streams fail");
            e.printStackTrace();
            future.complete(false);
        }
        assert localFile.exists();
        //run in different thread, so then it will not lock out with .join of downloads in FirestoreDb
        executor.submit(() ->
        {
            ref.child("/media/").child(filename).getFile(localFile)
                .addOnCompleteListener(executor, task ->
                {
                    if(task.isSuccessful())
                        future.complete(true);
                    else
                    {
                        Log.w(G.tag, "Cannot download vehicle image: " + filename, task.getException());
                        future.complete(false);
                    }
                });
        });
        
        return future;
    }
    
    public CompletableFuture<Boolean> uploadVehicleImage(File localFile)
    {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        assert localFile != null;
        Uri uri = Uri.fromFile(localFile);
        
        executor.submit(() ->
        {
            ref.child("/media/").child(localFile.getName()).putFile(uri)
                .addOnCompleteListener(executor, task ->
                {
                    if(task.isSuccessful())
                        future.complete(true);
                    else
                    {
                        Log.w(G.tag, "Cannot upload vehicle image: " + localFile.getName(), task.getException());
                        future.complete(false);
                    }
                });
        });
        
        return future;
    }
    
    public CompletableFuture<Boolean> deleteVehicleImage(String filename)
    {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        if(filename.isEmpty()) return null;
        executor.submit(() ->
        {
            ref.child(filename).delete()
                .addOnCompleteListener(executor, task ->
                {
                    if(task.isSuccessful())
                        future.complete(true);
                    else
                    {
                        Log.w(G.tag, "Cannot delete vehicle image in cloudstorage: " + filename, task.getException());
                        future.complete(false);
                    }
                });
        });
        
        return future;
    }
}
