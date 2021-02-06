package com.martin.carcharge.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.martin.carcharge.AppActivity;
import com.martin.carcharge.MainActivity;
import com.martin.carcharge.R;
import com.martin.carcharge.database.AppDatabase;
import com.martin.carcharge.models.MainViewModel.MainViewModel;
import com.martin.carcharge.models.User;
import com.martin.carcharge.models.Vehicle;

public class BottomSheetFragment extends BottomSheetDialogFragment
{
    private AppDatabase db;
    private SharedPreferences pref;
    private MainViewModel vm;
    
    
    ImageView image_userIcon;
    TextView text_nickname, text_email;
    ImageButton ibutton_newVehicle, ibutton_preferences;
    RecyclerView recycler_vehicles;
    
    VehiclesAdapter vehiclesAdapter;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_bottomsheet, container, false);
    
        db = AppActivity.getDatabase();
        pref = PreferenceManager.getDefaultSharedPreferences(requireContext());
        vm = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
    
        image_userIcon = root.findViewById(R.id.image_userIcon);
            image_userIcon.setImageDrawable(((MainActivity)requireActivity()).getUserIcon(pref.getString("user_icon", "")));
            
        text_nickname = root.findViewById(R.id.text_nickname);
            User user = vm.user().getValue();
            text_nickname.setVisibility(user.getNickname().isEmpty() ? View.GONE : View.VISIBLE);
            text_nickname.setText(user.getNickname());
        
        text_email = root.findViewById(R.id.text_email);
            text_email.setText(pref.getString("user_email", ""));
        
        ibutton_newVehicle = root.findViewById(R.id.ibutton_newVehicle);
            ibutton_newVehicle.setOnClickListener(v -> newVehicle());
        
        ibutton_preferences = root.findViewById(R.id.ibutton_settings);
            ibutton_preferences.setOnClickListener(v ->
            {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment_nav_host);
                navController.navigate(R.id.navigation_action_home_to_preferences);
            });
        
        vehiclesAdapter = new VehiclesAdapter(requireContext(), db.dao().getAllVehicles());
            vehiclesAdapter.setOnItemClickListener((view, position) ->
            {
                Vehicle currentVehicle = vehiclesAdapter.get(position);
                vm.postVehicle(currentVehicle);
            });
        
        recycler_vehicles = root.findViewById(R.id.recycler_vehicles);
            recycler_vehicles.setAdapter(vehiclesAdapter);
        
        return root;
    }
    
    
    void newVehicle()
    {
        View content = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(getString(R.string.app_new_vehicle));
        builder.setView(content);
        builder.setPositiveButton(getString(R.string.create), (dialog2, which) -> {});
        
        {
            AlertDialog newVehicleDialog = builder.create();
            newVehicleDialog.show();
            newVehicleDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        
            EditText edit_name = content.findViewById(R.id.edit_name);
            Button dialogPButton = newVehicleDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            
            edit_name.addTextChangedListener(new TextWatcher()
            {
                @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override public void afterTextChanged(Editable editable) {dialogPButton.setEnabled(!editable.toString().isEmpty()); }
            });
    
            dialogPButton.setEnabled(false);
            dialogPButton.setOnClickListener(view ->
            {
                newVehicleDialog.dismiss();
                Vehicle newVehicle = vm.createVehicle(edit_name.getText().toString());
                vehiclesAdapter.add(newVehicle);
            });
    
        }
    }
}
