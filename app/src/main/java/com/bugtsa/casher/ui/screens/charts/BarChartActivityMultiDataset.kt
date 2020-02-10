package com.bugtsa.casher.ui.screens.charts

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import com.bugtsa.casher.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.LargeValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import java.util.*

class BarChartActivityMultiDataset : DemoBase(), OnSeekBarChangeListener, OnChartValueSelectedListener {
    private var chart: BarChart? = null
    private var seekBarX: SeekBar? = null
    private var seekBarY: SeekBar? = null
    private var tvX: TextView? = null
    private var tvY: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_barchart)
        title = "BarChartActivityMultiDataset"
        tvX = findViewById(R.id.tvXMax)
        tvX?.textSize = 10f
        tvY = findViewById(R.id.tvYMax)
        seekBarX = findViewById(R.id.seekBar1)
        seekBarX?.max = 50
        seekBarX?.setOnSeekBarChangeListener(this)
        seekBarY = findViewById(R.id.seekBar2)
        seekBarY?.setOnSeekBarChangeListener(this)
        chart = findViewById(R.id.chart1)
        chart?.setOnChartValueSelectedListener(this)
        chart?.description?.isEnabled = false
        //        chart.setDrawBorders(true);
// scaling can now only be done on x- and y-axis separately
        chart?.setPinchZoom(false)
        chart?.setDrawBarShadow(false)
        chart?.setDrawGridBackground(false)
        // create a custom MarkerView (extend MarkerView) and specify the layout
// to use for it
        val mv = MyMarkerView(this, R.layout.custom_marker_view)
        mv.chartView = chart // For bounds control
        chart?.marker = mv // Set the marker to the chart
        seekBarX?.progress = 10
        seekBarY?.progress = 100

        chart?.legend?.apply {
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            orientation = Legend.LegendOrientation.VERTICAL
            setDrawInside(true)
            typeface = tfLight
            yOffset = 0f
            xOffset = 10f
            yEntrySpace = 0f
            textSize = 8f
        }

        val xAxis = chart?.xAxis
        xAxis?.typeface = tfLight
        xAxis?.granularity = 1f
        xAxis?.setCenterAxisLabels(true)
        xAxis?.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toString()
            }
        }
        val leftAxis = chart?.axisLeft
        leftAxis?.typeface = tfLight
        leftAxis?.valueFormatter = LargeValueFormatter()
        leftAxis?.setDrawGridLines(false)
        leftAxis?.spaceTop = 35f
        leftAxis?.axisMinimum = 0f // this replaces setStartAtZero(true)
        chart?.axisRight?.isEnabled = false
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        val groupSpace = 0.08f
        val barSpace = 0.03f // x4 DataSet
        val barWidth = 0.2f // x4 DataSet
        // (0.2 + 0.03) * 4 + 0.08 = 1.00 -> interval per "group"
        val groupCount = seekBarX!!.progress + 1
        val startYear = 1980
        val endYear = startYear + groupCount
        tvX!!.text = String.format(Locale.ENGLISH, "%d-%d", startYear, endYear)
        tvY!!.text = seekBarY!!.progress.toString()
        val values1 = ArrayList<BarEntry>()
        val values2 = ArrayList<BarEntry>()
        val values3 = ArrayList<BarEntry>()
        val values4 = ArrayList<BarEntry>()
        val randomMultiplier = seekBarY!!.progress * 100000f
        for (i in startYear until endYear) {
            values1.add(BarEntry(i.toFloat(), (1 * 1).toFloat()))
            values2.add(BarEntry(i.toFloat(), (1 * 2).toFloat()))
            values3.add(BarEntry(i.toFloat(), (1 * 3).toFloat()))
            values4.add(BarEntry(i.toFloat(), (1 * 4).toFloat()))
        }
        val set1: BarDataSet
        val set2: BarDataSet
        val set3: BarDataSet
        val set4: BarDataSet
        if (chart!!.data != null && chart!!.data.dataSetCount > 0) {
            set1 = chart!!.data.getDataSetByIndex(0) as BarDataSet
            set2 = chart!!.data.getDataSetByIndex(1) as BarDataSet
            set3 = chart!!.data.getDataSetByIndex(2) as BarDataSet
            set4 = chart!!.data.getDataSetByIndex(3) as BarDataSet
            set1.values = values1
            set2.values = values2
            set3.values = values3
            set4.values = values4
            chart!!.data.notifyDataChanged()
            chart!!.notifyDataSetChanged()
        } else { // create 4 DataSets
            set1 = BarDataSet(values1, "Good A")
            set1.color = Color.rgb(104, 241, 175)
            set2 = BarDataSet(values2, "Good B")
            set2.color = Color.rgb(164, 228, 251)
            set3 = BarDataSet(values3, "Good C")
            set3.color = Color.rgb(242, 247, 158)
            set4 = BarDataSet(values4, "Good D")
            set4.color = Color.rgb(255, 102, 0)
            val data = BarData(set1, set2, set3, set4)
            data.setValueFormatter(LargeValueFormatter())
            data.setValueTypeface(tfLight)
            chart!!.data = data
        }
        // specify the width each bar should have
        chart!!.barData.barWidth = barWidth
        // restrict the x-axis range
        chart!!.xAxis.axisMinimum = startYear.toFloat()
        // barData.getGroupWith(...) is a helper that calculates the width each group needs based on the provided parameters
        chart!!.xAxis.axisMaximum = startYear + chart!!.barData.getGroupWidth(groupSpace, barSpace) * groupCount
        chart!!.groupBars(startYear.toFloat(), groupSpace, barSpace)
        chart!!.invalidate()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.bar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionToggleValues -> {
                for (set in chart!!.data.dataSets) set.setDrawValues(!set.isDrawValuesEnabled)
                chart!!.invalidate()
            }
            R.id.actionTogglePinch -> {
                if (chart!!.isPinchZoomEnabled) chart!!.setPinchZoom(false) else chart!!.setPinchZoom(true)
                chart!!.invalidate()
            }
            R.id.actionToggleAutoScaleMinMax -> {
                chart!!.isAutoScaleMinMaxEnabled = !chart!!.isAutoScaleMinMaxEnabled
                chart!!.notifyDataSetChanged()
            }
            R.id.actionToggleBarBorders -> {
                for (set in chart!!.data.dataSets) (set as BarDataSet).barBorderWidth = if (set.getBarBorderWidth() == 1f) 0f else 1f
                chart!!.invalidate()
            }
            R.id.actionToggleHighlight -> {
                if (chart!!.data != null) {
                    chart!!.data.isHighlightEnabled = !chart!!.data.isHighlightEnabled
                    chart!!.invalidate()
                }
            }
            R.id.animateX -> {
                chart!!.animateX(2000)
            }
            R.id.animateY -> {
                chart!!.animateY(2000)
            }
            R.id.animateXY -> {
                chart!!.animateXY(2000, 2000)
            }
        }
        return true
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}
    override fun onStopTrackingTouch(seekBar: SeekBar) {}
    override fun onValueSelected(e: Entry, h: Highlight) {
        Log.i("Activity", "Selected: " + e.toString() + ", dataSet: " + h.dataSetIndex)
    }

    override fun onNothingSelected() {
        Log.i("Activity", "Nothing selected.")
    }
}