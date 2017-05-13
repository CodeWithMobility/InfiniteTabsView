package com.mobiledev.infinitetabdemo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mobiledev.infinitetabs.InfiniteTabAdapter;
import com.mobiledev.infinitetabs.InfiniteTabView;
import com.mobiledev.infinitetabs.TabLayoutManager;

import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity {
    private InfiniteTabView infiniteTabView;
    private TextView horizontalItem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        infiniteTabView = (InfiniteTabView) findViewById(R.id.infiniteTabs);
        horizontalItem = (TextView)findViewById(R.id.positionTitle);

        infiniteTabView.setUnSelectedColor(Color.parseColor("#5079f4"));
        infiniteTabView.setSelectedColor(Color.parseColor("#314172"));
      //  infiniteTabView.setTextSelectedColor(Color.RED);

        final List<String> Tabs = new ArrayList<String>();
        int size = 5;
        for (int i = 0; i < size; i++) {
            Tabs.add("TAB " + i);
        }

        final TabLayoutManager tabLayoutManager = new TabLayoutManager();
        tabLayoutManager.attach(infiniteTabView, Tabs);
        tabLayoutManager.setOnItemSelectedListener(new TabLayoutManager.OnItemSelectedListener() {
            @Override
            public void onItemSelected(RecyclerView recyclerView, View item, int position) {
                horizontalItem.setText("selected:" + position + "\n");
            }
        });
        InfiniteTabAdapter infiniteTabAdapter = new InfiniteTabAdapter(Tabs);
        infiniteTabAdapter.setOnItemClickListener(new InfiniteTabAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                infiniteTabView.smoothScrollToPosition(position);
            }
        });
        infiniteTabView.setAdapter(infiniteTabAdapter);
    }
}
