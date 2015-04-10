<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>

<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Ripples - Logbook</title>
<script src="http://code.jquery.com/jquery-2.1.3.min.js"></script>

<link rel="stylesheet" href="http://code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css">
<script src="http://code.jquery.com/ui/1.11.4/jquery-ui.js"></script>

<script src="js/jquery.cookie.js"></script>

<link href='http://fonts.googleapis.com/css?family=Open+Sans+Condensed:300,300italic,700&subset=latin,cyrillic-ext,latin-ext,cyrillic' rel='stylesheet' type='text/css'>
<link rel="stylesheet" href="http://netdna.bootstrapcdn.com/font-awesome/4.0.1/css/font-awesome.min.css">
<link rel="stylesheet" href="css/jquery.multilevelpushmenu_grey.css">
<script type="text/javascript" src="http://oss.maxcdn.com/libs/modernizr/2.6.2/modernizr.min.js"></script>
<script src="js/jquery.multilevelpushmenu.min.js"></script>

<script type='text/javascript' src='js/jquery.msgBox.js'></script>
<link rel="stylesheet" href="css/msgBoxLight.css" />

</head>
<body>
<%
    String logbookUsername = request.getParameter("logbookUsername");
    if (logbookUsername == null) {
    	logbookUsername = "default";
    }
    pageContext.setAttribute("logbookUsername", logbookUsername);
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
    if (user != null) {
        pageContext.setAttribute("user", user);
%>

<script type='text/javascript' src='js/log.js'></script>

<p>Hello, <b id="log_user">${fn:escapeXml(user.nickname)}</b> (You can
    <a href="<%= userService.createLogoutURL("/index.jsp") %>">sign out</a>.)</p>
    
    <h3 id="log_title">Logbook for today:</h3>
    <p>Date: <input type="text" id="datepicker"></p>
    <h5 id="log_alert"></h5>
	<iframe id="log_view" style="display:none;"></iframe>
	<div id="pushobj" style="float:left;margin-left: 55px;"></div>
	
	        <div id="menu">
            <nav>
                
                        <h2><i class="fa fa-book"></i>Ripples - Logbook</h2>
                        <ul>
                            <li>
                                <a href="#">Place</a>
                            </li>
                            <li>
                                <a href="#">Conditions</a>
                            </li>
                            <li>
                                <a href="#">Objectives</a>
                            </li>
                            <li>
                                <a href="#">Team</a>
                            </li>
                            <li>
                                <a href="#">Systems</a>
                            </li>
                            <li>
                                <a href="#">Log</a>
                            </li>
                            <li>
                                <a href="#">Actions</a>
                            </li>
                        </ul>

            </nav>
        </div>    
<%
    } else {
%>
<p>Hello!
    <a href="<%= userService.createLoginURL(request.getRequestURI()) %>">Sign in</a>
    , please.</p>
    
<%
    }
%>
</body>
</html>