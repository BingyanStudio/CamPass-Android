package net.bingyan.campass.module.electric;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import net.bingyan.campass.ElectricRecord;
import net.bingyan.campass.ElectricRecordDao;
import net.bingyan.campass.MyApplication;
import net.bingyan.campass.R;
import net.bingyan.campass.rest.API;
import net.bingyan.campass.rest.RestHelper;
import net.bingyan.campass.ui.BaseActivity;
import net.bingyan.campass.util.AppLog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ElectricActivity extends BaseActivity implements View.OnClickListener {

    AppLog mLog = new AppLog(getClass());

    private API.ElectricService service;

    //View
    private Spinner area;
    private EditText building;
    private EditText dorm;
    private MyRecordAdapter myRecordAdapter;

    //校区、楼栋、寝室号
    private String areaStr;
    private int buildingNum;
    private int dormNum;
    //数据库中最近一次的日期
    private Date recentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_electric);

        service = RestHelper.getService(API.ElectricService.HOST, API.ElectricService.class);
        init();
    }

    private void init() {
        initView();
        //先展示数据库中的数据
        display();
        //再从网络请求
        getFromWeb();
    }

    private void initView() {
        //电费的列表
        RecyclerView recordList = (RecyclerView) findViewById(R.id.record_list);
        recordList.setLayoutManager(new LinearLayoutManager(this));
        myRecordAdapter = new MyRecordAdapter(this);
        recordList.setAdapter(myRecordAdapter);

        //初始化Spinner
        area = (Spinner) findViewById(R.id.electric_loc_area);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, API.ElectricService.AREA);
        area.setAdapter(spinnerAdapter);

        //填写寝室地址
        building = (EditText) findViewById(R.id.electric_loc_building);
        dorm = (EditText) findViewById(R.id.electric_loc_dorm);

        //初始时默认的寝室，应该是从sharePreference中获取
        areaStr = API.ElectricService.AREA[0];
        buildingNum = 15;
        dormNum = 306;
        area.setSelection(0);
        building.setText(String.valueOf(buildingNum));
        dorm.setText(String.valueOf(dormNum));

        //查询按钮
        Button query = (Button) findViewById(R.id.electric_query);
        query.setOnClickListener(this);

    }

    private void getFromWeb() {
        service.getElectricJson(areaStr, buildingNum, dormNum, new Callback<ElectricBean>() {
            @Override
            public void success(ElectricBean electricJson, Response response) {
                saveAndDisplay(electricJson);
            }

            @Override
            public void failure(RetrofitError error) {
                mLog.i(error.toString());
            }
        });
    }

    public void saveAndDisplay(ElectricBean electricJson) {
        ElectricRecordDao electricRecordDao = ((MyApplication) this.getApplicationContext())
                .getDaoSession().getElectricRecordDao();
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
            myRecordAdapter.dateList.add(i, listFormat.format(date));
            myRecordAdapter.remainList.add(i++, Float.valueOf(history.get(0)));
        }
        //更新剩余电量
        float remain = myRecordAdapter.remainList.size() == 0 ? 0 : myRecordAdapter.remainList.get(0);
        TextView remainText = (TextView) findViewById(R.id.electric_remain);
        remainText.setText(String.valueOf(remain));
        //刷新电费列表
        myRecordAdapter.notifyDataSetChanged();
    }

    private void display() {
        ElectricRecordDao electricRecordDao = ((MyApplication) this.getApplicationContext())
                .getDaoSession().getElectricRecordDao();
        QueryBuilder queryBuilder = electricRecordDao.queryBuilder();
        queryBuilder.where(ElectricRecordDao.Properties.Area.eq(areaStr),
                queryBuilder.and(ElectricRecordDao.Properties.Building.eq(buildingNum),
                        ElectricRecordDao.Properties.Dorm.eq(dormNum)))
                .orderDesc(ElectricRecordDao.Properties.Date);
        List list = queryBuilder.list();
        //数据库中最后缓存的日期和剩余电量
        recentDate = list.size() == 0 ? null : ((ElectricRecord) list.get(0)).getDate();
        float remain = list.size() == 0 ? 0 : ((ElectricRecord) list.get(0)).getRemain();
        TextView remainText = (TextView) findViewById(R.id.electric_remain);
        remainText.setText(String.valueOf(remain));

        //先清空电费列表
        myRecordAdapter.clear();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        for (Object aList : list) {
            ElectricRecord record = (ElectricRecord) aList;
            myRecordAdapter.dateList.add(simpleDateFormat.format(record.getDate()));
            myRecordAdapter.remainList.add(record.getRemain());
        }
        //刷新电费列表
        myRecordAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.electric_query:
                areaStr = area.getSelectedItem().toString();
                buildingNum = Integer.valueOf(building.getText().toString());
                dormNum = Integer.valueOf(dorm.getText().toString());
                building.clearFocus();
                dorm.clearFocus();
                //先展示数据库中的数据
                display();
                //再从网络请求
                getFromWeb();
                break;
        }
    }

    class MyRecordAdapter extends RecyclerView.Adapter<MyRecordAdapter.ViewHolder> {

        private Context context;
        private List<String> dateList;
        private List<Float> remainList;

        public MyRecordAdapter(Context context) {
            this.context = context;
            dateList = new ArrayList<String>();
            remainList = new ArrayList<Float>();
        }

        public void clear() {
            dateList.clear();
            remainList.clear();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.electric_record_item, viewGroup, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            if (i < getItemCount()) {
                viewHolder.date.setText(dateList.get(i));
                viewHolder.remain.setText(String.valueOf(remainList.get(i)));
            }
        }

        @Override
        public int getItemCount() {
            return remainList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView date;
            TextView remain;

            public ViewHolder(View itemView) {
                super(itemView);
                date = (TextView)itemView.findViewById(R.id.electric_record_item_date);
                remain = (TextView)itemView.findViewById(R.id.electric_record_item_remain);
            }
        }
    }
}
