package multico.in.btctrade;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;

public class CreateOrderActivity extends AppCompatActivity {

    public static final String EXTRA_OPER = "OPER";
    public static final String EXTRA_PRICE = "PRICE";
    public static final String EXTRA_AMT = "AMT";
    public static final String OPER_BUY = "buy";
    public static final String OPER_SELL = "sell";
    private String currOper;
    private EditText orderAmt;
    private EditText orderPrice;
    private TextView calcRez;
    private TextView avaCcy;
    private Loader loader;
    private float currPrice;
    private String propAmt;
    private Button placeOrder;
    private double ava;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loader = Loader.getInstance(this);
        setContentView(R.layout.activity_create_order);
        orderAmt = (EditText) findViewById(R.id.ord_amt);
        orderPrice = (EditText) findViewById(R.id.ord_price);
        calcRez = (TextView) findViewById(R.id.ord_eqv);
        avaCcy = (TextView) findViewById(R.id.ord_ava_ccy);
        placeOrder = (Button) findViewById(R.id.ord_do);
        currPrice = getIntent().getFloatExtra(EXTRA_PRICE, Loader.getCurrPrice());
        propAmt = getIntent().hasExtra(EXTRA_AMT) ? getIntent().getStringExtra(EXTRA_AMT) : "";
        if (!getIntent().hasExtra(EXTRA_OPER)) {
            finish();
        } else {
            currOper = getIntent().getStringExtra(EXTRA_OPER);
        }
        if (OPER_BUY.equals(currOper)) {
            ((TextView)findViewById(R.id.ord_title)).setText(R.string.buy_bitcoins);
            avaCcy.setText("UAH");
            placeOrder.setText(R.string.buy_bitcoins);
            orderPrice.setText(String.valueOf(currPrice));
            findViewById(R.id.ord_amt).requestFocus();
        } else {
            ((TextView)findViewById(R.id.ord_title)).setText(R.string.sell_bitcoins);
            avaCcy.setText("BTC");
            placeOrder.setText(R.string.sell_bitcoins);
            orderPrice.setText(String.valueOf(currPrice));
            findViewById(R.id.ord_amt).requestFocus();
        }
        orderAmt.setText(propAmt);
        recalc();
        orderAmt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                recalc();
            }
        });
    }

    private void recalc() {
        try {
            String as = orderAmt.getText().toString();
            if (as.isEmpty()) return;
            double dAmt = Double.parseDouble(as);
            try {
                double dPrice = Double.parseDouble(orderPrice.getText().toString());
                double rez = dAmt * dPrice;
                calcRez.setText(new BigDecimal(rez).setScale(6, BigDecimal.ROUND_HALF_DOWN).toPlainString());
                placeOrder.setEnabled(OPER_BUY.equals(currOper) ? ava >= rez : ava >= dAmt);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMessage(getString(R.string.err_amt));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showProgress();
        loader.getBalance(new Loader.BalanceListener() {
            @Override
            public void onSuccess(final String uah, final String btc) {
                hideProgress();
                ava = OPER_BUY.equals(currOper) ? Double.parseDouble(uah) : Double.parseDouble(btc);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView)findViewById(R.id.ord_ava)).setText(OPER_BUY.equals(currOper) ? uah : btc);
                        recalc();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                hideProgress();
            }
        });
    }

    public void placeOrder(View view) {
        try {
            Double.parseDouble(orderAmt.getText().toString());
            try {
                Double.parseDouble(orderPrice.getText().toString());
                String val = orderAmt.getText().toString();
                showProgress();
                loader.orderCreate(val, orderPrice.getText().toString(), OPER_BUY.equals(currOper), new Loader.CreateOrderListener() {
                    @Override
                    public void onSuccess() {
                        hideProgress();
                        showMessage(getString(R.string.order_created));
                        finish();
                    }

                    @Override
                    public void onProcessed() {
                        hideProgress();
                        showMessage(getString(R.string.order_processed));
                        finish();
                    }

                    @Override
                    public void onError(Exception e) {
                        hideProgress();
                        e.printStackTrace();
                        showMessage(e.getMessage());
                    }
                });

            } catch (Exception e) {
                showMessage(getString(R.string.err_price));
            }
        } catch (Exception e) {
            showMessage(getString(R.string.err_amt));
        }
    }

    private void showMessage(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CreateOrderActivity.this, s, Toast.LENGTH_LONG).show();
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
}
