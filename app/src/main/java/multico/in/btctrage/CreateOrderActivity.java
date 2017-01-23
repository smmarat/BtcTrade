package multico.in.btctrage;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;

public class CreateOrderActivity extends AppCompatActivity {

    public static final String EXTRA_OPER = "OPER";
    public static final String EXTRA_PRICE = "PRICE";
    public static final String OPER_BUY = "buy";
    public static final String OPER_SELL = "sell";
    private String currOper;
    private EditText orderAmt;
    private EditText orderPrice;
    private TextView calcRez;
    private Loader loader;
    private float currPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loader = Loader.getInstance(this);
        setContentView(R.layout.activity_create_order);
        orderAmt = (EditText) findViewById(R.id.ord_amt);
        orderPrice = (EditText) findViewById(R.id.ord_price);
        calcRez = (TextView) findViewById(R.id.ord_receive);
        currPrice = getIntent().getFloatExtra(EXTRA_PRICE, Loader.getCurrPrice());
        if (!getIntent().hasExtra(EXTRA_OPER)) {
            finish();
        } else {
            currOper = getIntent().getStringExtra(EXTRA_OPER);
        }
        if (OPER_BUY.equals(currOper)) {
            ((TextView)findViewById(R.id.ord_title)).setText(R.string.buy_bitcoins);
            ((TextView)findViewById(R.id.ord_ccy)).setText("BTC");
            ((TextView)findViewById(R.id.ord_rcv_ccy)).setText("UAH");
            ((Button)findViewById(R.id.ord_do)).setText(R.string.buy_bitcoins);
            orderPrice.setText(String.valueOf(currPrice));
            orderAmt.setHint("0.001");
            findViewById(R.id.ord_amt).requestFocus();
        } else {
            ((TextView)findViewById(R.id.ord_title)).setText(R.string.sell_bitcoins);
            ((TextView)findViewById(R.id.ord_ccy)).setText("UAH");
            ((TextView)findViewById(R.id.ord_rcv_ccy)).setText("BTC");
            ((Button)findViewById(R.id.ord_do)).setText(R.string.sell_bitcoins);
            orderPrice.setText(String.valueOf(currPrice));
            orderAmt.setHint("20000");
            findViewById(R.id.ord_amt).requestFocus();
        }
    }

    public void calculate(View view) {
        try {
            double dAmt = Double.parseDouble(orderAmt.getText().toString());
            try {
                double dPrice = Double.parseDouble(orderPrice.getText().toString());
                double rez = OPER_BUY.equals(currOper) ? dAmt * dPrice : dAmt / dPrice;
                calcRez.setText(new BigDecimal(rez).setScale(6, BigDecimal.ROUND_HALF_DOWN).toPlainString());
            } catch (Exception e) {
                showMessage("Invalid price");
            }
//            loader.recalc(s, Oper.buy == currOper, new Loader.ReacalcListener() {
//                @Override
//                public void onSuccess(final String amt) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            calcRez.setText(amt);
//                        }
//                    });
//                }
//
//                @Override
//                public void onError(Exception e) {
//                    e.printStackTrace();
//                    showMessage(e.getMessage());
//                }
//            });
        } catch (Exception e) {
            showMessage("Invalid amount");
        }
    }

    public void placeOrder(View view) {
        try {
            double dAmt = Double.parseDouble(orderAmt.getText().toString());
            try {
                double dPrice = Double.parseDouble(orderPrice.getText().toString());
                String val = OPER_BUY.equals(currOper)
                        ? orderAmt.getText().toString()
                        : new BigDecimal(dAmt / dPrice).setScale(6, BigDecimal.ROUND_HALF_DOWN).toPlainString();
                loader.orderCreate(val, orderPrice.getText().toString(), OPER_BUY.equals(currOper), new Loader.CreateOrderListener() {
                    @Override
                    public void onSuccess() {
                        showMessage("Order created successfully");
                        finish();
                    }

                    @Override
                    public void onProcessed() {
                        showMessage("Your order has been fully processed successfully");
                        finish();
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                        showMessage(e.getMessage());
                    }
                });

            } catch (Exception e) {
                showMessage("Invalid price");
            }
        } catch (Exception e) {
            showMessage("Invalid amount");
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
}
