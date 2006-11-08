/****************************************************************
 * Copyright (C) 2005 LAMS Foundation (http://lamsfoundation.org)
 * =============================================================
 * License Information: http://lamsfoundation.org/licensing/lams/2.0/
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2.0 
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
 * USA
 * 
 * http://www.gnu.org/licenses/gpl.txt
 * ****************************************************************
 */

/* $Id$ */
package org.lamsfoundation.lams.admin.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.lamsfoundation.lams.admin.AdminConstants;
import org.lamsfoundation.lams.admin.service.AdminServiceProxy;
import org.lamsfoundation.lams.admin.web.dto.UserOrgRoleDTO;
import org.lamsfoundation.lams.usermanagement.Organisation;
import org.lamsfoundation.lams.usermanagement.OrganisationState;
import org.lamsfoundation.lams.usermanagement.OrganisationType;
import org.lamsfoundation.lams.usermanagement.Role;
import org.lamsfoundation.lams.usermanagement.SupportedLocale;
import org.lamsfoundation.lams.usermanagement.User;
import org.lamsfoundation.lams.usermanagement.UserOrganisation;
import org.lamsfoundation.lams.usermanagement.UserOrganisationRole;
import org.lamsfoundation.lams.usermanagement.service.IUserManagementService;
import org.lamsfoundation.lams.util.Configuration;
import org.lamsfoundation.lams.util.ConfigurationKeys;
import org.lamsfoundation.lams.util.MessageService;
import org.lamsfoundation.lams.util.WebUtil;
import org.lamsfoundation.lams.web.action.LamsDispatchAction;

/**
 * @author Jun-Dir Liew
 *
 * Created at 17:00:18 on 13/06/2006
 */

/**
 * @struts:action path="/user"
 *              name="UserForm"
 *              scope="request"
 *              parameter="method"
 * 				validate="false"
 * 
 * @struts:action-forward name="user" path=".user"
 * @struts:action-forward name="userlist" path="/usermanage.do"
 * @struts:action-forward name="remove" path=".remove"
 * @struts:action-forward name="disabledlist" path="/disabledmanage.do"
 * @struts:action-forward name="usersearch" path="/usersearch.do"
 */
public class UserAction extends LamsDispatchAction {

	private static Logger log = Logger.getLogger(UserAction.class);
	private static IUserManagementService service;
	private static MessageService messageService;
	private static List<SupportedLocale> locales;
	
	public ActionForward edit(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
		
		service = AdminServiceProxy.getService(getServlet().getServletContext());
		messageService = AdminServiceProxy.getMessageService(getServlet().getServletContext());
		if (locales==null) {
			locales = service.findAll(SupportedLocale.class);
			Collections.sort(locales);
		}
		
		DynaActionForm userForm = (DynaActionForm)form;
		Integer orgId = WebUtil.readIntParam(request,"orgId",true);
		Integer userId = WebUtil.readIntParam(request,"userId",true);
		
		// test requestor's permission
		Organisation org = null;
		Boolean requestorHasRole = false;
		if (orgId!=null) {
			org = (Organisation)service.findById(Organisation.class,orgId);
			OrganisationType orgType = org.getOrganisationType();
			Integer orgIdOfCourse = (orgType.getOrganisationTypeId().equals(OrganisationType.CLASS_TYPE)) 
				? org.getParentOrganisation().getOrganisationId() : orgId;
			User requestor = (User)service.getUserByLogin(request.getRemoteUser());
			requestorHasRole = service.isUserInRole(requestor.getUserId(), orgIdOfCourse, Role.COURSE_ADMIN)
				|| service.isUserInRole(requestor.getUserId(), orgIdOfCourse, Role.COURSE_MANAGER);
		}
		Boolean isSysadmin = request.isUserInRole(Role.SYSADMIN);
		
		if (!(requestorHasRole || isSysadmin)) {
			request.setAttribute("errorName", "UserAction");
			request.setAttribute("errorMessage", messageService.getMessage("error.authorisation"));
			return mapping.findForward("error");
		}

		// editing a user
		if (userId!=null && userId!=0) {
			User user = (User)service.findById(User.class, userId);
			log.debug("got userid to edit: "+userId);
			BeanUtils.copyProperties(userForm, user);
			userForm.set("password", null);
	        SupportedLocale locale = user.getLocale();
			userForm.set("localeId", locale.getLocaleId());
			// set user's organisations to display
			request.setAttribute("userOrgRoles", getUserOrgRoles(user));
		} else {  // create a user
			try {
				String defaultLocale = Configuration.get(ConfigurationKeys.SERVER_LANGUAGE);
				log.debug("using defaultLocale: "+defaultLocale);
				SupportedLocale locale = service.getSupportedLocale(defaultLocale.substring(0,2),defaultLocale.substring(3));
				userForm.set("localeId", locale.getLocaleId());
			} catch(Exception e) {
                log.debug(e);				
			}
		}
		userForm.set("orgId", (org==null ? null : org.getOrganisationId()));
		
		// for breadcrumb links
		if (org!=null) {
			request.setAttribute("orgName",org.getName());
			Organisation parentOrg = org.getParentOrganisation();
			if (parentOrg!=null && !parentOrg.equals(service.getRootOrganisation())) {
				request.setAttribute("pOrgId", parentOrg.getOrganisationId());
				request.setAttribute("parentName", parentOrg.getName());
			}
		}
		
		request.setAttribute("locales",locales);

		return mapping.findForward("user");
	}
	
