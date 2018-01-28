package jiankunking;

/**
 * 需要动态代理的接口
 */
public interface Subject
{
    /**
     * 你好
     *
     * @param name
     * @return
     */
    public String SayHello(String name);

    /**
     * 再见
     *
     * @return
     */
    public String SayGoodBye();
}