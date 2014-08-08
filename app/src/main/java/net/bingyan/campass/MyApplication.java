package net.bingyan.campass;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

import net.bingyan.campass.DaoMaster;
import net.bingyan.campass.DaoSession;

/**
 * Created by ant on 14-8-8.
 */
public class MyApplication extends Application{

    public DaoSession daoSession;

    private void setupDatabase() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "example-db", null);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        setupDatabase();
    }
}
