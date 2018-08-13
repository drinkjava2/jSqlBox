package com.demo.blog;

import static com.github.drinkjava2.jsqlbox.JSQLBOX.pagin;

import java.util.List;

import com.demo.common.Blog;
import com.jfinal.aop.Before;
import com.jfinal.aop.Enhancer;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;

/**
 * 本 demo 仅表达最为粗浅的 jfinal 用法，更为有价值的实用的企业级用法 详见 JFinal 俱乐部:
 * http://jfinal.com/club
 * 
 * BlogService 所有 sql 与业务逻辑写在 Service 中，不要放在 Model 中，更不 要放在 Controller
 * 中，养成好习惯，有利于大型项目的开发与维护
 */
public class BlogService {

	/**
	 * 线程安全的 Service 可以new一个 static me 变量，方便随处使用, 如果要使用业务层 AOP支持声明式事务，可以使用如下代码创建：
	 * Enhancer.enhance(BlogService.class);
	 */
	public static final BlogService me = Enhancer.enhance(BlogService.class);

	public Page<Blog> paginate(int pageNumber, int pageSize) {
		List<Blog> blogs = new Blog().findAll(pagin(pageNumber, pageSize), " order by id asc");
		int totalRows = new Blog().countAll();
		return new Page<Blog>(blogs, pageNumber, pageSize, (totalRows-1)/pageSize+1, totalRows);
	}

	public Blog findById(int id) {
		return 	new Blog().loadById(id);
	}

	@Before(Tx.class)
	public void deleteById(int id) {
		new Blog().deleteById(id); 
		//int i=1/0;  //如果加上这句，事务将回滚
	}
}
