package multico.in.btctrade.model;

import org.json.JSONObject;

/**
 * Created by Smmarat on 12.01.17.
 */

public class MyOrder {

    private String amnt_base, pub_date, price, sum2, sum1, amnt_trade, type, id;

    public MyOrder(JSONObject jo) {
        amnt_base = jo.optString("amnt_base");
        pub_date = jo.optString("pub_date");
        price = jo.optString("price");
        sum2 = jo.optString("sum2", null);
        sum1 = jo.optString("sum1", null);
        amnt_trade = jo.optString("amnt_trade");
        type = jo.optString("type");
        id = jo.optString("id");
    }

    public String getAmnt_base() {
        return amnt_base;
    }

    public String getPub_date() {
        return pub_date;
    }

    public String getPrice() {
        return price;
    }

    public String getSum2() {
        return sum2 == null ? amnt_trade : sum2;
    }

    public String getSum1() {
        return sum1 == null ? amnt_base : sum1;
    }

    public String getAmnt_trade() {
        return amnt_trade;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }
}
