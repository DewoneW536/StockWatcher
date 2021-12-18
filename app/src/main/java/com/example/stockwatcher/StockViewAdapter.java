package com.example.stockwatcher;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StockViewAdapter extends RecyclerView.Adapter<StockViewHolder> {
    private static final String TAG = "StockAdapter";
    private List<Stock> stockList;
    private MainActivity mainAct;

    StockViewAdapter(List<Stock> nList, MainActivity main){
        this.stockList = nList;
        mainAct = main;
    }
    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: MAKING NEW");
        View itemV = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_entry,parent,false);
        itemV.setOnClickListener(mainAct);
        itemV.setOnLongClickListener(mainAct);
        return new StockViewHolder(itemV);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        Stock stock = stockList.get(position);

        String strStockPrice = String.valueOf(stock.getPrice());
        String strPriceChange = String.valueOf(stock.getPriceChange());
        String strChangePer = String.valueOf(stock.getChangePercentage());

        holder.stockSymbol.setText(stock.getStockSymbol());
        holder.companyName.setText(stock.getCompanyName());
        holder.stockPrice.setText(strStockPrice.substring(0,strStockPrice.indexOf(".")+2));
        holder.priceChange.setText(strPriceChange.substring(0,strPriceChange.indexOf(".")+2));
        holder.changePercentage.setText("(" + (strChangePer.substring(0,strChangePer.indexOf(".")+2)) + "%)");

        if(stock.getPriceChange() >= 0){
            holder.stockSymbol.setTextColor(Color.GREEN);
            holder.companyName.setTextColor(Color.GREEN);
            holder.stockPrice.setTextColor(Color.GREEN);
            holder.priceChange.setTextColor(Color.GREEN);
            holder.changePercentage.setTextColor(Color.GREEN);
            holder.redGreenUpDown.setImageResource(R.drawable.up_arrow_green);
        }
        else{
            holder.stockSymbol.setTextColor(Color.RED);
            holder.companyName.setTextColor(Color.RED);
            holder.stockPrice.setTextColor(Color.RED);
            holder.priceChange.setTextColor(Color.RED);
            holder.changePercentage.setTextColor(Color.RED);
            holder.redGreenUpDown.setImageResource(R.drawable.red_arrow_down);
        }
    }

    @Override
    public int getItemCount() { return stockList.size(); }
}
