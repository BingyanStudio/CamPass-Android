package net.bingyan.campass;

import android.content.Context;

import net.bingyan.campass.greendao.Module;
import net.bingyan.campass.greendao.ModuleDao;
import net.bingyan.campass.module.electric.ElectricActivity;
import net.bingyan.campass.module.news.NewsListActivity;

import java.util.List;

/**
 * Created by ant on 14-8-8.
 */
public class ModuleConfig {
    static int moduleNameRes[] = {R.string.module_ann, R.string.module_classroom, R.string.module_computer,
            R.string.module_elec, R.string.module_food, R.string.module_iknow,
            R.string.module_lecture, R.string.module_lib, R.string.module_map,
            R.string.module_news, R.string.module_score, R.string.module_wifi};
    static int moduleIconRes[] = {R.drawable.home_icon_ann, R.drawable.home_icon_classroom, R.drawable.home_icon_computer,
            R.drawable.home_icon_elec, R.drawable.home_icon_food, R.drawable.home_icon_iknow,
            R.drawable.home_icon_lecture, R.drawable.home_icon_lib, R.drawable.home_icon_map,
            R.drawable.home_icon_news, R.drawable.home_icon_score, R.drawable.home_icon_wifi};
    static String moduleClassName[] = {null, null, null,
            ElectricActivity.class.getName(), null, null,
            null, null, null,
            NewsListActivity.class.getName(), null, null};


    public static List<Module> initModuleDao(Context context, ModuleDao moduleDao) {
        for (int i = 0; i < moduleNameRes.length; i++) {
            Module module = new Module();
            module.setIconid(moduleIconRes[i]);
            module.setName(context.getString(moduleNameRes[i]));
            module.setFrequency(0);
            if (moduleClassName[i] == null) moduleClassName[i] = "";
            module.setClassname(moduleClassName[i]);
            moduleDao.insert(module);
        }
        return moduleDao.queryBuilder().
                orderDesc(ModuleDao.Properties.Frequency).
                list();
    }

    public static void updateModuleName(Module module) {
        if (module.getClassname().equals("") && moduleClassName[module.getId().intValue() - 1] != null) {
            module.setClassname(moduleClassName[module.getId().intValue() - 1]);
        }
    }

}
