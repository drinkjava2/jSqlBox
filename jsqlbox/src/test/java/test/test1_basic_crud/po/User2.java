package test.test1_basic_crud.po;

import java.lang.reflect.Field;

/**
 * This file should automatically created by a code generator robot
 *
 */
public class User2 extends User {
	public static final String Table = "user2";

	public static final String UserName = "username2";

	public static final String Address = "address2";

	public static final String Age = "age2";

	public static void main(String[] args) throws Exception {
		Field[] fields = User2.class.getFields();
		for (Field field : fields) {
			System.out.println(field.getDeclaringClass());
			System.out.println(field.getName() + "=" + field.get(null));
		}
	}
}