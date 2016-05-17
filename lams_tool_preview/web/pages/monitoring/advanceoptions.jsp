<%@ include file="/common/taglibs.jsp"%>

<c:set var="adTitle"><fmt:message key="monitor.summary.th.advancedSettings" /></c:set>
<lams:AdvancedAccordian title="${adTitle}">
             
<table class="table table-striped table-condensed">
	<tr>
		<td>
			<fmt:message key="label.authoring.advance.lock.on.finished" />
		</td>
		
		<td>
			<c:choose>
				<c:when test="${sessionMap.peerreview.lockWhenFinished}">
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
			<fmt:message key="label.minimum" />
		</td>
		
		<td>
			<c:choose>
				<c:when test="${sessionMap.peerreview.minimumRates == 0}">
					<fmt:message key="label.no.minimum" />
				</c:when>
				<c:otherwise>
					${sessionMap.imageGallery.minimumRates}
				</c:otherwise>
			</c:choose>	
		</td>
	</tr>
		
	<tr>
		<td>
			<fmt:message key="label.maximum" />
		</td>
			
		<td>
			<c:choose>
				<c:when test="${sessionMap.peerreview.maximumRates == 0}">
					<fmt:message key="label.no.maximum" />
				</c:when>
				<c:otherwise>
					${sessionMap.imageGallery.maximumRates}
				</c:otherwise>
			</c:choose>	
		</td>
	</tr>
	
	<tr>
		<td>
			<fmt:message key="label.show.ratings.left.for.user" />
		</td>
		
		<td>
			<c:choose>
				<c:when test="${sessionMap.peerreview.showRatingsLeftForUser}">
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
				<c:when test="${sessionMap.peerreview.reflectOnActivity}">
					<fmt:message key="label.on" />
				</c:when>
				<c:otherwise>
					<fmt:message key="label.off" />
				</c:otherwise>
			</c:choose>	
		</td>
	</tr>
	
	<c:choose>
		<c:when test="${sessionMap.peerreview.reflectOnActivity}">
			<tr>
				<td>
					<fmt:message key="monitor.summary.td.notebookInstructions" />
				</td>
				<td>
					<lams:out escapeHtml="true" value="${sessionMap.peerreview.reflectInstructions}"/>	
				</td>
			</tr>
		</c:when>
	</c:choose>
</table>

</lams:AdvancedAccordian>
