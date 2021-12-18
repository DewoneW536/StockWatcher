package com.example.stockwatcher;

import java.io.Serializable;
import java.util.Objects;

public class Stock implements Serializable, Comparable<Stock> {
    private String stockSymbol;
    private String companyName;
    private double price;
    private double priceChange;
    private double changePercentage;

    public Stock(String stockSymbol,String companyName,double price,double priceChange,
                 double changePercentage){
        this.stockSymbol = stockSymbol;
        this.companyName = companyName;
        this.price = price;
        this.priceChange = priceChange;
        this.changePercentage = changePercentage;
    }

    public String getStockSymbol(){return stockSymbol;}

    public String getCompanyName(){return companyName;}

    public double getPrice(){return price;}

    public double getPriceChange(){return priceChange;}

    public double getChangePercentage(){return changePercentage;}

    @Override
    public boolean equals(Object o){
        if(this == o)return true;
        if(o == null || getClass() != o.getClass())return false;
        Stock stock = (Stock)o;
        return stockSymbol.equals(stock.stockSymbol)&&companyName.equals(stock.companyName);
    }

    @Override
    public int hashCode(){return Objects.hash(stockSymbol,companyName);}

    @Override
    public int compareTo(Stock stock) {
        return stockSymbol.compareTo(stock.getStockSymbol());}
}
