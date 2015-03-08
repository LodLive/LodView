<%@page session="true"%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<aside>
	<span class="c1"></span>
	<div id="widgets" class="c2">
		<%
			/* including images */
		%>
		<c:if test='${results.getImages()!=null && results.getImages().size()>0}'>
			<div id="images">
				<c:forEach items="${results.getImages() }" var="item">
					<a href="${item}" target="_new"><img src="${item}"></a>
				</c:forEach>
			</div>
		</c:if>
		<%
			/* drawing a map */
		%>
		<c:if test='${hasLod || hasMap}'>
			<script>	
			function drawMap(id, lat, lon, testoPopup) {
				var map = L.map(id,{scrollWheelZoom:false,zoomControl:false}).setView([ lat, lon ], 3);
				L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
					attribution : '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
				}).addTo(map);

				L.marker([ lat, lon ]).addTo(map);
			}
 		</script>
		</c:if>
		<c:if test='${hasMap}'>
			<div id="resourceMapCnt">
				<map name="resourceMap" id="resourceMap" class="sp"></map>
			</div>
			<script>drawMap("resourceMap",${results.getLatitude()},${results.getLongitude()});</script>
		</c:if>
		<%
			/* external resources link */
		%>
		<c:if test='${hasLod}'>
			<div id="linking">
				<a href="#lodCloud" title="data from the lod cloud"><span class="sp"></span></a>
			</div>
		</c:if>
	</div>
</aside>