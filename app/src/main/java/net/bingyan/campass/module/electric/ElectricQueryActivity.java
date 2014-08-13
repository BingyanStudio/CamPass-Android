package net.bingyan.campass.module.electric;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.path.android.jobqueue.JobManager;

import net.bingyan.campass.MyApplication;
import net.bingyan.campass.R;
import net.bingyan.campass.rest.API;

public class ElectricQueryActivity extends Activity implements View.OnClickListener {

    //View
    private Spinner area;
    private EditText building;
    private EditText dorm;

    //校区、楼栋、寝室号
    private String areaStr;
    private int buildingNum;
    private int dormNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_electric_query);

        initView();
    }

    private void initView() {
        //初始化Spinner
        area = (Spinner) findViewById(R.id.electric_loc_area);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, API.ElectricService.AREA);
        area.setAdapter(spinnerAdapter);

        //填写寝室地址
        building = (EditText) findViewById(R.id.electric_loc_building);
        dorm = (EditText) findViewById(R.id.electric_loc_dorm);

        //查询按钮
        Button query = (Button) findViewById(R.id.electric_query);
        query.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.electric_query:
                areaStr = area.getSelectedItem().toString();
                buildingNum = Integer.valueOf(building.getText().toString());
                dormNum = Integer.valueOf(dorm.getText().toString());

                JobManager jobManager = MyApplication.getInstance().getJobManager();
                jobManager.addJobInBackground(new ElectricQueryJob(areaStr, buildingNum, dormNum));
                finish();
                break;
        }
    }
}
