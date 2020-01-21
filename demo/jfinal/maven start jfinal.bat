call mvn clean package 
cd target
xcopy ".\jsqlbox-in-jfinal-1.0\*.*" ".\classes\webapp\" /S /D /Y /Q >nul
cd classes 
java -classpath .;.\\webapp\WEB-INF\lib\* com.demo.common.DemoConfig
@pause