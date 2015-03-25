<%@ page contentType="text/html; charset=iso-8859-1" language="java"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Add Log</title>
<script src="http://code.jquery.com/jquery-2.1.3.min.js"></script>
<script src="js/ajax.js" type="text/javascript"></script>
</head>
<body>
	<!--form name="frm" method="get" action="log_entry.jsp"-->
	<form id="add_log">
		<table width="100%" border="0" cellspacing="0" cellpadding="0">
			<tr>
				<td width="10%">&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td>Place:</td>
				<td><input type="text" id="logbook_place"></td>
			</tr>
			<tr>
				<td>Coords:</td>
				<td><input type="text" id="logbook_coord_x"> <input
					type="text" id="logbook_coord_y"></td>
			</tr>
			<tr>
				<td>Conditions:</td>
				<td><textarea id="logbook_conditions"></textarea></td>
			</tr>
			<tr>
				<td>Objectives:</td>
				<td><textarea id="logbook_objectives"></textarea></td>
			</tr>
			<tr>
				<td>Team:</td>
				<td><textarea id="logbook_team"></textarea></td>
			</tr>
			<tr>
				<td>Systems:</td>
				<td><textarea id="logbook_systems"></textarea></td>
			</tr>
			<tr>
				<td>&nbsp;</td>
				<td><input type="submit" name="submit" value="Submit" /> <input
					type="reset" name="reset" value="Reset" /></td>
			</tr>
			<tr>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
		</table>
	</form>
	
	<br>
    <br>
 
    <strong>Ajax Response</strong>:
    <div id="ajaxGetResponse"></div>
	
</body>
</html>