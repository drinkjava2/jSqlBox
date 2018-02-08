<?xml version="1.0" encoding="ISO-8859-1" ?>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<title>Add team page</title>
</head>
<body>
<h1>Add team page</h1>
<p>Here you can add a new team.</p>
${message}<br/>
<form method="post" action="${pageContext.request.contextPath}/team/add.html">
<table>
<tbody>
	<tr>
		<td>Name:</td>
		<td><input type="text" name="name" /></td>
	</tr>
	<tr>
		<td>Rating:</td>
		<td><input type="text" name="rating" /></td>
	</tr>
	<tr>
		<td><input type="submit" value="Add" /></td>
		<td></td>
	</tr>
</tbody>
</table>
</form>

<p><a href="${pageContext.request.contextPath}/home.html">Home page</a></p>
</body>
</html>