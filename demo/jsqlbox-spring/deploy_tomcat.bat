call mvn clean -DskipTests package 

rd /s/q /q C:\tomcat8\logs
md C:\tomcat8\logs

rd /s/q /q C:\tomcat8\work
md C:\tomcat7\work

rd /s/q /q C:\tomcat8\webapps
md C:\tomcat8\webapps 

cd target
del ROOT.war
ren *.war ROOT.war 
copy ROOT.war C:\tomcat8\webapps\ /y 

c:
cd C:\tomcat8\bin\
call startup.bat

start http://127.0.0.1 
