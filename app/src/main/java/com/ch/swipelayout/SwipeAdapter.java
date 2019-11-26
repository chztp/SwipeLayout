package com.ch.swipelayout;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

/**
 * Created by chztp
 */

public class SwipeAdapter extends RecyclerView.Adapter<SwipeAdapter.MyViewHolder> {

    private List<String> datas;
    private int item_recyclerview_swipe;


    public SwipeAdapter(int item_recylerview_swipe, List<String> datas) {
        this.datas = datas;
        this.item_recyclerview_swipe = item_recylerview_swipe;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(item_recyclerview_swipe, viewGroup,false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, final int position) {
        myViewHolder.text.setText(datas.get(position));
        myViewHolder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datas.remove(position);
                 notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView text;
        Button deleteBtn;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.content);
            deleteBtn = itemView.findViewById(R.id.btnDelete);
        }
    }
}
