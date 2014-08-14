package net.bingyan.campass.module.electric;

import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jinge on 14-8-13.
 */
public class ElectricRefreshEvent {
    private List<Date> dateList;
    private List<Float> remainList;

    public ElectricRefreshEvent() {
        dateList = new ArrayList<Date>();
        remainList = new ArrayList<Float>();
    }

    public void clear() {
        dateList.clear();
        remainList.clear();
    }

    public void addDate(Date date) {
        dateList.add(date);
    }

    public void addRemain(float remain) {
        remainList.add(remain);
    }

    public void addDate(int location, Date date) {
        dateList.add(location, date);
    }

    public void addRemain(int location, float remain){
        remainList.add(location, remain);
    }

    public List<Date> getDateList() {
        return dateList;
    }

    public List<Float> getRemainList() {
        return remainList;
    }
}
