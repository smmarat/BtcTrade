package multico.in.btctrade.model;

import org.json.JSONObject;

/**
 * Created by Smmarat on 11.01.17.
 */

public class Order {

    private String amtInCcy, amtInBaseCcy;
    private double price;

    public Order(JSONObject jo) {
        amtInCcy = jo.optString("currency_trade");
        amtInBaseCcy = jo.optString("currency_base");
        amtInBaseCcy = amtInBaseCcy.substring(0, amtInBaseCcy.indexOf(".") + 3);
        price = jo.optDouble("price");
    }

    public String getAmtInCcy() {
        return amtInCcy;
    }

    public String getAmtInBaseCcy() {
        return amtInBaseCcy;
    }

    public double getPrice() {
        return price;
    }
}
