package com.github.drinkjava2.jsqlbox.function.entitynet.entity;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jsqlbox.ActiveRecord;

@Table(name = "usertb")
public class User extends ActiveRecord<User> {
    @Id
    String id;

    String userName;

    String teatherId;

    String bossId;

    Integer age;

    Address address;

    List<Role> roleList;

    List<UserRole> userRoleList;

    List<Email> emailList;

    List<Address> addressList;

    Email email;
    
    Map<String, Object> emailMap;

    Map<Integer, Role> roleMap;

    Set<Privilege> privilegeSet;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTeatherId() {
        return teatherId;
    }

    public void setTeatherId(String teatherId) {
        this.teatherId = teatherId;
    }

    public String getBossId() {
        return bossId;
    }

    public void setBossId(String bossId) {
        this.bossId = bossId;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public List<Role> getRoleList() {
        return roleList;
    }

    public void setRoleList(List<Role> roleList) {
        this.roleList = roleList;
    }

    public Set<Privilege> getPrivilegeSet() {
        return privilegeSet;
    }

    public void setPrivilegeSet(Set<Privilege> privilegeSet) {
        this.privilegeSet = privilegeSet;
    }

    public Map<Integer, Role> getRoleMap() {
        return roleMap;
    }

    public void setRoleMap(Map<Integer, Role> roleMap) {
        this.roleMap = roleMap;
    }

    public List<Email> getEmailList() {
        return emailList;
    }

    public void setEmailList(List<Email> emailList) {
        this.emailList = emailList;
    }

    public List<UserRole> getUserRoleList() {
        return userRoleList;
    }

    public void setUserRoleList(List<UserRole> userRoleList) {
        this.userRoleList = userRoleList;
    }

    public List<Address> getAddressList() {
        return addressList;
    }

    public void setAddressList(List<Address> addressList) {
        this.addressList = addressList;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public Map<String, Object> getEmailMap() {
        return emailMap;
    }

    public void setEmailMap(Map<String, Object> emailMap) {
        this.emailMap = emailMap;
    }

}