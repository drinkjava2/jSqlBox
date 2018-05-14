## jsqlbox-xa-atomikos （Use Spring)

这是一个演示项目，演示jSqlBox进行分库操作时，利用atomikos来实现分布式事务以保证多个库之间的数据一致性。

Reference:
https://www.atomikos.com/Main/InstallingTransactionsEssentials
http://forum.spring.io/forum/spring-projects/integration/119662-spring-integration-flow-and-xa-transactions/page2
http://blog.51cto.com/aiilive/1658102
http://blog.itpub.net/28624388/viewspace-2137095/
http://www.cnblogs.com/cczhoufeng/archive/2012/05/16/2502769.html
https://my.oschina.net/pingpangkuangmo/blog/423210



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
