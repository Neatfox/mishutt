package com.neatfox.mishutt.ui.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatCheckBox;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.neatfox.mishutt.R;
import com.neatfox.mishutt.ui.model.InputFilterMinMax;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class FDCalculatorActivity extends MainActivity {

    EditText fd_amount,interest_rate,year,month,maturity_value,total_interest;
    AppCompatCheckBox monthly,quarterly,half_yearly,annually;
    PieChart chart;
    ArrayList<PieEntry> entries = new ArrayList<>();
    ArrayList<Integer> colors = new ArrayList<>();
    int backPress = 0,n = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_fd_calculator, contentFrameLayout);

        FDCalculatorActivity.this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        tv_toolbar.setText(R.string.FD_calculator);

        fd_amount = findViewById(R.id.et_fixed_deposit_amount);
        maturity_value = findViewById(R.id.et_maturity_value);
        interest_rate = findViewById(R.id.et_interest_rate);
        total_interest = findViewById(R.id.et_total_interest);
        month = findViewById(R.id.et_month);
        year = findViewById(R.id.et_year);
        monthly = findViewById(R.id.check_monthly);
        quarterly = findViewById(R.id.check_quarterly);
        half_yearly = findViewById(R.id.check_half_yearly);
        annually = findViewById(R.id.check_annually);
        chart = findViewById(R.id.chart);

        year.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "15")});
        month.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "11")});

        fd_amount.setText(R.string.p);
        interest_rate.setText(R.string.r);
        year.setText(R.string.t);

        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        Legend legend = chart.getLegend();
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP); //top
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setTextSize(10f);

        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            legend.setTextColor(Color.parseColor("#FFFFFFFF"));
            chart.setHoleColor(Color.parseColor("#FF000000"));
        } else {
            legend.setTextColor(Color.parseColor("#FF000000"));
            chart.setHoleColor(Color.parseColor("#FFFFFFFF"));
        }

        chart.setDrawHoleEnabled(true);
        chart.getDescription().setEnabled(false);
        chart.animate();
        chart.setEntryLabelColor(Color.BLACK);
        chart.setEntryLabelTextSize(12f);
        chart.setExtraOffsets(25, 0, 25, 0);

        for (int c : ColorTemplate.MATERIAL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        annually.setChecked(true);

        monthly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    n = 12;
                    monthly.setChecked(true);
                    quarterly.setChecked(false);
                    half_yearly.setChecked(false);
                    annually.setChecked(false);
                    calculate();
                }
            }
        });

        quarterly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    n = 4;
                    monthly.setChecked(false);
                    quarterly.setChecked(true);
                    half_yearly.setChecked(false);
                    annually.setChecked(false);
                    calculate();
                }
            }
        });

        half_yearly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    n = 2;
                    monthly.setChecked(false);
                    quarterly.setChecked(false);
                    half_yearly.setChecked(true);
                    annually.setChecked(false);
                    calculate();
                }
            }
        });

        annually.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    n = 1;
                    monthly.setChecked(false);
                    quarterly.setChecked(false);
                    half_yearly.setChecked(false);
                    annually.setChecked(true);
                    calculate();
                }
            }
        });

        fd_amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if ((s.toString().length() > 0 &&
                        interest_rate.getText().toString().length() > 0) &&
                        (year.getText().toString().length() > 0 ||
                                month.getText().toString().length() > 0))
                    calculate();
            }
        });

        interest_rate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if ((s.toString().length() > 0 &&
                        fd_amount.getText().toString().length() > 0) &&
                        (year.getText().toString().length() > 0 ||
                                month.getText().toString().length() > 0))
                    calculate();
            }
        });

        month.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (fd_amount.getText().toString().length() > 0 &&
                        interest_rate.getText().toString().length() > 0)
                    calculate();
            }
        });

        year.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (fd_amount.getText().toString().length() > 0 &&
                        interest_rate.getText().toString().length() > 0)
                    calculate();
            }
        });

        calculate();
    }

    private void calculate(){
        float principle, rate, f_year, f_month;

        principle = Float.parseFloat(fd_amount.getText().toString().trim());
        rate = Float.parseFloat(interest_rate.getText().toString().trim());
        if (month.getText().toString().trim().equalsIgnoreCase("0") ||
                month.getText().toString().trim().length()<1)
            f_month = 0;
        else
            f_month = Float.parseFloat(month.getText().toString().trim());

        if (year.getText().toString().trim().equalsIgnoreCase("0") ||
                year.getText().toString().trim().length()<1)
            f_year = 0;
        else
            f_year = Float.parseFloat(year.getText().toString().trim());

        float CI = (float) (principle * (Math.pow(1 + (rate / n)/100, (f_year+(f_month/12))*n)));

        String _maturity_value, _total_interest;
        _maturity_value = String.valueOf(CI);
        _total_interest = String.valueOf(CI - principle);
        maturity_value.setText(addComma(_maturity_value));
        total_interest.setText(addComma(_total_interest));
        createChart(principle,CI-principle);
    }

    private void createChart(Float total_investment, Float total_interest){
        entries = new ArrayList<>();

        entries.add(new PieEntry(total_investment,"Investment"));
        entries.add(new PieEntry(total_interest,"Interest"));

        PieDataSet dataSet = new PieDataSet(entries, " ");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(2f);
        dataSet.setValueTextSize(12f);
        PieData data = new PieData(dataSet);
        chart.setData(data);
        chart.highlightValues(null);
        chart.invalidate();
    }

    /*..........................................Add Comma.........................................*/
    private String addComma(String s){
        double amount = Double.parseDouble(s);
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(amount);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void onBackPressed() {
        backPress++;
        if (backPress == 1) {
            backPressed();
        } else {
            finishAffinity();
        }
    }

    public void backPressed(){
        Intent intent = new Intent (FDCalculatorActivity.this, HomeActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        FDCalculatorActivity.this.startActivity(intent);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
