<%@ include file="/common/taglibs.jsp"%>
<script type="text/javascript" src="<lams:LAMSURL/>/includes/javascript/monitorToolSummaryAdvanced.js" ></script>

<c:set var="dto" value="${wookieDTO}" />

<script type="text/javascript">
	function submitForm(method, uid) {
		document.getElementById("dispatch").value = method;
		document.getElementById("hideUserImageUid").value = uid;
		document.getElementById("monitoringForm").submit();
	}
</script>

<h1>
	<img src="<lams:LAMSURL/>/images/tree_closed.gif" id="treeIcon" onclick="javascript:toggleAdvancedOptionsVisibility(document.getElementById('advancedDiv'), document.getElementById('treeIcon'), '<lams:LAMSURL/>');" />

	<a href="javascript:toggleAdvancedOptionsVisibility(document.getElementById('advancedDiv'), document.getElementById('treeIcon'),'<lams:LAMSURL/>');" >
		<fmt:message key="monitor.summary.th.advancedSettings" />
	</a>
</h1>
<br />

<div class="monitoring-advanced" id="advancedDiv" style="display:none">
<table class="alternative-color">

	<tr>
		<td>
			<fmt:message key="advanced.lockOnFinished" />
		</td>
		
		<td>
			<c:choose>
				<c:when test="${dto.lockOnFinish}">
					<fmt:message key="label.on" />
				</c:when>
				<c:otherwise>
					<fmt:message key="label.off" />
				</c:otherwise>
			</c:choose>	
		</td>
	</tr>
	<tr>
		<td>
			<fmt:message key="advanced.allowViewOthersImages" />
		</td>
		
		<td>
			<c:choose>
				<c:when test="${dto.allowViewOthersImages}">
					<fmt:message key="label.on" />
				</c:when>
				<c:otherwise>
					<fmt:message key="label.off" />
				</c:otherwise>
			</c:choose>	
		</td>
	</tr>
	<tr>
		<td>
			<fmt:message key="monitor.summary.td.addNotebook" />
		</td>
		
		<td>
			<c:choose>
				<c:when test="${dto.reflectOnActivity == true}">
					<fmt:message key="label.on" />
				</c:when>
				<c:otherwise>
					<fmt:message key="label.off" />
				</c:otherwise>
			</c:choose>	
		</td>
	</tr>
	
	<c:choose>
		<c:when test="${dto.reflectOnActivity == true}">
			<tr>
				<td>
					<fmt:message key="monitor.summary.td.notebookInstructions" />
				</td>
				<td>
					${dto.reflectInstructions}	
				</td>
			</tr>
		</c:when>
	</c:choose>
</table>
</div>


<html:form action="/monitoring" method="post" styleId="monitoringForm">
	
	<html:hidden property="dispatch" styleId="dispatch" value="toggleHideImage" />
	<html:hidden property="contentFolderID" value="${contentFolderID}" />	
	<html:hidden property="toolContentID" value="${toolContentID}"/>
	<html:hidden property="hideUserImageUid" styleId="hideUserImageUid"/>
	
	<c:forEach var="session" items="${dto.sessionDTOs}">
	
		<h2>
			${session.sessionName}
		</h2>
	
		<table cellpadding="0">
			<tr>
				<td class="field-name" width="30%">
					<fmt:message key="heading.totalLearners" />
				</td>
				<td width="70%">
					${session.numberOfLearners}
				</td>
			</tr>
		</table>
	
		<table cellpadding="0" class="alternative-color">
			<tr>
				<th><fmt:message key="monitoring.th.learner" /></th>
				<th><fmt:message key="monitoring.th.image" /></th>
				<c:if test="${wookieDTO.reflectOnActivity}">
					<th><fmt:message key="monitoring.th.reflection" /></th>
				</c:if>
			</tr>
			
			<c:forEach var="user" items="${session.userDTOs}">
				<tr>
					<td>
						<a href="javascript:openPopup('${wookieImageFolderURL}/${user.imageFileName}', ${user.imageHeight}, ${user.imageWidth})">
							${user.firstName} ${user.lastName}
						</a>
					</td>
					<td align="center">
						<c:choose>
							<c:when test="${user.imageFileName != null && user.imageFileName != wookieDTO.imageFileName}">
								<img src="${wookieImageFolderURL}/${user.imageFileName}" 
									height="${user.imageThumbnailHeight}" 
									width="${user.imageThumbnailWidth}"
									title='<fmt:message key="tooltip.openfullsize" />'
									onclick="openPopup('${wookieImageFolderURL}/${user.imageFileName}', ${user.imageHeight}, ${user.imageWidth})"
								 />
								 <c:if test="${wookieDTO.allowViewOthersImages}">
								 	<br />
								 	<a href="javascript:submitForm('toggleHideImage', '${user.uid}')">
								 	<c:choose>
								 		<c:when test="${user.imageHidden}">
											<fmt:message key="monitoring.showImage" />
								 		</c:when>
								 		<c:otherwise>
								 			<fmt:message key="monitoring.hideImage" />
								 		</c:otherwise>
								 	</c:choose>
								 	</a>
								 </c:if>
							</c:when>
							<c:otherwise>
								<fmt:message key="label.notAvailable" />
							</c:otherwise>
						</c:choose>
					</td>
					
					<c:if test="${wookieDTO.reflectOnActivity}">
						<td >
							<c:choose>
								<c:when test="${user.finishedReflection}">
									${user.notebookEntry}
								</c:when>
								<c:otherwise>
									<fmt:message key="label.notAvailable" />
								</c:otherwise>
							</c:choose>
						</td>
					</c:if>
				</tr>
			</c:forEach>
		</table>
	
	</c:forEach>

</html:form>
