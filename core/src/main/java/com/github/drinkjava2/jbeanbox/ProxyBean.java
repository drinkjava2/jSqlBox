/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jbeanbox;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.aopalliance.intercept.MethodInvocation;

import com.github.drinkjava2.cglib.proxy.MethodInterceptor;
import com.github.drinkjava2.cglib.proxy.MethodProxy;

/**
 * ProxyBean to build a Invocation, Invocation call next invocation...
 * 
 * @author Yong Zhu
 * @since 2.4
 *
 */
@SuppressWarnings("all")
class ProxyBean implements MethodInterceptor {
	protected Object[] box_ctx;

	protected ProxyBean(BeanBox box, BeanBoxContext ctx) {
		box_ctx = new Object[] { box, ctx };
	}

	@Override
	public Object intercept(Object obj, Method m, Object[] args, MethodProxy mprxy) throws Throwable {

		List<Object> allInters = new ArrayList<Object>();

		BeanBox box = (BeanBox) box_ctx[0];// box method aops
		BeanBoxContext ctx = (BeanBoxContext) box_ctx[1];
		if (box.getMethodAops() != null) {
			List<Object> inters = box.getMethodAops().get(m);
			if (inters != null && !inters.isEmpty())
				allInters.addAll(inters);
		}

		if (box.getAopRules() != null) // box methods aops, need add cache in future
			for (Object[] entry : box.getAopRules()) {
				if (NameMatchUtil.nameMatch((String) entry[1], m.getName()))
					allInters.add(entry[0]);
			}

		if (ctx.getAopRules() != null) {// BeanBoxContext aops, need add cache in future
			String thisClassName = obj.getClass().getName();

			for (Object[] aops : ctx.getAopRules()) {
				String classReg = (String) aops[1];
				if (NameMatchUtil.nameMatch(classReg, thisClassName)) {
					String methodRegex = (String) aops[2];
					if (NameMatchUtil.nameMatch(methodRegex, m.getName()))
						allInters.add(aops[0]);
				}
			}
		}

		if (allInters.isEmpty())
			return mprxy.invokeSuper(obj, args);
		// inters can be AOP class, AOP instance, BeanBox class, BeanBox instance
		org.aopalliance.intercept.MethodInterceptor inter = ctx.getBean(allInters.get(0));
		BeanBoxException.assureNotNull(inter);
		return inter.invoke(new MethodInvoc(obj, m, args, mprxy, allInters, ctx, 1));
	}

	//@formatter:off
	public static class MethodInvoc implements MethodInvocation {// AOP alliance required
		private final Object obj;
		private final Method m;
		private final Object[] args;
		private final MethodProxy mprxy;
		private final List<Object> inters;
		private final BeanBoxContext ctx;
		private int count;

		protected MethodInvoc(Object obj, Method m, Object[] args, MethodProxy mprxy, List<Object> inters, BeanBoxContext ctx,
				int count) {
			this.obj = obj;	this.m = m;	this.args = args;	this.mprxy = mprxy;
			this.inters = inters;	this.ctx = ctx;	this.count = count;
		}
 
		public Object proceed() throws Throwable {
			if (count <= (inters.size() - 1)) { 
				org.aopalliance.intercept.MethodInterceptor inter = ctx.getBean(inters.get(count));
			    return inter.invoke(new MethodInvoc(obj, m, args, mprxy, inters, ctx, count + 1));
			}
			return mprxy.invokeSuper(obj, args);
		} 
		
		public final Object getThis() { return obj; }
 
		public final AccessibleObject getStaticPart() { return m; }
 
		public final Method getMethod() { return m; }
 
		public final Object[] getArguments() { return this.args != null ? this.args : new Object[0]; }
	}

}