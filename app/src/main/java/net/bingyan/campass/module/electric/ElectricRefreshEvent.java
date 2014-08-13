package net.bingyan.campass.module.electric;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jinge on 14-8-13.
 */
public class ElectricRefreshEvent {
    private List<String> dateList;
    private List<Float> remainList;

    public ElectricRefreshEvent() {
        dateList = new ArrayList<String>();
        remainList = new ArrayList<Float>();
    }

    public void clear() {
        dateList.clear();
        remainList.clear();
    }

    public void addDate(String date) {
        dateList.add(date);
    }

    public void addRemain(float remain) {
        remainList.add(remain);
    }

    public void addDate(int location, String date) {
        dateList.add(location, date);
    }

    public void addRemain(int location, float remain){
        remainList.add(location, remain);
    }

    public List<String> getDateList() {
        return dateList;
    }

    public List<Float> getRemainList() {
        return remainList;
    }
}
