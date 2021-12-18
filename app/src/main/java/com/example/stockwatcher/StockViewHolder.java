package com.example.stockwatcher;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

class StockViewHolder extends RecyclerView.ViewHolder {
    TextView stockSymbol;
    TextView companyName;
    TextView stockPrice;
    TextView priceChange;
    TextView changePercentage;
    ImageView redGreenUpDown;

    StockViewHolder(View view){
        super(view);
        stockSymbol = view.findViewById(R.id.stock_symbol);
        companyName = view.findViewById(R.id.company_name);
        stockPrice = view.findViewById(R.id.stock_price);
        priceChange = view.findViewById(R.id.price_change);
        changePercentage = view.findViewById(R.id.change_percentage);
        redGreenUpDown = view.findViewById(R.id.imageView);
    }
}
