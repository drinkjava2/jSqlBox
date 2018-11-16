package com.demo.common;

import com.demo.blog.BlogController;
import com.demo.index.IndexController;
//import com.github.drinkjava2.jdbpro.DbProConfig;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jtransactions.jfinal.JFinalTxConnectionManager;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.core.JFinal;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Config;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.IContainerFactory;
import com.jfinal.plugin.activerecord.cache.EhCache;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.template.Engine;

/**
 * 本 demo 仅表达最为粗浅的 jfinal 用法，更为有价值的实用的企业级用法 详见 JFinal 俱乐部:
 * http://jfinal.com/club
 * 
 * API引导式配置
 */
public class DemoConfig extends JFinalConfig {
	// 命令行下运行模式:
	private static final String path = "webapp";

	// Eclipse下运行模式， 详见Readme.md:
	// private static final String path = "target/jsqlbox-in-jfinal-1.0";

	public static void main(String[] args) {
		JFinal.start(path, 80, "/", 0);

	}

	//@formatter:off
	/* 声明式事务有3个关键： 
	 * 1.IOC/AOP工具， 这里 就用jFinal自带的 AOP工具
	 * 2.声明式事务切面处理器， 这里就用jFinal自带的
	 * 3.连接管理器。不同的声明式事务工具获取和关闭connection的方式不一样，在jTransactions中为纯JDBC、jFinal、Spring等都准备了一个连接管理器,这里设成JFinalTxConnectionManager。
	 */
	@Override
	public void afterJFinalStart() {  
		SqlBoxContext ctx = new SqlBoxContext(DbKit.getConfig().getDataSource());
		ctx.setConnectionManager(JFinalTxConnectionManager.instance());
		SqlBoxContext.setGlobalSqlBoxContext(ctx);

		// 第一次运行，手工建一个H2数据库表
		String[] ddls = ctx.toCreateDDL(Blog.class);
		for (String ddl : ddls)
			ctx.nExecute(ddl);

		// 第一次运行，插入数据
		new Blog().forFields("id", "title", "content");
		new Blog().putValues(1, "JFinal Demo Title here (这一行是声明式事务自动回滚演示，删不掉) ", "JFinal Demo Content here").insert();
		for (int i = 2; i < 30; i++)
			new Blog().putValues(i, "test " + i, "test " + i).insert();
	}

	/** 配置常量 */
	@Override
	public void configConstant(Constants me) {
		// 加载少量必要配置，随后可用PropKit.get(...)获取值
		PropKit.use("a_little_config.txt");
		me.setDevMode(PropKit.getBoolean("devMode", false));
	}

	/** 配置路由 */
	@Override
	public void configRoute(Routes me) {
		me.add("/", IndexController.class, "/index"); // 第三个参数为该Controller的视图存放路径
		me.add("/blog", BlogController.class); // 第三个参数省略时默认与第一个参数值相同，在此即为 "/blog"
	}

	@Override
	public void configEngine(Engine me) {
		me.addSharedFunction("/common/_layout.html");
		me.addSharedFunction("/common/_paginate.html");
	}

	public static DruidPlugin createDruidPlugin() {
		return new DruidPlugin(PropKit.get("jdbcUrl"), PropKit.get("user"), PropKit.get("password").trim());
	}

	/** 配置插件 */
	@Override
	public void configPlugin(Plugins me) {
		// 配置数据库连接池插件
		DruidPlugin druidPlugin = createDruidPlugin();
		druidPlugin.start();
		me.add(druidPlugin);

		// 这里jFinal随便配一 个MySql方言, 但实际上jSqlBox不用它, 它用自已的H2方言
		Config config = new Config(DbKit.MAIN_CONFIG_NAME, druidPlugin.getDataSource(), new MysqlDialect(), false,
				false, DbKit.DEFAULT_TRANSACTION_LEVEL, IContainerFactory.defaultContainerFactory, new EhCache());
		DbKit.addConfig(config);
	}

	/** 配置全局拦截器 */
	@Override
	public void configInterceptor(Interceptors me) {

	}

	/** 配置处理器 */
	@Override
	public void configHandler(Handlers me) {

	}
}
