package multico.in.btctrade;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import multico.in.btctrade.model.Order;
import multico.in.btctrade.model.OrderListAdapter;

public class OrdersActivity extends AppCompatActivity {

    OrderListAdapter ola_buy, ola_sale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        ola_buy = new OrderListAdapter(this, false);
        ((ListView)findViewById(R.id.ord_buy)).setAdapter(ola_buy);
        ola_sale = new OrderListAdapter(this, true);
        ((ListView)findViewById(R.id.ord_sale)).setAdapter(ola_sale);
        hideRefresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showProgress();
        hideRefresh();
        Loader.loadOrders(this, new Loader.OrderDataListener() {
            @Override
            public void onBuyLoaded(final List<Order> buy) {
                hideProgress();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ola_buy.setOrders(buy);
                    }
                });
            }

            @Override
            public void onSellLoaded(final List<Order> sell) {
                hideProgress();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ola_sale.setOrders(sell);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                showMessage(getResources().getString(R.string.err_loading_data));
                hideProgress();
                showRefresh();
            }
        });
    }

    private void showMessage(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(OrdersActivity.this, s, Toast.LENGTH_LONG).show();
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
                findViewById(R.id.progress).setVisibility(View.INVISIBLE);
            }
        });
    }

    private void showRefresh() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.ord_refresh).setVisibility(View.VISIBLE);
            }
        });
    }

    private void hideRefresh() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.ord_refresh).setVisibility(View.GONE);
            }
        });
    }

    public void doRefresh(View view) {
        onResume();
    }
}
