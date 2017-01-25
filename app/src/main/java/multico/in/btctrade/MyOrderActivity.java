package multico.in.btctrade;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import multico.in.btctrade.model.MyOrder;
import multico.in.btctrade.model.MyOrderListAdapter;

public class MyOrderActivity extends AppCompatActivity {

    private Loader loader;
    private MyOrderListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_order);
        loader = Loader.getInstance(this);
        adapter = new MyOrderListAdapter(this);
        ((ListView)findViewById(R.id.my_order_list)).setAdapter(adapter);
        hideRefresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showProgress();
        hideRefresh();
        loader.ordersList(new Loader.OrderListListener() {
            @Override
            public void onSuccess(final List<MyOrder> orders) {
                hideProgress();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (orders.size() == 0) {
                            showMessage(getString(R.string.no_open_orders));
                            onBackPressed();
                        } else {
                            adapter.setOrders(orders);
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                showMessage(getString(R.string.err_loading_data));
                hideProgress();
                showRefresh();
            }
        });
    }

    public void deleteOrder(final String id) {
        showProgress();
        loader.orderCancel(id, new Loader.SimpleListener() {
            @Override
            public void onSuccess() {
                hideProgress();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.delOrder(id);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                hideProgress();
                e.printStackTrace();
                showMessage(e.getMessage());
            }
        });
    }

    private void showMessage(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MyOrderActivity.this, s, Toast.LENGTH_LONG).show();
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
                findViewById(R.id.mord_refresh).setVisibility(View.VISIBLE);
            }
        });
    }

    private void hideRefresh() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.mord_refresh).setVisibility(View.GONE);
            }
        });
    }

    public void doRefresh(View view) {
        onResume();
    }
}
