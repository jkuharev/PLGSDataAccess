package de.mz.jk.plgs.reader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;

import de.mz.jk.jsix.libs.XFiles;
import de.mz.jk.jsix.libs.XJDOM;
import de.mz.jk.plgs.data.Project;
import de.mz.jk.plgs.data.Sample;
import de.mz.jk.plgs.data.Workflow;

/**
 * extracting information from a PLGS's Project.xml file
 * @author J.Kuharev
 * @since 2009-09-01
 */
public class ProjectReader 
{
	public static final boolean DEBUG = false;
	private Element doc = null;
	private File file = null;
	
	/**
	 * @param prjXMLFile
	 * @throws Exception
	 */
	public ProjectReader(File prjXMLFile) throws Exception
	{
		readFile(prjXMLFile);
	}
	
	/**
	 * read Project.xml file
	 * @param prjXMLFile the file
	 * @throws Exception
	 */
	private void readFile(File prjXMLFile) throws Exception
	{
		this.file = prjXMLFile;
		try 
		{
			doc = XJDOM.getBadJDOMRootElement(prjXMLFile);
		}
		catch (Exception e) 
		{
			System.err.println("something is wrong with file '"+prjXMLFile.getAbsolutePath()+'"');
			throw(e);
		}
	}
	
	/**
	 * finds and lists Project.xml files in all subdirectories of given root directory 
	 * @param rootDirectory
	 * @return a list of Project.xml files
	 * @throws Exception
	 */
	public static List<File> getProjectFileList(File rootDirectory) throws Exception
	{
		List<File> res = XFiles.getFileList(rootDirectory, "project.xml", 1, true, false);
		if (DEBUG)
		{
			if(res.size()<1)
			{
				System.out.println("no Project.xml files found in subfolders of '"+rootDirectory+"'");
				System.out.println("following content found:");
				
				File[] rfs = rootDirectory.listFiles();
				for(File rf : rfs)
				{
					if( rf.isDirectory() )
					{
						System.out.println("\t./" + rf.getName() + "/");
						File[] fs = rf.listFiles();
						for(File f : fs)
						{
							if( f.isFile() )
							{
								System.out.println("\t\t" + f.getName() );
							}
							else
							{
								System.out.println("\t\t" + f.getName() + "/");
							}
						}
					}
					else
					{
						System.out.println("\t./" + rf.getName() + "");	
					}
				}
			}
			else
			{
				System.out.println(res.size() + " Project.xml files found!");
			}
		}
		
		return res;
	}	
	
	/**
	 * 
	 * @param loadSamples if true the reader will collect detailed sample information
	 * 			from workflow xml files 
	 * @return
	 */
	public Project getProject(boolean loadSamples)
	{
		Project prj = new Project();
			prj.root = file.getParentFile().getParentFile().getAbsolutePath();
			prj.id = XJDOM.getAttributeValue(doc, "PROJECT_ID");
			prj.title = XJDOM.getAttributeValue(doc, "TITLE");
			if(loadSamples) prj.samples = getSamples(prj);
			prj.expressionAnalysisIDs = getExpressionAnalysisIDs();
			prj.selectedExpressionAnalysisIDs.addAll( prj.expressionAnalysisIDs );

		ExpressionAnalysisReader.readExpressionAnalyses( prj );
		return prj;
	}
	
	/**
	 * @return Array of EXPRESSION_ANALYSIS_ID
	 */
	private List<String> getExpressionAnalysisIDs()
	{
		List<String> res = new ArrayList<String>();
		
		List<Element> eas = XJDOM.getChildren(doc, "EXPRESSION_ANALYSIS_EXTERNAL_REF", false);
		for(Element ea : eas)
		{
			List<Element> eaRefs = XJDOM.getChildren(ea, "REF_ATTRIBUTE", false);
			for(Element eaRef : eaRefs)
			{
				if(XJDOM.getAttributeValue(eaRef, "NAME").equalsIgnoreCase("EXPRESSION_ANALYSIS_ID"))
					res.add( XJDOM.getAttributeValue(eaRef, "VALUE") );
			}
		}
		return res;
	}
	
	/**
	 * grab samples and workflows from current Project.xml<br>
	 * we accept only "Electrospray-Shotgun" workflows at this moment!!! 
	 * @return list of samples
	 */
	private List<Sample> getSamples( Project prj )
	{
		List<Sample> res = new ArrayList<Sample>();
		List<Element> ses = XJDOM.getChildren(doc, "SAMPLE", false);
		System.out.println(ses.size() + " samples found, reading workflows ...");
		System.out.print("\t");
		for(Element se : ses)
		{
			Sample sample = new Sample();
			sample.name = XJDOM.getAttributeValue(se, "NAME");
			sample.id = XJDOM.getAttributeValue(se, "ID");
			
			List<Element> stes = XJDOM.getChildren(se, "SAMPLE_TRACKING", false);
			for(Element ste : stes)
			{
				String st_id = XJDOM.getAttributeValue(ste, "ID");
				String st_in_file = XJDOM.getAttributeValue(ste, "MS_DATA_NAME");
				List<Element> wfes = XJDOM.getChildren(ste, "WORKFLOW_EXTERNAL_REF", false);
				for(Element we : wfes)
				{
					List<Element> weRefs = XJDOM.getChildren(we, "REF_ATTRIBUTE", false);
					for(Element weRef : weRefs)
					{
						if(XJDOM.getAttributeValue(weRef, "NAME").equalsIgnoreCase("ID"))
						{
							Workflow w = new Workflow();
							w.sample_tracking_id = st_id;
							w.input_file = st_in_file;
							w.id = XJDOM.getAttributeValue(weRef, "VALUE");
							try
							{
								w.checkXMLFilePaths( prj );
								File workflowXML = new File( w.workflowXMLFilePath );
								String imode = WorkflowReader.getInstrumentMode( workflowXML );
								
								if( DEBUG ) 
									System.out.println( "\n" + w.workflowXMLFilePath + "\t-> instrument mode: " + imode );
								
								if( imode.contains("Electrospray-Shotgun") )
								{
									System.out.print(".");									
									w = WorkflowReader.getWorkflow( workflowXML, false, Workflow.AcquisitionMode.DIA );
									sample.workflows.add(w);
								}
                                else if ( imode.contains("DDA"))
                                {
                                    System.out.print(".");
									w = WorkflowReader.getWorkflow( workflowXML, false, Workflow.AcquisitionMode.DDA );
                                    sample.workflows.add(w);
                                }
								else
								{
									System.out.print(":");
								}
							} catch (Exception e) 
							{
								System.err.println( "cannot read file: " + w.workflowXMLFilePath );
								e.printStackTrace();
							}
						}
					}					
				}
			}
			System.out.print(" ");

			res.add(sample);
		}
		System.out.println();
		return res;
	}
	
	/**
	 * equivalent to call of 
	 * 	readFile(file); 
	 * and then
	 * 	getProject();
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static Project getProject( File file, boolean fillSamples ) throws Exception 
	{
		return new ProjectReader(file).getProject(fillSamples); 
	}
}
