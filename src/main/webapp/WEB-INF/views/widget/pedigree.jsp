<%@page session="true"%><%@taglib uri="http://www.springframework.org/tags" prefix="sp"%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><html version="XHTML+RDFa 1.1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.w3.org/1999/xhtml http://www.w3.org/MarkUp/SCHEMA/xhtml-rdfa-2.xsd" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" xmlns:xsd="http://www.w3.org/2001/XMLSchema#" xmlns:cc="http://creativecommons.org/ns#" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:foaf="http://xmlns.com/foaf/0.1/">
<head data-color="${colorPair}" profile="http://www.w3.org/1999/xhtml/vocab">
<title>${results.getTitle()}&mdash;LodView</title>
<%@include file="../inc/header.jsp"%>
<style>
body {
	white-space: nowrap;
	background: #fff;
	box-sizing: border-box;
	-moz-box-sizing: border-box;
	-webkit-box-sizing: border-box;
}

#familytree-content {
	text-align: left;
}

div.pair {
	display: inline-block;
}

.hb, .wf {
	background-color: #e2e2e2;
	width: 100px;
	height: 100px;
	-webkit-border-radius: 50px;
	-moz-border-radius: 50px;
	border-radius: 50px;
	overflow: hidden;
	-moz-border-radius: 50px;
	border-radius: 50px;
	white-space: normal;
	color: #6d6d6d;
	font-weight: 700;
	font-size: 12px;
	line-height: 14px;
}

div.pair div.hb {
	float: right;
}

div.pair div.wf {
	float: left;
}

.e {
	width: 100px;
	height: 100px;
	padding: 0 25px;
	display: inline-block;
}

h3 {
	line-height: 25px;
	padding: 0 25px;
}

.mainIRI .hb, .mainIRI .wf {
	color: #fff;
	background: ${color1
}

;
}
.mainIRI {
	height: 150px;
	background-color: #fff;
}
div.pair img{
	position: relative;
	top: 50%;
	transform: translateY(-50%);
	width:100px;
	height:auto;
}
div.pair div strong {
	display: none;
	margin-right: 5px;
	margin-left: 5px;
	overflow: hidden;
	position: relative;
	top: 50%;
	transform: translateY(-50%);
	text-align: center;
}

.sel {
	background: #cdcdcd
}

#grampas {
	height: 150px;
	background: #878787;
}

#sons {
	height: 150px;
	background: #dadada;
}

#grandsons {
	padding-left: 75px;
	height: 150px;
	background: #ededed;
	height: 150px;
}

#bros {
	height: 150px;
	background: #c6c6c6;
}

#sibl, #mainr {
	display: inline-block;
}

#spouse {
	padding-left: 75px;
	text-align: right;
	background: #b2b2b2;
	display: inline-block;
	height: 150px;
	float: left;
	min-width: 150px;
}

#sibl {
	position: relative;
	top: -25px;
}

.mainIRI>div {
	margin-top: 25px
}

#fgp {
	min-width: 300px;
	float: left;
	height: 150px;
	background-color: #706f6f;
	text-align: right;
}

#fgp h3 {
	text-align: right;
}

#mgp {
	float: left;
}

#parents {
	height: 150px;
	padding-left: 150px;
	background: #9d9d9c;
}

#parents h3 {
	padding: 0;
	width:450px;
	margin-left:-75px;
	text-align: center;
}

#sons {
	height: 150px;
	padding-left: 75px;
	background: #dadada;
}

#sons h3, #grandsons h3 {
	padding: 0;
	width:450px;
	margin-left:-75px;
	text-align: center;
}
</style>
</head>
<body class="">
	<div id="familytree-content">
		<div id="grampas">
			<div id="fgp">
				<h3>GRANDPARENTS</h3>
			</div>
			<div id="mgp">
				<h3>GRANDPARENTS</h3>
			</div>
		</div>
		<div id="parents">
			<h3>PARENTS</h3>
		</div>
		<div id="bros">
			<div id="mainr"></div>
			<div id="sibl">
				<h3>SIBLINGS</h3>
				<div class="pair"><div class="e"></div></div>
			</div>
		</div>
		<div id="sons">
			<h3>CHILDRENS</h3>
		</div>
		<div id="grandsons">
			<h3>GRANDCHILDRENS</h3>
		</div>
	</div>
	<script>
		$(function() {
			familytree.init();
		});

	</script>
</body>
</html>