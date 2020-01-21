cd  core
call mvn clean
call mvn eclipse:clean
cd..
 
cd demo

cd jbooox
call mvn clean
call mvn eclipse:clean
cd..

cd beetl
call mvn clean
call mvn eclipse:clean
cd.. 

cd actframework
call mvn clean
call mvn eclipse:clean
call del .act.*
call del act.*
cd..

cd jfinal
call mvn clean
call mvn eclipse:clean
call del *.log
cd..

cd spring
call mvn clean
call mvn eclipse:clean
cd..

cd springboot
call mvn clean
call mvn eclipse:clean
cd..

cd mybatis
call mvn clean
call mvn eclipse:clean
cd..

cd java8
call mvn clean
call mvn eclipse:clean
cd..

cd xa-atomikos
call mvn clean
call mvn eclipse:clean
del tmlog*.log
del tmlog.lck

cd..
cd.. 