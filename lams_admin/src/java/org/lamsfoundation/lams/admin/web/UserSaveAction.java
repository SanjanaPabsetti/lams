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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 * 
 * http://www.gnu.org/licenses/gpl.txt
 * ****************************************************************
 */

/* $Id$ */
package org.lamsfoundation.lams.admin.web;

import java.util.Date;
import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.DynaActionForm;
import org.lamsfoundation.lams.usermanagement.AuthenticationMethod;
import org.lamsfoundation.lams.usermanagement.Country;
import org.lamsfoundation.lams.usermanagement.Language;
import org.lamsfoundation.lams.usermanagement.Organisation;
import org.lamsfoundation.lams.usermanagement.Role;
import org.lamsfoundation.lams.usermanagement.User;
import org.lamsfoundation.lams.usermanagement.UserOrganisation;
import org.lamsfoundation.lams.usermanagement.UserOrganisationRole;
import org.lamsfoundation.lams.usermanagement.Workspace;
import org.lamsfoundation.lams.usermanagement.service.IUserManagementService;
import org.lamsfoundation.lams.web.util.HttpSessionManager;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author Jun-Dir Liew
 *
 * Created at 12:35:38 on 14/06/2006
 */

/**
 * struts doclets
 * 
 * @struts:action path="/usersave"
 *                name="UserForm"
 *                input=".user"
 *                scope="request"
 *                validate="false"
 *
 * @struts:action-forward name="user"
 *                        path=".user"                
 * @struts:action-forward name="userlist"
 *                        path="/usermanage.do"
 */
public class UserSaveAction extends Action {

	private static Logger log = Logger.getLogger(UserSaveAction.class);
	private static WebApplicationContext ctx = WebApplicationContextUtils
			.getWebApplicationContext(HttpSessionManager.getInstance()
					.getServletContext());
	private static IUserManagementService service = (IUserManagementService) ctx
			.getBean("userManagementServiceTarget");
	
	public ActionForward execute(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
		DynaActionForm userForm = (DynaActionForm)form;
		Boolean edit = false;
		Integer orgId = (Integer)userForm.get("orgId");
		
		if(isCancelled(request)){
			request.setAttribute("org",orgId);
			return mapping.findForward("userlist");
		}
		
		ActionMessages errors = new ActionMessages();
		if((userForm.get("login")==null)||(((String)userForm.getString("login").trim()).length()==0)){
			errors.add("login",new ActionMessage("error.login.required"));
		}
		if((userForm.get("password")==null)||(((String)userForm.getString("password").trim()).length()==0)){
			errors.add("password",new ActionMessage("error.password.required"));
		}
		if(!userForm.get("password").equals(userForm.get("password2"))){
			errors.add("password",new ActionMessage("error.newpassword.mismatch"));
		}
		
		if(errors.isEmpty()){
			Integer userId = (Integer)userForm.get("userId");
			User user;
			log.debug("got userId: "+userId);
			if(userId!=0){    // edit user
				edit = true;
				log.debug("editing userId: "+userId);
				user = (User)service.findById(User.class,userId);
				BeanUtils.copyProperties(user,userForm);
				service.save(user);
				
				String[] roles = (String[])userForm.get("roles");
				List rolesList = Arrays.asList(roles);
				log.debug("rolesList.size: "+rolesList.size());
				Set uos = user.getUserOrganisations();
				Iterator iter = uos.iterator();
				while(iter.hasNext()){
				    UserOrganisation uo = (UserOrganisation)iter.next();
				    if(uo.getOrganisation().getOrganisationId().equals(orgId)){
				    	Set uors = uo.getUserOrganisationRoles();
				        for(int i=0; i<roles.length; i++){    // add new roles set by user
				        	Integer roleId = Integer.valueOf(roles[i]);
				        	Boolean alreadyHasRole = false;
				        	Iterator iter2 = uors.iterator();
				        	while(iter2.hasNext()){
				        		UserOrganisationRole uor = (UserOrganisationRole)iter2.next();
				        		if(uor.getRole().getRoleId().equals(roleId)){
				        			alreadyHasRole = true;
				        			break;  // already found uor that matches this role
				        		}
				        	}
				        	if(!alreadyHasRole){    // add new role
				        		Role currentRole = (Role)service.findById(Role.class,roleId);
					            log.debug("setting role: "+currentRole);
					            UserOrganisationRole newUor = new UserOrganisationRole(uo,currentRole);
					            service.save(newUor);  // weird spring?/hibernate? behaviour where only first row is added
					            /*uors.add(newUor);
					            uo.setUserOrganisationRoles(uors);
					            user.setUserOrganisations(uos);
					            service.save(user);*/
					            //log.debug("num roles: "+uo.getUserOrganisationRoles().size());
				        	}
				        }
				        Iterator iter3 = uors.iterator();
				        while(iter3.hasNext()){
				        	UserOrganisationRole uor = (UserOrganisationRole)iter3.next();
				        	Integer currentRoleId = uor.getRole().getRoleId();
				        	//log.debug("currentRoleId: "+currentRoleId.toString());
				        	//log.debug("rolesList: "+rolesList);
				        	if(rolesList.indexOf(currentRoleId.toString())<0){    // remove roles not set by user
				        		log.debug("removing role: "+currentRoleId);
				        		iter3.remove();
				        		//log.debug("num roles: "+uors.size());
				        		uo.setUserOrganisationRoles(uors);
				        		uos.add(uo);
				        		user.setUserOrganisations(uos);
				        	}
				        }
				        break;  // already found uo that matches this org
				    }
				}
				service.save(user);

			}else{    // create user
				log.debug("creating user...");
				user = new User();
				BeanUtils.copyProperties(user,userForm);
				log.debug("new login: "+user.getLogin());
				if(service.getUserByLogin(user.getLogin())!=null){
					errors.add("loginUnique",new ActionMessage("error.login.unique"));
				}
				if(errors.isEmpty()){
					user.setDisabledFlag(false);
					user.setCreateDate(new Date());
					user.setAuthenticationMethod((AuthenticationMethod)service.findByProperty(AuthenticationMethod.class,"authenticationMethodName","LAMS-Database").get(0));
					user.setUserId(null);
					service.save(user);
					log.debug("user: "+user.toString());
					log.debug("organisation: "+service.findById(Organisation.class,orgId));
					UserOrganisation userOrganisation = new UserOrganisation(user, (Organisation)service.findById(Organisation.class,orgId));
					service.save(userOrganisation);
					log.debug("userOrganisation: "+userOrganisation);
					HashSet uos = new HashSet();
					uos.add(userOrganisation);
					user.setUserOrganisations(uos);
					//service.save(user);
				}
			}
		}
		
		if(errors.isEmpty()){
			request.setAttribute("org",orgId);
			log.debug("orgId: "+orgId);
			return mapping.findForward("userlist");
		}else{
			if(!edit){  // error screen on create user shouldn't show empty roles
			    userForm.set("userId",null);
			}
		    saveErrors(request,errors);
		    Organisation org = (Organisation)service.findById(Organisation.class,orgId);
			Organisation pOrg = org.getParentOrganisation();
			if(pOrg!=null){
				request.setAttribute("pOrgId",pOrg.getOrganisationId());
				request.setAttribute("pOrgName",pOrg.getName());
			}
			request.setAttribute("orgId",orgId);
			request.setAttribute("orgName",org.getName());
		    request.setAttribute("countries",service.findAll(Country.class));
		    request.setAttribute("languages",service.findAll(Language.class));
		    return mapping.findForward("user");
		}
	}
}
