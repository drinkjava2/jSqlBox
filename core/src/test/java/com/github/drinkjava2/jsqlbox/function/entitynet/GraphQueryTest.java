package com.github.drinkjava2.jsqlbox.function.entitynet;

import static com.github.drinkjava2.jsqlbox.DB.$;
import static com.github.drinkjava2.jsqlbox.DB.$1;
import static com.github.drinkjava2.jsqlbox.DB.entity;
import static com.github.drinkjava2.jsqlbox.DB.key;
import static com.github.drinkjava2.jsqlbox.DB.ms;
import static com.github.drinkjava2.jsqlbox.DB.one;
import static com.github.drinkjava2.jsqlbox.DB.pagin;
import static com.github.drinkjava2.jsqlbox.DB.que;

import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jsqlbox.DB;
import com.github.drinkjava2.jsqlbox.GraphQuery;
import com.github.drinkjava2.jsqlbox.config.TestBase;
import com.github.drinkjava2.jsqlbox.function.entitynet.entity.Address;
import com.github.drinkjava2.jsqlbox.function.entitynet.entity.Email;
import com.github.drinkjava2.jsqlbox.function.entitynet.entity.Privilege;
import com.github.drinkjava2.jsqlbox.function.entitynet.entity.Role;
import com.github.drinkjava2.jsqlbox.function.entitynet.entity.RolePrivilege;
import com.github.drinkjava2.jsqlbox.function.entitynet.entity.User;
import com.github.drinkjava2.jsqlbox.function.entitynet.entity.UserRole;
import com.github.drinkjava2.util.JsonUtil;

public class GraphQueryTest extends TestBase {
    {
        regTables(User.class, Email.class, Address.class, Role.class, Privilege.class, UserRole.class, RolePrivilege.class);
    }

    protected void insertDemoData() {
        new User().putField("id", "u1").putField("userName", "user1").insert();
        new User().putField("id", "u2").putField("userName", "user2").insert();
        new User().putField("id", "u3").putField("userName", "user3").insert();
        new User().putField("id", "u4").putField("userName", "user4").insert();
        new User().putField("id", "u5").putField("userName", "user5").insert();

        new Address().putField("id", "a1", "addressName", "address1", "userId", "u1").insert();
        new Address().putField("id", "a2", "addressName", "address2", "userId", "u2").insert();
        new Address().putField("id", "a3", "addressName", "address3", "userId", "u3").insert();
        new Address().putField("id", "a4", "addressName", "address4", "userId", "u4").insert();
        new Address().putField("id", "a5", "addressName", "address5", "userId", "u5").insert();

        new Email().forFields("id", "emailName", "userId");
        new Email().putValues("e1", "email1", "u1").insert();
        new Email().putValues("e2", "email2", "u1").insert();
        new Email().putValues("e3", "email3", "u2").insert();
        new Email().putValues("e4", "email4", "u2").insert();
        new Email().putValues("e5", "email5", "u3").insert();

        new Role().forFields("id", "roleName");
        new Role().putValues("r1", "role1").insert();
        new Role().putValues("r2", "role2").insert();
        new Role().putValues("r3", "role3").insert();
        new Role().putValues("r4", "role4").insert();
        new Role().putValues("r5", "role5").insert();

        new Privilege().forFields("id", "privilegeName");
        new Privilege().putValues("p1", "privilege1").insert();
        new Privilege().putValues("p2", "privilege2").insert();
        new Privilege().putValues("p3", "privilege3").insert();
        new Privilege().putValues("p4", "privilege4").insert();
        new Privilege().putValues("p5", "privilege5").insert();

        new UserRole().forFields("userId", "rid");
        new UserRole().putValues("u1", "r1").insert();
        new UserRole().putValues("u2", "r1").insert();
        new UserRole().putValues("u2", "r2").insert();
        new UserRole().putValues("u2", "r3").insert();
        new UserRole().putValues("u3", "r4").insert();
        new UserRole().putValues("u4", "r1").insert();

        new RolePrivilege().forFields("rid", "pid");
        new RolePrivilege().putValues("r1", "p1").insert();
        new RolePrivilege().putValues("r2", "p1").insert();
        new RolePrivilege().putValues("r2", "p2").insert();
        new RolePrivilege().putValues("r2", "p3").insert();
        new RolePrivilege().putValues("r3", "p3").insert();
        new RolePrivilege().putValues("r4", "p1").insert();
    }

    public void testMapListGraphQuery() {
        insertDemoData();
        Systemout.setAllowPrint(true);
        DB.gctx().setAllowShowSQL(true);
        GraphQuery q1 = //
                $("addresstb as a", "where id>", que("a0"), //
                        $1("usertb", ms("userId", "id"), //
                                $("userroletb", ms("id", "userId"), //
                                        $("roletb as role", ms("rid", "id"), //
                                                $("roleprivilegetb as rp", ms("id", "rid"), //
                                                        $1("privilegetb as privilege", ms("pid", "id")) //                                                     
                                                )//
                                        )//
                                ), //
                                $("emailtb", ms("id", "userId"), one), //
                                $1("addresstb as address", ms("id", "userId"))//
                        )//
                );
        GraphQuery q2 = //
                $("usertb as u", "where id>", que("u2"), pagin(1, 10), //
                        $("emailtb", ms("id", "userId"), one), //
                        $1("addresstb", ms("id", "userId"))//
                );
        Object result = DB.graphQuery(q1, q2);
        String json = JsonUtil.toJSONFormatted(result);
        Systemout.println(json);
    }

    @Test
    public void testObjectGraphQuery() {
        insertDemoData();
        Systemout.setAllowPrint(true);
        DB.gctx().setAllowShowSQL(true);
        GraphQuery q1 = //
                $("addresstb as a", "where id>", que("a0"), entity(Address.class), //
                        $1("usertb", key("user"), ms("userId", "id"), entity(User.class), //user是SQL保留字，采用手工指定key
                                $("userroletb as userRoleList", ms("id", "userId"), entity(UserRole.class), //
                                        $("roletb as roleList", ms("rid", "id"), entity(Role.class), //
                                                $("roleprivilegetb as rolePrivilegeList", ms("id", "rid"), entity(RolePrivilege.class), //
                                                        $1("privilegetb as privilege", ms("pid", "id"), entity(Privilege.class)) //                                                 
                                                )//
                                        )//
                                ), //
                                $1("emailtb as email", ms("id", "userId"), one, entity(Email.class)), //
                                $("addresstb as addressList", ms("id", "userId"), entity(Address.class))//
                        )//
                );
        GraphQuery q2 = //
                $("usertb as u", "where id>", que("u2"), pagin(1, 10), entity(User.class), //
                        $1("emailtb as email", ms("id", "userId"), one, entity(Email.class)), //
                        $("addresstb as addressList", ms("id", "userId"), entity(Address.class))//
                );
        Object result = DB.graphQuery(q1, q2);
        Systemout.println(result);
        String json = JsonUtil.toJSONFormatted(result);
        Systemout.println(json);
    }

}