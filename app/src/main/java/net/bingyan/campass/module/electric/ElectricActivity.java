package net.bingyan.campass.module.electric;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import net.bingyan.campass.greendao.ElectricRecord;
import net.bingyan.campass.greendao.ElectricRecordDao;
import net.bingyan.campass.MyApplication;
import net.bingyan.campass.R;
import net.bingyan.campass.rest.API;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.dao.query.QueryBuilder;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ElectricActivity extends Activity implements View.OnClickListener {

    public static String URL = "http://202.114.18.13:9093";

    public static String[] AREA = {"韵苑", "紫菘", "东区", "西区"};

    private List<String> dateList;
    private List<Float> remainList;
    private MyListAdapter myListAdapter;

    private Spinner area;
    private EditText building;
    private EditText dorm;

    private String areaStr;
    private int buildingNum;
    private int dormNum;
    private Date recentDate;
    private float remain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_electric);

        dateList = new ArrayList<String>();
        remainList = new ArrayList<Float>();

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
        ListView recordList = (ListView) findViewById(R.id.record_list);
        myListAdapter = new MyListAdapter(this);
        recordList.setAdapter(myListAdapter);

        area = (Spinner) findViewById(R.id.electric_loc_area);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, AREA);
        area.setAdapter(spinnerAdapter);

        building = (EditText) findViewById(R.id.electric_loc_building);
        dorm = (EditText) findViewById(R.id.electric_loc_dorm);

        //初始时默认的寝室，应该是从sharePreference中获取
        areaStr = AREA[0];
        buildingNum = 15;
        dormNum = 306;
        area.setSelection(0);
        building.setText(String.valueOf(buildingNum));
        dorm.setText(String.valueOf(dormNum));

        Button setLoc = (Button) findViewById(R.id.electric_query);
        setLoc.setOnClickListener(this);

    }

    private void getFromWeb() {
        Map<String, String> options = new HashMap<String, String>();
        options.put("quyu", areaStr);
        options.put("loudong", building.getText().toString());
        options.put("fangjian", dorm.getText().toString());

        RestAdapter rest = new RestAdapter.Builder().setEndpoint(URL).build();
        API.ElectricService test = rest.create(API.ElectricService.class);
        test.getElectricJson(options, new Callback<ElectricJson>() {
            @Override
            public void success(ElectricJson electricJson, Response response) {
                Log.i("dianfei", electricJson.toString());
                saveAndDiaplay(electricJson);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("error", error.toString());
            }
        });
    }

    public void saveAndDiaplay(ElectricJson electricJson) {
//        //如果是绑定的寝室，则缓存json。也可以不用缓存json，因为电费已经做了缓存
//        if(areaStr.equals("韵苑") && buildingNum == 15 && dormNum == 306) {
//            CacheDaoHelper cacheDaoHelper = new CacheDaoHelper(
//                    (MyApplication) this.getApplicationContext());
//            cacheDaoHelper.putCache(electricJson);
//        }

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
            if ((recentDate != null) && (date == null || !date.after(recentDate))) {
                continue;
            }

            electricRecord = new ElectricRecord();
            electricRecord.setArea(areaStr);
            electricRecord.setBuilding(buildingNum);
            electricRecord.setDorm(dormNum);
            electricRecord.setRemain(Float.valueOf(history.get(0)));
            electricRecord.setDate(date);
            electricRecordDao.insertOrReplace(electricRecord);

            //将数据库中没有的数据加到List中
            dateList.add(i, listFormat.format(date));
            remainList.add(i ++, Float.valueOf(history.get(0)));
        }
        myListAdapter.notifyDataSetInvalidated();
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
        //数据库中最后缓存的日期
        recentDate = list.size() == 0 ? null : ((ElectricRecord) list.get(0)).getDate();
        remain = list.size() == 0 ? 0 : ((ElectricRecord) list.get(0)).getRemain();
        TextView remainText = (TextView)findViewById(R.id.electric_remain);
        remainText.setText(String.valueOf(remain));

        dateList.clear();
        remainList.clear();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        for (Object aList : list) {
            Log.i("list", aList.toString());
            ElectricRecord record = (ElectricRecord) aList;
            dateList.add(simpleDateFormat.format(record.getDate()));
            remainList.add(record.getRemain());
        }
        myListAdapter.notifyDataSetInvalidated();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.electric_query:
                areaStr = area.getSelectedItem().toString();
                buildingNum = Integer.valueOf(building.getText().toString());
                dormNum = Integer.valueOf(dorm.getText().toString());
                //先展示数据库中的数据
                display();
                //再从网络请求
                getFromWeb();
                break;
        }
    }

    class MyListAdapter extends BaseAdapter {

        private Context context;

        public MyListAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return dateList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(context, R.layout.activity_electric_record_list, null);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.date = (TextView) convertView.findViewById(R.id.record_list_date);
                viewHolder.remain = (TextView) convertView.findViewById(R.id.record_list_remain);
                convertView.setTag(viewHolder);
            }
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.date.setText(dateList.get(position));
            viewHolder.remain.setText(remainList.get(position).toString());

            return convertView;
        }

        class ViewHolder {
            TextView date;
            TextView remain;
        }
    }

}
