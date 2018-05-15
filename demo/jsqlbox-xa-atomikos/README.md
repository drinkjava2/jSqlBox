## jsqlbox-xa-atomikos （Use Spring)

这是一个演示项目，演示jSqlBox进行分库操作时，利用atomikos来实现分布式事务以保证多个库之间的数据一致性。

Reference:
https://www.atomikos.com/Main/InstallingTransactionsEssentials
http://forum.spring.io/forum/spring-projects/integration/119662-spring-integration-flow-and-xa-transactions/page2
http://blog.51cto.com/aiilive/1658102
http://blog.itpub.net/28624388/viewspace-2137095/
http://www.cnblogs.com/cczhoufeng/archive/2012/05/16/2502769.html
https://my.oschina.net/pingpangkuangmo/blog/423210
http://sparkgis.com/java/2017/12/spring-%E5%A4%9A%E6%95%B0%E6%8D%AE%E6%BA%90-%E9%9B%86%E6%88%90jta-atomikos%E5%AE%9E%E7%8E%B0%E5%8A%A8%E6%80%81%E5%88%87%E6%8D%A2%E6%95%B0%E6%8D%AE%E6%BA%90-%E5%8E%9F-spring-%E5%A4%9A%E6%95%B0/
https://blog.csdn.net/luo_deng/article/details/50525073

WARNING
=======

This example does not guarantee the right startup ordering for the Atomikos beans. Only ExtremeTransactions (https://www.atomikos.com/Main/ExtremeTransactions) can do this.

ARCHITECTURE
------------

        2. receive                    3. onMessage
Queue <--------- MessageDrivenContainer ---------> MessageDrivenBank
      <---------      |                             |
      4. commit |     |                             | 3.1 withdraw
        (delete)|     |  1. begin tx                |
                |     |  4. commit tx               --> Bank
                |     |                                 |
                |     |                                 | 3.1.1 SQL
                |     |                                 |
                |     -->                               |
                --------  Atomikos  ---------------->   --> Database
                                        4. commit           
