/*
 * Copyright (C) 2016 Yong Zhu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jentitynet;

/**
 * EntityNetUtils is utility class store static methods about EntityNet
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class EntityNetUtils {

	public static void weave(EntityNet net) {
		//TODO work at here
		// SqlBox[] boxes = net.get();
		// if (boxes == null || boxes.length == 0)
		// throw new SqlBoxException("Can not weave EntityNet with empty config
		// parameters");
		//
		net.setWeaved(true);
	}
 
}
