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
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
<title>Ripples Map</title>
	<link href="//maxcdn.bootstrapcdn.com/font-awesome/4.2.0/css/font-awesome.min.css" rel="stylesheet">
	<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
	<link rel="stylesheet" href="https://ajax.googleapis.com/ajax/libs/jqueryui/1.11.1/themes/smoothness/jquery-ui.css">
	<script src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.11.1/jquery-ui.min.js"></script>
	<script src="js/jquery.cookie.js"></script>
	
	<link href='http://fonts.googleapis.com/css?family=Source+Sans+Pro:200,300,400,600,700,900,200italic,300italic,400italic,600italic,700italic,900italic' rel='stylesheet' type='text/css'>
	<script type='text/javascript' src='js/jquery.msgBox.js'></script>
	<link rel="stylesheet" href="css/msgBoxLight.css" />
	<link rel="stylesheet" href="http://cdn.leafletjs.com/leaflet-0.7.3/leaflet.css" />
	<link rel="stylesheet" href="css/L.Control.Locate.min.css"/>
	<link rel="stylesheet" href="css/leaflet.contextmenu.css"/>
	<link rel="stylesheet" href="css/Control.OSMGeocoder.css" />
	<link rel="stylesheet" href="css/Leaflet.Coordinates-0.1.5.css"/>
	<link rel="stylesheet" href="css/index.css"/>
	<link href="css/animate.css" rel="stylesheet" type="text/css" />
	<link href="css/animated-notifications.css" rel="stylesheet" type="text/css" />
	<script type="text/javascript" src="js/animated-notifications.js"></script>
	<link href="css/Leaflet.Weather.css" rel="stylesheet" type="text/css" />
	<link rel="stylesheet" href="css/control.layers.minimap.css" />
	<script src="http://cdn.leafletjs.com/leaflet-0.7.3/leaflet.js"></script>	
	<script type='text/javascript' src='https://cdn.firebase.com/js/client/2.4.2/firebase.js'></script>
	<script type='text/javascript' src='js/tracksymbol.js'></script>
	<script src="js/L.Control.Locate.min.js"></script>
	<script src="js/KML.js"></script>			
	<script src="js/leaflet.contextmenu.js"></script>
	<script src="js/Leaflet.Weather.js"></script>
	<script src="js/Control.OSMGeocoder.js"></script>
	<script src="js/Leaflet.Coordinates-0.1.5.src.js"></script>
	<script src="js/L.Control.Layers.Minimap.js"></script>
	<script src="js/Leaflet.layerscontrol-minimap.js"></script>	

</head>
<body>
	<nav id='menu-ui' class='menu-ui'>
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
	  <a href='<%= userService.createLogoutURL(request.getRequestURI()) %>' id='logbook-out'>Logout</a>
	  <div id="log_user" style="float:left;position:absolute;bottom:60px;font-weight:bold;font-size:11px;">${fn:escapeXml(user.nickname)}</div>
	  <%
   	  } else {
	  %>
	  <a href='<%= userService.createLoginURL(request.getRequestURI()) %>' id='logbook-in'>Login</a>
	  <%
  	  }
	  %>
	  <a href='#' id='logbook-login'>Logbook</a>
	</nav>
	<div id="notifications">
		<div id="notifications-bottom-right"></div>
	</div>
	<script src="js/index.js"></script>
	<div id="map"></div>
	
	<script src="js/map.js"></script>						
</body>
