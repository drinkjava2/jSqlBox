cd jsqlbox-actframework
call mvn clean
call mvn eclipse:clean
call del .act.*
call del act.*
cd..

cd jsqlbox-atomikos
call mvn clean
call mvn eclipse:clean
del tmlog*.log
del tmlog.lck
cd..

cd jsqlbox-beetl
call mvn clean
call mvn eclipse:clean
cd.. 

cd jsqlbox-jbooox
call mvn clean
call mvn eclipse:clean
cd..

cd jsqlbox-jfinal
call mvn clean
call mvn eclipse:clean
call del *.log
cd..

cd jsqlbox-mybatis
call mvn clean
call mvn eclipse:clean
cd..

cd jsqlbox-qclass
call mvn clean
call mvn eclipse:clean
cd..

cd jsqlbox-spring
call mvn clean
call mvn eclipse:clean
cd..

cd jsqlbox-springboot
call mvn clean
call mvn eclipse:clean
cd..

