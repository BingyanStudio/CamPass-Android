package net.bingyan.campass.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import net.bingyan.campass.ModuleConfig;
import net.bingyan.campass.MyApplication;
import net.bingyan.campass.R;
import net.bingyan.campass.greendao.Module;
import net.bingyan.campass.greendao.ModuleDao;

import java.util.List;

public class HomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        init();
    }

    private void init(){
        initGridView();
    }

    private void initGridView() {
        ModuleDao moduleDao = ((MyApplication) getApplication()).getDaoSession().getModuleDao();
        List<Module> modules = moduleDao.
                queryBuilder().
                orderDesc(ModuleDao.Properties.Frequency).
                list();
        if (modules.isEmpty()) {
            modules = ModuleConfig.initModuleDao(this, moduleDao);
        }


        GridView gridView = (GridView) findViewById(R.id.home_gridview);
        gridView.setNumColumns(3);

        HomeGridViewAdapter adapter = new HomeGridViewAdapter(modules, moduleDao);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(adapter);
    }



    class HomeGridViewAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
        List<Module> modules;
        ModuleDao moduleDao;

        public HomeGridViewAdapter(List<Module> modules, ModuleDao moduleDao) {
            this.modules = modules;
            this.moduleDao = moduleDao;
        }

        @Override
        public int getCount() {
            return modules.size();
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = View.inflate(HomeActivity.this, R.layout.activity_home_gridview_simple_item, null);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.imageView = (ImageView) view.findViewById(R.id.imageview);
                viewHolder.textView = (TextView) view.findViewById(R.id.textview);
                view.setTag(viewHolder);
            }
            ViewHolder viewHolder = (ViewHolder) view.getTag();
            viewHolder.imageView.setImageResource(modules.get(i).getIconid());
            viewHolder.textView.setText(modules.get(i).getName());
            return view;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public Object getItem(int i) {
            return modules.get(i);
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            //update module frequency
            Module module = modules.get(i);
            module.setFrequency(module.getFrequency() + 1);
            ModuleConfig.updateModuleName(module);
            moduleDao.update(module);

            String n = module.getClassname();
            if(n.equals("")) return;
            try {
                Intent intent = new Intent();
                intent.setClass(HomeActivity.this, Class.forName(n));
                startActivity(intent);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        class ViewHolder {
            public ImageView imageView;
            public TextView textView;
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
