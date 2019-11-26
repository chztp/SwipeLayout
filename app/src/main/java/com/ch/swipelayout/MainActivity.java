package com.ch.swipelayout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SwipeAdapter swipeAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recyclerView);
        List<String> datas = new ArrayList<>();
        datas.add("滑动删除1");
        datas.add("滑动删除2");
        datas.add("滑动删除3");
        datas.add("滑动删除4");
        swipeAdapter = new SwipeAdapter(R.layout.item_recylerview_swipe, datas);
        recyclerView.setAdapter(swipeAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
