package net.bingyan.campass.network.rest;

import retrofit.RestAdapter;

/**
 * Created by ant on 14-8-8.
 */
public class RestHelper {
    static String HOST="http://api.hustonline.net";

    public static <T> T getService(Class<T> t){
        return  getService(HOST,t);
    }
    public static <T> T getService(String HOST,Class<T> t){
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(HOST)
                .build();
        return restAdapter.create(t);
    }
}
