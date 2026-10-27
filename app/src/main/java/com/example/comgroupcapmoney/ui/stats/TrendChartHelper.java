package com.example.comgroupcapmoney.ui.stats;

import android.content.Context;
import androidx.core.content.ContextCompat;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.model.TrendPoint;
import com.example.comgroupcapmoney.utils.CurrencyUtils;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import java.util.ArrayList;
import java.util.List;

// Bieu do cot Chi/Thu 6 thang o Tab Thong ke (4C-1), tach khoi StatsFragment cho gon
public final class TrendChartHelper {

    private static final float GROUP_SPACE = 0.2f;
    private static final float BAR_SPACE = 0.05f;
    private static final float BAR_WIDTH = 0.35f; // (0.35 + 0.05) * 2 + 0.2 = 1 nhom moi thang

    private TrendChartHelper() {}

    public static void setup(BarChart chart, Context ctx) {
        int textSec = ContextCompat.getColor(ctx, R.color.text_secondary);
        int grid = ContextCompat.getColor(ctx, R.color.bg_card_secondary);

        chart.getDescription().setEnabled(false);
        chart.setNoDataText("");
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setScaleEnabled(false);
        chart.setDrawGridBackground(false);
        chart.getAxisRight().setEnabled(false);

        Legend legend = chart.getLegend();
        legend.setTextColor(textSec);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setForm(Legend.LegendForm.CIRCLE);

        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setTextColor(textSec);
        x.setDrawGridLines(false);
        x.setGranularity(1f);
        x.setCenterAxisLabels(true);

        YAxis y = chart.getAxisLeft();
        y.setTextColor(textSec);
        y.setGridColor(grid);
        y.setAxisMinimum(0f);
        y.setDrawAxisLine(false);
        y.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value <= 0 ? "0" : CurrencyUtils.formatShort(value);
            }
        });
    }

    // Nhan truc X: "T5", "T6"...; thang 1 ghi kem nam ("T1/27") de biet da qua nam
    static String label(TrendPoint p) {
        String l = "T" + p.getMonth();
        if (p.getMonth() == 1) {
            l += "/" + String.format(java.util.Locale.US, "%02d", p.getYear() % 100);
        }
        return l;
    }

    public static void bind(BarChart chart, List<TrendPoint> points, Context ctx) {
        List<BarEntry> expense = new ArrayList<>();
        List<BarEntry> income = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            TrendPoint p = points.get(i);
            expense.add(new BarEntry(i, (float) p.getTotalExpense(), p));
            income.add(new BarEntry(i, (float) p.getTotalIncome(), p));
            labels.add(label(p));
        }

        BarDataSet dsExpense = new BarDataSet(expense, ctx.getString(R.string.trend_legend_expense));
        dsExpense.setColor(ContextCompat.getColor(ctx, R.color.color_expense));
        dsExpense.setDrawValues(false);
        BarDataSet dsIncome = new BarDataSet(income, ctx.getString(R.string.trend_legend_income));
        dsIncome.setColor(ContextCompat.getColor(ctx, R.color.color_income));
        dsIncome.setDrawValues(false);

        BarData data = new BarData(dsExpense, dsIncome);
        data.setBarWidth(BAR_WIDTH);
        chart.setData(data);

        XAxis x = chart.getXAxis();
        // Nhan canh giua nhom cot nam o i + 0.5: lay phan nguyen (IndexAxisValueFormatter lam tron nen lech 1 thang)
        x.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int i = (int) Math.floor(value);
                return i >= 0 && i < labels.size() ? labels.get(i) : "";
            }
        });
        x.setAxisMinimum(0f);
        x.setAxisMaximum(points.size());
        x.setLabelCount(points.size());

        chart.groupBars(0f, GROUP_SPACE, BAR_SPACE);
        chart.highlightValues(null);
        chart.animateY(500);
        chart.invalidate();
    }
}
