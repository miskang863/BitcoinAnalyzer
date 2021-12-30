package com.example.bitcoinanalyzer;

public class BitcoinItem implements Comparable<BitcoinItem> {
    private final Long date;
    private final Double value;

    public BitcoinItem(Long date, Double value) {
        this.date = date;
        this.value = value;
    }

    public Long getDate() {
        return date;
    }

    public Double getValue() {
        return value;
    }

    @Override
    public int compareTo(BitcoinItem item) {
        if (this.getValue() > item.getValue()) {
            return 1;
        } else if (this.getValue() < item.getValue()) {
            return -1;
        }
        return 0;
    }
}
