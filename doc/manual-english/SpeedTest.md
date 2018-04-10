The following is the performance test of jSqlBox different SQL writing loop 10000 CRUD operations (see the test source [here] (../blob/master/core/src/test/java/com/github/drinkjava2/helloworld/UsuageAndSpeedTest.java) ), run on the H2 memory database (i3 CPU), you can exclude the impact of disk read and write, reflecting the performance of the framework itself.  
```
Compare method execute time for repeat 10000 times:
                    pureJdbc: 0.266 s
       dbUtilsWithConnMethod: 0.359 s
         dbUtilsNoConnMethod: 0.390 s
                   nXxxStyle: 0.391 s
                   iXxxStyle: 0.515 s
                   tXxxStyle: 1.591 s
                   xXxxStyle: 1.623 s
     xXxxStyle_BasicTemplate: 1.562 s
             dataMapperStyle: 0.891 s
           activeRecordStyle: 0.702 s
  activeRecordDefaultContext: 0.734 s
       sqlMapperSqlAnnotaion: 3.261 s
            sqlMapperUseText: 3.651 s
    abstractSqlMapperUseText: 3.651 s
```

This test does not include NoSQL-type queries because NoSQL currently has only a query method and no write method in this project, so no comparison test was added.  