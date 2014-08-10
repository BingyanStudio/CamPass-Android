package net.bingyan.campass;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table ELECTRIC_RECORD.
 */
public class ElectricRecord {

    private Long id;
    private String area;
    private Integer building;
    private Integer dorm;
    private Float remain;
    private java.util.Date date;

    public ElectricRecord() {
    }

    public ElectricRecord(Long id) {
        this.id = id;
    }

    public ElectricRecord(Long id, String area, Integer building, Integer dorm, Float remain, java.util.Date date) {
        this.id = id;
        this.area = area;
        this.building = building;
        this.dorm = dorm;
        this.remain = remain;
        this.date = date;
    }

    public ElectricRecord(String area, Integer building, Integer dorm, Float remain, java.util.Date date) {
        this.area = area;
        this.building = building;
        this.dorm = dorm;
        this.remain = remain;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public Integer getBuilding() {
        return building;
    }

    public void setBuilding(Integer building) {
        this.building = building;
    }

    public Integer getDorm() {
        return dorm;
    }

    public void setDorm(Integer dorm) {
        this.dorm = dorm;
    }

    public Float getRemain() {
        return remain;
    }

    public void setRemain(Float remain) {
        this.remain = remain;
    }

    public java.util.Date getDate() {
        return date;
    }

    public void setDate(java.util.Date date) {
        this.date = date;
    }

}
