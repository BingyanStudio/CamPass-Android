package net.bingyan.campass.module.electric;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import net.bingyan.campass.MyApplication;
import net.bingyan.campass.greendao.ElectricRecord;
import net.bingyan.campass.greendao.ElectricRecordDao;
import net.bingyan.campass.rest.API;
import net.bingyan.campass.rest.RestHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by jinge on 14-8-13.
 */
public class ElectricQueryJob extends Job {
    //校区、楼栋、寝室号
    private String areaStr;
    private int buildingNum;
    private int dormNum;

    private Date recentDate;

    private API.ElectricService service;

    private ElectricRecordDao electricRecordDao;

    private ElectricRefreshEvent refreshEvent;


    protected ElectricQueryJob(String areaStr, int buildingNum, int dormNum) {
        super(new Params(500));
        this.areaStr = areaStr;
        this.buildingNum = buildingNum;
        this.dormNum = dormNum;

        service = RestHelper.getService(API.ElectricService.HOST, API.ElectricService.class);
        electricRecordDao = MyApplication.getInstance().getDaoSession().getElectricRecordDao();
        refreshEvent = new ElectricRefreshEvent();
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {

        getFromCache();

        service.getElectricJson(areaStr, buildingNum, dormNum, new Callback<ElectricBean>() {
            @Override
            public void success(ElectricBean electricJson, Response response) {
                cacheDataFromHttp(electricJson);
            }

            @Override
            public void failure(RetrofitError error) {
//                mLog.i(error.toString());
            }
        });
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }

    private void cacheDataFromHttp(ElectricBean electricJson) {
        ElectricRecord electricRecord;

        //从json到数据库的格式转换
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //从数据库到显示出来的转换
        SimpleDateFormat listFormat = new SimpleDateFormat("yyyy-MM-dd");

        int i = 0;
        for (List<String> history : electricJson.getHistory()) {
            Date date = null;
            try {
                date = simpleDateFormat.parse(history.get(1));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            //如果数据时最后缓存日期之前的，则不在重复缓存
            if (null != recentDate) {
                if ((null == date || !date.after(recentDate))) continue;
            }

            electricRecord = new ElectricRecord(areaStr, buildingNum, dormNum,
                    Float.valueOf(history.get(0)), date);
            //插入到数据库
            electricRecordDao.insertOrReplace(electricRecord);

            //将数据库中没有的数据加到List中
            refreshEvent.addDate(i, listFormat.format(date));
            refreshEvent.addRemain(i++, Float.valueOf(history.get(0)));
        }
        EventBus.getDefault().post(refreshEvent);
    }

    private void getFromCache() {
        QueryBuilder queryBuilder = electricRecordDao.queryBuilder();
        queryBuilder.where(ElectricRecordDao.Properties.Area.eq(areaStr),
                queryBuilder.and(ElectricRecordDao.Properties.Building.eq(buildingNum),
                        ElectricRecordDao.Properties.Dorm.eq(dormNum))
        )
                .orderDesc(ElectricRecordDao.Properties.Date);
        List<ElectricRecord> list = queryBuilder.list();

        //数据库中最后缓存的日期和剩余电量
        recentDate = list.size() == 0 ? null : list.get(0).getDate();

        refreshEvent.clear();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        for (ElectricRecord record : list) {
            refreshEvent.addDate(simpleDateFormat.format(record.getDate()));
            refreshEvent.addRemain(record.getRemain());
        }
        EventBus.getDefault().post(refreshEvent);
    }
}
