jSqlBox supports both fixed and dynamic configuration, which is a feature of it.  

First look at the fixed configuration:
```
@Table(name = "emailtb")
Public class Email{
@Id
String id;
String emailName;

@SingleFKey(refs = { "usertb", "id" })
String userId;
        ......
}
```
jSqlBox is an ORM tool, which means that for an entity bean such as the Email class above, it must know which database table the class corresponds to, and which column of the database table each entity attribute corresponds to, and each column and other. Foreign key constraints between tables (for jSqlBox can be used to perform associated queries, see the NoSQL query section), Annotation annotation can achieve this goal. However, the configuration of this annotation method is difficult to change during the runtime. I call it a fixed configuration. For example, the XML configuration in Hibernate is difficult to change at runtime, so it is a fixed configuration. The advantage of fixed configuration is that the workload is small and the entire project can be configured once. The disadvantage is inflexibility. For example, the configuration of Hibernate can only have one configuration for one entity. When the relationship between entities is uncertain or it needs to be temporarily changed, it is difficult. deal with. MyBatis responds to different queries and creates different XML configurations temporarily. It looks flexible, but the consequence is that you need to create an XML for each complex association query. The field names, attribute names, and so on are duplicated. Increased a lot of unnecessary workload.  

When the persistence tool reads the contents of an annotation or XML configuration into memory and parses it, it generates a Java object and exposes its access methods, allowing changes to the configuration at run time, called dynamic configuration, and few persistence tools are currently available. To this point.  

jSqlBox supports fixed configuration and dynamic configuration at the same time, using the fixed configuration to lay the foundation of the entire project configuration. For occasions that need to temporarily change the configuration, you can change the fixed configuration at runtime to obtain a modified copy. This change only needs to be a little Changes can be, the workload is very small, so at the same time jSqlBox has the advantages of fixed configuration and dynamic configuration.  

jSqlBox's fixed configuration and dynamic configuration features are provided by the jDialects module. This is a stand-alone database modeling tool that can be used to create a database-independent model based on annotations or Java methods. This model is also used by jSqlBox. The use of to dynamically modify at runtime, and modify its Java method is the same as the establishment of its Java method.  
In the following example, the fixed and dynamic methods are interspersed and used. The config method is a fixed configuration method for entity classes (this is a convention in jDialects). UserDemoSqlBox (ie, the class name + SqlBox format) is a fixed configuration method for entity classes. (This is a convention in jSqlBox, the configuration can be written outside the class), u.columnModel("id").pkey(); This method dynamically adds primary keys at run time, but it is dynamic configuration, but static and dynamic configuration Use the same syntax.  
```
Public class DynamicConfigTest extends TestBase {
Public static class UserDemo extends ActiveRecord {
@UUID32
Private String id;

@Column(name = "user_name2", length = 32)
Private String userName;
               
                ......

Public static void config(TableModel t) {
t.setTableName("table2");
T.column("user_name2").setColumnName("user_name3");
}
}

Public static class UserDemoSqlBox extends SqlBox {
{
TableModel t = TableModelUtils.entity2Model(UserDemo.class);
t.addColumn("anotherColumn1").STRING(40);
this.setTableModel(t);
}
}

@Test
Public void doTest() {
UserDemo u = new UserDemo();
u.columnModel("id").pkey();
u.setUserName("Sam");
Ctx.insert(u);
}
}
```

Regarding dynamic configuration, the NoSQL query section has seen its application. You can dynamically add and remove foreign key constraints at runtime to implement flexible inter-object correlation queries without having to create a complex from start to finish for each query. Annotation, configuration like XML.  

In addition, entities that dynamically change its SqlBoxContext context instance during runtime are also a special type of dynamic configuration, such as user.userContext(ctx), which is equivalent to dynamically switching data sources at runtime. For a pure POJO class entity, you can use the SqlBoxUtils.findBox(entity) method to get its configuration. This is a SqlBox object with two properties, SqlBoxContext and TableModel.  