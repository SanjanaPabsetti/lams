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

package org.lamsfoundation.lams.contentrepository.service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.lamsfoundation.lams.contentrepository.AccessDeniedException;
import org.lamsfoundation.lams.contentrepository.CrCredential;
import org.lamsfoundation.lams.contentrepository.CrNode;
import org.lamsfoundation.lams.contentrepository.CrWorkspace;
import org.lamsfoundation.lams.contentrepository.CrWorkspaceCredential;
import org.lamsfoundation.lams.contentrepository.FileException;
import org.lamsfoundation.lams.contentrepository.ICredentials;
import org.lamsfoundation.lams.contentrepository.ITicket;
import org.lamsfoundation.lams.contentrepository.IValue;
import org.lamsfoundation.lams.contentrepository.IVersionedNode;
import org.lamsfoundation.lams.contentrepository.IVersionedNodeAdmin;
import org.lamsfoundation.lams.contentrepository.InvalidParameterException;
import org.lamsfoundation.lams.contentrepository.ItemExistsException;
import org.lamsfoundation.lams.contentrepository.ItemNotFoundException;
import org.lamsfoundation.lams.contentrepository.LoginException;
import org.lamsfoundation.lams.contentrepository.NodeKey;
import org.lamsfoundation.lams.contentrepository.NodeType;
import org.lamsfoundation.lams.contentrepository.PropertyName;
import org.lamsfoundation.lams.contentrepository.RepositoryCheckedException;
import org.lamsfoundation.lams.contentrepository.RepositoryRuntimeException;
import org.lamsfoundation.lams.contentrepository.ValidationException;
import org.lamsfoundation.lams.contentrepository.ValueFormatException;
import org.lamsfoundation.lams.contentrepository.WorkspaceNotFoundException;
import org.lamsfoundation.lams.contentrepository.dao.ICredentialDAO;
import org.lamsfoundation.lams.contentrepository.dao.IWorkspaceDAO;
import org.lamsfoundation.lams.usermanagement.dto.UserDTO;
import org.lamsfoundation.lams.util.FileUtil;
import org.lamsfoundation.lams.util.FileUtilException;
import org.lamsfoundation.lams.util.zipfile.ZipFileUtil;
import org.lamsfoundation.lams.util.zipfile.ZipFileUtilException;
import org.lamsfoundation.lams.web.session.SessionManager;
import org.lamsfoundation.lams.web.util.AttributeNames;

/**
 * Many methods in this class will throw a RepositoryRuntimeException
 * if the internal data is missing. This is not indicated
 * on the method signatures.
 *
 * The methods in this class do not explicitly check that a credential
 * or ticket has been supplied. This is checked by the
 * checkCredentialTicketBeforeAdvice advisor, for all transactioned
 * calls (see the application context file). Therefore this
 * class must be used in the Spring framework - if it is ever
 * run separately and without suitable AOP support then
 * each transaction method must check that the credential is okay
 * or that the ticket is a known ticket (isTicketOkay() method).
 *
 * This class also depends on the transactions defined in the
 * application context for the hibernate sessions to work properly.
 * If the method isn't transactioned, then there won't be a proper
 * hibernate session in the DAO and all sorts of errors will occur
 * on lazy loading (even lazy loading withing the DAO) and when we
 * write out nodes.
 *
 * So while the only footprint you see here of Spring is the beanfactory,
 * the use of this as a singleton (generated by Spring) affects
 * more than just how the object is created.
 *
 * @author Fiona Malikoff
 */
public class SimpleRepository implements IRepositoryAdmin {

    protected Logger log = Logger.getLogger(SimpleRepository.class);

    private ICredentialDAO credentialDAO = null;
    private IWorkspaceDAO workspaceDAO = null;
    private INodeFactory nodeFactory = null;

    private Set ticketIdSet = new HashSet(); // set of currently known tickets.

    public SimpleRepository() {
	log.info("Repository singleton being created.");
    }

    /* ********** Whole of repository methods - login, logout, addWorkspace, etc ****/

