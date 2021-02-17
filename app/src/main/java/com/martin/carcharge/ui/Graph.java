package com.martin.carcharge.ui;

import android.graphics.Color;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.martin.carcharge.BaseActivity;
import com.martin.carcharge.R;
import com.martin.carcharge.models.VehicleStatus;
import com.martin.carcharge.ui.history.HistoryFragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Graph
{
    final HistoryFragment context;
    LineChart chart;
    
    long timestampBase;
    
    public Graph(HistoryFragment context, LineChart chart)
    {
        this.context = context;
        this.chart = chart;
    }
    
    public void initChart()
    {
        chart.setVisibility(View.VISIBLE);
        chart.setEnabled(true);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleXEnabled(true);
        chart.setScaleYEnabled(false);
        chart.setDoubleTapToZoomEnabled(true);
        
        chart.setNoDataText(context.getString(R.string.history_graph_nodata));
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
        legend.setTextColor(((BaseActivity)context.requireActivity()).getResColor(R.color.tint_blue));
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
        yAxisL.setTextColor(((BaseActivity)context.requireActivity()).getResColor(R.color.white));
        yAxisL.setGridColor(((BaseActivity)context.requireActivity()).getResColor(R.color.gray_medium));
        //yAxisL.setAxisMaximum(100);
        yAxisL.setAxisMinimum(0);
        yAxisL.setValueFormatter(percentFormatter);
        
        YAxis yAxisR = chart.getAxisRight();
        yAxisR.setEnabled(false);
        yAxisR.setDrawAxisLine(false);
        yAxisR.setDrawGridLines(false);
        yAxisR.setTextColor(((BaseActivity)context.requireActivity()).getResColor(R.color.white));
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
    
    public void newChart(List<VehicleStatus> list)
    {
        List<Entry> entriesCharge = new ArrayList<>();
        List<Entry> entriesCurrent = new ArrayList<>();
        
        VehicleStatus.State previousState = VehicleStatus.State.Unknown;
        if(list.size() != 0)
        {
            timestampBase = list.get(0).getTimestamp().getTime();
            previousState = list.get(0).getState();
        }
        
        for(VehicleStatus vs : list)
        {
            float x = (vs.getTimestamp().getTime() - timestampBase);
            entriesCharge.add(new Entry(x, vs.getCurrent_charge()));
            entriesCurrent.add(new Entry(x, vs.getCurrent()));
            
            if(vs.getState() != previousState)
            {
                previousState = vs.getState();
                LimitLine ll = new LimitLine(x, new String());
                ll.setLineColor(((BaseActivity)context.requireActivity()).getResColor(R.color.gray_medium));
                ll.enableDashedLine(3, 5, 0);
                ll.setLabel(vs.getState().asString(context.requireContext(), false));
                ll.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
                ll.setYOffset(6);
                ll.setTextColor(((BaseActivity)context.requireActivity()).getResColor(R.color.gray_medium));
                ll.setTextSize(8);
                chart.getXAxis().addLimitLine(ll);
            }
            
        }
        
        LineDataSet dataSetCharge = createDataSet(entriesCharge, context.getString(R.string.history_graph_charge_level), ((BaseActivity)context.requireActivity()).getResColor(R.color.alternative_blue));
        dataSetCharge.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSetCharge.setValueFormatter(percentFormatter);
        dataSetCharge.setDrawFilled(true);
        dataSetCharge.setFillColor(((BaseActivity)context.requireActivity()).getResColor(R.color.tint_blue));
        LineDataSet dataSetCurrent = createDataSet(entriesCurrent, context.getString(R.string.history_graph_current), ((BaseActivity)context.requireActivity()).getResColor(R.color.alternative_orange));
        dataSetCurrent.setAxisDependency(YAxis.AxisDependency.RIGHT);
        dataSetCurrent.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        dataSetCurrent.setValueFormatter(amperFormatter);
        
        LineData lineData = new LineData(dataSetCharge, dataSetCurrent);
        chart.setData(lineData);
        chart.invalidate();
    }
    
    public void updateChart(List<VehicleStatus> list)
    {
        newChart(list);
        chart.notifyDataSetChanged();
        //chart.moveViewToX(data.getEntryCount());
    }
    
    
    ValueFormatter timeFormatter = new ValueFormatter()
    {
        @Override
        public String getFormattedValue(float value)
        {
            return context.getDateFormatter().format(new Date((long)value+timestampBase));
        }
    };
    
    ValueFormatter percentFormatter = new ValueFormatter()
    {
        @Override
        public String getFormattedValue(float value)
        {
            return String.format(((BaseActivity)context.requireActivity()).getCurrentLocale(), "%.0f%%", value);
        }
    };
    
    ValueFormatter amperFormatter = new ValueFormatter()
    {
        @Override
        public String getFormattedValue(float value)
        {
            return String.format(((BaseActivity)context.requireActivity()).getCurrentLocale(), "%.0fA", value);
        }
    };
}
