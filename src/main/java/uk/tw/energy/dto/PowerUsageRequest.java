package uk.tw.energy.dto;

public class PowerUsageRequest {
    private Integer day;
    private String smartMeterId;

    public PowerUsageRequest(){}

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public String getSmartMeterId() {
        return smartMeterId;
    }

    public void setSmartMeterId(String smartMeterId) {
        this.smartMeterId = smartMeterId;
    }
}
