package de.mz.jk.plgs.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Project implements Comparable<Project>
{
/* project properties given by PLGS and ISOQuant-DB */
	
	/** DB: row index */
	public int index=0;

	/** PLGS: unique id e.g. Proj__12505921454590_3868760712547077 */
	public String id="";

	/** PLGS: human readable title */
	public String title="";

	/** PLGS: file system directory path containing the folder named with project's id */
	public String root="";
	
	/** DB: some state, may be used for additional information */
	public String state="";
	
	/** additional information */
	public String info = "";
	
	/** DB: database schema name */
	public String db = "";

	/** file describing this project **/
	public String projectFilePath = "";

	/** PLGS: list of known samples from Project.xml: PROJECT.SAMPLES */
	public List<Sample> samples = new ArrayList<Sample>();
	
	/** PLGS: list of known expression analyses from Project.xml: PROJECT.EXPRESSION_ANALYSES */
	public List<ExpressionAnalysis> expressionAnalyses = new ArrayList<ExpressionAnalysis>();
	
	/**
	 * builds file path from project's attributes by using following pattern<br>
	 * [root]/[id]/Project.xml
	 * @return project files path
	 */
	public String getProjectFilePath()
	{
		if (projectFilePath == null || projectFilePath.length() < 1)
			projectFilePath = root + File.separator + id + File.separator + "Project.xml";
		return projectFilePath;
	}
	
	/**
	 * builds project directories path by using following pattern<br>
	 * [root]/[id]
	 * @return
	 */
	public String getProjectDirectoryPath() 
	{
		return root + File.separator + id;
	}
	
	/** title prefix used in toString() */
	public String titlePrefix = "";
	/** title suffix used in toString() */
	public String titleSuffix = "";
	/** constructs project's string representation */
	@Override public String toString()
	{
		return titlePrefix + title + titleSuffix;
	}
	
	/** the ability for a list of {@link Project}s to be ordered */
	@Override public int compareTo(Project p)
	{
		return this.title.compareTo(p.title);
	}
	

	/**
	 * TODO implement a complete cloning of all project components like Expression Analyses, Groups, Samples, Runs, ...
	 */
	/**
	 * clone project,
	 * no samples and no expression analyses copied!!!
	 */
	@Override public Project clone()
	{
		Project clone = new Project();
		clone.index = index;
		clone.id = id;
		clone.title = title;
		clone.root = root;
		clone.state = state;
		clone.info = info;
		clone.db = db;
		clone.titlePrefix = titlePrefix;
		clone.titleSuffix = titleSuffix;
		clone.projectFilePath = projectFilePath;

		return clone;
	}

	/**
	 * remove given expression analysis from its old project 
	 * and add it to this project
	 * @param ea the expression analysis object
	 */
	public void addExpressionAnalysis(ExpressionAnalysis ea)
	{
		if(ea.project!=null) ea.project.expressionAnalyses.remove(ea);
		ea.project = this;
		if(!expressionAnalyses.contains(ea)) expressionAnalyses.add(ea);
	}
	
/* ----------- WORKAROUND SOLUTIONS -------------- */	
	/**
	 * list of expression analysis ids<br>
	 * <b>this is a workaround for being able to determine the number of existing expression analyses
	 */
	public List<String> expressionAnalysisIDs = new ArrayList<String>();
	
	/**
	 * list of selected expression analysis ids
	 * <b>this is a workaround for beeing able to determine the number of existing expression analyses
	 */
	public List<String> selectedExpressionAnalysisIDs = new ArrayList<String>();
	
	public void dump()
	{
		System.out.println("Project '" + titlePrefix + title + titleSuffix + "'");
		System.out.println("\tid:\t" + id);
		System.out.println("\troot:\t" + root);
		System.out.println("\tdir:\t" + getProjectDirectoryPath() );
		System.out.println("\tpath:\t" + getProjectFilePath());
		System.out.println( "\tdb:\t" + db );
	}
}
