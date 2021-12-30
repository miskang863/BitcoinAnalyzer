package com.example.bitcoinanalyzer;

import androidx.lifecycle.ViewModel;

public class MainActivityViewModel extends ViewModel {
    private boolean startOrEndDateBoolean = false;
    private String startDate;
    private String endDate;

    private String bearishString;
    private String volumeString;
    private String profitString;

    private final BitcoinCalculations calculator = new BitcoinCalculations();

    public boolean isStartOrEndDateBoolean() {
        return startOrEndDateBoolean;
    }

    public void setStartOrEndDateBoolean(boolean startOrEndDateBoolean) {
        this.startOrEndDateBoolean = startOrEndDateBoolean;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getBearishString() {
        return bearishString;
    }

    public void setBearishString(String bearishString) {
        this.bearishString = bearishString;
    }

    public String getVolumeString() {
        return volumeString;
    }

    public void setVolumeString(String volumeString) {
        this.volumeString = volumeString;
    }

    public String getProfitString() {
        return profitString;
    }

    public void setProfitString(String profitString) {
        this.profitString = profitString;
    }

    public long getUnixStartDate() {
        return calculator.toUnixConverter(startDate);
    }

    public long getUnixEndDate() {
        return calculator.toUnixConverter(endDate);
    }
}
