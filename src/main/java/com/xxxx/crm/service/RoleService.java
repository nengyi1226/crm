package com.xxxx.crm.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xxxx.base.BaseService;
import com.xxxx.crm.dao.ModuleMapper;
import com.xxxx.crm.dao.PermissionMapper;
import com.xxxx.crm.dao.RoleMapper;
import com.xxxx.crm.dao.UserRoleMapper;
import com.xxxx.crm.query.RoleQuery;
import com.xxxx.crm.query.UserQuery;
import com.xxxx.crm.utils.AssertUtil;
import com.xxxx.crm.vo.Permission;
import com.xxxx.crm.vo.Role;
import com.xxxx.crm.vo.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RoleService extends BaseService<Role,Integer> {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private PermissionMapper permissionMapper;


    @Autowired
    private ModuleMapper moduleMapper;


    public List<Map<String,Object>> queryAllRoles(Integer userId){
        return roleMapper.queryAllRoles(userId);
    }


    public Map<String,Object> queryRolesByParams(RoleQuery roleQuery){
        Map<String,Object> map=new HashMap<String,Object>();
        PageHelper.startPage(roleQuery.getPage(),roleQuery.getLimit());
        PageInfo<Role> pageInfo=new PageInfo<Role>(selectByParams(roleQuery));
        map.put("code",0);
        map.put("msg","");
        map.put("count",pageInfo.getTotal());
        map.put("data",pageInfo.getList());
        return  map;
    }


    public  void saveRole(Role role){
        /**
         * 1.参数校验
         *    角色名 非空 唯一
         * 2.参数默认值设置
         *    isValid
         *    createDate
         *    updateDate
         * 3.执行添加  判断结果
         */
        AssertUtil.isTrue(StringUtils.isBlank(role.getRoleName()),"请输入角色名称!");
        AssertUtil.isTrue(null !=roleMapper.queryRoleByRoleName(role.getRoleName()),"该角色已存在!");
        role.setIsValid(1);
        role.setCreateDate(new Date());
        role.setUpdateDate(new Date());
        AssertUtil.isTrue(insertSelective(role)<1,"角色记录添加失败!");
    }


    public  void updateRole(Role role){
        /**
         * 1.参数校验
         *    角色名 非空 唯一
         * 2.参数默认值设置
         *    updateDate
         * 3.执行更新  判断结果
         */
        AssertUtil.isTrue(StringUtils.isBlank(role.getRoleName()),"请输入角色名称!");
        Role temp =roleMapper.queryRoleByRoleName(role.getRoleName());
        AssertUtil.isTrue(null !=temp && !(temp.getId().equals(role.getId())) ,"该角色已存在!");
        role.setUpdateDate(new Date());
        AssertUtil.isTrue(updateByPrimaryKeySelective(role)<1,"角色记录更新失败!");
    }


    public void deleteRole(Integer roleId){
        /**
         * 1.参数校验
         *    非空  记录必须存在
         * 2.查询用户角色表记录
         *     如果存在子表记录  删除子表记录
         * 3.执行角色删除操作 判断结果
         */
        Role role=selectByPrimaryKey(roleId);
        AssertUtil.isTrue(null == role,"待删除的记录不存在!");
        int total =userRoleMapper.countUserRoleByRoleId(roleId);
        if(total>0){
            AssertUtil.isTrue(userRoleMapper.deleteUserRoleByRoleId(roleId)!=total,"用户角色记录删除失败!");
        }

        role.setIsValid(0);
        AssertUtil.isTrue(updateByPrimaryKeySelective(role)<1,"角色记录删除失败!");
    }


    /**
     * 角色授权
     * @param mids
     * @param roleId
     */
    public void addGrant(Integer[] mids, Integer roleId) {
        /**
         *   授权思路  核心表 t_permission
         *      直接批量添加  不适合 有可能是对角色权限进行更新(权限更新后有可能添加新的菜单 删除原始菜单  甚至情况权限)
         *  推荐: 角色存在原始权限时  先删除原始权限记录  然后批量添加新的角色权限
         */
        int total = permissionMapper.countPermissionByRoleId(roleId);
        if(total>0){
            AssertUtil.isTrue(permissionMapper.deletePermissionByRoleId(roleId)!=total,"角色授权失败!");
        }

        if(null !=mids && mids.length>0){
            List<Permission> permissions=new ArrayList<Permission>();
            for(Integer mid:mids){
                Permission permission=new Permission();
                permission.setCreateDate(new Date());
                permission.setModuleId(mid);
                permission.setRoleId(roleId);
                permission.setUpdateDate(new Date());
                permission.setAclValue(moduleMapper.selectByPrimaryKey(mid).getOptValue());
                permissions.add(permission);
            }
            AssertUtil.isTrue(permissionMapper.insertBatch(permissions)!=permissions.size(),"角色授权失败!");
        }
    }
}
