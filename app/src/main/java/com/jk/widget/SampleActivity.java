package com.jk.widget;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jk.recyclerview.JKRecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * sample
 *
 * @author JewelKing
 * @date 2017-03-07
 */

public class SampleActivity extends AppCompatActivity {

    private List<String> listData = new ArrayList<>();
    JKRecyclerView listView;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        listData.add("123");
        listData.add("234");
        listData.add("456");
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        listView = (JKRecyclerView) findViewById(R.id.listView);
//            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_red_light, android.R.color.holo_blue_light, android.R.color.holo_green_light);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData(1);
            }
        });
        listView.setOnLoadMoreListener(new JKRecyclerView.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                loadData(2);
            }
        });
        listView.setAdapter(new ItemAdapter(listData));
    }

    public void loadData(int type) {
        swipeRefreshLayout.setRefreshing(false);
        if (type == 1) {
            listData.clear();
            listView.setLoadComplete();
        } else {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    /**
                     *要执行的操作
                     */
                    for (int i = 0; i < 5; i++) {
                        listData.add("Jewel" + i);
                    }
                    listView.setLoadComplete();
                }
            }, 3000);//3秒后执行Runnable中的run方法
        }

    }

    public class ItemAdapter extends RecyclerView.Adapter {

        private List<String> list = new ArrayList<>();
        private LayoutInflater inflater;

        public ItemAdapter(List<String> list) {
            this.list = list;
            this.inflater = LayoutInflater.from(getBaseContext());
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new VH(inflater.inflate(R.layout.item, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ItemAdapter.VH h = (VH) holder;
            h.tvTitle.setText(list.get(position));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class VH extends RecyclerView.ViewHolder {

            TextView tvTitle;

            public VH(View itemView) {
                super(itemView);
                tvTitle = (TextView) itemView.findViewById(R.id.tv_title);
            }
        }
    }
}
