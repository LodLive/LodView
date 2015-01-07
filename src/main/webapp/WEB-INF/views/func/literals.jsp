<%@page session="true"%><c:forEach items='${data.keySet()}' var="iprop">
	<label class="${c1name}"><a data-label="${iprop.getLabel()}"  data-comment="${iprop.getComment()}" href="${iprop.getPropertyUrl()}"><c:choose>
				<c:when test='${iprop.getNsProperty().startsWith("null:")}'>&lt;${iprop.getProperty().replaceAll("([#/])([^#/]+)$","$1<span>$2")}</span>&gt;</c:when>
				<c:otherwise>${iprop.getNsProperty().replaceAll(":",":<span>")}</span>
				</c:otherwise>
			</c:choose></a></label>
	<div class="${c2name} value ">
		<c:forEach items='${data.get(iprop)}' var="ela">
			<div class="toMultiLine <c:if test='${ela.getLang() != null && !ela.getLang().equals("")}'> lang ${ela.getLang()}" data-lang="${ela.getLang()}</c:if>">
				<div class="fixed">
					<c:if test='${ela.getDataType()!=null && !ela.getDataType().equals("")}'>
						<span class="dType">${ela.getNsDataType().replaceAll("null:(.*)","&lt;$1&gt;")}</span>
					</c:if>
					${ela.getValue()}
				</div>
			</div>
		</c:forEach>
	</div>
</c:forEach>