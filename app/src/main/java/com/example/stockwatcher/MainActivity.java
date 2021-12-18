package com.example.stockwatcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
    View.OnLongClickListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "MainActivity";
    private final List<Stock> stockList = new ArrayList<>();
    private RecyclerView recyclerView;
    private StockViewAdapter sAdapter;
    private SwipeRefreshLayout swiper;
    private String choice;
    private final List<String> tempList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view);
        sAdapter = new StockViewAdapter(stockList,this);
        recyclerView.setAdapter(sAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if(checkConnection()){
            NameDownloader rd = new NameDownloader();
            new Thread(rd).start();
            readJSONData(true);
            for(String symbol : tempList){
                StockDownloader sd = new StockDownloader(this,symbol);
                new Thread(sd).start();
            }
            tempList.clear();
            Collections.sort(stockList);
            sAdapter.notifyDataSetChanged();
        }
        else{
            readJSONData(false);
            displayNoNetworkConnection("Device cannot connect to network!");
        }
        //SwipeRefreshLayout stuff
        swiper = findViewById(R.id.refreshLayout);
        swiper.setOnRefreshListener(this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_options_menu,menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.id.menu_add_Stock){
            createStockDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createStockDialog() {
        if(!checkConnection()){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Network Connection!");
            builder.setMessage("New Stocks Cannot Be Added Without A Network Connection");
            AlertDialog dialog = builder.create();
            dialog.show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setGravity(Gravity.CENTER_HORIZONTAL);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

        builder.setView(editText);

        builder.setPositiveButton("OK", (dialog, id) -> {
            choice = editText.getText().toString().trim();

            final ArrayList<String> results = NameDownloader.findMatches(choice);
            if(results.size() == 0){
                doNoAnswer(choice);
            }
            else if(results.size() == 1){
                doSelection(results.get(0));
            }
            else{
                String[] sArray = results.toArray(new String[0]);

                AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                builder1.setTitle("Please Make a Selection");
                builder1.setItems(sArray, (dialog1, which) -> {
                    String sym = results.get(which);
                    doSelection(sym);
                });
                builder1.setNegativeButton("Cancel", (dialog12, which) -> {
                    //User cancelled the dialog
                });
                AlertDialog dialog2 = builder1.create();
                dialog2.show();
            }

        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {

        });
        builder.setMessage("Please Enter a Symbol or Company Name: ");
        builder.setTitle("Stock Selection");

        AlertDialog dialog = builder.create();
        dialog.show();

    }
    private boolean checkConnection(){
        ConnectivityManager cm =(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onClick(View v) {
        int pos = recyclerView.getChildLayoutPosition(v);
        Stock s = stockList.get(pos);
        String urlS = "https://www.marketwatch.com/investing/stock/" + s.getStockSymbol();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(urlS));
        startActivity(intent);
    }

    @Override
    public boolean onLongClick(View v) {
        final int pos = recyclerView.getChildLayoutPosition(v);
        Stock s = stockList.get(pos);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.remove_circle_24px);
        builder.setPositiveButton("Delete", (dialog, which) -> {
            stockList.remove(pos);
            writeJSONData();
            sAdapter.notifyDataSetChanged();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            //User cancelled dialog
        });
        builder.setMessage("Delete " + stockList.get(pos).getCompanyName() + "?");
        builder.setTitle("Delete Selection");

        AlertDialog dialog = builder.create();
        dialog.show();
        return true;
    }
    @Override
    public void onPause(){
        super.onPause();
        writeJSONData();
    }

    private void writeJSONData() {
        try{
            FileOutputStream fileOS = getApplicationContext().
                    openFileOutput(getString(R.string.app_name),Context.MODE_PRIVATE);
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(fileOS, StandardCharsets.UTF_8));
            writer.setIndent("  ");
            writer.beginArray();
            for(Stock s: stockList){
                writer.beginObject();

                writer.name("symbol").value(s.getStockSymbol());
                writer.name("companyName").value(s.getCompanyName());
                writer.name("latestPrice").value(s.getPrice());
                writer.name("change").value(s.getPriceChange());
                writer.name("changePercent").value(s.getChangePercentage());

                writer.endObject();
            }
            writer.endArray();
            writer.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    private void readJSONData(boolean hasConnection){
        if(hasConnection){
            try{
                FileInputStream fileIS = getApplicationContext().openFileInput(getString(R.string.app_name));
                byte[] data = new byte[fileIS.available()];
                int load = fileIS.read(data);
                Log.d(TAG, "readJSONData: Loaded "+load+" bytes");
                fileIS.close();
                String json = new String(data);

                JSONArray stockArr = new JSONArray(json);
                for(int i=0;i<stockArr.length();i++){
                    JSONObject sObj = stockArr.getJSONObject(i);

                    String symbol = sObj.getString("symbol");
                    String companyName = sObj.getString("companyName");
                    double latestPrice = sObj.getDouble("latestPrice");
                    double priceChange = sObj.getDouble("change");
                    double percentChange = sObj.getDouble("changePercent");

                    Stock s = new Stock(symbol,companyName,latestPrice,priceChange,percentChange);
                    stockList.add(s);
                }
                sAdapter.notifyDataSetChanged();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onRefresh() {
        if(!checkConnection()){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Network Connection!");
            builder.setMessage("New Stocks Cannot Be Added Without A Network Connection");
            AlertDialog dialog = builder.create();
            dialog.show();
            return;
        }
        stockList.clear();
        readJSONData(true);
        for(String symbol : tempList){
            StockDownloader sd = new StockDownloader(this,symbol);
            new Thread(sd).start();
        }
        tempList.clear();
        Collections.sort(stockList);
        sAdapter.notifyDataSetChanged();
        swiper.setRefreshing(false);
    }

    public void addStock(Stock stock) {
        if(stock == null){
            badDataAlert(choice);
            return;
        }
        if(stockList.contains(stock)){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(stock.getCompanyName() + "is already shown in list.");
            builder.setTitle("Duplicate Stock");
            builder.setIcon(R.drawable.warning_24px);

            AlertDialog dialog = builder.create();
            dialog.show();
            return;
        }
        stockList.add(stock);
        Collections.sort(stockList);
        writeJSONData();
        sAdapter.notifyDataSetChanged();
    }

    private void badDataAlert(String sym) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("No Data for Selection!");
        builder.setTitle("Symbol Not Found: "+sym);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void doNoAnswer(String symbol){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("No data has been found for specified symbol/name");
        builder.setTitle("No Data Found: "+symbol);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void displayNoNetworkConnection(String s){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(s);
        builder.setTitle("Could Not Establish Network Connection!");
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void doSelection(String symbol){
        String[] data = symbol.split(" : ");
        StockDownloader stockDownloader = new StockDownloader(this,data[1].trim());
        new Thread(stockDownloader).start();
    }
}