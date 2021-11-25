package com.martin.carcharge.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog;
import com.github.mikephil.charting.charts.LineChart;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.martin.carcharge.App;
import com.martin.carcharge.BaseActivity;
import com.martin.carcharge.G;
import com.martin.carcharge.MainActivity;
import com.martin.carcharge.R;
import com.martin.carcharge.databinding.FragmentHistoryBinding;
import com.martin.carcharge.models.MainViewModel.MainViewModel;
import com.martin.carcharge.models.VehicleStatus;
import com.martin.carcharge.network.Downloader;
import com.martin.carcharge.ui.Graph;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Response;

import static java.text.DateFormat.getDateTimeInstance;

public class HistoryFragment extends Fragment
{
    private MainViewModel vm;
    private HistoryViewModel historyViewModel;
    
    FragmentHistoryBinding binding;
    View root;
    Toolbar toolbar;
    ProgressBar progressbar;
    EditText edit_periodFrom, edit_periodTo;
    Button button_load;
    LineChart chart;
    FloatingActionButton fab_action;
    
    Graph graph;
    SimpleDateFormat formatter;
    Timestamp timestampFrom, timestampTo;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ((MainActivity)requireActivity()).setBottomBarVisible(true, true);
        
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        root = binding.getRoot();
        
        vm = App.getViewModel();
        //todo observe status change
        
        //historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        //historyViewModel.getText().observe(getViewLifecycleOwner(), s -> {});
        
        formatter = new SimpleDateFormat(new String(), ((MainActivity)requireActivity()).getCurrentLocale());

        toolbar = binding.toolbarHistory;
            toolbar.setNavigationOnClickListener(view1 -> requireActivity().onBackPressed());
    
        progressbar = binding.progressHistory;
            
        edit_periodFrom = binding.editPeriodFrom;
            edit_periodFrom.setShowSoftInputOnFocus(false);
            edit_periodFrom.setOnFocusChangeListener((view1, b) ->
            {
                if(!b) return;
                edit_periodFrom.clearFocus();
                new SingleDateAndTimePickerDialog.Builder(requireContext())
                        .title("Period from")
                        .mainColor(getResources().getColor(R.color.tint_blue_variant, requireActivity().getTheme()))
                        .backgroundColor(getResources().getColor(R.color.tile_gray, requireActivity().getTheme()))
                        .listener(datetimeFromPickerListener)
                        .build()
                        .display();
            });
            
        edit_periodTo = binding.editPeriodTo;
            edit_periodTo.setShowSoftInputOnFocus(false);
            edit_periodTo.setOnFocusChangeListener((view1, b) ->
            {
                if(!b) return;
                edit_periodTo.clearFocus();
                new SingleDateAndTimePickerDialog.Builder(requireContext())
                        .title("Period to")
                        .mainColor(getResources().getColor(R.color.tint_blue_variant, requireActivity().getTheme()))
                        .backgroundColor(getResources().getColor(R.color.tile_gray, requireActivity().getTheme()))
                        .listener(datetimeToPickerListener)
                        .build()
                        .display();
            });
            
        button_load = binding.buttonLoad;
            button_load.setOnClickListener(onLoadClickListener);
    
        chart =  binding.chartHistory;
            graph = new Graph(this, chart);
            graph.initChart();
    
        fab_action = ((MainActivity)requireActivity()).getFab();
            fab_action.setOnClickListener(null);
        
        return root;
    }
    
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        binding = null;
    }
    
    
    SingleDateAndTimePickerDialog.Listener datetimeFromPickerListener = date ->
    {
        timestampFrom = new Timestamp(date.getTime());
        DateFormat df = getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, ((BaseActivity)requireActivity()).getCurrentLocale());
        String string = df.format(timestampFrom);
        edit_periodFrom.setText(string);
    };
    
    SingleDateAndTimePickerDialog.Listener datetimeToPickerListener = date ->
    {
        timestampTo = new Timestamp(date.getTime());
        DateFormat df = getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, ((BaseActivity)requireActivity()).getCurrentLocale());
        String string = df.format(timestampTo);
        edit_periodTo.setText(string);
    };
    
    View.OnClickListener onLoadClickListener = view ->
    {
        /**/ timestampFrom = new Timestamp(1612371300000L); timestampTo = new Timestamp(1614061813000L);
        if(timestampFrom == null || timestampTo == null) return;
        
        edit_periodFrom.clearFocus();
        edit_periodTo.clearFocus();
        
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        if(f.format(timestampFrom).equals(f.format(timestampTo))) //is the same day
            formatter.applyLocalizedPattern("MMM d, HH:mm");
        else
            formatter.applyLocalizedPattern("HH:mm");
    
        List<VehicleStatus> localList = vm.getVehicleStatuses(vm.getCurrentVehicle(), timestampFrom, timestampTo);
        graph.newChart(localList);
        
        //network part
        progressbar.setVisibility(View.VISIBLE);
        ((MainActivity)requireActivity()).getDownloader()
                .downloadRange(vm.getCurrentVehicle(), timestampFrom, timestampTo, new Downloader.RangeListener()
                {
                    @Override
                    public void onSuccess(@NonNull List<VehicleStatus> remoteList)
                    {
                        progressbar.setVisibility(View.INVISIBLE);
                        vm.addVehicleStatuses(remoteList);
                        graph.updateChart(remoteList);
                    }
    
                    @Override
                    public void onFail(@Nullable Response<?> response)
                    {
                        progressbar.setVisibility(View.INVISIBLE);
                        Snackbar.make(((MainActivity)requireActivity()).getRootLayout(),
                                "Server request failed.\nShowing local only (" + localList.size() + " entries)",
                                Snackbar.LENGTH_SHORT)
                                .setAnchorView(R.id.fab_action).show();
                    }
                });
    
    
        //G.debug(requireContext(), "Found " + localList.size() + " local entries.");
        /*Snackbar snack = Snackbar.make(((MainActivity)requireActivity()).getRootLayout(), "Found " + localList.size() + " local entries.", Snackbar.LENGTH_SHORT)
                .setAnchorView(R.id.fab_action);
        View snackView = snack.getView();
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) snackView.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin + 20,layoutParams.topMargin + 20,layoutParams.rightMargin + 20,layoutParams.bottomMargin + 20);
        snackView.setLayoutParams(layoutParams);
        snack.show();*/
    };
    
    public DateFormat getDateFormatter()
    {
        return formatter;
    }
}