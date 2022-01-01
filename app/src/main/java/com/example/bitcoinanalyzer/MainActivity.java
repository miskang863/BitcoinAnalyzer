package com.example.bitcoinanalyzer;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;

public class MainActivity extends AppCompatActivity {
    private TextView dateTv;
    private TextView bearishTv;
    private TextView volumeTv;
    private TextView buySellTv;

    private RequestQueue mQueue;
    private MainActivityViewModel viewModel;
    BitcoinCalculations calculator = new BitcoinCalculations();


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        mQueue = Volley.newRequestQueue(this);

        CalendarView mCalendarView = findViewById(R.id.calendarView);
        dateTv = findViewById(R.id.dateTv);
        bearishTv = findViewById(R.id.bearishTv);
        volumeTv = findViewById(R.id.volumeTv);
        buySellTv = findViewById(R.id.profitTv);

        //If viewModel has data, update TextViews
        if (viewModel.getProfitString() != null) {
            dateTv.setText(getString(R.string.dateRange, viewModel.getEndDate(), viewModel.getStartDate()));
            bearishTv.setText(viewModel.getBearishString());
            volumeTv.setText(viewModel.getVolumeString());
            buySellTv.setText(viewModel.getProfitString());
        }

        //Here we handle user inputs, save dates user selects and make the API call
        mCalendarView.setOnDateChangeListener((calendarView, i, i1, i2) -> {

            String date = i2 + "." + (i1 + 1) + "." + i + " 00:00:00";

            //If user selects a date from future, show error Toast.
            if ((calculator.toUnixConverter(date) * 1000) > System.currentTimeMillis()) {
                showCustomToast(getString(R.string.dateError));
            }
            else if (!viewModel.isStartOrEndDateBoolean()) {
                viewModel.setStartDate(date);
                viewModel.setStartOrEndDateBoolean(true);
                showCustomToast(getString(R.string.startDate, viewModel.getStartDate()));
            } else {
                viewModel.setEndDate(date);
                viewModel.setStartOrEndDateBoolean(false);
                showCustomToast(getString(R.string.endDate, viewModel.getEndDate()));

                //If user selects endDate first, reverse the dates
                if (viewModel.getUnixStartDate() > viewModel.getUnixEndDate()) {
                    dateTv.setText(getString(R.string.dateRange, viewModel.getEndDate(), viewModel.getStartDate()));
                    getBitcoinData(viewModel.getUnixEndDate(), viewModel.getUnixStartDate());
                } else {
                    dateTv.setText(getString(R.string.dateRange, viewModel.getStartDate(), viewModel.getEndDate()));
                    getBitcoinData(viewModel.getUnixStartDate(), viewModel.getUnixEndDate());
                }
            }
        });
    }

    public void showCustomToast(String dateText) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, findViewById(R.id.custom_toast_layout));
        TextView toastTv = layout.findViewById(R.id.toastTxt);
        toastTv.setText(dateText);
        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    //dateChecker makes sure that you only get the first price/market_cap/total_volumes of the day. (Nearest to midnight)
    private boolean dateChecker(String date1, String date2) {
        return !date1.equals(date2);
    }

    //Fetch the data from CoinGecko API
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getBitcoinData(long startDate, long endDate) {
        Currency currency = Currency.getInstance("EUR");
        String url = "https://api.coingecko.com/api/v3/coins/bitcoin/market_chart/range?vs_currency=eur&from=" + startDate + "&to=" + endDate;

        ArrayList<BitcoinItem> priceList = new ArrayList<>();
        ArrayList<BitcoinItem> volumeList = new ArrayList<>();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                String dateCheckString = "";

                //Parse prices
                JSONArray jsonArrayPrices = response.getJSONArray("prices");
                for (int i = 0; i < jsonArrayPrices.length(); i++) {
                    JSONArray price = jsonArrayPrices.getJSONArray(i);
                    long date = (Long) price.get(0);
                    double value = (Double) price.get(1);

                    if (dateChecker(dateCheckString, calculator.toDateConverter(date))) {
                        priceList.add(new BitcoinItem(date, value));
                        dateCheckString = calculator.toDateConverter(date);
                    }
                }

                dateCheckString = "";

                //Parse total_volumes
                JSONArray jsonArrayTotalVolumes = response.getJSONArray("total_volumes");
                for (int i = 0; i < jsonArrayTotalVolumes.length(); i++) {
                    JSONArray volume = jsonArrayTotalVolumes.getJSONArray(i);
                    long date = (Long) volume.get(0);
                    double value = (Double) volume.get(1);

                    if (dateChecker(dateCheckString, calculator.toDateConverter(date))) {
                        volumeList.add(new BitcoinItem(date, value));
                        dateCheckString = calculator.toDateConverter(date);
                    }
                }

                //Execute calculations and update UI + viewModel
                //Bearish trend
                int bearishDateCount = calculator.longestBearish(priceList);
                viewModel.setBearishString(getString(R.string.bearish, bearishDateCount));
                bearishTv.setText(viewModel.getBearishString());

                //Maximum Volume
                BitcoinItem highestVolumeItem = calculator.highestVolume(volumeList);
                BigDecimal highestVolume = BigDecimal.valueOf(highestVolumeItem.getValue()).setScale(2, BigDecimal.ROUND_HALF_EVEN);
                viewModel.setVolumeString(getString(R.string.volume, calculator.toDateConverter(highestVolumeItem.getDate()), highestVolume, currency.getSymbol()));
                volumeTv.setText(viewModel.getVolumeString());

                //Best profit
                viewModel.setProfitString(calculator.bestProfit(priceList));
                buySellTv.setText(viewModel.getProfitString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, Throwable::printStackTrace);
        mQueue.add(request);
    }
}