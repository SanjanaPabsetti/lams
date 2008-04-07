<%@ include file="/common/taglibs.jsp"%>
<c:set var="ctxPath" value="${pageContext.request.contextPath}"
	scope="request" />

<c:set var="tool">
	<lams:WebAppURL />
</c:set>

<div id="conditionList">
	<h2 class="spacer-left">
		<fmt:message key="label.authoring.conditions.list.title" />
		<img src="${ctxPath}/includes/images/indicator.gif"	style="display:none" id="conditionListArea_Busy" />
	</h2>
	
	<table class="alternative-color" id="conditionTable" cellspacing="0">
		<c:set var="sessionMap" value="${sessionScope[sessionMapID]}" />

		<c:forEach var="condition" items="${sessionMap.conditionList}" varStatus="status">
			<tr>
                <td>
					${condition.name}
				</td>

				<td width="40px" align="center">
					<c:if test="${not status.first}">
						<img src="<html:rewrite page='/includes/images/uparrow.gif'/>"
							border="0" title="<fmt:message key="label.authoring.up"/>"
							onclick="upCondition(${status.index},'${sessionMapID}')">
						<c:if test="${status.last}">
							<img
								src="<html:rewrite page='/includes/images/downarrow_disabled.gif'/>"
								border="0" title="<fmt:message key="label.authoring.down"/>">
						</c:if>
					</c:if>

					<c:if test="${not status.last}">
						<c:if test="${status.first}">
							<img
								src="<html:rewrite page='/includes/images/uparrow_disabled.gif'/>"
								border="0" title="<fmt:message key="label.authoring.up"/>">
						</c:if>

						<img src="<html:rewrite page='/includes/images/downarrow.gif'/>"
							border="0" title="<fmt:message key="label.authoring.down"/>"
							onclick="downCondition(${status.index},'${sessionMapID}')">
					</c:if>
				</td>
				
				<td width="20px">
					<img src="${tool}includes/images/edit.gif"
						title="<fmt:message key="label.authoring.basic.resource.edit" />"
						onclick="editCondition(${status.index},'${sessionMapID}')" />
                </td>
                
				<td width="20px">
					<img src="${tool}includes/images/cross.gif"
						title="<fmt:message key="label.authoring.basic.resource.delete" />"
						onclick="deleteCondition(${status.index},'${sessionMapID}')" />
				</td>
			</tr>
		</c:forEach>
	</table>
</div>

<%-- This script will works when a new resoruce Condition submit in order to refresh "TaskList List" panel. --%>
<script lang="javascript">

	if(window.top != null){
		window.top.hideConditionMessage();
		var obj = window.top.document.getElementById('conditionsArea');
		obj.innerHTML= document.getElementById("conditionList").innerHTML;
	}
</script>
