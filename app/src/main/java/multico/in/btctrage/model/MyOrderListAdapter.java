package multico.in.btctrage.model;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.multico.tool.Tool;
import multico.in.btctrage.MyOrderActivity;
import multico.in.btctrage.R;

/**
 * Created by Smmarat on 11.01.17.
 */

public class MyOrderListAdapter extends BaseAdapter {

    private Activity ctx;
    private List<MyOrder> orders = new ArrayList<>();

    public MyOrderListAdapter(Activity ctx) {
        this.ctx = ctx;
    }

    public void setOrders(List<MyOrder> orders) {
        this.orders = orders;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return orders.size();
    }

    @Override
    public Object getItem(int i) {
        return orders.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        final MyOrder order = orders.get(position);
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(ctx).inflate(R.layout.my_order_list_item, null);
            holder = new ViewHolder();
            holder.price = (TextView) convertView.findViewById(R.id.moli_price);
            holder.amt1 = (TextView) convertView.findViewById(R.id.moli_amt1);
            holder.amt2 = (TextView) convertView.findViewById(R.id.moli_amt2);
            holder.time = (TextView) convertView.findViewById(R.id.moli_time);
            holder.type = (ImageView) convertView.findViewById(R.id.moli_type);
            holder.delete = (ImageView) convertView.findViewById(R.id.moli_del);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.price.setText(String.valueOf(Tool.cutAmt(order.getPrice(), 2)));
        holder.price.setTextColor("sell".equals(order.getType()) ? Color.RED : ctx.getResources().getColor(R.color.m_green));
        holder.type.setImageResource("sell".equals(order.getType()) ? android.R.drawable.arrow_down_float : android.R.drawable.arrow_up_float);
        holder.time.setText(order.getPub_date());
        holder.amt1.setText("sell".equals(order.getType()) ? Tool.cutAmt(order.getSum1(), 6) + " BTC" : Tool.cutAmt(order.getSum1(), 3) + " UAH");
        holder.amt2.setText("buy".equals(order.getType()) ? Tool.cutAmt(order.getSum2(), 6) + " BTC" : Tool.cutAmt(order.getSum2(), 3) + " UAH");
        if (ctx instanceof MyOrderActivity) {
            holder.delete.setVisibility(View.VISIBLE);
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((MyOrderActivity)ctx).deleteOrder(order.getId());
                }
            });
        } else {
            holder.delete.setVisibility(View.GONE);
        }
        return convertView;

    }

    public void delOrder(String id) {
        List<MyOrder> nOrders = new ArrayList<>();
        for (MyOrder o : orders) {
            if (id.equals(o.getId())) continue;
            nOrders.add(o);
        }
        orders = nOrders;
        notifyDataSetChanged();
    }

    private class ViewHolder {
        TextView amt1, amt2, price, time;
        ImageView type, delete;
    }
}
