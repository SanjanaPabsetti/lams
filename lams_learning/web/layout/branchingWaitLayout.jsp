<%-- 
Copyright (C) 2005 LAMS Foundation (http://lamsfoundation.org)
License Information: http://lamsfoundation.org/licensing/lams/2.0/

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License version 2 as 
  published by the Free Software Foundation.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
  USA

  http://www.gnu.org/licenses/gpl.txt
--%>

<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=utf-8" %>
<%@ taglib uri="tags-bean" prefix="bean"%>
<%@ taglib uri="tags-html" prefix="html"%>
<%@ taglib uri="tags-tiles" prefix="tiles"%>
<%@ taglib uri="tags-core" prefix="c"%>
<%@ taglib uri="tags-lams" prefix="lams" %>
<%@ taglib uri="tags-fmt" prefix="fmt" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Frameset//EN" "http://www.w3.org/TR/html4/frameset.dtd">
<lams:html xhtml="true">

	<lams:head>
		<title><fmt:message key="${label.branching.title}"/></title>
		<lams:css/>
		<c:set var="formAction">/branching.do?method=performBranching&type=${BranchingForm.map.type}&activityID=${BranchingForm.map.activityID}&progressID=${BranchingForm.map.progressID}</c:set>
		<c:if test="${BranchingForm.map.previewLesson == true}">
			<c:set var="formAction"><c:out value="${formAction}"/>&force=true</c:set>
		</c:if>
		<META HTTP-EQUIV="Refresh" CONTENT="300;URL=<lams:WebAppURL/>${formAction}">
		<script src="<lams:LAMSURL/>includes/javascript/AC_RunActiveContent.js" type="text/javascript"></script>
	  </lams:head>

	<body class="stripes">
	      <tiles:insert attribute="body" />
	</body>

</lams:html>
