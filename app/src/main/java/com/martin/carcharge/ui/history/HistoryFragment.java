package com.martin.carcharge.ui.history;

import android.graphics.Color;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.martin.carcharge.App;
import com.martin.carcharge.MainActivity;
import com.martin.carcharge.R;
import com.martin.carcharge.databinding.FragmentHistoryBinding;
import com.martin.carcharge.models.MainViewModel.MainViewModel;
import com.martin.carcharge.models.VehicleStatus;

import java.sql.Timestamp;
import java.util.List;

import static java.text.DateFormat.getDateTimeInstance;

public class HistoryFragment extends Fragment
{
    private MainViewModel vm;
    private HistoryViewModel historyViewModel;
    
    FragmentHistoryBinding binding;
    View root;
    Toolbar toolbar;
    EditText edit_periodFrom, edit_periodTo;
    Button button_load;
    LineChart chart;
    FloatingActionButton fab_action;
    
    Timestamp timestampFrom, timestampTo;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ((MainActivity)requireActivity()).setBottomBarVisible(true);
        
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        root = binding.getRoot();
        
        vm = App.getViewModel();
        
        historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        historyViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>()
        {
            @Override
            public void onChanged(@Nullable String s)
            {
            }
        });
    
        toolbar = binding.toolbarHistory;
            toolbar.setNavigationOnClickListener(view1 -> requireActivity().onBackPressed());
            
        edit_periodFrom = binding.editPeriodFrom;
            edit_periodFrom.setShowSoftInputOnFocus(false);
            edit_periodFrom.setOnFocusChangeListener((view1, b) ->
            {
                if(!b) return;
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
            initChart();
    
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
    
    private void initChart()
    {
        // enable description text
        chart.getDescription().setEnabled(true);
        chart.getDescription().setText("Charge level history");
        chart.getDescription().setXOffset(10);
        chart.getDescription().setYOffset(0);
        chart.getDescription().setTextSize(12.5f);
        chart.getDescription().setTextColor(getResources().getColor(R.color.white_shade, requireActivity().getTheme()));
    
        //chart.setTouchEnabled(true);
    
        //chart.setDragEnabled(true);
        //chart.setScaleEnabled(true);
        //chart.setDrawGridBackground(false);
    
        // if disabled, scaling can be done on x- and y-axis separately
        //chart.setPinchZoom(true);
    
        //chart.setBackgroundColor(getResources().getColor(R.color.tile_gray, requireActivity().getTheme()));
    
        LineData data = new LineData();
        //data.setValueTextColor(Color.RED);
    
        // add empty data
        chart.setData(data);
    
        // get the legend (only possible after setting data)
        Legend legend = chart.getLegend();
    
        // modify the legend ...
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setTextColor(getResources().getColor(R.color.tint_blue, requireActivity().getTheme()));
    
        XAxis xAxis = chart.getXAxis();
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setEnabled(true);
    
        YAxis yAxisL = chart.getAxisLeft();
        yAxisL.setTextColor(getResources().getColor(R.color.white_shade, requireActivity().getTheme()));
        yAxisL.setDrawAxisLine(false);
        yAxisL.setDrawGridLines(false);
        yAxisL.setAxisMaximum(100f);
        yAxisL.setAxisMinimum(0f);
        yAxisL.setDrawGridLines(true);
    
        YAxis yAxisR = chart.getAxisRight();
        yAxisR.setEnabled(false);
    
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getXAxis().setDrawGridLines(false);
    }
    
    private void addEntry(SensorEvent event)
    {
        LineData data = chart.getData();
        
        if(data != null)
        {
            ILineDataSet set = data.getDataSetByIndex(0);
            
            if(set == null)
            {
                set = createSet();
                data.addDataSet(set);
            }
            
            //data.addEntry(new Entry(set.getEntryCount(), (float) (Math.random() * 80) + 10f), 0);
            
            Entry entry = new Entry(set.getEntryCount(), event.values[0] + 5);
            data.addEntry(entry, 0);
            data.notifyDataChanged();
            
            // let the chart know it's data has changed
            chart.notifyDataSetChanged();
            
            // limit the number of visible entries
            chart.setVisibleXRangeMaximum(150);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);
            
            // move to the latest entry
            chart.moveViewToX(data.getEntryCount());
        }
    }
    
    private LineDataSet createSet()
    {
        LineDataSet set = new LineDataSet(null, "x-axis value");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(3f);
        set.setColor(Color.MAGENTA);
        set.setHighlightEnabled(false);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
    }
    
    
    
    
    SingleDateAndTimePickerDialog.Listener datetimeFromPickerListener = date ->
    {
        timestampFrom = new Timestamp(date.getTime()/1000);
        binding.editPeriodFrom.setText(getDateTimeInstance().format(timestampFrom.getTime()));
    };
    
    SingleDateAndTimePickerDialog.Listener datetimeToPickerListener = date ->
    {
        timestampTo = new Timestamp(date.getTime()/1000);
        binding.editPeriodTo.setText(getDateTimeInstance().format(timestampTo.getTime()));
    };
    
    View.OnClickListener onLoadClickListener = view ->
    {
        edit_periodFrom.clearFocus();
        edit_periodTo.clearFocus();
        timestampFrom = new Timestamp(1612371300); timestampTo = new Timestamp(1613061813);
        if(timestampFrom == null || timestampTo == null) return;
        
        List<VehicleStatus> list = vm.getVehicleStatuses(vm.getActualVehicleId(), timestampFrom, timestampTo);
        int entries = list.size();
        
        LineData data = new LineData();
        ILineDataSet set = createSet();
        data.addDataSet(set);
        
        for(VehicleStatus vs : list)
        {
            Entry entry = new Entry(vs.getTimestamp().getTime(), vs.getCurrent_charge());
            data.addEntry(entry, 0);
        }
        
        chart.setData(data);
        chart.notifyDataSetChanged();
        chart.moveViewToX(data.getEntryCount());
    
    
    
        Snackbar snack = Snackbar.make(((MainActivity)requireActivity()).getRootLayout(), "Found " + entries + " entries.", Snackbar.LENGTH_SHORT)
                .setAnchorView(R.id.fab_action);
        View snackView = snack.getView();
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) snackView.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin + 20,layoutParams.topMargin + 20,layoutParams.rightMargin + 20,layoutParams.bottomMargin + 20);
        snackView.setLayoutParams(layoutParams);
        snack.show();
        
        
    };
}