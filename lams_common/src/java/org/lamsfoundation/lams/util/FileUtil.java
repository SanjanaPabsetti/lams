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
/* $$Id$$ */
package org.lamsfoundation.lams.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.lamsfoundation.lams.util.zipfile.ZipFileUtilException;

/**
 * General File Utilities
 */
public class FileUtil {

	private static Logger log = Logger.getLogger(FileUtil.class);
	
	protected static final String prefix = "lamstmp_"; // protected rather than private to suit junit test
	public static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

	/** 
	 * Deleting a directory using File.delete() only works if
	 * the directory is empty. This method deletes a directory and 
	 * all of its contained files.
	 * 
	 * This method is not transactional - if it fails to delete some
	 * contained files or directories, it will continue deleting all 
	 * the other files in the directory. If only a partial deletion
	 * is done, then the files and directories that could not be
	 * deleted are listed in the log file, and the method returns false.
	 * 
	 * This method has not been tested in Linux or Unix systems, 
	 * so the behaviour across symbolic links is unknown.
	 */
	public static boolean deleteDirectory(File directory) {
	    boolean retValue = true;

		File[] files = directory.listFiles();
		if ( files != null ) {
			for ( int i=0; i<files.length; i++ ) {
				File file = files[i];
				if ( file.isDirectory() ) {
					deleteDirectory(file);
				} else if ( ! file.delete() ) {
					log.error("Unable to delete file "+file.getName());
					retValue = false;
				}
			}
		}
		if ( directory.delete() ) {
		    return retValue;
		} else {
		    return false;
		}
	}
	
	public static boolean deleteDirectory(String directoryName) throws FileUtilException {
		boolean isDeleted = false;
		if (directoryName == null || directoryName.length() == 0)
			throw new FileUtilException("A directory name must be specified");
	
		File dir = new File(directoryName);
		isDeleted = deleteDirectory(dir);
		
		return isDeleted;
		
	
	}
	
	/**
	 * Create a temporary directory with the name in the form
	 * lamstmp_timestamp_suffix inside the default temporary-file directory 
	 * for the system.
	 * This method is protected (rather than private) so that it may be 
	 * called by the junit tests for this class.
	 * @param zipFileName
	 * @return name of the new directory
	 * @throws ZipFileUtilException if the java io temp directory is not defined,
	 * or we are unable to calculate a unique name for the expanded directory,
	 * or an IOException occurs.
	 */
	public static String createTempDirectory(String suffix) throws FileUtilException {
	    
		String tempSysDirName = TEMP_DIR;
		if ( tempSysDirName == null )
			throw new FileUtilException("No temporary directory known to the server. [System.getProperty( \"java.io.tmpdir\" ) returns null. ]\n Cannot upload package.");
	
		String tempDirName = tempSysDirName+File.separator
			+prefix+System.currentTimeMillis()+"_"+suffix;
		
		// try 100 slightly different variations. If I can't find a unique
		// one in ten tries, then give up.
		File tempDir = new File(tempDirName);
		int i = 0;
		while ( tempDir.exists() && i < 100 ) {
			tempDirName = tempSysDirName+File.separator
				+prefix+System.currentTimeMillis()+"_"+i+suffix;
			tempDir = new File(tempDirName);
		}
		if ( tempDir.exists() )
			throw new FileUtilException("Unable to create temporary directory. The temporary filename/directory that we would use to extract files already exists: "
					+tempDirName);
		
		tempDir.mkdirs();
		return tempDirName;
	}
	
	/**
	 * This method creates a directory with the name <code>directoryName</code>.
	 * Also creates any necessary parent directories that may not yet exist.
	 * 
	 * If the directoryname is null or an empty string, a FileUtilException is thrown
	 * @param directoryName the name of the directory to create
	 * @return boolean. Returns true if the directory is created and false otherwise
	 * @throws FileUtilException if the directory name is null or an empty string
	 */
	public static boolean createDirectory(String directoryName) throws FileUtilException
	{
		boolean isCreated = false;
		//check directoryName to see if its empty or null
		if (directoryName == null || directoryName.length() == 0)
			throw new FileUtilException("A directory name must be specified");
	
		File dir = new File(directoryName);
		isCreated = dir.exists() ? false : dir.mkdirs();
		
		return isCreated;
	}
	
	/**
     * Creates a subdirectory under the parent directory <code>parentDirName</code>
     * If the parent or child directory is null, FileUtilException is thrown.
     * 
     * If the parent directory has not been created yet, it will be created.
     * 
     * 
	 * @param parentDirName The name of the parent directory in which the subdirectory should be created in
	 * @param subDirName The name of the subdirectory to create
	 * @return boolean. Returns true if the subdirectory was created and false otherwise
	 * @throws FileUtilException if the parent/child directory name is null or empty.
	 */
	public static boolean createDirectory(String parentDirName, String subDirName) throws FileUtilException
	{
		boolean isSubDirCreated = false;
		boolean isParentDirCreated;
	
		if (parentDirName == null || parentDirName.length()==0 || subDirName == null || subDirName.length() == 0)
			throw new FileUtilException("A parent or subdirectory name must be specified");
	
		File parentDir = new File(parentDirName);
		if (!parentDir.exists())
			isParentDirCreated = createDirectory(parentDirName);
		else
			isParentDirCreated = true; //parent directory already exists
		
		if (trailingForwardSlashPresent(parentDirName)) /* for eg. "parentDirectoryName/" <-- slash at end of name */
			parentDirName = removeTrailingForwardSlash(parentDirName);
		
		//concatenate the two together
		String combinedDirName = parentDirName + File.separator + subDirName;
		
		isSubDirCreated = createDirectory(combinedDirName);
				
		return isSubDirCreated && isParentDirCreated;
	}
	
