cd  core
call mvn clean
call mvn eclipse:clean
cd..

cd jsqlbox-java8
call mvn clean
call mvn eclipse:clean
cd..

cd demo

cd jbooox
call mvn clean
call mvn eclipse:clean
cd..

cd jsqlbox-beetl
call mvn clean
call mvn eclipse:clean
cd.. 

cd jsqlbox-in-actframework
call mvn clean
call mvn eclipse:clean
call del .act.*
call del act.*
cd..

cd jsqlbox-in-jfinal
call mvn clean
call mvn eclipse:clean
call del *.log
cd..

cd jsqlbox-in-spring
call mvn clean
call mvn eclipse:clean
cd..

cd jsqlbox-in-springboot
call mvn clean
call mvn eclipse:clean
cd..

cd jsqlbox-in-springboot-mybatis
call mvn clean
call mvn eclipse:clean
cd..

cd jsqlbox-java8-demo
call mvn clean
call mvn eclipse:clean
cd..

cd jsqlbox-xa-atomikos
call mvn clean
call mvn eclipse:clean
del tmlog*.log
del tmlog.lck

cd..
cd.. 