    /**
     * Get the current user from the user's session. This is used to record the user
     * who creates piece of content in the content repository. By doing it here, the
     * tools do not have to be modified nor do we rely on other web-apps to tell
     * us the correct user.
     */
    private Integer getCurrentUserId() throws AccessDeniedException {
	HttpSession ss = SessionManager.getSession();
	if (ss != null) {
	    log.debug("Getting user from UserDTO - must have come from a normal request");
	    UserDTO user = (UserDTO) ss.getAttribute(AttributeNames.USER);
	    if (user == null) {
		throw new AccessDeniedException(
			"Cannot get user details for content repository. User may not be logged in.");
	    }
	    return user.getUserID();
	}
	throw new AccessDeniedException(
		"Cannot get user details for content repository. No session found - user not logged in or the webservice call has not set up the session details.");

    }

    /**
     * @param workspaceId
     * @return
     * @throws WorkspaceNotFoundException
     */
    private CrWorkspace getWorkspace(Long workspaceId) throws WorkspaceNotFoundException {
	// call workspace dao to get the workspace
	CrWorkspace workspace = (CrWorkspace) workspaceDAO.find(CrWorkspace.class, workspaceId);
	if (workspace == null) {
	    throw new WorkspaceNotFoundException("Workspace id=" + workspaceId + " does not exist.");
	}
	return workspace;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.lamsfoundation.lams.contentrepository.IRepository#login(org.lamsfoundation.lams.contentrepository.
     * ICredentials, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public ITicket login(ICredentials credentials, String workspaceName)
	    throws AccessDeniedException, LoginException, WorkspaceNotFoundException {

	if (workspaceDAO == null || credentialDAO == null) {
	    throw new RepositoryRuntimeException(
		    "Workspace or Credential DAO object missing. Unable to process login.");
	}

	CrWorkspace workspace = workspaceDAO.findByName(workspaceName);
	if (workspace == null) {
	    try {
		createCredentials(credentials);
		workspace = addWorkspace(credentials, workspaceName);
	    } catch (Exception e) {
		throw new RepositoryRuntimeException("Error while creating workspace \"" + workspaceName + "\"", e);
	    }
	} else if (!credentialDAO.checkCredential(credentials, workspace)) {
	    throw new LoginException("Login failed. Password incorrect or not authorised to access this workspace.");
	}

	// okay, we should now be able to create a ticket
	// 	make ticket, create new credentials without the password
	ITicket ticket = new SimpleTicket(workspace.getWorkspaceId());
	ticketIdSet.add(ticket.getTicketId());
	return ticket;
    }

    /**
     * Add a workspace, giving the credentials as the user of this workspace.
     * It does not clear the password in the credentials
     *
     * @param credentials
     *            this user/password must already exist in the repository. Password will be checked.
     * @param workspaceName
     * @throws LoginException
     *             if credentials are not authorised to add/access the new workspace.
     * @throws ItemExistsException
     *             if the workspace already exists.
     * @throws RepositoryCheckedException
     *             if parameters are missing.
     */
    @Override
    public CrWorkspace addWorkspace(ICredentials credentials, String workspaceName)
	    throws AccessDeniedException, LoginException, ItemExistsException, RepositoryCheckedException {

	// call workspace dao to check the login and get the workspace
	if (workspaceDAO == null || credentialDAO == null || nodeFactory == null) {
	    throw new RepositoryRuntimeException(
		    "Workspace, Credential DAO or Node Factory object missing. Unable to process login.");
	}

	CrWorkspace workspace = workspaceDAO.findByName(workspaceName);
	if (workspace != null) {
	    throw new ItemExistsException("Workspace " + workspaceName + " already exists, cannot add workspace.");
	}

	// check the credentials
	if (!credentialDAO.checkCredential(credentials)) {
	    throw new LoginException("User not authorised to access the repository.");
	}

	// try to create the workspace - this should be done via the Spring bean factory.
	CrWorkspace crWorkspace = new CrWorkspace();
	crWorkspace.setName(workspaceName);
	workspaceDAO.insert(crWorkspace);
	assignCredentials(credentials, crWorkspace);
	return crWorkspace;
    }

    /**
     * Create a new repository "user" - usually a tool.
     * The password must be at least 6 chars.
     * This method will not wipe out the password in the newCredential object.
     * Possibly this should only be available to an internal management tool
     * *** Security Risk - I'm converting two passwords to a string... ***
     */
    @Override
    public void createCredentials(ICredentials newCredential)
	    throws AccessDeniedException, RepositoryCheckedException, ItemExistsException {
	if (newCredential == null || newCredential.getName() == null || newCredential.getPassword() == null) {
	    throw new RepositoryCheckedException(
		    "Credential is null or name/password is missing - cannot create credential.");
	}

	verifyNewPassword(newCredential.getPassword());

	// check that the user doesn't already exist
	CrCredential cred = credentialDAO.findByName(newCredential.getName());
	if (cred != null) {
	    throw new ItemExistsException(
		    "Credential name " + newCredential.getName() + " already exists - cannot create credential.");
	}

	// try to create the credential - this should be done via the Spring bean factory.
	cred = new CrCredential();
	cred.setName(newCredential.getName());
	cred.setPassword(new String(newCredential.getPassword()));
	credentialDAO.insert(cred);
    }

    /**
     * Update a credential. Name cannot change, so really only the password changes
     * The password must be at least 6 chars.
     * Possibly this should only be available to an internal management tool
     * *** Security Risk - I'm converting the password to a string... ***
     *
     * @throws LoginException
     *             if the oldCredential fails login test (e.g. wrong password)
     * @throws RepositoryCheckedException
     *             if one of the credentials objects are missing
     * @throws RepositoryRuntimeException
     *             if an internal error occurs.
     */
    @Override
    public void updateCredentials(ICredentials oldCredential, ICredentials newCredential)
	    throws AccessDeniedException, LoginException, RepositoryCheckedException, RepositoryRuntimeException {
	///throws RepositoryCheckedException {
	if (workspaceDAO == null || credentialDAO == null) {
	    throw new RepositoryRuntimeException(
		    "Workspace or Credential DAO object missing. Cannot update credentials.");
	}

	if (oldCredential == null || newCredential == null) {
	    throw new RepositoryCheckedException("Credentials missing. Cannot update credentials.");
	}

	if (!credentialDAO.checkCredential(oldCredential)) {
	    throw new LoginException("Old password wrong. Cannot update credentials.");
	}

	char[] newPassword = newCredential.getPassword();
	if (newPassword != null) {
	    // if there isn't a new password then there isn't anything to change...
	    verifyNewPassword(newPassword);
	    CrCredential cred = credentialDAO.findByName(oldCredential.getName());
	    cred.setPassword(new String(newPassword));
	    credentialDAO.update(cred);
	}
    }

    /**
     * Checks that a password meets our password criteria. This could be implemented
     * as a Strategy, but that's overkill!
     *
     * Checks that the password is six or more characters.
     *
     * @param password
     * @throws RepositoryCheckedException
     *             if
     */
    private void verifyNewPassword(char[] password) throws RepositoryCheckedException {
	if (password != null && password.length < 6) {
	    throw new RepositoryCheckedException(
		    "Password invalid - must be 6 or more characters. Cannot create credential.");
	}
    }

    /**
     * Assign credentials to a workspace.
     * Will check the credentials to ensure they are in the database.
     * Possibly this should only be available to an internal management tool
     */
    @Override
    public void assignCredentials(ICredentials credentials, String workspaceName)
	    throws AccessDeniedException, RepositoryCheckedException, WorkspaceNotFoundException, LoginException {

	if (workspaceDAO == null) {
	    throw new RepositoryRuntimeException("Workspace DAO object missing. Cannot assign credentials.");
	}

	if (credentials == null || workspaceName == null) {
	    throw new RepositoryCheckedException("Credentials or workspace is missing. Cannot assign credentials.");
	}

	if (!credentialDAO.checkCredential(credentials)) {
	    throw new LoginException("Credentials are not authorised to have access to the repository/workspace.");
	}

	// call workspace dao to get the workspace
	CrWorkspace workspace = workspaceDAO.findByName(workspaceName);
	if (workspace == null) {
	    throw new WorkspaceNotFoundException("Workspace " + workspaceName + " does not exist.");
	}

	assignCredentials(credentials, workspace);
    }

    /**
     * Assign credentials to a workspace. Assume credentials are already checked.
     * Possibly this should only be available to an internal management tool. Workspace
     * is expected to be attached to a session.
     * *** Security Risk - I'm converting the password to a string by reading it in from the database... ***
     */
    private void assignCredentials(ICredentials credentials, CrWorkspace workspace) throws RepositoryCheckedException {

	if (workspaceDAO == null || credentialDAO == null) {
	    throw new RepositoryRuntimeException(
		    "Workspace or Credential DAO object missing. Cannot assign credentials.");
	}

	if (credentials == null || workspace == null) {
	    throw new RepositoryCheckedException("Credentials or workspace is missing. Cannot assign credentials.");
	}

	CrCredential crCredential = credentialDAO.findByName(credentials.getName());
	if (crCredential == null) {
	    throw new RepositoryCheckedException(
		    "Credential object cannot be found in database. Cannot assign credentials.");
	}

	CrWorkspaceCredential wc = new CrWorkspaceCredential();
	wc.setCrCredential(crCredential);
	wc.setCrWorkspace(workspace);

	Set wcSet = workspace.getCrWorkspaceCredentials();
	if (wcSet == null) {
	    log.debug("Creating new wc set for workspace " + workspace.getName());
	    wcSet = new HashSet();
	    wcSet.add(wc);
	    workspace.setCrWorkspaceCredentials(wcSet);
	} else {
	    Iterator iter = wcSet.iterator();
	    CrWorkspaceCredential found = null;
	    while (iter.hasNext() && found == null) {
		CrWorkspaceCredential item = (CrWorkspaceCredential) iter.next();
		if (item.getCrCredential() != null
			&& item.getCrCredential().getCredentialId().equals(crCredential.getCredentialId())
			&& item.getCrWorkspace() != null
			&& item.getCrWorkspace().getWorkspaceId().equals(workspace.getWorkspaceId())) {
		    found = item;
		}
	    }
	    if (found == null) {
		// not already in the set, so we can add!
		wcSet.add(wc);
	    }
	}

	workspaceDAO.insert(wc);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.lamsfoundation.lams.contentrepository.IRepository#logout(org.lamsfoundation.lams.contentrepository.ITicket)
     */
    @Override
    public void logout(ITicket ticket) throws AccessDeniedException {
	ticketIdSet.remove(ticket.getTicketId());
	ticket.clear();
    }

    /* ********** Node related methods, requiring ticket for access ****/

    /**
     * Is this ticket okay?
     */
    @Override
    public boolean isTicketOkay(ITicket ticket) {
	return (ticket != null && ticketIdSet.contains(ticket.getTicketId()));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.lamsfoundation.lams.contentrepository.IRepository#addFileItem(org.lamsfoundation.lams.contentrepository.
     * ITicket, java.io.InputStream, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public NodeKey addFileItem(ITicket ticket, InputStream istream, String filename, String mimeType,
	    String versionDescription) throws FileException, AccessDeniedException, InvalidParameterException {

	try {
	    CrWorkspace workspace = getWorkspace(ticket.getWorkspaceId());
	    Integer userId = getCurrentUserId();
	    SimpleVersionedNode initialNodeVersion = nodeFactory.createFileNode(workspace, null, null, istream,
		    filename, mimeType, versionDescription, userId);
	    initialNodeVersion.save();
	    return initialNodeVersion.getNodeKey();
	} catch (ValidationException e) {
	    // if this is thrown, then it is bug - nothing external should cause it.
	    throw new RepositoryRuntimeException("Internal error: unable to add file. " + e.getMessage(), e);
	} catch (WorkspaceNotFoundException e) {
	    // if this is thrown, then it is bug - ticket shouldn't contain a workspace that doesn't exist.
	    throw new RepositoryRuntimeException("Internal error: unable to add file. " + e.getMessage(), e);
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.lamsfoundation.lams.contentrepository.IRepository#addPackageItem(org.lamsfoundation.lams.contentrepository.
     * ITicket, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public NodeKey addPackageItem(ITicket ticket, String dirPath, String startFile, String versionDescription)
	    throws AccessDeniedException, InvalidParameterException, FileException {

	CrWorkspace workspace = null;
	try {
	    workspace = getWorkspace(ticket.getWorkspaceId());
	} catch (WorkspaceNotFoundException e) {
	    // if this is thrown, then it is bug - ticket shouldn't contain a workspace that doesn't exist.
	    throw new RepositoryRuntimeException("Internal error: unable to add file. " + e.getMessage(), e);
	}

	Integer userId = getCurrentUserId();
	SimpleVersionedNode packageNode = null;
	packageNode = nodeFactory.createPackageNode(workspace, startFile, versionDescription, userId);

	try {
	    packageNode.addPackageFiles(workspace, dirPath, versionDescription, userId);
	    packageNode.save();
	} catch (ValidationException e) {
	    // if this is thrown, then it is bug - nothing external should cause it.
	    throw new RepositoryRuntimeException("Internal error: unable to add package." + e.getMessage(), e);
	}
	return packageNode.getNodeKey();

    }

    /*
     * (non-Javadoc)
     *
     * @see org.lamsfoundation.lams.contentrepository.IRepository#getFileItem(org.lamsfoundation.lams.contentrepository.
     * ITicket, java.lang.Long, java.lang.Long)
     */
    @Override
    public IVersionedNode getFileItem(ITicket ticket, Long uuid, Long version)
	    throws AccessDeniedException, ItemNotFoundException, FileException {

	return nodeFactory.getNode(ticket.getWorkspaceId(), uuid, version);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.lamsfoundation.lams.contentrepository.IRepository#getFileItem(org.lamsfoundation.lams.contentrepository.
     * ITicket, java.lang.Long, java.lang.Long, java.lang.String)
     */
    @Override
    public IVersionedNode getFileItem(ITicket ticket, Long uuid, Long version, String relPath)
	    throws AccessDeniedException, ItemNotFoundException, FileException {

	long start = System.currentTimeMillis();
	String key = "getFileItem " + uuid;

	IVersionedNode latestNodeVersion = nodeFactory.getNode(ticket.getWorkspaceId(), uuid, version);
	log.error(key + " latestNodeVersion " + (System.currentTimeMillis() - start));

	if (relPath == null) {

	    // return the package node - getFile() on the package node
	    // returns the input stream for the initial path node.
	    return latestNodeVersion;

	} else {

	    // find the node indicated by the relPath
	    IVersionedNode childNode = latestNodeVersion.getNode(relPath);
	    log.error(key + " latestNodeVersion.getNode " + (System.currentTimeMillis() - start));
	    return childNode;
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see org.lamsfoundation.lams.contentrepository.IRepository#getFileItem(org.lamsfoundation.lams.contentrepository.
     * ITicket, java.lang.Long, java.lang.Long, java.lang.String)
     */
    @Override
    public List getPackageNodes(ITicket ticket, Long uuid, Long version)
	    throws AccessDeniedException, ItemNotFoundException, FileException {

	long start = System.currentTimeMillis();
	IVersionedNodeAdmin latestNodeVersion = nodeFactory.getNode(ticket.getWorkspaceId(), uuid, version);
	log.error("getPackageNodes latestNodeVersion " + (System.currentTimeMillis() - start));

	Set childNodes = latestNodeVersion.getChildNodes();
	int childNodesSize = childNodes != null ? childNodes.size() : 0;
	log.error("getPackageNodes getChildNodes " + (System.currentTimeMillis() - start));

	ArrayList list = new ArrayList(1 + childNodesSize);
	list.add(latestNodeVersion);
	list.addAll(childNodes);

	log.error("getPackageNodes end " + (System.currentTimeMillis() - start));
	return list;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.lamsfoundation.lams.contentrepository.IRepository#getNodeList(org.lamsfoundation.lams.contentrepository.
     * ITicket)
     */
    @Override
    public SortedMap getNodeList(ITicket ticket) throws AccessDeniedException {

	Long workspaceId = ticket.getWorkspaceId();
	List nodes = workspaceDAO.findWorkspaceNodes(workspaceId);

	if (log.isDebugEnabled()) {
	    log.debug("Workspace " + workspaceId + " has " + nodes.size() + " nodes.");
	}

	TreeMap map = new TreeMap();
	Iterator iter = nodes.iterator();
	while (iter.hasNext()) {
	    CrNode node = (CrNode) iter.next();
	    map.put(node.getNodeId(), node.getVersionHistory());
	}

	return map;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.lamsfoundation.lams.contentrepository.IRepository#getVersionHistory(org.lamsfoundation.lams.contentrepository
     * .ITicket, java.lang.Long)
     */
    @Override
    public SortedSet getVersionHistory(ITicket ticket, Long uuid) throws ItemNotFoundException, AccessDeniedException {

	IVersionedNode node = nodeFactory.getNode(ticket.getWorkspaceId(), uuid, null);
	return node.getVersionHistory();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.lamsfoundation.lams.contentrepository.IRepository#updateFileItem(org.lamsfoundation.lams.contentrepository.
     * ITicket, java.lang.Long, java.lang.String, java.io.InputStream, java.lang.String, java.lang.String)
     */
    @Override
    public NodeKey updateFileItem(ITicket ticket, Long uuid, String filename, InputStream istream, String mimeType,
	    String versionDescription)
	    throws AccessDeniedException, ItemNotFoundException, FileException, InvalidParameterException {

	Integer userId = getCurrentUserId();
	SimpleVersionedNode newNodeVersion = nodeFactory.getNodeNewVersion(ticket.getWorkspaceId(), uuid, null,
		versionDescription, userId);

	if (!newNodeVersion.isNodeType(NodeType.FILENODE)) {
	    throw new InvalidParameterException("Node is not a file node - it is a " + newNodeVersion.getNodeType()
		    + ". Unable to update as a file.");
	}
	newNodeVersion.setFile(istream, filename, mimeType);

	try {
	    newNodeVersion.save();
	} catch (ValidationException e) {
	    // if this is thrown, then it is bug - nothing external should cause it.
	    throw new RepositoryRuntimeException("Internal error: unable to update file." + e.getMessage(), e);
	}
	return newNodeVersion.getNodeKey();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.lamsfoundation.lams.contentrepository.IRepository#updatePackageItem(org.lamsfoundation.lams.contentrepository
     * .ITicket, java.lang.Long, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public NodeKey updatePackageItem(ITicket ticket, Long uuid, String dirPath, String startFile,
	    String versionDescription)
	    throws AccessDeniedException, ItemNotFoundException, FileException, InvalidParameterException {

	Integer userId = getCurrentUserId();
	SimpleVersionedNode newNodeVersion = nodeFactory.getNodeNewVersion(ticket.getWorkspaceId(), uuid, null,
		versionDescription, userId);

	if (!newNodeVersion.isNodeType(NodeType.PACKAGENODE)) {
	    throw new InvalidParameterException("Node is not a package node - it is a " + newNodeVersion.getNodeType()
		    + ". Unable to update as a package.");
	}

	newNodeVersion.setProperty(PropertyName.INITIALPATH, startFile);

	try {
	    CrWorkspace workspace = getWorkspace(ticket.getWorkspaceId());

	    newNodeVersion.addPackageFiles(workspace, dirPath, versionDescription, userId);
	    newNodeVersion.save();

	} catch (ValidationException e) {
	    // if this is thrown, then it is bug - nothing external should cause it.
	    throw new RepositoryRuntimeException("Internal error: unable to add package." + e.getMessage(), e);
	} catch (WorkspaceNotFoundException e) {
	    // if this is thrown, then it is bug - ticket shouldn't contain a workspace that doesn't exist.
	    throw new RepositoryRuntimeException("Internal error: unable to add file. " + e.getMessage(), e);
	}
	return newNodeVersion.getNodeKey();
    }

    /**
     * Sets the property to a value, based on the specified type. Removes the property if the value is null.
     * Use this for custom properties only - change the filename or mimetype at your own risk.
     *
     * @param name
     *            The name of a property of this node
     * @param value
     *            The value to be assigned
     * @param type
     *            The type of the property
     * @throws ValidationException
     *             if the call has made the node invalid. This would occur if the
     *             call had set the filename to blank.
     */
    @Override
    public void setProperty(ITicket ticket, Long uuid, Long versionId, String name, Object value, int type)
	    throws AccessDeniedException, ItemNotFoundException, ValidationException {

	// check that the previous version was a file node - error otherwise
	SimpleVersionedNode node = nodeFactory.getNode(ticket.getWorkspaceId(), uuid, versionId);
	node.setProperty(name, value, type);
	node.saveDB();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.lamsfoundation.lams.contentrepository.IRepository#copyNodeVersion(org.lamsfoundation.lams.contentrepository.
     * ITicket, java.lang.Long, java.lang.Long)
     */
    @Override
    public NodeKey copyNodeVersion(ITicket ticket, Long uuid, Long versionId)
	    throws AccessDeniedException, ItemNotFoundException {

	Integer userId = getCurrentUserId();
	try {
	    SimpleVersionedNode originalNode = nodeFactory.getNode(ticket.getWorkspaceId(), uuid, versionId);
	    SimpleVersionedNode newNode = nodeFactory.copy(originalNode, userId);
	    newNode.save();
	    return newNode.getNodeKey();

	} catch (ValidationException e) {
	    // if this is thrown, then it is bug - nothing external should cause it.
	    throw new RepositoryRuntimeException("Internal error: unable to copy node. " + e.getMessage(), e);
	} catch (FileException e) {
	    // if this is thrown, then it is bug - nothing external should cause it.
	    throw new RepositoryRuntimeException("Internal error: unable to copy node. " + e.getMessage(), e);
	} catch (ValueFormatException e) {
	    // if this is thrown, then it is bug - nothing external should cause it.
	    throw new RepositoryRuntimeException("Internal error: unable to copy node. " + e.getMessage(), e);
	} catch (InvalidParameterException e) {
	    // if this is thrown, then it is bug - nothing external should cause it.
	    throw new RepositoryRuntimeException("Internal error: unable to copy node. " + e.getMessage(), e);
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.lamsfoundation.lams.contentrepository.IRepository#saveFile(org.lamsfoundation.lams.contentrepository.ITicket,
     * java.lang.Long, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void saveFile(ITicket ticket, Long uuid, Long versionId, String toFileName)
	    throws AccessDeniedException, ItemNotFoundException, IOException, RepositoryCheckedException {
	try {
	    IVersionedNode node = nodeFactory.getNode(ticket.getWorkspaceId(), uuid, versionId);
	    if (node == null) {
		throw new ItemNotFoundException("Unable find File node by uuid [" + uuid + "]");
	    }

	    if (node.isNodeType(NodeType.FILENODE)) {
		saveSigleFile(toFileName, node);
	    } else if (node.isNodeType(NodeType.PACKAGENODE)) {
		Set<IVersionedNode> children = node.getChildNodes();
		String tempRoot = FileUtil.createTempDirectory("export_package");
		for (IVersionedNode child : children) {
		    String path = child.getPath();
		    String fullname = FileUtil.getFullPath(tempRoot, path);

		    //if folder does not exist, create first.
		    FileUtil.createDirectory(FileUtil.getFileDirectory(fullname));

		    saveSigleFile(fullname, child);
		}
		if (toFileName == null) {
		    IValue prop = node.getProperty(PropertyName.FILENAME);
		    toFileName = prop != null ? prop.getString() : null;
		    FileUtil.createDirectory(FileUtil.getTempDir());
		    toFileName = FileUtil.getFullPath(FileUtil.getTempDir(), toFileName);
		}
		ZipFileUtil.createZipFile(FileUtil.getFileName(toFileName), tempRoot,
			FileUtil.getFileDirectory(toFileName));
	    }
	} catch (FileException e) {
	    // if this is thrown, then it is bug - nothing external should cause it.
	    throw new RepositoryRuntimeException("Internal error: unable to save node. " + e.getMessage(), e);
	} catch (ValueFormatException e) {
	    throw new RepositoryRuntimeException("Internal error: unable to save node. " + e.getMessage(), e);
	} catch (FileUtilException e) {
	    throw new RepositoryRuntimeException("Internal error: unable to save node. " + e.getMessage(), e);
	} catch (ZipFileUtilException e) {
	    throw new RepositoryRuntimeException("Internal error: unable to save node. " + e.getMessage(), e);
	}
    }

    /**
     * @param toFileName
     * @param node
     * @throws FileException
     * @throws ValueFormatException
     * @throws FileUtilException
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void saveSigleFile(String toFileName, IVersionedNode node)
	    throws FileException, ValueFormatException, FileUtilException, FileNotFoundException, IOException {
	InputStream is = node.getFile();
	if (toFileName == null) {
	    IValue prop = node.getProperty(PropertyName.FILENAME);
	    toFileName = prop != null ? prop.getString() : null;
	    FileUtil.createDirectory(FileUtil.getTempDir());
	    toFileName = FileUtil.getFullPath(FileUtil.getTempDir(), toFileName);
	}
	OutputStream os = new FileOutputStream(toFileName);
	byte[] out = new byte[8 * 1024];
	int len = -1;
	while ((len = is.read(out)) != -1) {
	    os.write(out, 0, len);
	}
	os.close();
	is.close();

	log.debug("File save success:" + toFileName);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.lamsfoundation.lams.contentrepository.IRepository#updatePackageItem(org.lamsfoundation.lams.contentrepository
     * .ITicket, java.lang.Long, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public String[] deleteNode(ITicket ticket, Long uuid)
	    throws AccessDeniedException, InvalidParameterException, ItemNotFoundException {

	if (uuid == null) {
	    throw new InvalidParameterException("UUID is required for deleteItem.");
	}

	// get the first version of the node and delete from there.
	SimpleVersionedNode latestNodeVersion = nodeFactory.getNode(ticket.getWorkspaceId(), uuid, new Long(1));
	if (latestNodeVersion.hasParentNode()) {
	    throw new InvalidParameterException("You cannot delete a node that is in a package (ie has a parent). "
		    + "Please delete the parent. Node UUID " + uuid);
	}
	List problemPaths = latestNodeVersion.deleteNode();
	return problemPaths != null ? (String[]) problemPaths.toArray(new String[problemPaths.size()]) : new String[0];

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.lamsfoundation.lams.contentrepository.IRepository#updatePackageItem(org.lamsfoundation.lams.contentrepository
     * .ITicket, java.lang.Long, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public String[] deleteVersion(ITicket ticket, Long uuid, Long version)
	    throws AccessDeniedException, InvalidParameterException, ItemNotFoundException {

	if (uuid == null || version == null) {
	    throw new InvalidParameterException("Both uuid and version are required for deleteVersion.");
	}

	// get the first version of the node and delete from there.
	SimpleVersionedNode nodeVersion = nodeFactory.getNode(ticket.getWorkspaceId(), uuid, version);
	List problemPaths = nodeVersion.deleteVersion();
	return problemPaths != null ? (String[]) problemPaths.toArray(new String[problemPaths.size()]) : new String[0];

    }

    public boolean workspaceExists(ICredentials credentials, Long workspaceId) {
	return workspaceDAO.find(CrWorkspace.class, workspaceId) != null;
    }

    public boolean workspaceExists(ICredentials credentials, String workspaceName) {
	return workspaceDAO.findByName(workspaceName) != null;
    }

    /* ********** setters and getters for DAOs *******************/
    /**
     * @return Returns the workspaceDAO.
     */
    public IWorkspaceDAO getWorkspaceDAO() {
	return workspaceDAO;
    }

    /**
     * @param workspaceDAO
     *            The workspaceDAO to set.
     */
    public void setWorkspaceDAO(IWorkspaceDAO workspaceDAO) {
	this.workspaceDAO = workspaceDAO;
    }

    /**
     * @return Returns the credentialDAO.
     */
    public ICredentialDAO getCredentialDAO() {
	return credentialDAO;
    }

    /**
     * @param credentialDAO
     *            The credentialDAO to set.
     */
    public void setCredentialDAO(ICredentialDAO credentialDAO) {
	this.credentialDAO = credentialDAO;
    }

    public INodeFactory getNodeFactory() {
	return nodeFactory;
    }

    public void setNodeFactory(INodeFactory nodeFactory) {
	this.nodeFactory = nodeFactory;
    }

}
