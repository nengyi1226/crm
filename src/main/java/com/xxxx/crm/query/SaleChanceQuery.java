package com.xxxx.crm.query;


import com.xxxx.base.BaseQuery;

public class SaleChanceQuery extends BaseQuery {
    // 客户名
    private String customerName;
    // 创建人
    private String createMan;
    // 分配状态
    private String state;

    //分配人
    private Integer assignMan;

    // 开发状态
    private Integer devResult;

    public Integer getAssignMan() {
        return assignMan;
    }

    public void setAssignMan(Integer assignMan) {
        this.assignMan = assignMan;
    }

    public Integer getDevResult() {
        return devResult;
    }

    public void setDevResult(Integer devResult) {
        this.devResult = devResult;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCreateMan() {
        return createMan;
    }

    public void setCreateMan(String createMan) {
        this.createMan = createMan;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
