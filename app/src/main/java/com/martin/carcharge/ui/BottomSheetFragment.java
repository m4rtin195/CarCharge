package com.martin.carcharge.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.martin.carcharge.App;
import com.martin.carcharge.MainActivity;
import com.martin.carcharge.R;
import com.martin.carcharge.database.AppDatabase;
import com.martin.carcharge.databinding.FragmentBottomsheetBinding;
import com.martin.carcharge.models.MainViewModel.MainViewModel;
import com.martin.carcharge.models.User;
import com.martin.carcharge.models.Vehicle;

import java.util.List;

public class BottomSheetFragment extends BottomSheetDialogFragment
{
    private AppDatabase db;
    private MainViewModel vm;
    
    FragmentBottomsheetBinding binding;
    View root;
    ImageView image_userIcon;
    TextView text_nickname, text_email;
    ImageButton ibutton_preferences;
    RecyclerView recycler_vehicles;
    
    VehiclesAdapter vehiclesAdapter;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        binding = FragmentBottomsheetBinding.inflate(inflater, container, false);
        root = binding.getRoot();
    
        db = App.getDatabase();
        vm = App.getViewModel();
    
        vm.user().observe(this, this::updateUserFields);
        //vm.vehicles().observe(this, this::updateVehiclesRecycler);
        
        image_userIcon = binding.imageUserIcon;
        text_nickname = binding.textNickname;
        text_email = binding.textEmail;
        
        ibutton_preferences = binding.ibuttonPreferences;
            ibutton_preferences.setOnClickListener(v ->
            {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment_navHost);
                navController.navigate(R.id.navigation_action_home_to_preferences);
                ((MainActivity)requireActivity()).setBottomSheetExpanded(false);
            });
        
        vehiclesAdapter = new VehiclesAdapter(requireContext(), db.dao().getAllVehicles());
            vehiclesAdapter.setOnItemClickListener((view, position) ->
            {
                Vehicle currentVehicle = vehiclesAdapter.get(position);
                vm.setVehicle(currentVehicle);
                ((MainActivity)requireActivity()).setBottomSheetExpanded(false);
            });
        
        recycler_vehicles = binding.recyclerVehicles;
            recycler_vehicles.setAdapter(vehiclesAdapter);
            
        root.setOnClickListener(null); //chyti bottomsheet touches, inak by prepadli na scrim
        return root;
    }
    
    private void updateUserFields(User user)
    {
        image_userIcon.setImageDrawable(user.getImage());
        text_nickname.setVisibility(user.getNickname().isEmpty() ? View.GONE : View.VISIBLE);
        text_nickname.setText(user.getNickname());
        text_email.setText(user.getEmail());
    }
    
    private void updateVehiclesRecycler(List<Vehicle> vehicles)
    {}
}
