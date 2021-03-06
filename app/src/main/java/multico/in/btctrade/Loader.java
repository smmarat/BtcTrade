package multico.in.btctrade;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import in.multico.tool.Tool;
import multico.in.btctrade.model.MyOrder;
import multico.in.btctrade.model.Order;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Smmarat on 10.01.17.
 */

public class Loader {

    private static final String PREF_CHART_CASHE = "chart_cashe";
    private static final String PREF_BALANCE_CASHE = "balance_cashe";
    private static final String PREF_MY_ORDERS_CASHE = "my_orders_cashe";
    private static final String PREF_ORDERS_BUY_CASHE = "orders_buy_cashe";
    private static final String PREF_ORDERS_SELL_CASHE = "orders_sell_cashe";
    private static final String PREF_HISTORY_CASHE = "orders_cashe";
    private static final String PREF_CASHE_STATE = "chart_cashe_state";
    private static final int RETRY_COUNT = 5;
    private static final long RETRY_TIMEOUT = 1 * 1000;

    private static Loader instance;
    private static float currPrice;
    private final Object monitor = new Object();
    private boolean ready = false;
    private Context ctx;
    private static final String TAG = Loader.class.getSimpleName();

    private Loader(final Context ctx) {
        this.ctx = ctx;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Tool.post(ctx, "https://btc-trade.com.ua/api/auth?is_mobile=1", new HashMap<String, String>());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                synchronized (monitor) {
                    ready = true;
                    monitor.notifyAll();
                }
            }
        }).start();
        synchronized (monitor) {
            while (!ready) {
                try {
                    monitor.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void checkCashe(final int count, final Context ctx, final CasheChangeListener ccl) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String s = Tool.post(ctx, "https://btc-trade.com.ua/time?is_mobile=1", new HashMap<String, String>());
                    JSONObject jo = new JSONObject(s);
                    if (!jo.has("state")) {
                        ccl.onSuccess(true);
                        return;
                    }
                    String state = jo.optString("state");
                    final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
                    if (pref.getString(PREF_CASHE_STATE, "dtvhrt7kpasdfq1lr5").equals(state)) {
                        ccl.onSuccess(false);
                    } else {
                        pref.edit().putString(PREF_CASHE_STATE, state).apply();
                        ccl.onSuccess(true);
                    }
                } catch (Exception e) {
                    if (count > RETRY_COUNT) ccl.onError(e);
                    else {
                        SystemClock.sleep(RETRY_TIMEOUT);
                        Log.v(TAG, "Retry checkCashe " + count);
                        checkCashe(count + 1, ctx, ccl);
                    }
                }
            }
        }).start();
    }

    public static Loader getInstance(Context ctx) {
        if (instance == null) instance = new Loader(ctx);
        return instance;
    }

    public static void clearCashe(Context ctx) {
        PreferenceManager.getDefaultSharedPreferences(ctx).edit().remove(PREF_CASHE_STATE).apply();
    }

    public void getBalance(final BalanceListener bl) {
        getBalance(0, bl);
    }

    private void getBalance(final int count, final BalanceListener bl) {
        checkCashe(0, ctx, new CasheChangeListener() {
            @Override
            public void onSuccess(boolean changed) {
                final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
                if (changed) {
                    try {
                        String s = Tool.post(ctx, "https://btc-trade.com.ua/api/balance?is_mobile=1", new HashMap<String, String>());
                        processBalanceResp(s, bl);
                        pref.edit().putString(PREF_BALANCE_CASHE, s).apply();
                    } catch (Exception e) {
                        if (count > RETRY_COUNT) bl.onError(e);
                        else {
                            try {
                                Tool.post(ctx, "https://btc-trade.com.ua/api/auth?is_mobile=1", new HashMap<String, String>());
                            } catch (Exception e2) {
                                e2.printStackTrace();
                            }
                            Log.v(TAG, "Retry getBalance " + count);
                            getBalance(count + 1, bl);
                        }
                    }
                } else {
                    try {
                        processBalanceResp(pref.getString(PREF_BALANCE_CASHE, ""), bl);
                    } catch (JSONException e) {
                        bl.onError(e);
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                bl.onError(e);
            }
        });
    }

    private void processBalanceResp(String s, BalanceListener bl) throws JSONException {
        JSONArray acc = new JSONObject(s).getJSONArray("accounts");
        String uah = "0.00", btc = "0.00000";
        for (int i = 0; i < acc.length(); i++) {
            JSONObject a = acc.getJSONObject(i);
            if ("UAH".equals(a.optString("currency"))) uah = a.getString("balance");
            if ("BTC".equals(a.optString("currency"))) btc = a.getString("balance");
        }
        uah = uah.substring(0, uah.indexOf(".") + 3);
        bl.onSuccess(uah, btc);
    }

    public static float getCurrPrice() {
        return currPrice;
    }

    public void orderCreate(final String count, final String price, final boolean buy, final CreateOrderListener col) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, String> m = new HashMap<>();
                m.put("count", count);
                m.put("price", price);
                m.put("currency1", "UAH");
                m.put("currency", "BTC");
                try {
                    String s = Tool.post(ctx, "https://btc-trade.com.ua/api/" + (buy ? "buy" : "sell") + "/btc_uah?is_mobile=1", m);
                    JSONObject jo = new JSONObject(s);
                    if (jo.optBoolean("status")) {
                        col.onSuccess();
                    } else if ("processed".equals(jo.optString("status"))) {
                        col.onProcessed();
                    }
                    else {
                        throw new Exception(jo.optString("description", "Order is not created"));
                    }
                } catch (Exception e) {
                    col.onError(e);
                }
            }
        }).start();
    }

    public void ordersList(final OrderListListener oll) {
        ordersList(0, oll);
    }

    private void ordersList(final int count, final OrderListListener oll) {
        checkCashe(0, ctx, new CasheChangeListener() {
            @Override
            public void onSuccess(boolean changed) {
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
                if (changed) {
                    try {
                        String s = Tool.post(ctx, "https://btc-trade.com.ua/api/my_orders/btc_uah?is_mobile=1", new HashMap<String, String>());
                        processMyOrderResp(s, oll);
                        pref.edit().putString(PREF_MY_ORDERS_CASHE, s).apply();
                    } catch (Exception e) {
                        if (count > RETRY_COUNT) oll.onError(e);
                        else {
                            SystemClock.sleep(RETRY_TIMEOUT);
                            Log.v(TAG, "Retry ordersList " + count);
                            ordersList(count + 1, oll);
                        }
                    }
                } else {
                    try {
                        processMyOrderResp(pref.getString(PREF_MY_ORDERS_CASHE, ""), oll);
                    } catch (JSONException e) {
                        oll.onError(e);
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                oll.onError(e);
            }
        });
    }

    private void processMyOrderResp(String s, OrderListListener oll) throws JSONException {
        JSONObject jo = new JSONObject(s);
        List<MyOrder> orders = new ArrayList<>();
        JSONArray arr = jo.getJSONArray("your_open_orders");
        for (int i = 0; i < arr.length(); i++) {
            orders.add(new MyOrder(arr.getJSONObject(i)));
        }
        oll.onSuccess(orders);
    }

    public void history(final OrderListListener oll) {
        history(0, oll);
    }

    private void history(final int count, final OrderListListener oll) {
        checkCashe(0, ctx, new CasheChangeListener() {
            @Override
            public void onSuccess(boolean changed) {
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
                if (changed) {
                    try {
                        String s = Tool.post(ctx, "https://btc-trade.com.ua/api/my_deals/btc_uah?is_mobile=1", new HashMap<String, String>());
                        processHistoryResp(s, oll);
                        pref.edit().putString(PREF_HISTORY_CASHE, s).apply();
                    } catch (Exception e) {
                        if (count > RETRY_COUNT) oll.onError(e);
                        else {
                            SystemClock.sleep(RETRY_TIMEOUT);
                            Log.v(TAG, "Retry history " + count);
                            ordersList(count + 1, oll);
                        }
                    }
                } else {
                    try {
                        processHistoryResp(pref.getString(PREF_HISTORY_CASHE, ""), oll);
                    } catch (JSONException e) {
                        oll.onError(e);
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                oll.onError(e);
            }
        });
    }

    private void processHistoryResp(String s, OrderListListener oll) throws JSONException {
        List<MyOrder> orders = new ArrayList<>();
        JSONArray arr = new JSONArray(s);
        for (int i = 0; i < arr.length(); i++) {
            orders.add(new MyOrder(arr.getJSONObject(i)));
        }
        oll.onSuccess(orders);
    }


    public void orderCancel(final String id, final SimpleListener sl) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, String> m = new HashMap<>();
                try {
                    String s = Tool.post(ctx, "https://btc-trade.com.ua/api/remove/order/" + id + "?is_mobile=1", m);
                    JSONObject jo = new JSONObject(s);
                    if (jo.optBoolean("status")) {
                        sl.onSuccess();
                    }
                    else {
                        throw new Exception("Order is not cancelled");
                    }
                } catch (Exception e) {
                    sl.onError(e);
                }
            }
        }).start();
    }

    public void recalc(final String amt, final boolean buy, final ReacalcListener rl) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, String> m = new HashMap<>();
                m.put("amount", amt);
                try {
                    String s = Tool.post(ctx, "https://btc-trade.com.ua/api/" + (buy ? "ask" : "bid") + "/btc_uah?is_mobile=1", m);
                    JSONObject jo = new JSONObject(s);
                    rl.onSuccess(jo.optString("amount2pay", "error"));
                } catch (Exception e) {
                    rl.onError(e);
                }
            }
        }).start();
    }

    public static void loadOrders(Context ctx, final OrderDataListener odl) {
        loadOrders(0, ctx, odl);
    }

    private static void loadOrders(final int count, final Context ctx, final OrderDataListener odl) {
        checkCashe(0, ctx, new CasheChangeListener() {
            @Override
            public void onSuccess(boolean changed) {
                final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
                if (changed) {
                    String url1 = "https://btc-trade.com.ua/api/trades/sell/btc_uah?is_mobile=1";
                    Log.v(TAG, "~~~> " + url1);
                    new OkHttpClient().newCall(new Request.Builder().url(url1).build()).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            if (count > RETRY_COUNT) {
                                odl.onError(e);
                            } else {
                                SystemClock.sleep(RETRY_TIMEOUT);
                                Log.v(TAG, "Retry loadOrders " + count);
                                loadOrders(count + 1, ctx, odl);
                            }
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String s = response.body().string();
                            Log.v(TAG, "<~~~ " + (s.startsWith("{") ? s : "NO JSON"));
                            try {
                                processOrdersResp(s, true, odl);
                                pref.edit().putString(PREF_ORDERS_SELL_CASHE, s).apply();
                                String url2 = "https://btc-trade.com.ua/api/trades/buy/btc_uah?is_mobile=1";
                                Log.v(TAG, "~~~> " + url2);
                                new OkHttpClient().newCall(new Request.Builder().url(url2).build()).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        if (count > RETRY_COUNT) {
                                            odl.onError(e);
                                        } else {
                                            SystemClock.sleep(RETRY_TIMEOUT);
                                            Log.v(TAG, "Retry loadOrders " + count);
                                            loadOrders(count + 1, ctx, odl);
                                        }
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        String s = response.body().string();
                                        Log.v(TAG, "<~~~ " + (s.startsWith("{") ? s : "NO JSON"));
                                        try {
                                            processOrdersResp(s, false, odl);
                                            pref.edit().putString(PREF_ORDERS_BUY_CASHE, s).apply();
                                        } catch (JSONException e) {
                                            if (count > RETRY_COUNT) {
                                                odl.onError(e);
                                            } else {
                                                SystemClock.sleep(RETRY_TIMEOUT);
                                                Log.v(TAG, "Retry loadOrders " + count);
                                                loadOrders(count + 1, ctx, odl);
                                            }
                                        }
                                    }
                                });
                            } catch (JSONException e) {
                                if (count > RETRY_COUNT) {
                                    odl.onError(e);
                                } else {
                                    SystemClock.sleep(RETRY_TIMEOUT);
                                    Log.v(TAG, "Retry loadOrders " + count);
                                    loadOrders(count + 1, ctx, odl);
                                }
                            }
                        }
                    });
                } else {
                    try {
                        processOrdersResp(pref.getString(PREF_ORDERS_SELL_CASHE, ""), true, odl);
                        processOrdersResp(pref.getString(PREF_ORDERS_BUY_CASHE, ""), false, odl);
                    } catch (JSONException e) {
                        odl.onError(e);
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                odl.onError(e);
            }
        });
    }

    private static void processOrdersResp(String s, boolean sell, OrderDataListener odl) throws JSONException {
        final List<Order> orders = new ArrayList<>();
        JSONArray list = new JSONObject(s).getJSONArray("list");
        for (int i = 0; i < list.length(); i++) {
            orders.add(new Order(list.getJSONObject(i)));
        }
        if (sell) {
            odl.onSellLoaded(orders);
        } else {
            odl.onBuyLoaded(orders);
        }
    }

    public static void loadChart(final Context ctx, final ChartDataListener cdl) {
        loadChart(0, ctx, cdl);
    }

    private static void loadChart(final int count, final Context ctx, final ChartDataListener cdl) {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        checkCashe(0, ctx, new CasheChangeListener() {
            @Override
            public void onSuccess(boolean changed) {
                if (changed) {
                    String url = "https://btc-trade.com.ua/api/japan_stat/high/btc_uah?is_mobile=1";
                    Log.v(TAG, "~~~> " + url);
                    new OkHttpClient().newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            cdl.onError(e);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String s = response.body().string();
                            Log.v(TAG, "<~~~ " + (s.startsWith("{") ? s : "NO JSON"));
                            try {
                                new JSONObject(s).getJSONArray("trades");
                                pref.edit().putString(PREF_CHART_CASHE, s).apply();
                                processChartResp(ctx, s, cdl);
                            } catch (JSONException e) {
                                if (count > RETRY_COUNT) {
                                    cdl.onError(e);
                                } else {
                                    SystemClock.sleep(RETRY_TIMEOUT);
                                    Log.v(TAG, "Retry loadChart " + count);
                                    loadChart(count + 1, ctx, cdl);
                                }
                            }
                        }
                    });
                } else {
                    processChartResp(ctx, pref.getString(PREF_CHART_CASHE, ""), cdl);
                }
            }

            @Override
            public void onError(Exception e) {
                cdl.onError(e);
            }
        });
    }

    private static void processChartResp(Context ctx, String s, ChartDataListener cdl) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault());
        try {
            JSONArray arr = new JSONObject(s).getJSONArray("trades");
            ArrayList<CandleEntry> yVals = new ArrayList<CandleEntry>();
            ArrayList<BarEntry> yVals2 = new ArrayList<>();
            ArrayList<String> xVals = new ArrayList<String>();
            float yMax = 0, yMin = Float.MAX_VALUE, close = 0;
            JSONArray arr2 = new JSONArray();
            int size = 200;
            for(int i = arr.length() - size; i < arr.length(); i++) {
                arr2.put(arr.getJSONArray(i));
            }
            for (int i = 0; i < arr2.length(); i++) {
                JSONArray jo = arr2.getJSONArray(i);
                close = (float) jo.optDouble(4);
                if (yMax < close) yMax = close;
                if (yMin > close) yMin = close;
                yVals.add(new CandleEntry(
                        i
                        , (float) jo.optDouble(2)
                        , (float) jo.optDouble(3)
                        , (float) jo.optDouble(1)
                        , close
                        , ""
                ));
                yVals2.add(new BarEntry((float) jo.optDouble(5), i));
                xVals.add(sdf.format(new Date(jo.getLong(0))));
            }
            CandleDataSet set = new CandleDataSet(yVals, "Price");
            set.setAxisDependency(YAxis.AxisDependency.LEFT);
            set.setShadowColor(Color.DKGRAY);
            set.setShadowWidth(0.7f);
            set.setDrawValues(false);
            set.setDecreasingColor(Color.RED);
            set.setDecreasingPaintStyle(Paint.Style.FILL);
            set.setIncreasingColor(ctx.getResources().getColor(R.color.m_green));
            set.setIncreasingPaintStyle(Paint.Style.FILL);
            set.setNeutralColor(Color.BLUE);
            CandleData prices = new CandleData(xVals, set);

            BarDataSet set2 = new BarDataSet(yVals2, "Volume");
            set2.setAxisDependency(YAxis.AxisDependency.RIGHT);
            set2.setDrawValues(false);
            set2.setColor(Color.argb(200, 210, 210, 210));
            BarData volumes = new BarData(xVals, set2);
            currPrice = close;
            cdl.onSuccess(xVals, prices, volumes, yMax, yMin, close);
        } catch (Exception e) {
            cdl.onError(e);
        }
    }

    public interface CasheChangeListener extends BaseListener {
        void onSuccess(boolean changed);
    }

    public interface OrderListListener extends BaseListener {
        void onSuccess(List<MyOrder> orders);
    }

    public interface SimpleListener extends BaseListener {
        void onSuccess();
    }

    public interface CreateOrderListener extends BaseListener {
        void onSuccess();
        void onProcessed();
    }

    public interface ReacalcListener extends BaseListener {
        void onSuccess(String amt);
    }

    public interface OrderDataListener extends BaseListener {
        void onBuyLoaded(List<Order> buy);
        void onSellLoaded(List<Order> sell);
    }

    public interface BalanceListener extends BaseListener {
        void onSuccess(String uah, String btc);
    }

    public interface ChartDataListener extends BaseListener{
        void onSuccess(List<String> xVals, CandleData price, BarData volume, float yMax, float yMin, float yCurr);
    }

    public interface BaseListener {
        void onError(Exception e);
    }
}
