package com.martin.carcharge.ui.history;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.MarkerImage;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.martin.carcharge.App;
import com.martin.carcharge.MainActivity;
import com.martin.carcharge.R;
import com.martin.carcharge.databinding.FragmentHistoryBinding;
import com.martin.carcharge.models.MainViewModel.MainViewModel;
import com.martin.carcharge.models.VehicleStatus;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    
    SimpleDateFormat formatter;
    Timestamp timestampFrom, timestampTo;
    long timestampBase;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ((MainActivity)requireActivity()).setBottomBarVisible(true);
        
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        root = binding.getRoot();
        
        vm = App.getViewModel();
        
        historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        historyViewModel.getText().observe(getViewLifecycleOwner(), s -> {});
    
        formatter = new SimpleDateFormat(new String(), locale());

        toolbar = binding.toolbarHistory;
            toolbar.setNavigationOnClickListener(view1 -> requireActivity().onBackPressed());
            
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
        chart.setVisibility(View.VISIBLE);
        chart.setEnabled(true);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleXEnabled(true);
        chart.setScaleYEnabled(false);
        chart.setDoubleTapToZoomEnabled(true);
        
        chart.setNoDataText(getString(R.string.history_graph_nodata));
        chart.setMaxVisibleValueCount(10); //show values only when zoomed
        //chart.animateX(1000, Easing.EaseInOutElastic);
        chart.setVisibleXRangeMinimum(4); //todo nejde?
        chart.setVisibleXRangeMaximum(50);
        chart.setExtraBottomOffset(20);
    
        Description description = chart.getDescription();
        description.setEnabled(false);
    
        Legend legend = chart.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setTextColor(getResources().getColor(R.color.tint_blue, requireActivity().getTheme()));
        legend.setXOffset(0);
        legend.setYOffset(6);
        legend.setXEntrySpace(12);
        
        XAxis xAxis = chart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setLabelCount(6, false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(60000-1); //ani srnka netusi preco -1 ale s 60k to nejde
        xAxis.setValueFormatter(timeFormatter);
        xAxis.setLabelRotationAngle(330);
        xAxis.setDrawLimitLinesBehindData(true);
    
        YAxis yAxisL = chart.getAxisLeft();
        yAxisL.setEnabled(true);
        yAxisL.setDrawAxisLine(false);
        yAxisL.setDrawGridLines(false);
        yAxisL.setTextColor(getResources().getColor(R.color.white, requireActivity().getTheme()));
        yAxisL.setGridColor(getResources().getColor(R.color.gray_medium, requireActivity().getTheme()));
        //yAxisL.setAxisMaximum(100);
        yAxisL.setAxisMinimum(0);
        yAxisL.setValueFormatter(percentFormatter);
    
        YAxis yAxisR = chart.getAxisRight();
        yAxisR.setEnabled(false);
        yAxisR.setDrawAxisLine(false);
        yAxisR.setDrawGridLines(false);
        yAxisR.setTextColor(getResources().getColor(R.color.white, requireActivity().getTheme()));
        //yAxisR.setAxisMinimum(-500);
        yAxisR.setAxisMaximum(500); //todo na listener
        yAxisR.setInverted(true);
    }
    
    private LineDataSet createDataSet(List<Entry> entries, String label, int color)
    {
        LineDataSet set = new LineDataSet(entries, label);
        set.setLineWidth(3f);
        set.setColor(color);
        set.setHighlightEnabled(true);
        set.setDrawVerticalHighlightIndicator(true);
        set.setDrawHorizontalHighlightIndicator(false);
        set.setDrawValues(true);
        set.setDrawCircles(true);
        set.setCircleColor(color);
        set.setDrawCircleHole(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        return set;
    }
    
    ValueFormatter timeFormatter = new ValueFormatter()
    {
        @Override
        public String getFormattedValue(float value)
        {
            return formatter.format(new Date((long)value+timestampBase));
        }
    };
    
    ValueFormatter percentFormatter = new ValueFormatter()
    {
        @Override
        public String getFormattedValue(float value)
        {
            return String.format(locale(), "%.0f%%", value);
        }
    };
    
    ValueFormatter amperFormatter = new ValueFormatter()
    {
        @Override
        public String getFormattedValue(float value)
        {
            return String.format(locale(), "%.0fA", value);
        }
    };
    
    Locale locale()
    {
        return getResources().getConfiguration().getLocales().get(0);
    }
    
    SingleDateAndTimePickerDialog.Listener datetimeFromPickerListener = date ->
    {
        timestampFrom = new Timestamp(date.getTime());
        String string = getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale()).format(timestampFrom);
        edit_periodFrom.setText(string);
    };
    
    SingleDateAndTimePickerDialog.Listener datetimeToPickerListener = date ->
    {
        timestampTo = new Timestamp(date.getTime());
        String string = getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale()).format(timestampTo);
        edit_periodTo.setText(string);
    };
    
    View.OnClickListener onLoadClickListener = view ->
    {
        /**/ timestampFrom = new Timestamp(1612371300000L); timestampTo = new Timestamp(1613061813000L);
        /**/ if(timestampFrom == null || timestampTo == null) return;
        
        edit_periodFrom.clearFocus();
        edit_periodTo.clearFocus();
        
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        if(f.format(timestampFrom).equals(f.format(timestampTo))) //is the same day
            formatter.applyLocalizedPattern("MMM d, HH:mm");
        else
            formatter.applyLocalizedPattern("HH:mm");
    
        List<VehicleStatus> list = vm.getVehicleStatuses(vm.getActualVehicleId(), timestampFrom, timestampTo);
        List<Entry> entriesCharge = new ArrayList<>();
        List<Entry> entriesCurrent = new ArrayList<>();
        
        timestampBase = list.get(0).getTimestamp().getTime();
        VehicleStatus.State previousState = list.get(0).getState();
        
        for(VehicleStatus vs : list)
        {
            float x = (vs.getTimestamp().getTime() - timestampBase);
            entriesCharge.add(new Entry(x, vs.getCurrent_charge()));
            entriesCurrent.add(new Entry(x, vs.getCurrent()));

            if(vs.getState() != previousState)
            {
                previousState = vs.getState();
                LimitLine ll = new LimitLine(x, new String());
                ll.setLineColor(getResources().getColor(R.color.gray_medium, requireActivity().getTheme()));
                ll.enableDashedLine(3, 5, 0);
                ll.setLabel("Charging");
                ll.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
                ll.setYOffset(6);
                ll.setTextColor(getResources().getColor(R.color.gray_medium, requireActivity().getTheme()));
                ll.setTextSize(8);
                chart.getXAxis().addLimitLine(ll);
            }
    
        }
        
        
        LineDataSet dataSetCharge = createDataSet(entriesCharge, getString(R.string.history_graph_charge_level), getResources().getColor(R.color.alternative_blue, requireActivity().getTheme()));
            dataSetCharge.setAxisDependency(YAxis.AxisDependency.LEFT);
            dataSetCharge.setValueFormatter(percentFormatter);
            dataSetCharge.setDrawFilled(true);
            dataSetCharge.setFillColor(getResources().getColor(R.color.tint_blue, requireActivity().getTheme()));
        LineDataSet dataSetCurrent = createDataSet(entriesCurrent, getString(R.string.history_graph_current), getResources().getColor(R.color.alternative_orange, requireActivity().getTheme()));
            dataSetCurrent.setAxisDependency(YAxis.AxisDependency.RIGHT);
            dataSetCurrent.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
            dataSetCurrent.setValueFormatter(amperFormatter);
            
        LineData lineData = new LineData(dataSetCharge, dataSetCurrent);
        chart.setData(lineData);
        chart.invalidate();
        //chart.notifyDataSetChanged();
        //chart.moveViewToX(data.getEntryCount());
        
    
        Snackbar snack = Snackbar.make(((MainActivity)requireActivity()).getRootLayout(), "Found " + list.size() + " entries.", Snackbar.LENGTH_SHORT)
                .setAnchorView(R.id.fab_action);
        View snackView = snack.getView();
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) snackView.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin + 20,layoutParams.topMargin + 20,layoutParams.rightMargin + 20,layoutParams.bottomMargin + 20);
        snackView.setLayoutParams(layoutParams);
        snack.show();
    };
}