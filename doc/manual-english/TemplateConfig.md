The jSqlBox template is configured as follows:
```
DataSource dataSource=....;
SqlBoxContextConfig config=new SqlBoxContextConfig();
/ / Set a template engine, if you do not configure, default use jSqlBox own template engine
config.setTemplateEngine (a template engine instance);
SqlBoxContext ctx=new SqlBoxContext (dataSource, config);
```
jSqlBox comes with a template engine NamedParamSqlTemplate, which is a simple template engine, there is no syntax support, using "#{variable name}" or ":variable name" as a placeholder, and can be mixed, the runtime will simply The placeholder's location resolves to a question mark.  

jSqlBox also comes with a BasicSqlTemplate template engine, which is more simple, only allows the use of "#{variable name}" as a placeholder. If you want to use this template engine, you can set it like this:  
```
SqlBoxContextConfig config=new SqlBoxContextConfig();  
config.setTemplateEngine(BasicSqlTemplate.instance());  
SqlBoxContext ctx=new SqlBoxContext (dataSource, config);  
```

If you want to replace jSqlBox's default template engine with its own template engine, you need to implement the SqlTemplateEngine interface and set it using the config.setTemplateEngine method.  

The SqlBoxContext also provides the following static methods for quickly configuring the template engine. The purpose of configuring the template engine can be achieved by calling the following static methods before the new SlqBoxContext() or new SlqBoxContext(DataSource) method:  
SqlBoxContext.setGlobalTemplateEngine (a template engine);  
This method starts with "setGlobal", which means that it is a global switch method that will affect the default configuration of the entire project, so use it with caution, usually only once at the beginning of the program. Note that it only affects the creation of the next instance of SlqBoxContext. Calling this static method without affecting the previous SqlBoxContext instance when the SqlBoxContext instance has been created.  