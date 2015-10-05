<%@page session="true"%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><c:forEach items='${data.keySet()}' var="iprop">
	<c:if test="${results.getDescriptionProperty() != iprop}">
		<label class="${c1name}"><a data-label="${iprop.getLabel()}" data-comment="${iprop.getComment()}" href="${iprop.getPropertyUrl()}"><c:choose>
					<c:when test='${iprop.getNsProperty().startsWith("null:")}'>&lt;${iprop.getProperty().replaceAll("([#/])([^#/]+)$","$1<span>$2")}</span>&gt;</c:when>
					<c:otherwise>${iprop.getNsProperty().replaceAll(":",":<span>")}</span>
					</c:otherwise>
				</c:choose></a></label>
		<c:set var="veryShortProps" value="true" />
		<c:set var="done" value="false" />
		<c:forEach items='${data.get(iprop)}' var="ela">
			<c:if test="${!done}">
				<c:if test="${ela.getValue().length() gt 8  || !ela.getLang().equals('')}">
					<c:set var="veryShortProps" value="false" />
					<c:set var="done" value="true" />
				</c:if>
			</c:if>
		</c:forEach>
		<div class="${c2name} value <c:if test="${veryShortProps}">multiInLineBlock</c:if>">
			<c:set var="done" value="false" />
			<c:forEach items='${data.get(iprop)}' var="ela">
				<c:choose>
					<c:when test="${veryShortProps}">
						<span class="multiInLine"> <c:if test="${!done}">
								<c:if test='${ela.getDataType()!=null && !ela.getDataType().equals("")}'>
									<span class="dType">${ela.getNsDataType().replaceAll("null:(.*)","&lt;$1&gt;")}</span>
									<c:set var="done" value="true" />
								</c:if>
							</c:if> ${ela.getValue()}&#160;
						</span>
					</c:when>
					<c:otherwise>
						<div class="toMultiLine <c:if test='${ela.getLang() != null && !ela.getLang().equals("")}'> lang ${ela.getLang()}" data-lang="${ela.getLang()}</c:if>">
							<div class="fixed">
								<c:if test='${ela.getDataType()!=null && !ela.getDataType().equals("")}'>
									<span class="dType">${ela.getNsDataType().replaceAll("null:(.*)","&lt;$1&gt;")}</span>
								</c:if>
								${ela.getValue()}&#160;
							</div>
						</div>
					</c:otherwise>
				</c:choose>

			</c:forEach>
		</div>
	</c:if>
</c:forEach>