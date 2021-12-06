package com.martin.carcharge.ui.history;

import android.os.Bundle;
import android.text.format.DateUtils;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    FloatingActionButton fab_button;
    
    Graph graph;
    SimpleDateFormat formatter1, formatter2; //1 for editTexts, 2 for graph
    
    Date timeFrom, timeTo;
    
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
        
        formatter1 = new SimpleDateFormat("MMM d, yyyy HH:mm", ((MainActivity)requireActivity()).getCurrentLocale());
        formatter2 = new SimpleDateFormat(new String(), ((MainActivity)requireActivity()).getCurrentLocale());

        timeFrom = new Date(new Date().getTime() - DateUtils.WEEK_IN_MILLIS); //-1 week
        timeTo = new Date(); //now
       
        toolbar = binding.toolbarHistory;
            toolbar.setNavigationOnClickListener(view1 -> requireActivity().onBackPressed());
    
        progressbar = binding.progressbarHistory;
        
        edit_periodFrom = binding.editPeriodFrom;
            edit_periodFrom.setText(formatter1.format(timeFrom));
            edit_periodFrom.setShowSoftInputOnFocus(false);
            edit_periodFrom.setOnFocusChangeListener((view1, hasFocus) ->
            {
                if(!hasFocus) return;
                edit_periodFrom.clearFocus();
                new SingleDateAndTimePickerDialog.Builder(requireContext())
                        .title("Period from")
                        .mainColor(getResources().getColor(R.color.tint_blue_variant, requireActivity().getTheme()))
                        .backgroundColor(getResources().getColor(R.color.tile_gray, requireActivity().getTheme()))
                        .defaultDate(timeFrom)
                        .listener(datetimeFromPickerListener)
                        .build()
                        .display();
            });
            
        edit_periodTo = binding.editPeriodTo;
            edit_periodTo.setText(formatter1.format(timeTo));
            edit_periodTo.setShowSoftInputOnFocus(false);
            edit_periodTo.setOnFocusChangeListener((view1, hasFocus) ->
            {
                if(!hasFocus) return;
                edit_periodTo.clearFocus();
                new SingleDateAndTimePickerDialog.Builder(requireContext())
                        .title("Period to")
                        .mainColor(getResources().getColor(R.color.tint_blue_variant, requireActivity().getTheme()))
                        .backgroundColor(getResources().getColor(R.color.tile_gray, requireActivity().getTheme()))
                        .defaultDate(timeTo)
                        .maxDateRange(new Date())
                        .listener(datetimeToPickerListener)
                        .build()
                        .display();
            });
            
        button_load = binding.buttonLoad;
            button_load.setOnClickListener(onLoadClickListener);
    
        chart =  binding.chartHistory;
            graph = new Graph(this, chart);
            graph.initChart();
    
        fab_button = ((MainActivity)requireActivity()).getFab();
            fab_button.setOnClickListener(null);
        
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
        timeFrom = date;
        DateFormat df = getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, ((BaseActivity)requireActivity()).getCurrentLocale());
        String string = df.format(timeFrom);
        edit_periodFrom.setText(string);
    };
    
    SingleDateAndTimePickerDialog.Listener datetimeToPickerListener = date ->
    {
        timeTo = date;
        DateFormat df = getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, ((BaseActivity)requireActivity()).getCurrentLocale());
        String string = df.format(timeTo);
        edit_periodTo.setText(string);
    };
    
    View.OnClickListener onLoadClickListener = view ->
    {
        if(timeFrom == null || timeTo == null) return;
        
        edit_periodFrom.clearFocus();
        edit_periodTo.clearFocus();
        
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        if(f.format(timeFrom).equals(f.format(timeTo))) //is the same day
            formatter2.applyLocalizedPattern("MMM d, HH:mm");
        else
            formatter2.applyLocalizedPattern("HH:mm");
        
        List<VehicleStatus> localList = vm.getVehicleStatuses(vm.getCurrentVehicle(), timeFrom, timeTo);
        graph.newChart(localList);
        
        //network part
        progressbar.setVisibility(View.VISIBLE);
        ((MainActivity)requireActivity()).getDownloader()
                .downloadRange(vm.getCurrentVehicle(), timeFrom, timeTo, new Downloader.RangeListener()
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
                        
                        String message;
                        if(response != null && response.code() == 204)
                            message = "No data from the selected time range.";
                        else
                            message = "Server request failed.\nShowing local only (" + localList.size() + " entries)";
                            
                        Snackbar.make(root, message, Snackbar.LENGTH_SHORT).setAnchorView(R.id.fab_button).show();
                    }
                });
        
        G.debug(requireContext(), "Found " + localList.size() + " local entries.");
    };
    
    public DateFormat getGraphDateFormatter()
    {
        return formatter2;
    }
}