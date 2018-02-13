<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<?xml version="1.0" encoding="ISO-8859-1" ?>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<title>Edit team page</title>
</head>
<body>
<h1>Edit team page</h1>
<p>Here you can edit the existing team.</p>
<p>${message}</p>
<form method="post" action="${pageContext.request.contextPath}/team/edit/${team.id}.html">
<table>
<tbody>
	<tr>
		<td>Name:</td>
		<td><input name="name" value="${team.name}" /></td>
	</tr>
	<tr>
		<td>Rating:</td>
		<td><input name="rating" value="${team.rating}"/></td>
	</tr>
	<tr>
		<td><input type="submit" value="Edit" /></td>
		<td></td>
	</tr>
</tbody>
</table>
</form>

<p><a href="${pageContext.request.contextPath}/home.html">Home page</a></p>
</body>
</html>