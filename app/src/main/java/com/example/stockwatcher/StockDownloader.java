package com.example.stockwatcher;

import android.net.Uri;
import android.util.Log;

import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class StockDownloader implements Runnable{

    private static final String TAG = "StockFinancialDownload";
    private static final String FINANCES_URL_FIRST = "https://cloud.iexapis.com/stable/stock/";
    private static final String FINANCES_URL_SECOND = "/quote?token=pk_8f8afc939b1442b6ab081531aaa68574";

    private MainActivity mainAct;
    private String searchTarget;

    public StockDownloader(MainActivity mainActivity,String searchTarget){
        this.mainAct = mainActivity;
        this.searchTarget = searchTarget;
    }

    @Override
    public void run() {
        Uri.Builder uriBuilder = Uri.parse(FINANCES_URL_FIRST + searchTarget + FINANCES_URL_SECOND).buildUpon();
        //uriBuilder.appendQueryParameter("fullText","true");
        String urlToUse = uriBuilder.toString();
        
        Log.d(TAG,"run: "+urlToUse);
        StringBuilder sb = new StringBuilder();
        try{
            URL url = new URL(urlToUse);
            
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            
            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                Log.d(TAG,"run: HTTP ResponseCode NOT OK: " + connection.getResponseCode());
                return;
            }
            InputStream is = connection.getInputStream();
            BufferedReader bReader = new BufferedReader((new InputStreamReader(is)));
            
            String sLine;
            while((sLine = bReader.readLine()) != null){
                sb.append(sLine).append('\n');
            }
            Log.d(TAG,"run: " + sb.toString());
        }
        catch(Exception e){
            e.printStackTrace();
            return;
        }
        process(sb.toString());
        Log.d(TAG,"run: ");
    }

    private void process(String s) {
        try{
            JSONArray jArray = new JSONArray();
            //JSONObject jStock = (JSONObject)jArray.get(0);
            JSONObject jStock = new JSONObject(s);
            jArray.put(jStock);

            String symbol = jStock.getString("symbol");
            String companyName = jStock.getString("companyName");

            String latestPrice = jStock.getString("latestPrice");
            double updatedPrice = 0.0;
            if(!latestPrice.trim().isEmpty() && !latestPrice.trim().equals("null")){
                updatedPrice = Double.parseDouble(latestPrice);
            }

            String change = jStock.getString("change");
            double priceChange = 0.0;
            if(!change.trim().isEmpty() && !change.trim().equals("null")){
                priceChange = Double.parseDouble(change);
            }

            String changePercent = jStock.getString("changePercent");
            double percentChange = 0.0;
            if(!changePercent.trim().isEmpty() && !changePercent.trim().equals("null")){
                percentChange = Double.parseDouble(changePercent);
            }
            final Stock stock = new Stock(symbol,companyName,updatedPrice,priceChange,percentChange);

            mainAct.runOnUiThread(new Runnable() {
                @Override
                public void run() { mainAct.addStock(stock); }
            });


        }
        catch(Exception e){
            e.printStackTrace();
            return;
        }
    }
}
