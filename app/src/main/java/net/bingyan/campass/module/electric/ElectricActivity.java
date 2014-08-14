package net.bingyan.campass.module.electric;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.path.android.jobqueue.JobManager;

import net.bingyan.campass.MyApplication;
import net.bingyan.campass.R;
import net.bingyan.campass.network.rest.API;
import net.bingyan.campass.ui.BaseActivity;
import net.bingyan.campass.util.AppLog;
import net.bingyan.campass.view.LineGraphView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

public class ElectricActivity extends BaseActivity implements View.OnClickListener {

    public static int DATA_REFRESH = 1;

    AppLog mLog = new AppLog(getClass());

    //View
    private TextView remainText;
    private TextView detailDate;
    private TextView detailRemain;
    private LineGraphView recordGraph;

    private JobManager jobManager;

    //校区、楼栋、寝室号
    private String areaStr;
    private int buildingNum;
    private int dormNum;

    private float remain;

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

        //初始时默认的寝室，应该是从sharePreference中获取
        areaStr = API.ElectricService.AREA[0];
        buildingNum = 15;
        dormNum = 306;

        //剩余电量
        remainText = (TextView) findViewById(R.id.electric_remain);
        detailDate = (TextView) findViewById(R.id.electric_record_date);
        detailRemain = (TextView) findViewById(R.id.electric_record_remain);
        recordGraph = (LineGraphView) findViewById(R.id.electric_record_graph);
        recordGraph.setOnLineGraphBarChangeListener(new LineGraphView.OnLineGraphBarChangeListener() {
            @Override
            public void onBarChanged(float percentX, float percentY) {
                Date date = new Date((long)(percentX * 1000));
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm");
                detailDate.setText(simpleDateFormat.format(date));
                detailRemain.setText((int)(percentY * 10 + 0.5) / 10.0 + "");
            }
        });

        //查询按钮
        Button query = (Button) findViewById(R.id.electric_loc);
        query.setOnClickListener(this);

    }

    //EventBus事件处理
    public void onEvent(ElectricRefreshEvent refreshEvent) {
        remain = refreshEvent.getRemainList().size() == 0 ? 0 : refreshEvent.getRemainList().get(0);

        ArrayList<LineGraphView.LinePoint> points = new ArrayList<LineGraphView.LinePoint>();

        for (int i = refreshEvent.getRemainList().size() - 1; i >= 0; i--) {

            mLog.i(refreshEvent.getDateList().get(i).getTime() / 1000 + "");
            points.add(new LineGraphView.LinePoint(refreshEvent.getDateList().get(i).getTime() / 1000,
                    refreshEvent.getRemainList().get(i)));
        }

        LineGraphView.Line line = new LineGraphView.Line();
        line.setPoints(points);
        recordGraph.setLine(line);

        //更新剩余电量
        Message msg = new Message();
        msg.what = DATA_REFRESH;
        handler.sendMessage(msg);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (DATA_REFRESH == msg.what) {
                remainText.setText(String.valueOf(remain));
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

}
