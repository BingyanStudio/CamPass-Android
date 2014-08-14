package net.bingyan.campass.module.news;

import android.os.Bundle;
import android.webkit.WebView;

import net.bingyan.campass.R;
import net.bingyan.campass.network.rest.API;
import net.bingyan.campass.network.rest.RestHelper;
import net.bingyan.campass.ui.BaseActivity;
import net.bingyan.campass.util.AppLog;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by ant on 14-8-9.
 */
public class NewsContentActivity extends BaseActivity {
    AppLog myLog = new AppLog(getClass());

    API.NewsService service = RestHelper.getService(API.NewsService.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_webview);

        init();
    }

    private void init(){
        getByHttp(getIntent().getIntExtra("news_id", -1));
    }

    private void getByHttp(int id) {
        service.getContent(id, new Callback<NewsContentBean>() {
            @Override
            public void success(NewsContentBean newsContentBean, Response response) {
                initData(newsContentBean);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void initData(NewsContentBean newsContentBean) {
        WebView webView = (WebView) findViewById(R.id.simple_viewpager);
        webView.loadDataWithBaseURL(null, newsContentBean.getArticle().getContent(), "text/html", "utf-8", null);
    }
}
