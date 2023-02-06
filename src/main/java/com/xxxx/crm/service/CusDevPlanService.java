package com.xxxx.crm.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xxxx.base.BaseService;
import com.xxxx.crm.dao.SaleChanceMapper;
import com.xxxx.crm.query.CusDevPlanQuery;
import com.xxxx.crm.utils.AssertUtil;
import com.xxxx.crm.vo.CusDevPlan;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class CusDevPlanService extends BaseService<CusDevPlan,Integer> {


    @Autowired
    private SaleChanceMapper saleChanceMapper;
    public Map<String,Object> queryCusDevPlansByParams(CusDevPlanQuery cusDevPlanQuery){
        Map<String,Object> map=new HashMap<String,Object>();
        PageHelper.startPage(cusDevPlanQuery.getPage(),cusDevPlanQuery.getLimit());
        PageInfo<CusDevPlan> pageInfo=new PageInfo<CusDevPlan>(selectByParams(cusDevPlanQuery));
        map.put("code",0);
        map.put("msg","");
        map.put("count",pageInfo.getTotal());
        map.put("data",pageInfo.getList());
        return  map;
    }



    public void saveCusDevPlan(CusDevPlan cusDevPlan){
        /**
         * 1.参数校验
         *    机会id 非空 记录必须存在
         *    计划项内容非空
         *    计划项时间非空
         * 2. 参数默认值
         *    is_valid  1
         *    createDate 系统时间
         *    updateDate  系统时间
         * 3.执行添加 判断结果
         */
        checkParams(cusDevPlan.getSaleChanceId(),cusDevPlan.getPlanItem(),cusDevPlan.getPlanDate());
        cusDevPlan.setIsValid(1);
        cusDevPlan.setCreateDate(new Date());
        cusDevPlan.setUpdateDate(new Date());
        AssertUtil.isTrue(insertSelective(cusDevPlan)<1,"计划项记录添加失败!");
    }

    private void checkParams(Integer saleChanceId, String planItem, Date planDate) {
        AssertUtil.isTrue(null==saleChanceId||null==saleChanceMapper.selectByPrimaryKey(saleChanceId),"请设置营销机会id");
        AssertUtil.isTrue(StringUtils.isBlank(planItem),"请输入计划项内容!");
        AssertUtil.isTrue(null==planDate,"请指定计划项日期!");
    }


    public void updateCusDevPlan(CusDevPlan cusDevPlan){
        /**
         * 1.参数校验
         *    id 记录必须存在
         *    机会id 非空 记录必须存在
         *    计划项内容非空
         *    计划项时间非空
         * 2. 参数默认值
         *    updateDate  系统时间
         * 3.执行更新 判断结果
         */
        AssertUtil.isTrue(null==cusDevPlan.getId() || null==selectByPrimaryKey(cusDevPlan.getId()),"待更新的记录不存在!");
        checkParams(cusDevPlan.getSaleChanceId(),cusDevPlan.getPlanItem(),cusDevPlan.getPlanDate());
        cusDevPlan.setUpdateDate(new Date());
        AssertUtil.isTrue(updateByPrimaryKeySelective(cusDevPlan)<1,"记录更新失败!");
    }


    public void deleteCusDevPlan(Integer id){
        CusDevPlan cusDevPlan= selectByPrimaryKey(id);
        AssertUtil.isTrue( null==cusDevPlan,"待删除的记录不存在!");
        cusDevPlan.setIsValid(0);
        AssertUtil.isTrue(updateByPrimaryKeySelective(cusDevPlan)<1,"记录删除失败!");
    }





}
