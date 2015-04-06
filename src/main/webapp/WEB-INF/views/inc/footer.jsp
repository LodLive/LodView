<%@page session="true"%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@taglib uri="http://www.springframework.org/tags" prefix="sp"%><footer>
	<div id="download">
		<a href="http://lodlive.it" id="linkBack"></a>
		<a href="https://github.com/dvcama/LodView" id="linkGit" target="_blank" title="based on LodView v1.1.3-SNAPSHOT"><sp:message code='footer.download' text='download lodview to publish your data' /></a>
	</div>
	<div id="endpoint">
		<ul>
			<c:choose>
				<c:when test='${conf.getEndPointUrl().equals("<>")}'>
					<li><sp:message code='footer.noSparql' text='data from: deferencing IRI' /></li>
				</c:when>
				<c:otherwise>
					<li><sp:message code='footer.yesSparql' text='data from:' /> <a href="${conf.getEndPointUrl()}">${conf.getEndPointUrl()}</a></li>
				</c:otherwise>
			</c:choose>
			<li><a target="_blank" href="${lodliveUrl }"><sp:message code='footer.viewLodlive' text='view on lodlive' /></a></li>
			<li class="viewas"><span><sp:message code='footer.viewAs' text='view as' /></span> <a title="application/rdf+xml">xml</a>, <a title="text/plain">ntriples</a>, <a title="text/turtle">turtle</a>, <a title="application/ld+json">ld+json</a></li>
		</ul>
	</div>
</footer>
<c:if test="${not empty conf.getLicense()}">
	<div id="license">
		<div>${conf.getLicense()}</div>
	</div>
</c:if>
