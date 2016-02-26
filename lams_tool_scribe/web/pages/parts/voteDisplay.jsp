<%@ include file="/common/taglibs.jsp"%>
<div class="progress">
	<div class="progress-bar progress-bar-success progress-bar-striped" role="progressbar"
		aria-valuenow="${scribeSessionDTO.votePercentage}" aria-valuemin="0" aria-valuemax="100"
		style="width: ${scribeSessionDTO.votePercentage}%">
		<span class="sr-only">${scribeSessionDTO.votePercentage}%</span>
		<div id="agreementPercentage">
		<fmt:message key="message.voteStatistics">
			<fmt:param value="${scribeSessionDTO.numberOfVotes}"></fmt:param>
			<fmt:param value="${scribeSessionDTO.numberOfLearners}"></fmt:param>
		</fmt:message>
		(${scribeSessionDTO.votePercentage}%)
		</div>
	</div>
</div>
