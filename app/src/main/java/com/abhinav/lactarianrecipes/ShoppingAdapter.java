package com.abhinav.lactarianrecipes;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class ShoppingAdapter extends RecyclerView.Adapter<ShoppingAdapter.MyViewHolder> {

    private List<ItemShopping> items;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name;

        public MyViewHolder(View view) {
            super(view);

            name = (TextView) view.findViewById(R.id.tv_name);
        }
    }


    public ShoppingAdapter(List<ItemShopping> items) {
        this.items = items;
    }

    @Override
    public ShoppingAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shopping, parent, false);

        return new ShoppingAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ShoppingAdapter.MyViewHolder holder, int position) {
        ItemShopping itemShopping = items.get(position);
        holder.name.setText(itemShopping.getName());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public ItemShopping getItem(int position) {
        return items.get(position);
    }
}