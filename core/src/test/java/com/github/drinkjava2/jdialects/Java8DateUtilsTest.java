/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jdialects;

/*- JAVA8_BEGIN
import static com.github.drinkjava2.jdialects.Java8DateUtils.date2LocalDate;
import static com.github.drinkjava2.jdialects.Java8DateUtils.date2LocalDateTime;
import static com.github.drinkjava2.jdialects.Java8DateUtils.instant2SqlTimestamp;
import static com.github.drinkjava2.jdialects.Java8DateUtils.localDate2Date;
import static com.github.drinkjava2.jdialects.Java8DateUtils.localDate2SqlDate;
import static com.github.drinkjava2.jdialects.Java8DateUtils.localDateTime2Date;
import static com.github.drinkjava2.jdialects.Java8DateUtils.localDateTime2SqlTimestamp;
import static com.github.drinkjava2.jdialects.Java8DateUtils.localTime2SqlTime;
import static com.github.drinkjava2.jdialects.Java8DateUtils.offsetDateTime2SqlTimestamp;
import static com.github.drinkjava2.jdialects.Java8DateUtils.offsetTime2SqlTime;
import static com.github.drinkjava2.jdialects.Java8DateUtils.zonedDateTime2SqlTimestamp;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.Date;

import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
  
 
public class Java8DateUtilsTest {// NOSONAR
  
	@Test
	public void testDatetimes() {

		Systemout.println("==========Date==============");
		Date d = new Date();
		Systemout.println(d);
		Systemout.println(d.getTime());

		Systemout.println("==========sqlDate==============");
		java.sql.Date sqlD = new java.sql.Date(d.getTime());
		Systemout.println(sqlD);
		Systemout.println(sqlD.getTime());

		Systemout.println("==========localDate==============");
		LocalDate ld = date2LocalDate(d);
		Systemout.println(ld);

		java.util.Date jd = localDate2Date(ld);
		Systemout.println(jd);
		Systemout.println(jd.getTime());

		java.sql.Date sd = localDate2SqlDate(ld);
		Systemout.println(sd);
		Systemout.println(sd.getTime());

		Systemout.println("==========localTime==============");
		LocalTime lt = LocalTime.now();
		Systemout.println(lt);

		java.sql.Time st = localTime2SqlTime(lt);
		Systemout.println(st);
		Systemout.println(st.getTime());

		Systemout.println("==========OffsetTime==============");
		OffsetTime oft = OffsetTime.now();
		Systemout.println(oft);

		java.sql.Time st1 = offsetTime2SqlTime(oft);
		Systemout.println(st1);
		Systemout.println(st1.getTime());

		Systemout.println("==========Instant==============");
		Instant ins = Instant.now();
		Systemout.println(ins);

		Timestamp stmp1 = instant2SqlTimestamp(ins);
		Systemout.println(stmp1);
		Systemout.println(stmp1.getTime());

		Systemout.println("===========localDateTime=============");
		LocalDateTime ldt = date2LocalDateTime(d);
		Systemout.println(ldt);

		jd = localDateTime2Date(ldt);
		Systemout.println(jd);
		Systemout.println(d.getTime());

		Timestamp stmp = localDateTime2SqlTimestamp(ldt);
		Systemout.println(stmp);
		Systemout.println(stmp.getTime());

		Systemout.println("==========OffsetDateTime==============");
		OffsetDateTime off = OffsetDateTime.now();
		Systemout.println(off);

		Timestamp stmp2 = offsetDateTime2SqlTimestamp(off);
		Systemout.println(stmp2);
		Systemout.println(stmp2.getTime());

		Systemout.println("==========ZonedDateTime==============");
		ZonedDateTime zoned = ZonedDateTime.now();
		Systemout.println(zoned);

		Timestamp stmp3 = zonedDateTime2SqlTimestamp(zoned);
		Systemout.println(stmp3);
		Systemout.println(stmp3.getTime());
	}

}
JAVA8_END */
