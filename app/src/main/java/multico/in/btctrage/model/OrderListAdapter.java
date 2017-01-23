package multico.in.btctrage.model;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import multico.in.btctrage.CreateOrderActivity;
import multico.in.btctrage.R;

/**
 * Created by Smmarat on 11.01.17.
 */

public class OrderListAdapter extends BaseAdapter {

    private Context ctx;
    private boolean sale;
    private List<Order> orders = new ArrayList<>();

    public OrderListAdapter(Context ctx, boolean isSale) {
        this.ctx = ctx;
        this.sale = isSale;
    }

    public void setOrders(List<Order> orders) {
//        if (sale) {
//            Collections.reverse(orders);
//        }
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
        final Order order = orders.get(position);
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(ctx).inflate(R.layout.order_list_item, null);
            holder = new ViewHolder();
            holder.price = (TextView) convertView.findViewById(R.id.oli_price);
            holder.amt = (TextView) convertView.findViewById(R.id.oli_amt);
            holder.eqv = (TextView) convertView.findViewById(R.id.oli_amt_base);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.price.setText(String.valueOf(order.getPrice()));
        holder.price.setTextColor(sale ? Color.RED : ctx.getResources().getColor(R.color.m_green));
        holder.amt.setText(order.getAmtInCcy());
        holder.eqv.setText(order.getAmtInBaseCcy());
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ctx.startActivity(new Intent(ctx, CreateOrderActivity.class)
                        .putExtra(CreateOrderActivity.EXTRA_OPER, sale ? CreateOrderActivity.OPER_SELL : CreateOrderActivity.OPER_BUY)
                        .putExtra(CreateOrderActivity.EXTRA_PRICE, (float) order.getPrice()));
            }
        });
        return convertView;

    }

    private class ViewHolder {
        TextView price, amt, eqv;
    }
}