	// display user's organisations and roles in them
	private List<UserOrgRoleDTO> getUserOrgRoles(User user) {
		
		List<UserOrgRoleDTO> uorDTOs = new ArrayList<UserOrgRoleDTO>();
		List<UserOrganisation> uos = service.getUserOrganisationsForUserByTypeAndStatus(
				user.getLogin(), 
				OrganisationType.COURSE_TYPE, 
				OrganisationState.ACTIVE);
		for (UserOrganisation uo : uos) {
			UserOrgRoleDTO uorDTO = new UserOrgRoleDTO();
			List<String> roles = new ArrayList<String>();
			for (Object uor : uo.getUserOrganisationRoles())
				roles.add(((UserOrganisationRole)uor).getRole().getName());
			Collections.sort(roles);
			uorDTO.setOrgName(uo.getOrganisation().getName());
			uorDTO.setRoles(roles);
			List<UserOrgRoleDTO> childDTOs = new ArrayList<UserOrgRoleDTO>();
/* JunDir - please fix!
 * 			List<UserOrganisation> childuos = service.getUserOrganisationsForUserByTypeAndStatusAndParent(
					user.getLogin(), 
					OrganisationType.CLASS_TYPE, 
					OrganisationState.ACTIVE,
					uo.getOrganisation().getOrganisationId());
			for (UserOrganisation childuo : childuos) {
				UserOrgRoleDTO childDTO = new UserOrgRoleDTO();
				List<String> childroles = new ArrayList<String>();
				for (Object uor : childuo.getUserOrganisationRoles())
					childroles.add(((UserOrganisationRole)uor).getRole().getName());
				Collections.sort(childroles);
				childDTO.setOrgName(childuo.getOrganisation().getName());
				childDTO.setRoles(childroles);
				childDTOs.add(childDTO);
			}
			uorDTO.setChildDTOs(childDTOs);
			uorDTOs.add(uorDTO);
*/		}
		
		return uorDTOs;
	}
	
	// determine whether to disable or delete user based on their lams data
	public ActionForward remove(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
		
		service = AdminServiceProxy.getService(getServlet().getServletContext());
		messageService = AdminServiceProxy.getMessageService(getServlet().getServletContext());
		
		if (!request.isUserInRole(Role.SYSADMIN)) {
			request.setAttribute("errorName","UserAction");
			request.setAttribute("errorMessage",messageService.getMessage("error.need.sysadmin"));
			return mapping.findForward("error");
		}
		
		Integer orgId = WebUtil.readIntParam(request,"orgId",true);
		Integer userId = WebUtil.readIntParam(request,"userId");
		User user = (User)service.findById(User.class,userId);
		
		Boolean hasData = service.userHasData(user);

		request.setAttribute("method", (hasData?"disable":"delete"));
		request.setAttribute("orgId",orgId);
		request.setAttribute("userId",userId);
		return mapping.findForward("remove");
	}
	
	public ActionForward disable(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

		messageService = AdminServiceProxy.getMessageService(getServlet().getServletContext());
		
		if (!request.isUserInRole(Role.SYSADMIN)) {
			request.setAttribute("errorName","UserAction");
			request.setAttribute("errorMessage",messageService.getMessage("error.need.sysadmin"));
			return mapping.findForward("error");
		}
		
		Integer orgId = WebUtil.readIntParam(request,"orgId",true);
		Integer userId = WebUtil.readIntParam(request,"userId");
		AdminServiceProxy.getService(getServlet().getServletContext()).disableUser(userId);
		String[] args = new String[1];
		args[0] = userId.toString();
		String message = messageService.getMessage("audit.user.disable", args);
		AdminServiceProxy.getAuditService(getServlet().getServletContext()).log(AdminConstants.MODULE_NAME, message);
		
		if (orgId==null || orgId==0) {
			return mapping.findForward("usersearch");
		} else {
			request.setAttribute("org",orgId);
			return mapping.findForward("userlist");
		}
	}
	
	public ActionForward delete(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
		
		messageService = AdminServiceProxy.getMessageService(getServlet().getServletContext());
		
		if (!request.isUserInRole(Role.SYSADMIN)) {
			request.setAttribute("errorName","UserAction");
			request.setAttribute("errorMessage",messageService.getMessage("error.need.sysadmin"));
			return mapping.findForward("error");
		}
		
		Integer orgId = WebUtil.readIntParam(request,"orgId",true);
		Integer userId = WebUtil.readIntParam(request,"userId");
		try {
			AdminServiceProxy.getService(getServlet().getServletContext()).removeUser(userId);
		} catch (Exception e) {
			request.setAttribute("errorName","UserAction");
			request.setAttribute("errorMessage",e.getMessage());
			return mapping.findForward("error");
		}
		String[] args = new String[1];
		args[0] = userId.toString();
		String message = messageService.getMessage("audit.user.delete", args);
		AdminServiceProxy.getAuditService(getServlet().getServletContext()).log(AdminConstants.MODULE_NAME, message);
		
		if (orgId==null || orgId==0) {
			return mapping.findForward("usersearch");
		} else {
			request.setAttribute("org",orgId);
			return mapping.findForward("userlist");
		}
	}
	
	// called from disabled users screen
	public ActionForward enable(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
		
		service = AdminServiceProxy.getService(getServlet().getServletContext());
		messageService = AdminServiceProxy.getMessageService(getServlet().getServletContext());
		
		if (!request.isUserInRole(Role.SYSADMIN)) {
			request.setAttribute("errorName","UserAction");
			request.setAttribute("errorMessage",messageService.getMessage("error.need.sysadmin"));
			return mapping.findForward("error");
		}
		
		Integer userId = WebUtil.readIntParam(request,"userId",true);
		User user = (User)service.findById(User.class,userId);
		
		log.debug("enabling user: "+userId);
		user.setDisabledFlag(false);
		service.save(user);
				
		return mapping.findForward("disabledlist");
	}

}