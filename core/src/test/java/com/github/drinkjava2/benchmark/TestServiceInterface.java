package com.github.drinkjava2.benchmark;

public interface TestServiceInterface {

    /**
     * 测试添加的方法
     */
    void testAdd();

    /**
     * 测试根据id查询的方法
     */
    void testUnique();

    /**
     * 测试根据id更新数据的方法
     */
    void testUpdateById();

    /**
     * 分页查询
     */
    void testPageQuery();

    /**
     *
     */
    void testExampleQuery();
    
    void testOrmQUery();
}
