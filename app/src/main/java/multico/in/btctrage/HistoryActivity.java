package multico.in.btctrage;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import multico.in.btctrage.model.MyOrder;
import multico.in.btctrage.model.MyOrderListAdapter;

public class HistoryActivity extends AppCompatActivity {

    private MyOrderListAdapter mola;
    private Loader loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        loader = Loader.getInstance(this);
        mola = new MyOrderListAdapter(this);
        ((ListView)findViewById(R.id.h_list)).setAdapter(mola);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showProgress();
        hideRefresh();
        loader.history(new Loader.OrderListListener() {
            @Override
            public void onSuccess(final List<MyOrder> orders) {
                hideProgress();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (orders.size() == 0) {
                            showMessage(getString(R.string.no_history));
                            onBackPressed();
                        } else {
                            mola.setOrders(orders);
                        }

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
                Toast.makeText(HistoryActivity.this, s, Toast.LENGTH_LONG).show();
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
                findViewById(R.id.h_refresh).setVisibility(View.VISIBLE);
            }
        });
    }

    private void hideRefresh() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.h_refresh).setVisibility(View.GONE);
            }
        });
    }

    public void doRefresh(View view) {
        onResume();
    }
}
