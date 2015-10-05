<%@page session="true"%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@taglib uri="http://www.springframework.org/tags" prefix="sp"%><footer>
	<div id="download">
		<a href="http://lodview.it" id="linkBack"></a>
		<a href="https://github.com/dvcama/LodView" id="linkGit" target="_blank" title="based on LodView v1.2.1-SNAPSHOT"><sp:message code='footer.download' text='download lodview to publish your data' /></a>
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
			<li><a target="_blank" href="${lodliveUrl}"><sp:message code='footer.viewLodlive' text='view on lodlive' /></a></li>
			<c:forEach items="${rawdatalinks.keySet()}" var="list">
				<li class="viewas"><span>${list}</span> 
					<c:set var="tot" value="${rawdatalinks.get(list).size()}" />
					<c:forEach items="${rawdatalinks.get(list)}" var="ele" varStatus="status">
						<a href="${ele.value}">${ele.key}</a><c:if test="${status.count < tot}">, </c:if>
					</c:forEach>
				</li>
			</c:forEach>
		</ul>
	</div>
</footer>
<c:if test="${not empty conf.getLicense()}">
	<div id="license">
		<div>${conf.getLicense()}</div>
	</div>
</c:if>
