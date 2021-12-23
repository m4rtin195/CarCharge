package com.martin.carcharge.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Source;
import com.martin.carcharge.App;
import com.martin.carcharge.G;
import com.martin.carcharge.models.MainViewModel.MainViewModel;
import com.martin.carcharge.models.Vehicle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class FirestoreDb
{
    private final Context context;
    private final FirebaseFirestore firestore;
    private final MainViewModel vm;
    private final SharedPreferences pref;
    private final CloudStorage cstrg;

    private String uid;
    
    public FirestoreDb(Context context)
    {
        this.context = context.getApplicationContext();
        
        firestore = FirebaseFirestore.getInstance();
        firestore.setFirestoreSettings(new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(false).build());
        //firestore.disableNetwork();
        uid = FirebaseAuth.getInstance().getUid();
        
        vm = App.getViewModel();
        pref = App.getPreferences();
        cstrg = App.getCloudStorage();
        
        updateLastLoggedIn();
    }
    
    public void resetAuthUid()
    {
        uid = FirebaseAuth.getInstance().getUid();
    }
    
    private void updateLastLoggedIn()
    {
        firestore.document("/users/" + uid).update(Collections.singletonMap(G.FIRESTORE_LAST_LOGGED_IN, FieldValue.serverTimestamp()));
    }
    
    public void updateUserData(String key, Object value)
    {
        Map<String,Object> updates = Collections.singletonMap(key,value);
        firestore.document("/users/" + uid).update(updates)
            .addOnCompleteListener(task ->
            {
                if(!task.isSuccessful())
                {
                    G.debug(context, "Error updating profile in Firestore database");
                    Log.w(G.tag, "Error updating profile in Firestore database: ", task.getException());
                }
            });
    }
    
    public Task<Void> updateVehicleData(Vehicle v, String key, Object value)
    {
        Task<Void> task;
        String attrPath = "vehicles." + v.getId() + "." + key;
        task = firestore.document("/users/" + uid).update(attrPath, value);
        return task;
    }
    
    //pouziva callback mechanizmus, see also ClouStorage.java ktory pouziva Future na ten isty ucel
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    public Task<?> fetchFirestoreProfile(CompletedListener listener)
    {
        return firestore.document("/users/" + uid).get(Source.SERVER)
            .addOnCompleteListener(task ->
            {
                if(task.isSuccessful())
                {
                    DocumentSnapshot doc = task.getResult();
                    if(doc.getData() != null)
                    {
                        Map<String,Object> profileMap = doc.getData();
                        
                        //SharedPreferences
                        updateSharedPreferences(profileMap); //do we want to sync sharedprefs?
                        
                        //VehiclesRepo
                        if(profileMap.get("vehicles") != null)
                        {
                            //doesnt matter if exists in repo or not, because if is different in firestore, will update it
                            //noinspection rawtypes
                            Collection<HashMap<String,Object>> vehicles = ((HashMap)profileMap.get("vehicles")).values();
                            List<CompletableFuture<?>> downloadings = new ArrayList<>();
                            
                            for(HashMap<String,Object> vehicleMap : vehicles)
                            {
                                String vid = (String)vehicleMap.get(G.FIRESTORE_VEHICLE_ID);
                                Vehicle v;
                                if(vm.getVehicleFromRepo(vid) != null) //vehicle already exists locally
                                {
                                    v = vm.getVehicleFromRepo(vid);
                                    v.update(vehicleMap);
                                }
                                else //vehicle from firestore not found locally
                                {
                                    Log.i(G.tag, "vehicle from firestore not found locally, creating new one");
                                    v = new Vehicle(vehicleMap);
                                    vm.insertOrUpdateVehicle(v);
                                }
                                
                                if(!FileStorage.checkFileExists(context, "/media/" + v.getImageFilename())) //image not found locally, download
                                {
                                    CompletableFuture<Void> future = cstrg.downloadVehicleImage(v.getImageFilename())
                                        .thenAccept((success) -> v.loadVehicleImage(context));
                                    downloadings.add(future);
                                }
                                else //already have image locally
                                {
                                    v.loadVehicleImage(context);
                                }
                            }//foreach
                            
                            if(downloadings.size() > 0)
                                CompletableFuture.allOf(downloadings.toArray(new CompletableFuture[0])).join(); //wait for all downloads to complete
                            
                            if(listener != null)
                                listener.onCompleted(true);
                            
                            Log.i(G.tag, "Profile successfully synced with Firestore.");
                        }
                    }//doc.getData()!=null
                    else
                    {
                        Log.w(G.tag, "No profile found in Firestore database.");
                        if(listener != null) listener.onCompleted(false);
                    }
                }//task.isSuccessful()
                else
                {
                    G.debug(context, "Error getting profile from Firestore database");
                    Log.w(G.tag, "Error getting profile from Firestore database: ", task.getException());
                    if(listener != null) listener.onCompleted(false);
                }
            });
    }
    
    @SuppressWarnings("ConstantConditions")
    private void updateSharedPreferences(Map<String,Object> map)
    {
        SharedPreferences.Editor prefE = pref.edit();
        
        //all gets are nullable, pri stringoch null vymaze preference
        if(map.get(G.FIRESTORE_DEBUG_ENABLED) != null) prefE.putBoolean(G.PREF_DEBUG_ENABLED, (boolean)map.get(G.FIRESTORE_DEBUG_ENABLED));
            else prefE.remove(G.PREF_DEBUG_ENABLED);
        if(map.get(G.FIRESTORE_FCM_ENABLED) != null) prefE.putBoolean(G.PREF_FCM_ENABLED, (boolean)map.get(G.FIRESTORE_FCM_ENABLED));
            else prefE.remove(G.PREF_FCM_ENABLED);
        if(map.get(G.FIRESTORE_UPDATE_INTERVAL) != null) prefE.putInt(G.PREF_UPDATE_INTERVAL, ((Number)map.get(G.FIRESTORE_UPDATE_INTERVAL)).intValue());
            else prefE.remove(G.PREF_UPDATE_INTERVAL);
        if(map.get(G.FIRESTORE_ACTUALITY_THRESHOLD) != null) prefE.putInt(G.PREF_ACTUALITY_THRESHOLD, ((Number)map.get(G.FIRESTORE_ACTUALITY_THRESHOLD)).intValue());
            else prefE.remove(G.PREF_ACTUALITY_THRESHOLD);
        prefE.putString(G.PREF_LANGUAGE, (String)map.get(G.FIRESTORE_LANGUAGE));
        //prefE.putString(G.PREF_LAST_VEHICLE_ID, (String)map.get(G.FIRESTORE_LAST_VEHICLE_ID)); //not synced
        
        prefE.apply();
    }
    
    public interface CompletedListener
    {
        void onCompleted(boolean successfully);
    }
}
