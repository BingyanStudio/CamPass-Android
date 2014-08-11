package net.bingyan.campass.rest;

import net.bingyan.campass.module.electric.ElectricJson;
import net.bingyan.campass.module.news.NewsContentBean;
import net.bingyan.campass.module.news.NewsListBean;

import java.util.Map;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;
import retrofit.http.QueryMap;


/**
 * Created by ant on 14-8-9.
 */
public class API {
    public static final String APIHOST = "http://api.hustonline.net/";

    public interface ElectricService {
        @GET("/check_dianfei")
        void getElectricJson(@QueryMap Map<String, String> options,Callback<ElectricJson> cb);

    }

    public interface NewsService {
        public static final int[] sort = {1138, 12, 289};
        public static final String[] title = {"学校要闻", "综合新闻", "菁菁校园"};

        /**
         * 四个参数
         * token : hustnewsapi
         * pagenum : 每页的数目
         * pagesize : 每页多少条
         * sort : 新闻分类，1138为学校要闻，289为菁菁校园，12为综合新闻。
         */
        @GET("/hustnewsapi/article_list.aspx?site=1&pagesize=10&token=hustnewsapi")
        void getlist(@Query("sort") int sort, @Query("pagenum") int pagenum, Callback<NewsListBean> cb);

        /**
         * 两个参数
         * token : hustnewsapi
         * id : 新闻id
         */
        @GET("/hustnewsapi/single_article.aspx?token=hustnewsapi")
        void getContent(@Query("id") int id, Callback<NewsContentBean> cb);
    }
}
