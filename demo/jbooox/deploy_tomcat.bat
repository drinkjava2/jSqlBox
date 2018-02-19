call mvn clean -DskipTests package 

rd /s/q /q C:\tomcat7\logs
md C:\tomcat7\logs

rd /s/q /q C:\tomcat7\work
md C:\tomcat7\work

rd /s/q /q C:\tomcat7\webapps
md C:\tomcat7\webapps 

cd target
del ROOT.war
ren *.war ROOT.war 
copy ROOT.war C:\tomcat7\webapps\ /y 

c:
cd C:\tomcat7\bin\
call startup.bat

start http://127.0.0.1 
