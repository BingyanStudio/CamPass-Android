package net.bingyan.campass.module.electric;

import net.bingyan.campass.greendao.ElectricRecordDao;
import net.bingyan.campass.MyApplication;

import java.util.Date;

/**
 * Created by Jinge on 2014/8/9.
 */
public class ElectricRecordDaoHelper {

    private ElectricRecordDao electricRecordDao;
    public ElectricRecordDaoHelper(MyApplication myApplication) {
        electricRecordDao = myApplication.getDaoSession().getElectricRecordDao();
    }

    public void insert(String area, int building, int dorm, float remain, Date date) {
//        electricRecordDao.insertOrReplace();
    }

}
