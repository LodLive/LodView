<%@page session="true" %><c:forEach items='${data.keySet()}' var="aprop">
	<label class="${c1name}"><a data-label="${aprop.getLabel() }"  data-comment="${aprop.getComment()}" href="${aprop.getPropertyUrl()}"><c:choose>
				<c:when test='${aprop.getNsProperty().startsWith("null:")}'>&lt;${aprop.getProperty().replaceAll("([#/])([^#/]+)$","$1<span>$2")}</span>&gt;</c:when>
				<c:otherwise>${aprop.getNsProperty().replaceAll(":",":<span>")}</span>
				</c:otherwise>
			</c:choose></a></label>
	<div class="${c2name} value">
		<c:forEach items='${data.get(aprop)}' var="ela">
			<div class="toOneLine">
				<a title="&lt;${ela.getValue()}&gt;" class="<c:if test="${results.getLinking().contains(ela.getUrl())}">linkingElement</c:if> <c:if test="${ela.isLocal()}">isLocal</c:if>" href="${ela.getUrl()}<c:if test='${ela.isLocal()}'></c:if>" <c:if test="${!ela.isLocal()}">target="_blank" </c:if>> <c:choose>
						<c:when test='${ela.getNsValue().startsWith("null:")}'>&lt;${ela.getValue()}&gt;</c:when>
						<c:otherwise>${ela.getNsValue().replaceAll(" :",":<span>")}</c:otherwise>
					</c:choose>
				</a>
			</div>
		</c:forEach>
	</div>
</c:forEach>