	/**
	 * If the directory name specified has a slash at the end of it
	 * such as "directoryName/", then the slash will be removed
	 * and "directoryName" will be returned. The createDirectory(parentdir, childdir)
	 * method requires that there is no slash at the end of the directory name.
	 * @param stringToModify
	 * @return
	 */
	public static String removeTrailingForwardSlash(String stringToModify)
	{
		String stringWithoutSlashAtEnd = stringToModify.substring(0, stringToModify.length()-1);
		return stringWithoutSlashAtEnd;
	}
	
	/**
	 * Checks to see if there is a slash at the end of the string.
	 * 
	 * @param stringToCheck the directoryName to check
	 * @return boolean. Returns true if there is a slash at the end and false if not.
	 */
	public static boolean trailingForwardSlashPresent(String stringToCheck)
	{
		int indexOfSlash = stringToCheck.lastIndexOf("/");
		if (indexOfSlash == (stringToCheck.length()-1))
			return true;
		else
			return false;
	}
	
	public static boolean directoryExist(String directoryToCheck)
	{
		File dir = new File(directoryToCheck);
		return dir.exists();
	}
	
	private static String generateDumpFilename(String id, String extension) throws FileUtilException {
		// get dump directory name and make sure directory exists
		String dumpDirectory = Configuration.get(ConfigurationKeys.LAMS_DUMP_DIR);
		if ( dumpDirectory == null ) {
			dumpDirectory = TEMP_DIR;
		}
		createDirectory(dumpDirectory);
	
		String dumpFilename = dumpDirectory+File.separator
			+id+System.currentTimeMillis()
			+( extension != null ? "."+extension : "");
		
		File dumpFile = new File(dumpFilename);
		int i = 0;
		while ( dumpFile.exists() && i < 100 ) {
			dumpFilename = dumpDirectory+File.separator
				+id+System.currentTimeMillis()+"_"+i
				+( extension != null ? "."+extension : "");
			dumpFile = new File(dumpFilename);
		}
		if ( dumpFile.exists() ) {
			throw new FileUtilException("Unable to create dump file. The filename that we would use already exists: "
					+dumpFile);
		}
		
		return dumpFilename;
	}
	/** 
	 * Dump some data to a file in the Dump Directory. The directory is set in the LAMS
	 * configuration file. These dumps are primarily for support/debugging/problem reporting
	 * uses.
	 * 
	 * If the dump directory is not set, it will revert to the system temp directory.
	 * 
	 * Used by the FlashCrashDump servlet initially, may be used by other dump methods in future.
	 * 
	 * @param data data to dump
	 * @param id some identification name for the string. Does not need to be unique. e.g. FLASH_jsmith
	 * @param extension optional extension to be added to filename e.g. xml. Note: do not include the "." - that
	 * will be added automatically.
	 * 
	 * @author Fiona Malikoff
	 * @throws FileUtilException 
	 */
	public static String createDumpFile(byte[] data, String id, String extension) throws FileUtilException {
		String dumpFilename = generateDumpFilename(id, extension);
		OutputStream dumpFile = null;
		try {
			dumpFile = new FileOutputStream(dumpFilename);
			dumpFile.write(data);
		} catch (IOException e) {
			log.error("Unable to write dump out byte array to dump file. ID: "+id
					+" Dump: "+data+" Exception "+e.getMessage(), e);
			throw new FileUtilException(e);
		} finally {
			try {
				if ( dumpFile != null )
				dumpFile.close();
			} catch (IOException e) {
				log.error("Unable to close dump file. ID: "+id
						+" Dump: "+data+" Exception "+e.getMessage(), e);
				throw new FileUtilException(e);
			}
		}
		return dumpFilename;		
	}
	/**
	 * get file name from a string which may include directory information.
	 * For example : "c:\\dir\\ndp\\pp.txt"; will return pp.txt.?
	 * If file has no path infomation, then just return input fileName.
	 * 
	 */
	public static String getFileName(String fileName){
		if(fileName == null)
			return "";
			
		fileName = fileName.trim();

		int dotPos = fileName.lastIndexOf("/");
		int dotPos2 = fileName.lastIndexOf("\\");
		dotPos = Math.max(dotPos,dotPos2);
		if (dotPos == -1){
			return fileName;
		}
		return fileName.substring(dotPos + 1, fileName.length());
		
	}	
	/** 
	 * Get file directory info.
	 * @param fileName with path info.
	 * @return return only path info with the given fileName
	 */
	public static String getFileDirectory(String fileName){
		if(fileName == null)
			return "";
		
		fileName = fileName.trim();

		int dotPos = fileName.lastIndexOf("/");
		int dotPos2 = fileName.lastIndexOf("\\");
		dotPos = Math.max(dotPos,dotPos2);
		if (dotPos == -1){
			return "";
		}
		//return the last char is '/'
		return fileName.substring(0,dotPos+1);
	
	}
	/**
	 * Merge two input parameter into full path and adjust File.separator to 
	 * OS default separator as well.
	 * 
	 * @param path 
	 * @param file could be file name,or sub directory path.
	 * @return
	 */
	public static String getFullPath(String path, String file){
		String fullpath;
		if(path.endsWith(File.separator))
			fullpath = path + file;
		else
			fullpath = path + File.separator + file;
		if(File.separator.indexOf("\\") != -1)
			fullpath = fullpath.replaceAll("\\/","\\\\");
		else
			fullpath = fullpath.replaceAll("\\\\",File.separator);
		
		return fullpath;
		
	}
}
