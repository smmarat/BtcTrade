package multico.in.btctrage;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import multico.in.btctrage.model.Order;
import multico.in.btctrage.model.OrderListAdapter;

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
