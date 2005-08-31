package org.lamsfoundation.lams.workspace.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import org.lamsfoundation.lams.contentrepository.service.IRepositoryService;
import org.lamsfoundation.lams.contentrepository.service.RepositoryProxy;
import org.lamsfoundation.lams.learningdesign.dao.ILearningDesignDAO;
import org.lamsfoundation.lams.test.AbstractLamsTestCase;
import org.lamsfoundation.lams.usermanagement.dao.IWorkspaceFolderDAO;
import org.lamsfoundation.lams.workspace.dao.IWorkspaceFolderContentDAO;

/**
 * Base class for all workspace tests.
 * 
 * @author Fiona Malikoff
 */
public class BaseWorkspaceTest extends AbstractLamsTestCase {
	
    //  Used to set up the test file.
    private static final String testFileContents = System.getProperty( "This is a testfile." );
    protected String testFileString = null; 

	protected WorkspaceManagementService workspaceManagementService;
	protected IWorkspaceFolderDAO workspaceFolderDAO;
	protected IWorkspaceFolderContentDAO workspaceFolderContentDAO;
	protected ILearningDesignDAO learningDesignDAO;
	protected IRepositoryService repositoryService;	
	
	public BaseWorkspaceTest(String name){
		super(name);
	}
	public void setUp() throws Exception {
		super.setUp();		
		workspaceManagementService =(WorkspaceManagementService)context.getBean("workspaceManagementService");
		workspaceFolderDAO = (IWorkspaceFolderDAO)context.getBean("workspaceFolderDAO");
		workspaceFolderContentDAO =(IWorkspaceFolderContentDAO)context.getBean("workspaceFolderContentDAO");
		learningDesignDAO =(ILearningDesignDAO)context.getBean("learningDesignDAO");
		repositoryService = RepositoryProxy.getLocalRepositoryService();
		testFileString = getTestFileLocation();
	}
	

	/**
	 * (non-Javadoc)
	 * @see org.lamsfoundation.lams.AbstractLamsTestCase#getContextConfigLocation()
	 */
	protected String[] getContextConfigLocation() {
		return new String[] {"org/lamsfoundation/lams/contentrepository/applicationContext.xml",
							 "org/lamsfoundation/lams/localApplicationContext.xml",
							 "org/lamsfoundation/lams/authoring/authoringApplicationContext.xml",
							 "org/lamsfoundation/lams/workspace/workspaceApplicationContext.xml"};
	}

	protected String getHibernateSessionFactoryName() {
		return "coreSessionFactory";
	}

    
	/* Get the location of the test file. If it doesn't exist, create it */
	private String getTestFileLocation() throws FileNotFoundException {

		if ( testFileString == null ) {
			String tempSysDirName = System.getProperty( "java.io.tmpdir" );
			testFileString = tempSysDirName + File.separator + "test.txt";
		}

		// synchronize in case the test cases are running in parallel.
		// don't want to try to create the file more than once.
		// synchronization untested - attempted to synchronise across
		// all instances of this class.
		synchronized (this.getClass()) {
			
			File testFile = new File(testFileString);
			if ( ! testFile.exists() ) {
				FileWriter writer=null;
				try {
					writer = new FileWriter(testFile);
					writer.write(testFileContents);
				} catch (IOException e) {
					fail("Unable to write out test file "+e.getMessage());
					e.printStackTrace();
				} finally {
					try {
						writer.close();
					} catch (IOException e1) {
					}
				}
			}
			
		}
		
		return testFileString;
	}
	
}
