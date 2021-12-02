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
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.martin.carcharge.App;
import com.martin.carcharge.MainActivity;
import com.martin.carcharge.R;
import com.martin.carcharge.databinding.FragmentBottomsheetBinding;
import com.martin.carcharge.models.MainViewModel.MainViewModel;
import com.martin.carcharge.models.User;
import com.martin.carcharge.models.Vehicle;

public class BottomSheetFragment extends BottomSheetDialogFragment
{
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
    
        vm = App.getViewModel();
    
        vm.user().observe(this, this::updateUserFields);
        for(MutableLiveData<Vehicle> m : vm.vehiclesRepo())
            m.observe(this, this::updateVehiclesRecycler);
        
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
        
        vehiclesAdapter = new VehiclesAdapter(requireContext(), getViewLifecycleOwner(), vm.getAllVehicles());
            vehiclesAdapter.setOnItemClickListener((view, position) ->
            {
                Vehicle selectedVehicle = vehiclesAdapter.get(position);
                vm.switchCurrentVehicle(selectedVehicle); //internally change status too
                
                //download new one
                MainActivity mAct = ((MainActivity)requireActivity());
                mAct.getDownloader().downloadLast(selectedVehicle, mAct.autoNewDataListener);
                mAct.setBottomSheetExpanded(false);
            });
        
        recycler_vehicles = binding.recyclerVehicles;
            recycler_vehicles.setAdapter(vehiclesAdapter);
        
        root.setOnClickListener(null); //chyti bottomsheet touches, inak by prepadli na scrim
        return root;
    }
    
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        binding = null;
    }
    
    private void updateUserFields(User user)
    {
        image_userIcon.setImageBitmap(user.getIcon());
        text_nickname.setVisibility(user.getNickname().isEmpty() ? View.GONE : View.VISIBLE);
        text_nickname.setText(user.getNickname());
        text_email.setText(user.getEmail());
    }
    
    private void updateVehiclesRecycler(Vehicle vehicle)
    {
        vehiclesAdapter.update(vehicle);
    }
}
