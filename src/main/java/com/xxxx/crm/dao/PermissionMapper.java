package com.xxxx.crm.dao;

import com.xxxx.base.BaseMapper;
import com.xxxx.crm.vo.Permission;

import java.util.List;

public interface PermissionMapper extends BaseMapper<Permission,Integer> {


    public int  countPermissionByRoleId(Integer roleId);

    public int  deletePermissionByRoleId(Integer roleId);

    public List<Integer> queryRoleHasAllMids(Integer roleId);

    List<String>  queryUserHasRoleIdsHasModuleIds(Integer userId);


    Integer  countPermissionByModuleId(Integer mid);

    int  deletePermissionByModuleId(Integer mid);
}