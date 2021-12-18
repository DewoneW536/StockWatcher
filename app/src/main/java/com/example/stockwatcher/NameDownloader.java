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

public class NameDownloader implements Runnable{
    private static final String TAG = "StockNameDownloader";
    private static final String SYMBOL_CNAME_URL = "https://api.iextrading.com/1.0/ref-data/symbols";
    public static HashMap<String,String> namesAndSymbolsMap = new HashMap<>();

    @Override
    public void run() {
        Uri dataUri = Uri.parse(SYMBOL_CNAME_URL);

        String urlForUse = dataUri.toString();
        Log.d(TAG,"run: " + urlForUse);

        StringBuilder sb = new StringBuilder();
        try{
            URL url = new URL(urlForUse);

            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                Log.d(TAG, "run: HTTP ResponseCode NOT OK: " + connection.getResponseCode());
                return;
            }
            InputStream is = connection.getInputStream();
            BufferedReader bReader = new BufferedReader((new InputStreamReader(is)));

            String sLine;
            while((sLine = bReader.readLine()) != null){
                sb.append(sLine).append('\n');
            }
            Log.d(TAG,"run: " + sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG,"run: ",e);
            return;
        }
        process(sb.toString());
        Log.d(TAG,"run: ");
    }

    private void process(String s) {
        try{
            JSONArray jObjMain = new JSONArray(s);

            for(int i = 0; i <jObjMain.length();i++){
                JSONObject jStock = (JSONObject)jObjMain.get(i);

                String names = jStock.getString("name");
                String symbols = jStock.getString("symbol");

                namesAndSymbolsMap.put(names,symbols);
            }
            Log.d(TAG,"process: ");
        }
        catch (Exception e){
            e.printStackTrace();
            return;
        }
    }
    public static ArrayList<String> findMatches(String str){
        String strToMatch = str.toLowerCase().trim();
        HashSet<String> matchHSet = new HashSet<>();

        for(String sym: namesAndSymbolsMap.keySet()){
            if (sym.toLowerCase().trim().contains(strToMatch)) {
                matchHSet.add(sym + " : " + namesAndSymbolsMap.get(sym));
            }
            String name = namesAndSymbolsMap.get(sym);
            if(name != null && name.toLowerCase().trim().contains(strToMatch)){
                matchHSet.add(sym + " : " + name);
            }
        }
        ArrayList<String> results = new ArrayList<>(matchHSet);
        Collections.sort(results);

        return results;
    }
}