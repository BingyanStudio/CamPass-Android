package net.bingyan.campass.rest;

import android.app.Application;

import com.google.gson.Gson;

import net.bingyan.campass.greendao.Cache;
import net.bingyan.campass.greendao.CacheDao;
import net.bingyan.campass.greendao.DaoSession;
import net.bingyan.campass.MyApplication;

import java.util.Date;

/**
 * Created by ant on 14-8-9.
 */
public  class CacheHelper<T> {
    Class<T> t;
    CacheDao cacheDao;

    public CacheHelper(Application app, Class<T> t) {
        DaoSession session = ((MyApplication) app).getDaoSession();
        cacheDao = session.getCacheDao();
        this.t=t;
    }

    public T getCache() {
        Cache cache = cacheDao.queryBuilder()
                .where(CacheDao.Properties.Name.eq(t.getName()))
                .unique();
        if (cache!=null) {
            return new Gson().fromJson(cache.getJson(), t);
        }
        return null;
    }

    public T getCache(String tag) {
        Cache cache = cacheDao.queryBuilder()
                .where(CacheDao.Properties.Name.eq(t.getName() + tag))
                .unique();
        if (cache!=null) {
            return new Gson().fromJson(cache.getJson(), t);
        }
        return null;
    }

    public  void putCache(T t) {
        Cache newsListCache = new Cache();
        newsListCache.setName(t.getClass().getName());
        newsListCache.setJson(new Gson().toJson(t));
        newsListCache.setDate(new Date());
//        cacheDao.insert(newsListCache);
        cacheDao.insertOrReplace(newsListCache);
    }

    public void putCache(T t, String tag) {
        Cache newsListCache = new Cache();
        newsListCache.setName(t.getClass().getName() + tag);
        newsListCache.setJson(new Gson().toJson(t));
        newsListCache.setDate(new Date());
        cacheDao.insertOrReplace(newsListCache);
    }

    public  T getCache(long time) {
        Cache cache = cacheDao.queryBuilder()
                .where(CacheDao.Properties.Name.eq(t.getName()))
                .unique();
        //getTime() milliseconds 毫秒
        if (cache!=null&&(new Date().getTime()-cache.getDate().getTime()<time)) {
            return new Gson().fromJson(cache.getJson(), t);
        }
        return null;
    }

}

