<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
<title>spring-mvc-showcase</title>
<link href="<c:url value="/resources/form.css" />" rel="stylesheet" type="text/css" />
</head>
<body>
	<h1>Hi guy!</h1>
	<h2>${conf.getEndPointURL()} is ${online}</h2>

</body>
</html>