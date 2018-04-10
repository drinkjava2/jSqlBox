### 在MyBatis中使用
MyBatis已经有了很多分页插件，所以jDialects的分页功能对于MyBatis不重要，但MyBatis不支持跨数据库的DDL生成和函数变换功能，这时可以考虑用上jSqlBox(内含jDialets)，例如下例jSqlBox的使用使得MyBatis具有了DDL生成、分页、函数变换、ActiveRecord支持的能力。  
```
public class Demo {
	protected static ThreadLocal<Object[]> paginInfo = new ThreadLocal<Object[]>();

	@Table(name = "users")
	public static class User extends ActiveRecord {
		@UUID25
		@Id
		private String id;
		private String firstName;
		private String lastName;
		private Integer age;
		//getter & setters 略
	}

	public static interface UserMapper {
		@Select("select concat(firstName, ' ', lastName) as USERNAME, age as AGE from users where age>#{age}")
		List<Map<String, Object>> getOlderThan(int age);
	}

	//See https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/Interceptor.md
	@Intercepts({
			@Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
					RowBounds.class, ResultHandler.class }),
			@Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
					RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class }), })
	public class JDialectsPlugin implements Interceptor {

		@Override
		public Object intercept(Invocation invocation) throws Throwable {
			Object[] args = invocation.getArgs();
			MappedStatement ms = (MappedStatement) args[0];
			Object parameter = args[1];
			RowBounds rowBounds = (RowBounds) args[2];
			@SuppressWarnings("rawtypes")
			ResultHandler resultHandler = (ResultHandler) args[3];
			Executor executor = (Executor) invocation.getTarget();
			CacheKey cacheKey;
			BoundSql boundSql;
			if (args.length == 4) {
				boundSql = ms.getBoundSql(parameter);
				cacheKey = executor.createCacheKey(ms, parameter, rowBounds, boundSql);
			} else {
				cacheKey = (CacheKey) args[4];
				boundSql = (BoundSql) args[5];
			}
			if (paginInfo.get() != null) {// if paginInfo exist in threadlocal
				Configuration configuration = ms.getConfiguration();
				String pageSql = ((Dialect) paginInfo.get()[0]).paginAndTrans((int) paginInfo.get()[1],
						(int) paginInfo.get()[2], boundSql.getSql());
				BoundSql pageBoundSql = new BoundSql(configuration, pageSql, boundSql.getParameterMappings(),
						parameter);
				return executor.query(ms, parameter, RowBounds.DEFAULT, resultHandler, cacheKey, pageBoundSql);
			} else
				return executor.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
		}

		@Override
		public Object plugin(Object target) {
			return Plugin.wrap(target, this);
		}

		@Override
		public void setProperties(Properties properties) {
		}

	}

	@Test
	public void doTest() {
		HikariDataSource dataSource = new HikariDataSource();// DataSource

		// H2 is a memory database
		dataSource.setDriverClassName("org.h2.Driver");
		dataSource.setJdbcUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		dataSource.setUsername("sa");
		dataSource.setPassword("");

		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		SqlBoxContext.setDefaultContext(ctx);
		ctx.setAllowShowSQL(true);
		String[] ddlArray = ctx.toDropAndCreateDDL(User.class);
		for (String ddl : ddlArray)
			ctx.quiteExecute(ddl);
		for (int i = 1; i <= 100; i++)
			new User().put("firstName", "Foo" + i, "lastName", "Bar" + i, "age", i).insert();

		TransactionFactory transactionFactory = new JdbcTransactionFactory();
		Environment environment = new Environment("demo", transactionFactory, dataSource);
		Configuration configuration = new Configuration(environment);
		configuration.addMapper(UserMapper.class);
		SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
		configuration.addInterceptor(new JDialectsPlugin());

		SqlSession session = null;
		try {
			session = sqlSessionFactory.openSession();
			Connection conn = session.getConnection();
			Assert.assertEquals(100, ctx.nQueryForLongValue(conn, "select count(*) from users"));

			List<Map<String, Object>> users;
			try {
				paginInfo.set(new Object[] { ctx.getDialect(), 3, 10 });
				users = session.getMapper(UserMapper.class).getOlderThan(50);
			} finally {
				paginInfo.remove();
			}
			Assert.assertEquals(10, users.size());
			for (Map<String, Object> map : users)
				System.out.println("UserName=" + map.get("USERNAME") + ", age=" + map.get("AGE"));
		} finally {
			session.close();
		}
		dataSource.close();
	}
}
```
上例代码看起来很长，但是考虑到其中包含了一个晦涩难懂的MyBatis插件、数据源和MyBatis环境配置，这个长度同学们应该能接受吧。  
同样地，此例可以运行在所有数据库上，以上代码只需要更改数据源配置，就可以自动适应多种数据库，不必为每一种数据库写对应的DDL和SQL了(看起来和MyBatis的“一种数据库写一种SQL”的口号背道而驰了) 。  
上例中的JDialectsPlugin是一个MyBatis插件，通常在MyBatis中是用xml配置的，只是为了演示，我把所有内容都塞到上面一个Java文件里了，没用到XML。  
这个示例中简单地用ThreadLocal来传递分页参数到plugIn中，这是我能想到的最简单、最懒惰的解决方案，但为了安全起见，必须用try-finally块在分页结束后确保清除ThreadLocal参数，否则后果很严重。  
上例的完整源码请参见这里：[Demo源码](../../jDialects/blob/master/demo/demo-mybatis/src/test/java/com/github/drinkjava2/demo/Demo.java)。