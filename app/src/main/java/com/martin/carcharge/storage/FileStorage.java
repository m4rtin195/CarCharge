package com.martin.carcharge.storage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.Log;

import androidx.room.util.FileUtil;

import com.google.firebase.auth.FirebaseUser;
import com.martin.carcharge.G;
import com.martin.carcharge.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

public class FileStorage
{
    public static boolean checkMediaFolder(Context context)
    {
        File path = new File(context.getFilesDir().toString() + "/media");
        if(!path.exists())
            //noinspection RedundantIfStatement
            if(!path.mkdir())
                return false;
        
        return true;
    }
    
    public static boolean checkFileExists(Context context, String path)
    {
        File file = new File(context.getFilesDir().toString() + path);
        return file.exists();
    }
    
    public static boolean deleteFile(Context context, String path)
    {
        File file = new File(context.getFilesDir().toString() + path);
        return file.delete();
    }
    
    public static void deleteAllFiles(Context context)
    {
        Log.i(G.tag, "Deleting all files in /media folder");
        File dir = new File(context.getFilesDir().toString() + "/media/");
        File[] fa = dir.listFiles();
        if(fa != null && fa.length > 0)
            for(File f : fa)
                f.delete();
    }
    
    
    // User picture
    
    public static String downloadUserImage(Context context, FirebaseUser user) // returns filename
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
                    options.inDensity = 24; options.inTargetDensity = 24; //preco nescaluje?
                    
                    inStream = new URL(user.getPhotoUrl().toString()).openStream();
                    Bitmap raw = BitmapFactory.decodeStream(inStream, null, options);
                    assert raw != null;
                    
                    Bitmap image = roundProfilePicture(raw);
                    
                    File path = new File(context.getFilesDir().toString() + "/media");
                    File file = File.createTempFile("usericon_", ".png", path);
                    outStream = new FileOutputStream(file);
                    image.compress(Bitmap.CompressFormat.PNG, 0, outStream);
                    
                    Log.i(G.tag, "new user filename: " + file.getName());
                    filename.set(file.getName());
                }
                catch(IOException e)
                {
                    Log.e(G.tag, "Streams fail");
                    e.printStackTrace();
                }
                finally
                {
                    try
                    {
                        assert outStream != null;
                        assert inStream != null;
                        inStream.close();
                        outStream.close();
                    }
                    catch(NullPointerException | IOException e) {e.printStackTrace();}
                }
            });
            thr.start();
            try {thr.join();} catch(InterruptedException e) {e.printStackTrace();}
            
            return filename.get();
        } //user has photo
        
        else return null;
    }
    
    private static Bitmap roundProfilePicture(Bitmap raw)
    {
        Bitmap out = Bitmap.createBitmap(raw.getWidth(), raw.getHeight(), Bitmap.Config.ARGB_8888);
        
        Canvas canvas = new Canvas(out);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, raw.getWidth(), raw.getHeight());
        
        paint.setAntiAlias(true);
        canvas.drawCircle(raw.getWidth()/2.0f, raw.getHeight()/2.0f, raw.getWidth()/2.0f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN)); //clip
        canvas.drawBitmap(raw, rect, rect, paint);
        
        return out;
    }
    
    public static Bitmap loadCachedUserImage(Context context, String filename)
    {
        Bitmap icon = null;
        
        if(filename.isEmpty())
            icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_user);
        else
            icon = BitmapFactory.decodeFile(context.getFilesDir().toString() + "/media/" + filename);
        
        return icon;
    }
}
