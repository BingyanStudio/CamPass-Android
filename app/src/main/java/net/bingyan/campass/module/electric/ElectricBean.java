package net.bingyan.campass.module.electric;

import java.util.ArrayList;
import java.util.List;
//import javax.annotation.Generated;
import com.google.gson.annotations.Expose;

//@Generated("org.jsonschema2pojo")
public class ElectricBean {

    @Expose
    private String remain;
    @Expose
    private List<List<String>> history = new ArrayList<List<String>>();
    @Expose
    private String state;
    @Expose
    private String datetime;

    public String getRemain() {
        return remain;
    }

    public void setRemain(String remain) {
        this.remain = remain;
    }

    public List<List<String>> getHistory() {
        return history;
    }

    public void setHistory(List<List<String>> history) {
        this.history = history;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    @Override
    public String toString() {
        return "DianFei{" +
                "remain='" + remain + '\'' +
                ", history=" + history +
                ", state='" + state + '\'' +
                ", datetime='" + datetime + '\'' +
                '}';
    }
}