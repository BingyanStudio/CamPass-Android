package net.bingyan.campass.module.electric;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.path.android.jobqueue.JobManager;

import net.bingyan.campass.MyApplication;
import net.bingyan.campass.R;
import net.bingyan.campass.rest.API;
import net.bingyan.campass.ui.BaseActivity;
import net.bingyan.campass.util.AppLog;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class ElectricActivity extends BaseActivity implements View.OnClickListener {

    public static int DATA_REFRESH = 1;

    AppLog mLog = new AppLog(getClass());

    //View
    private TextView remainText;
    private List<String> dateList = new ArrayList<String>();
    private List<Float> remainList = new ArrayList<Float>();
    private MyRecordAdapter myRecordAdapter;

    private JobManager jobManager;

    //校区、楼栋、寝室号
    private String areaStr;
    private int buildingNum;
    private int dormNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_electric);
        EventBus.getDefault().register(this);

        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void init() {
        initView();

        jobManager = MyApplication.getInstance().getJobManager();
        jobManager.addJobInBackground(new ElectricQueryJob(areaStr, buildingNum, dormNum));
    }

    private void initView() {
        //电费的列表
        RecyclerView recordList = (RecyclerView) findViewById(R.id.record_list);
        recordList.setLayoutManager(new LinearLayoutManager(this));
        myRecordAdapter = new MyRecordAdapter(dateList, remainList);
        recordList.setAdapter(myRecordAdapter);

        //初始时默认的寝室，应该是从sharePreference中获取
        areaStr = API.ElectricService.AREA[0];
        buildingNum = 15;
        dormNum = 306;


        remainText = (TextView) findViewById(R.id.electric_remain);

        //查询按钮
        Button query = (Button) findViewById(R.id.electric_loc);
        query.setOnClickListener(this);

    }

    //EventBus事件处理
    public void onEvent(ElectricRefreshEvent refreshEvent) {
        mLog.d("" + refreshEvent.getDateList().size());
        dateList.clear();
        remainList.clear();
        for (int i = 0; i < refreshEvent.getRemainList().size(); i++) {
            dateList.add(refreshEvent.getDateList().get(i));
            remainList.add(refreshEvent.getRemainList().get(i));
        }

        Message msg = new Message();
        msg.what = DATA_REFRESH;
        handler.sendMessage(msg);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (DATA_REFRESH == msg.what) {
                float remain = remainList.size() == 0 ? 0 : remainList.get(0);
                remainText.setText(String.valueOf(remain));

                myRecordAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.electric_loc:
                Intent intent = new Intent(this, ElectricQueryActivity.class);
                startActivity(intent);
                break;
        }
    }

    class MyRecordAdapter extends RecyclerView.Adapter<MyRecordAdapter.ViewHolder> {

        private List<String> dateList;
        private List<Float> remainList;

        public MyRecordAdapter(List<String> dateList, List<Float> remainList) {
            this.dateList = dateList;
            this.remainList = remainList;
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
                date = (TextView) itemView.findViewById(R.id.electric_record_item_date);
                remain = (TextView) itemView.findViewById(R.id.electric_record_item_remain);
            }
        }
    }
}
