package com.github.drinkjava2.jsqlbox.function.entitynet.entity;

import java.util.List;

import com.github.drinkjava2.jdialects.annotation.jdia.SingleFKey;
import com.github.drinkjava2.jdialects.annotation.jdia.UUID25;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jsqlbox.ActiveRecord;

@Table(name = "userroletb")
public class UserRole extends ActiveRecord<UserRole> {
    @Id
    @UUID25
    String id;

    @SingleFKey(refs = {"usertb", "id"})
    String userId;

    @SingleFKey(refs = {"roletb", "id"})
    String rid;

    List<Role> roleList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRid() {
        return rid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    public List<Role> getRoleList() {
        return roleList;
    }

    public void setRoleList(List<Role> roleList) {
        this.roleList = roleList;
    }

}