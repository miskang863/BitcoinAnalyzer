package com.example.bitcoinanalyzer;

import android.app.Application;
import android.content.res.Resources;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

//Extends Application so we can access our strings.xml resources
public class BitcoinCalculations extends Application {
    private static Resources res;

    @Override
    public void onCreate() {
        super.onCreate();
        res = getResources();
    }

    public static Resources getRes() {
        return res;
    }

    //A. How many days is the longest bearish (downward) trend within a given date range?
    public int longestBearish(ArrayList<BitcoinItem> priceList) {
        ArrayList<Integer> bearishList = new ArrayList<>();
        int bearishCount = 0;

        for (int i = 1; i < priceList.size(); i++) {
            //If last index and value decreasing, increase bearishCount and add to bearishList, then reset bearishCount
            if (i + 1 == priceList.size() && priceList.get(i).getValue() < priceList.get(i - 1).getValue()) {
                bearishCount++;
                bearishList.add(bearishCount);
                bearishCount = 0;
            }
            //If last index and bearishCount is over 0, add to bearishList
            else if (i + 1 == priceList.size() && bearishCount > 0) {
                bearishList.add(bearishCount);
            }
            //If current value is lower than last, increase bearishCount
            else if (priceList.get(i).getValue() < priceList.get(i - 1).getValue()) {
                bearishCount++;
            }
            //If value is not decreasing, add bearishCount to bearishList and reset bearishCount to 0
            else if (bearishCount > 0) {
                bearishList.add(bearishCount);
                bearishCount = 0;
            }
        }

        //If there is no bearish trend, return 0
        if (bearishList.isEmpty()) {
            return 0;
        }

        //Return the highest int from bearishList
        return Collections.max(bearishList);
    }

    //B. Which date within a given date range had the highest trading volume?
    public BitcoinItem highestVolume(ArrayList<BitcoinItem> totalVolumeList) {
        return Collections.max(totalVolumeList);
    }

    //C. Find out best dates to buy and sell bitcoin for maximum profit
    @RequiresApi(api = Build.VERSION_CODES.O)
    public String bestProfit(ArrayList<BitcoinItem> priceList) {
        Currency currency = Currency.getInstance("EUR");
        final DecimalFormat df = new DecimalFormat("0.00");
        double minPrice = Double.MAX_VALUE;
        double maxProfit = 0.0;

        String buyDate = "";
        String sellDate = "";

        for (int i = 0; i < priceList.size(); i++) {
            if (priceList.get(i).getValue() < minPrice) {
                minPrice = priceList.get(i).getValue();
            } else if ((priceList.get(i).getValue() - minPrice) > maxProfit) {
                maxProfit = priceList.get(i).getValue() - minPrice;

                //Fetch the right BitcoinItem for buyDate
                for (BitcoinItem bitcoinItem : priceList) {
                    if (bitcoinItem.getValue() == minPrice) {
                        buyDate = toDateConverter(bitcoinItem.getDate());
                    }
                }
                sellDate = toDateConverter(priceList.get(i).getDate());
            }
        }

        //If there is no profit to be made
        if (maxProfit == 0.0) {
            return BitcoinCalculations.getRes().getString(R.string.noProfit);
        }
        return BitcoinCalculations.getRes().getString(R.string.profit, buyDate, sellDate, df.format(maxProfit), currency.getSymbol());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String toDateConverter(long timestamp) {
        Timestamp ts = new Timestamp(timestamp);
        SimpleDateFormat format = new SimpleDateFormat(BitcoinCalculations.getRes().getString(R.string.dateFormat), Locale.getDefault());
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format.format(ts);
    }

    public long toUnixConverter(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat(BitcoinCalculations.getRes().getString(R.string.toUnixdateFormat), Locale.getDefault());
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date date = null;

        try {
            date = format.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        assert date != null;
        long timestamp = date.getTime();
        return timestamp / 1000;
    }
}
