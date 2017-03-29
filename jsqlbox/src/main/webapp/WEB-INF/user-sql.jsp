<%@ include file="helper.jsp"%>
<%@page import="test.config.po.User"%>
<%
	User u = (User) request.getAttribute("user");
    String sqlID=(String)request.getAttribute("sqlID");
%>

<%if ("byUserNameAndAddress".equals(sqlID)) {%>

select * from <%=u.table() %> where
   <%=u.ADDRESS() %> = <%=q(u.getAddress())%>,
   <%=u.USERNAME() %> = <%=q(u.getUserName())%>
order by <%=u.ID() %>     

<%}%>