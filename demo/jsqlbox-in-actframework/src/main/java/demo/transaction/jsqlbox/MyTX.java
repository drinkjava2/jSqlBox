package demo.transaction.jsqlbox;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.drinkjava2.jbeanbox.annotation.AOP;

import demo.transaction.jsqlbox.TransactionDemo.TinyTxBox;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@AOP
public @interface MyTX {
	public Class<?> value() default TinyTxBox.class; 
}

 