call mvn clean -DskipTests package
@echo off 

@echo on
cd target
del ROOT.war
ren *.war ROOT.war 

copy ROOT.war C:\Oracle\Middleware\Oracle_Home\user_projects\domains\base_domain\autodeploy\ /y

call C:\Oracle\Middleware\Oracle_Home\user_projects\domains\base_domain\startWebLogic.cmd

rem To avoid input http://127.0.0.1/ROOT, need put a weblogic.xml file in web-inf folder
start http://127.0.0.1/