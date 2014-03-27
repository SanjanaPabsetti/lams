<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
        "http://www.w3.org/TR/html4/strict.dtd">

<%@ include file="/common/taglibs.jsp"%>

<html>
	<lams:head>
		<title><c:out value="${contentDTO.title}" escapeXml="true" />
		</title>
		<lams:css localLinkPath="../" />
	</lams:head>

	<body class="stripes">

		<div id="content">

			<p>
				<fmt:message key="export.toolExportNotSupported" />
			</p>

		</div>
		<!--closes content-->

		<div id="footer">
		</div>
		<!--closes footer-->

	</body>
</html>
