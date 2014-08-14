package net.bingyan.campass.module.news;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.bingyan.campass.R;
import net.bingyan.campass.network.rest.API;
import net.bingyan.campass.db.CacheHelper;
import net.bingyan.campass.network.rest.RestHelper;
import net.bingyan.campass.ui.BaseActivity;
import net.bingyan.campass.util.AppLog;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by ant on 14-8-9.
 */
public class NewsListActivity extends BaseActivity {
    //Log
    AppLog mLog = new AppLog(getClass());

    //Cache Http
    CacheHelper<NewsListBean> newsListBeanCacheHelper;
    API.NewsService service = RestHelper.getService(API.NewsService.class);
    public boolean loading = false;

    //View
    public List<NewsListBean.Articlelist> data = new ArrayList<NewsListBean.Articlelist>();
    MyAdapter mAdapter = new MyAdapter(this, data);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_recycleview);
        newsListBeanCacheHelper = new CacheHelper<NewsListBean>(NewsListBean.class);
        init();
    }

    private void init() {
        //view
        initRecyclerView();

        //Data by cache
        NewsListBean newsListBean = newsListBeanCacheHelper.getCache();
        if (newsListBean != null) {
            initData(newsListBean);
        }

        //Data by http
        getByHttp(1);
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        //mAdapter = new MyAdapter(this, data);
        recyclerView.setAdapter(mAdapter);
    }

    private void getByHttp(final int pageNum) {
        if (loading) return;
        loading = true;
        service.getlist(API.NewsService.sort[0], pageNum, new Callback<NewsListBean>() {
            @Override
            public void success(NewsListBean newsListBean, Response response) {
                if (pageNum == 1) {
                    initData(newsListBean);
                    mLog.v("start cache");
                    newsListBeanCacheHelper.putCache(newsListBean);
                    mLog.v("cached");
                } else {
                    addData(newsListBean);
                }
                loading = false;
            }

            @Override
            public void failure(RetrofitError error) {
                mLog.v("error:" + error.getUrl());
                mLog.v("error:" + error.getMessage());
                loading = false;
            }
        });
    }

    private void initData(NewsListBean newsListBean) {
        data.clear();
        addData(newsListBean);
    }

    private void addData(NewsListBean newsListBean) {
        for (NewsListBean.Articlelist a : newsListBean.getArticlelist()) {
            data.add(a);
        }
        mAdapter.notifyDataSetChanged();
    }


    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        List<NewsListBean.Articlelist> data;
        Context context;

        public MyAdapter(Context context, List<NewsListBean.Articlelist> data) {
            this.data = data;
            this.context = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            // 加载Item的布局.布局中用到的真正的CardView.
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.simple_list_item_cardview, viewGroup, false);
            // ViewHolder参数一定要是Item的Root节点.
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            mLog.v("onBind:" + i);
            if (i >= data.size() - 1) {
                getByHttp(data.size() / 10 + 1);
            }
            viewHolder.text.setText(data.get(i).getTitle());
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView text;

            public ViewHolder(View itemView) {
                // super这个参数一定要注意,必须为Item的根节点.否则会出现莫名的FC.
                super(itemView);
                text = (TextView) itemView.findViewById(R.id.text);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(NewsListActivity.this, NewsContentActivity.class);
                        intent.putExtra("news_id", Integer.valueOf(data.get(getPosition()).getId()));
                        startActivity(intent);
                    }
                });
            }
        }
    }
}