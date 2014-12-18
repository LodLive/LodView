<%@page session="true" %>
<%
				/* literals */
			%>
			<c:set var="data" value="${results.getLiterals(contentIRI)}" scope="page" />
			<%@include file="literals.jsp"%>
			<%
				/* resources */
			%>
			<c:set var="data" value="${results.getResources(contentIRI)}" scope="page" />
			<%@include file="resources.jsp"%>
			<%
				/* bnodes */
			%>
			<c:set var="data" value="${results.getBnodes(contentIRI)}" scope="page" />
			<%@include file="bnodes.jsp"%>