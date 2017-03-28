
<%@page import="com.github.drinkjava2.jsqlbox.SqlBoxContext"%>
<%@page import="test.config.po.User"%>
<% User u=new User(); %>

<%! String sql1="";%>
select * from <%=u.getAddress()%>
