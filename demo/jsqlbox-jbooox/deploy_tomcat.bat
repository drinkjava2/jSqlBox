call mvn clean -DskipTests package
 
set TomcatFolder=c:\tomcat8
rd /s/q /q %TomcatFolder%\logs
md %TomcatFolder%\logs
rd /s/q /q %TomcatFolder%\work
md %TomcatFolder%\work
rd /s/q /q %TomcatFolder%\webapps
md %TomcatFolder%\webapps 
cd target
del ROOT.war
ren *.war ROOT.war 
copy ROOT.war %TomcatFolder%\webapps\ /y 
call %TomcatFolder%\bin\startup.bat
start http://127.0.0.1 