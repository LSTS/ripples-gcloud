<%@ page contentType="text/html; charset=iso-8859-1" language="java" %>
<%@ page import="pt.lsts.ripples.servlets.LogbookServlet" %>
<%
String lb_place=request.getParameter("logbook_place");
String lb_c_x=request.getParameter("logbook_coord_x");
String lb_c_y=request.getParameter("logbook_coord_y");
String lb_cond=request.getParameter("logbook_conditions");
String lb_obj=request.getParameter("logbook_objectives");
String lb_team=request.getParameter("logbook_team");
String lb_sys=request.getParameter("logbook_systems");

%>
<html>
<body>
Value of logbook_place in JSP : <%=lb_place%> <br/>
Value of logbook_coord_x in JSP : <%=lb_c_x%> <br/>
Value of logbook_coord_y in JSP : <%=lb_c_y%> <br/>
Value of logbook_conditions in JSP : <%=lb_cond%> <br/>
Value of logbook_objectives in JSP : <%=lb_obj%> <br/>
Value of logbook_team in JSP : <%=lb_team%> <br/>
Value of logbook_systems in JSP : <%=lb_sys%>
</body>
</html>