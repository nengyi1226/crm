package com.xxxx.crm.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xxxx.base.BaseService;
import com.xxxx.crm.dao.UserMapper;
import com.xxxx.crm.dao.UserRoleMapper;
import com.xxxx.crm.model.UserModel;
import com.xxxx.crm.query.CusDevPlanQuery;
import com.xxxx.crm.query.UserQuery;
import com.xxxx.crm.utils.AssertUtil;
import com.xxxx.crm.utils.Md5Util;
import com.xxxx.crm.utils.PhoneUtil;
import com.xxxx.crm.utils.UserIDBase64;
import com.xxxx.crm.vo.CusDevPlan;
import com.xxxx.crm.vo.User;
import com.xxxx.crm.vo.UserRole;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.weaver.NewConstructorTypeMunger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService extends BaseService<User,Integer> {

    @Autowired
    private UserMapper userMapper;


    @Autowired
    private UserRoleMapper userRoleMapper;

    public UserModel login(String userName,String userPwd){
        /**
         * 1.参数校验
         *    用户名  非空
         *    密码    非空
         * 2.根据用户名  查询用户记录
         * 3.用户存在性校验
         *     不存在   -->记录不存在  方法结束
         * 4.用户存在
         *     校验密码
         *        密码错误 -->密码不正确   方法结束
         * 5.密码正确
         *     用户登录成功  返回用户信息
         */
        checkLoginParams(userName,userPwd);
        User user = userMapper.queryUserByUserName(userName);
        AssertUtil.isTrue(null == user,"用户不存在或已注销!");
        AssertUtil.isTrue(!(user.getUserPwd().equals(Md5Util.encode(userPwd))),"用户密码不正确，请重新输入!");
        return buildUserInfo(user);
    }

    private UserModel buildUserInfo(User user) {
        UserModel userModel=new UserModel();
        userModel.setUserIdStr(UserIDBase64.encoderUserID(user.getId()));
        userModel.setUserName(user.getUserName());
        userModel.setTrueName(user.getTrueName());
        return userModel;
    }

    private void checkLoginParams(String userName, String userPwd) {
        AssertUtil.isTrue(StringUtils.isBlank(userName),"用户名不能为空!");
        AssertUtil.isTrue(StringUtils.isBlank(userPwd),"用户密码不能为空!");
    }

    public void updateUserPassword(Integer userId,String oldPassword,String newPassword,String confirmPassword){
        /**
         * 1.参数校验
         *     userId  非空  记录必须存在
         *     oldPassword  非空 与数据库密文 密码保持一致
         *     newPassword  非空   与原始密码不能相同
         *     confirmPassword  非空 与新密码保持一致
         * 2.设置用户新密码
         *     新密码进行加密处理
         * 3.执行更新操作
         */
        checkParams(userId,oldPassword,newPassword,confirmPassword);
        User user =userMapper.selectByPrimaryKey(userId);
        user.setUserPwd(Md5Util.encode(newPassword));
        AssertUtil.isTrue(updateByPrimaryKeySelective(user)<1,"用户密码更新失败!");
    }

    private void checkParams(Integer userId, String oldPassword, String newPassword, String confirmPassword) {
        User temp =userMapper.selectByPrimaryKey(userId);
        AssertUtil.isTrue(null== userId || null==temp,"用户未登录或不存在!");
        AssertUtil.isTrue(StringUtils.isBlank(oldPassword),"请输入原始密码!");
        AssertUtil.isTrue(StringUtils.isBlank(newPassword),"请输入新密码!");
        AssertUtil.isTrue(StringUtils.isBlank(confirmPassword),"请输入确认密码!");
        AssertUtil.isTrue(!(temp.getUserPwd().equals(Md5Util.encode(oldPassword))),"原始密码不正确!");
        AssertUtil.isTrue(!(newPassword.equals(confirmPassword)),"新密码输入不一致!");
        AssertUtil.isTrue(oldPassword.equals(newPassword),"新密码与原始密码不能相同!");
    }


    public List<Map<String,Object>>  queryAllSales(){
        return userMapper.queryAllSales();
    }


    public Map<String,Object> queryUsersByParams(UserQuery userQuery){
        Map<String,Object> map=new HashMap<String,Object>();
        PageHelper.startPage(userQuery.getPage(),userQuery.getLimit());
        PageInfo<User> pageInfo=new PageInfo<User>(selectByParams(userQuery));
        map.put("code",0);
        map.put("msg","");
        map.put("count",pageInfo.getTotal());
        map.put("data",pageInfo.getList());
        return  map;
    }


    public  void saveUser(User user){
        /**
         * 1.参数校验
         *     用户名 非空 值唯一
         *     email  非空  格式合法
         *     手机号非空  格式合法
         * 2.默认参数设置
         *     isValid  1
         *     createDate  系统时间
         *     updateDate 系统时间
         *     默认密码设置   123456
         * 3.执行添加
         */
        checkFormParams(user.getUserName(),user.getEmail(),user.getPhone());
        AssertUtil.isTrue(null !=userMapper.queryUserByUserName(user.getUserName()),"用户名不能重复!");
        user.setIsValid(1);
        user.setCreateDate(new Date());
        user.setUpdateDate(new Date());
        user.setUserPwd(Md5Util.encode("123456"));
        AssertUtil.isTrue(insertSelective(user)<1,"用户记录添加失败!");


        // 用户角色管理(t_user_role)    user_id   role_id
        // 获取添加的用户id 主键
        Integer userId = userMapper.queryUserByUserName(user.getUserName()).getId();
        // 10,20,30
        String roleIds=user.getRoleIds();
        /**
         * 批量添加用户角色记录到用户角色表
         */
        relationUserRoles(userId,roleIds);





    }


    /**
     * 用户角色管理
     * @param userId
     * @param roleIds
     */
    private void relationUserRoles(Integer userId, String roleIds) {
        /**
         *  用户修改(添加同样适用)时
         *     用户原始的角色记录
         *       存在
         *          *          81    (1,2)-->81  null
         *          *          81   (1,2)  -->81  1,2,3,4
         *          *          81  (1,2)-->81 2
         *       不存在
         *          直接执行批量添加(选择角色记录)
         *   推荐方案--> 首先将用户原始用户角色记录删除(存在情况)  然后加入修改后的用户角色记录(选择角色记录)
         */
        int total = userRoleMapper.countUserRoleByUserId(userId);
        if(total>0){
            AssertUtil.isTrue(userRoleMapper.deleteUserRoleByUserId(userId)!=total,"用户角色记录关联失败!");
        }


        if(StringUtils.isNotBlank(roleIds)){
            List<UserRole> userRoles=new ArrayList<UserRole>();
            for(String s : roleIds.split(",")){
                UserRole userRole=new UserRole();
                userRole.setCreateDate(new Date());
                userRole.setRoleId(Integer.parseInt(s));
                userRole.setUpdateDate(new Date());
                userRole.setUserId(userId);
                userRoles.add(userRole);
            }
            AssertUtil.isTrue(userRoleMapper.insertBatch(userRoles)!=userRoles.size(),"用户角色记录管理失败!");
        }
    }

    private void checkFormParams(String userName, String email, String phone) {
        AssertUtil.isTrue(StringUtils.isBlank(userName),"请输入用户名!");
        AssertUtil.isTrue(StringUtils.isBlank(email),"请输入邮箱!");
        AssertUtil.isTrue(!(PhoneUtil.isMobile(phone)),"手机号格式非法!");
    }


    public  void updateUser(User user){
        /**
         * 1.参数校验
         *     id 记录存在
         *     用户名 非空 值唯一
         *     email  非空  格式合法
         *     手机号非空  格式合法
         * 2.默认参数设置
         *     updateDate 系统时间
         * 3.执行更新
         */
        User temp =selectByPrimaryKey(user.getId());
        AssertUtil.isTrue(null==temp,"待更新多的用户记录不存在!");
        checkFormParams(user.getUserName(),user.getEmail(),user.getPhone());
        temp = userMapper.queryUserByUserName(user.getUserName());
        AssertUtil.isTrue(null !=temp && !(temp.getId().equals(user.getId())),"该用户已存在!");
        user.setUpdateDate(new Date());
        AssertUtil.isTrue(updateByPrimaryKeySelective(user)<1,"用户记录更新失败!");


        /**
         *  用户修改时
         *     用户原始的角色记录
         *       存在
         *          *          81    (1,2)-->81  null
         *          *          81   (1,2)  -->81  1,2,3,4
         *          *          81  (1,2)-->81 2
         *       不存在
         *          直接执行批量添加(选择角色记录)
         *   推荐方案--> 首先将用户原始用户角色记录删除(存在情况)  然后加入修改后的用户角色记录(选择角色记录)
         */
        relationUserRoles(user.getId(),user.getRoleIds());

    }


    public void deleteUserByIds(Integer[] ids) {
        AssertUtil.isTrue(null==ids || ids.length==0,"请选择待删除的用户记录!");
        AssertUtil.isTrue(deleteBatch(ids)!=ids.length,"用户记录删除失败!");
    }
}
