<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Create log</title>
<!--script src="http://code.jquery.com/jquery-2.1.3.min.js"></script-->
<!--script src="js/create.js" type="text/javascript"></script-->
</head>
<body>
<p>Create Logbook on server!</p>
  <form id="prog" method="post" onSubmit='window.location.href=window.location.protocol + "//" + window.location.host + "/" +"logbook/create"'>
    <button id="submit" type="submit"> Create now! </button>
  </form>

	<!--strong>Ajax Response</strong>:
    <div id="ajaxGetResponse"></div-->
</body>
</html>