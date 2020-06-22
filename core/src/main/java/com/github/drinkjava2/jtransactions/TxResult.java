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
package com.github.drinkjava2.jtransactions;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * TxResult Store TX result
 */
public class TxResult {
	// Results
	public static final String SUCESS = "SUCESS";
	public static final String FAIL = "FAIL";
	public static final String UNKNOW = "UNKNOW";

	// Stages
	public static final String START = "START";
	public static final String LOCKED = "LOCKED";
	public static final String LOCK_FAIL = "LOCK_FAIL";
	public static final String COMMIT_FAIL = "COMMIT_FAIL";
	public static final String UNLOCK_FAIL = "UNLOCK_FAIL";
	public static final String CLEANUP_FAIL = "CLEANUP_FAIL";

	private String result; // SUCESS, FAIL, UNKNOW
	private String stage; // optional, stage of tx
	private int committed; // optional, how many DB committed
	private String gid;// optional, GTX id
	private Exception[] commitEx;// optional, exception caught at commit stage
	private Exception[] rollbackEx;// optional, exception caught at rollback stage
	private Exception[] cleanupEx;// optional, exception caught at cleanup stage

	public TxResult() {
	}

	public TxResult(String result) {
		this.result = result;
	}

	public static TxResult txSucess() {
		return new TxResult(SUCESS);
	}

	public static TxResult txFail() {
		return new TxResult(FAIL);
	}

	public TxResult(String result, Exception... commitEx) {
		this.result = result;
		this.commitEx = commitEx;
	}

	public String getInfo() {
		return getInfoByRequire(false);
	}

	public String getDetailedInfo() {
		return getInfoByRequire(true);
	}

	private String getInfoByRequire(boolean detail) {// NOSONAR
		StringBuilder sb = new StringBuilder();
		sb.append("TxResult:").append(result).append("\r");
		sb.append("TxId:").append(gid).append("\r");
		sb.append("TxMessage:").append(stage).append("\r");
		int i = 0;
		if (commitEx != null)
			for (Exception e : commitEx) {
				sb.append("Commit Exception ").append(i++).append(": ")
						.append(detail ? getStackTrace(e) : e.getMessage()).append("\r");
			}
		i = 0;
		if (rollbackEx != null)
			for (Exception e : rollbackEx) {
				sb.append("Rollback Exception ").append(i++).append(": ")
						.append(detail ? getStackTrace(e) : e.getMessage()).append("\r");
			}
		i = 0;
		if (cleanupEx != null)
			for (Exception e : cleanupEx) {
				sb.append("Cleanup Exception ").append(i++).append(": ")
						.append(detail ? getStackTrace(e) : e.getMessage()).append("\r");
			}
		return sb.toString();
	}

	private static String getStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		try {
			t.printStackTrace(pw);
			return sw.toString();
		} finally {
			pw.close();
		}
	}

	public TxResult addCommitEx(Exception e) {
		if (commitEx == null)
			commitEx = new Exception[1];
		else {
			for (Exception oldeX : commitEx)
				if (oldeX == e)
					return this;
			Exception[] newArray = new Exception[commitEx.length + 1];
			System.arraycopy(commitEx, 0, newArray, 0, commitEx.length);
			commitEx = newArray;
		}
		commitEx[commitEx.length - 1] = e;
		return this;
	}

	public TxResult addRollbackEx(Exception e) {
		if (rollbackEx == null)
			rollbackEx = new Exception[1];
		else {
			for (Exception oldeX : rollbackEx)
				if (oldeX == e)
					return this;
			Exception[] newArray = new Exception[rollbackEx.length + 1];
			System.arraycopy(rollbackEx, 0, newArray, 0, rollbackEx.length);
			rollbackEx = newArray;
		}
		rollbackEx[rollbackEx.length - 1] = e;
		return this;
	}

	public TxResult addCleanupEx(Exception e) {
		if (cleanupEx == null)
			cleanupEx = new Exception[1];
		else {
			for (Exception oldeX : cleanupEx)
				if (oldeX == e)
					return this;
			Exception[] newArray = new Exception[cleanupEx.length + 1];
			System.arraycopy(cleanupEx, 0, newArray, 0, cleanupEx.length);
			cleanupEx = newArray;
		}
		cleanupEx[cleanupEx.length - 1] = e;
		return this;
	}

	public boolean isSuccess() {
		return SUCESS.equals(result);
	}

	public boolean isFail() {
		return FAIL.equals(result);
	}

	public boolean isUnknow() {
		return UNKNOW.equals(result);
	}

	// ==========getter & setters=======

	public String getResult() {
		return result;
	}

	public TxResult setResult(String result) {
		this.result = result;
		return this;
	}

	public String getStage() {
		return stage;
	}

	public TxResult setStage(String stage) {
		this.stage = stage;
		return this;
	}

	public String getGid() {
		return gid;
	}

	public TxResult setGid(String gid) {
		this.gid = gid;
		return this;
	}

	public Exception[] getCommitEx() {
		return commitEx;
	}

	public void setCommitEx(Exception[] commitEx) {
		this.commitEx = commitEx;
	}

	public Exception[] getRollbackEx() {
		return rollbackEx;
	}

	public void setRollbackEx(Exception[] rollbackEx) {
		this.rollbackEx = rollbackEx;
	}

	public Exception[] getCleanupEx() {
		return cleanupEx;
	}

	public void setCleanupEx(Exception[] cleanupEx) {
		this.cleanupEx = cleanupEx;
	}

	public int getCommitted() {
		return committed;
	}

	public void setCommitted(int committed) {
		this.committed = committed;
	}

}
