package net.bingyan.campass.rest;

import net.bingyan.campass.module.electric.ElectricBean;
import net.bingyan.campass.module.news.NewsContentBean;
import net.bingyan.campass.module.news.NewsListBean;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;


/**
 * Created by ant on 14-8-9.
 */
public class API {
    public static final String APIHOST = "http://api.hustonline.net/";

    public interface ElectricService {
        public static final String HOST = "http://202.114.18.13:9093";
        public static final String[] AREA = {"韵苑", "紫菘", "东区", "西区"};

        /**
         * 四个参数，通过拼音可以看出含义
         * @param quyu
         * @param loudong
         * @param fangjian
        */
        @GET("/check_dianfei")
        void getElectricJson(@Query("quyu") String quyu,
                             @Query("loudong") int loudong,
                             @Query("fangjian") int fangjian,
                             Callback<ElectricBean> cb);

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
