package multico.in.btctrade;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;

import java.text.DecimalFormat;
import java.util.List;

import in.multico.tool.Tool;
import multico.in.btctrade.model.Order;
import multico.in.btctrade.model.OrderListSimpleAdapter;

public class MainActivity extends AppCompatActivity {

    private CombinedChart mChart;
    private float maxPrice, minPrice, currPrice;
    private Loader loader;
    private String balUah, balBtc;
    private OrderListSimpleAdapter ola_buy, ola_sale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ola_buy = new OrderListSimpleAdapter(this, false);
        ((ListView)findViewById(R.id.order_list_buy)).setAdapter(ola_buy);
        ola_sale = new OrderListSimpleAdapter(this, true);
        ((ListView)findViewById(R.id.order_list_sell)).setAdapter(ola_sale);
        mChart = (CombinedChart) findViewById(R.id.chart);
        mChart.setBackgroundColor(Color.WHITE);
        mChart.setDrawOrder(new CombinedChart.DrawOrder[]{
                CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.CANDLE
        });
        mChart.setDrawGridBackground(false);
        mChart.setDrawValueAboveBar(false);

        mChart.setDescription("");

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setSpaceBetweenLabels(2);
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setDrawLabels(true);
        leftAxis.setValueFormatter(new YAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, YAxis yAxis) {
                return new DecimalFormat("#.##").format(value);
            }
        });
        leftAxis.setLabelCount(10, true);
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawAxisLine(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setAxisMinValue(0);
        rightAxis.setEnabled(false);
        loader = Loader.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Tool.getPub(this).length() == 0 || Tool.getSecret(this).length() == 0) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else {
            refresh(true);
        }
    }

    public void refresh(final boolean animate) {
        showProgress();

        Loader.loadChart(this, new Loader.ChartDataListener() {
            @Override
            public void onSuccess(List<String> xVals, CandleData prices, BarData volumes, float yMax, float yMin, float yCurr) {
                hideProgress();
                maxPrice = yMax;
                minPrice = yMin;
                currPrice = yCurr;
                final CombinedData data = new CombinedData(xVals);
                data.setData(prices);
                data.setData(volumes);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mChart.setData(data);
                        mChart.invalidate();
                        if (animate) mChart.animateX(400);
                        mChart.setVisibleXRangeMaximum(40);
                        mChart.moveViewToX(8000);
                    }
                });
                loadBalance();
            }

            @Override
            public void onError(Exception e) {
                hideProgress();
                e.printStackTrace();
                showMessage(getString(R.string.err_loading_data));
            }
        });
    }

    private void loadBalance() {
        showProgress();
        loader.getBalance(new Loader.BalanceListener() {
            @Override
            public void onSuccess(final String uah, final String btc) {
                balUah = uah;
                balBtc = btc;
                hideProgress();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView)findViewById(R.id.bal_btc)).setText(btc + " BTC");
                        ((TextView)findViewById(R.id.bal_uah)).setText(uah + " UAH");
                    }
                });
                loadOrders();
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                hideProgress();
            }
        });
    }

    private void loadOrders() {
        showProgress();
        Loader.loadOrders(new Loader.OrderDataListener() {
            @Override
            public void onSuccess(final List<Order> buy, final List<Order> sale) {
                hideProgress();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ola_buy.setOrders(buy);
                        ola_sale.setOrders(sale);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                showMessage(getResources().getString(R.string.err_loading_data));
                hideProgress();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_refresh) {
            refresh(true);
            return true;
        }
        else if (id == R.id.menu_order) {
            startActivity(new Intent(this, MyOrderActivity.class));
        }
        else if (id == R.id.menu_history) {
            startActivity(new Intent(this, HistoryActivity.class));
        }
        else if (id == R.id.menu_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void showMessage(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.progress).setVisibility(View.VISIBLE);
            }
        });
    }

    private void hideProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.progress).setVisibility(View.GONE);
            }
        });
    }

    public void doBuy(View view) {
        startActivity(new Intent(this, CreateOrderActivity.class)
                .putExtra(CreateOrderActivity.EXTRA_OPER, CreateOrderActivity.OPER_BUY)
                .putExtra(CreateOrderActivity.EXTRA_PRICE, currPrice));
    }

    public void doSell(View view) {
        startActivity(new Intent(this, CreateOrderActivity.class)
                .putExtra(CreateOrderActivity.EXTRA_OPER, CreateOrderActivity.OPER_SELL)
                .putExtra(CreateOrderActivity.EXTRA_PRICE, currPrice));
    }
}
