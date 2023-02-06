package com.xxxx.crm.service;

import com.xxxx.base.BaseService;
import com.xxxx.crm.dao.PermissionMapper;
import com.xxxx.crm.vo.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissionService extends BaseService<Permission,Integer> {

    @Autowired
    private PermissionMapper permissionMapper;
    public List<String> queryUserHasRoleIdsHasModuleIds(Integer userId) {
        return  permissionMapper.queryUserHasRoleIdsHasModuleIds(userId);
    }
}
