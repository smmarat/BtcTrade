package multico.in.btctrade.model;

import org.json.JSONObject;

/**
 * Created by Smmarat on 12.01.17.
 */

public class MyOrder {

    private String amnt_base, pub_date, price, amnt_trade, type, id;

    public MyOrder(JSONObject jo) {
        amnt_base = jo.optString("amnt_base");
        pub_date = jo.optString("pub_date");
        price = jo.optString("price");
        amnt_trade = jo.optString("amnt_trade");
        amnt_base = jo.optString("amnt_base");
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
