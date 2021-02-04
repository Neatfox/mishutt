package com.neatfox.mishutt.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

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

public class EMICalculatorActivity extends MainActivity {

    EditText loan_amount,interest_rate,month,year,total_payment,total_interest,emi;
    PieChart chart;
    ArrayList<PieEntry> entries = new ArrayList<>();
    ArrayList<Integer> colors = new ArrayList<>();
    int backPress = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_emi_calculator, contentFrameLayout);

        EMICalculatorActivity.this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        tv_toolbar.setText(R.string.EMI_calculator);

        loan_amount = findViewById(R.id.et_loan_amount);
        total_payment = findViewById(R.id.et_total_payment);
        interest_rate = findViewById(R.id.et_interest_rate);
        total_interest = findViewById(R.id.et_total_interest);
        month = findViewById(R.id.et_month);
        year = findViewById(R.id.et_year);
        emi = findViewById(R.id.et_monthly_instalment);
        chart = findViewById(R.id.chart);

        year.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "35")});
        month.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "11")});

        loan_amount.setText(R.string.p);
        interest_rate.setText(R.string.r);
        year.setText(R.string.t);

        Legend legend = chart.getLegend();
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP); //top
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setTextColor(Color.parseColor("#FF000000"));
        legend.setTextSize(10f);

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

        loan_amount.addTextChangedListener(new TextWatcher() {
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
                        loan_amount.getText().toString().length() > 0) &&
                        (year.getText().toString().length() > 0 ||
                                month.getText().toString().length() > 0))
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
                if (s.toString().length() > 0 &&
                        loan_amount.getText().toString().length() > 0 &&
                        interest_rate.getText().toString().length() > 0)
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
                if (s.toString().length() > 0 &&
                        loan_amount.getText().toString().length() > 0 &&
                        interest_rate.getText().toString().length() > 0)
                    calculate();
            }
        });

        calculate();
    }

    private void calculate(){
        float principle, rate, time, f_year, f_month, m_emi;

        principle = Float.parseFloat(loan_amount.getText().toString().trim());
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
        time = (f_year * 12) + f_month;
        rate = rate / (12 * 100); // one month interest
        m_emi = (float) ((principle * rate * Math.pow(1 + rate, time)) / (Math.pow(1 + rate, time) - 1));

        String _emi,_total_interest,_total_payment;
        _emi = String.valueOf(m_emi);
        _total_interest = String.valueOf((m_emi * time)-principle);
        _total_payment = String.valueOf(m_emi * time);
        emi.setText(addComma(_emi));
        total_interest.setText(addComma(_total_interest));
        total_payment.setText(addComma(_total_payment));
        createChart(principle,(m_emi * time)-principle);
    }

    private void createChart(Float loan_amount, Float total_interest){
        entries = new ArrayList<>();

        entries.add(new PieEntry(loan_amount,"Loan Amount"));
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
        Intent intent = new Intent (EMICalculatorActivity.this, HomeActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        EMICalculatorActivity.this.startActivity(intent);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
