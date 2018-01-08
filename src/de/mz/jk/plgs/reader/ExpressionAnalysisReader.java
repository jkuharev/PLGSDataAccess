package de.mz.jk.plgs.reader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;

import de.mz.jk.jsix.libs.XJDOM;
import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.plgs.data.*;


/**
 * 
 * @author J.Kuharev
 * @since 2009-09-02
 */
public class ExpressionAnalysisReader 
{	
	private Element doc = null;

	/**
	 * read all expression analyses from a project
	 * and assign them to it
	 * @param p the project
	 */
	public static void readExpressionAnalyses(Project p)
	{
		ExpressionAnalysisReader ear = new ExpressionAnalysisReader();
		for(int i=0; i<p.expressionAnalysisIDs.size(); i++)
		{
			String eaid = p.expressionAnalysisIDs.get(i);
			try {
				ear.readExpressionAnalysis( p.root, p.id, eaid );
				p.expressionAnalyses.add( ear.getExpressionAnalysis() );
			}catch(Exception e){
				System.err.print("*");
				p.expressionAnalysisIDs.remove(i);
				i--;
			}
		}
	}

	/**
	 * assigns an ExpressionAnalysis file to work with
	 * @param file
	 * @throws Exception
	 */
	public void readFile(File file) throws Exception
	{
		if(file.canRead()) 
			doc = XJDOM.getBadJDOMRootElement(file);
		else
			throw new Exception("can not read file " + file.getAbsolutePath());
	}	
	
	/**
	 * assigns an ExpressionAnalysis file to work with 
	 * by building its path from ProjectID and ExpressionAnalysisID
	 * @param rootDir
	 * @param projectID
	 * @param expressionAnalysisID
	 * @throws Exception
	 */
	public void readExpressionAnalysis(String rootDir, String projectID, String expressionAnalysisID) throws Exception
	{
		readFile( getFile(rootDir, projectID, expressionAnalysisID) );
	}
	
	/**
	 * short version for readExpressionAnalysis(String rootDir, String projectID, String expressionAnalysisID)
	 * rootDir and projectID are extracted from project
	 * @param project
	 * @param expressionAnalysisID
	 * @throws Exception
	 */
	public void readExpressionAnalysis(Project project, String expressionAnalysisID) throws Exception
	{
		readFile( getFile(project.root, project.id, expressionAnalysisID) );
	}	
	
	/**
	 * constructs a File object from ExpressionAnalysisID
	 * @param rootDir
	 * @param projectID
	 * @param expressionAnalysisID 
	 * @return File object from constructed path
	 */
	public static File getFile(String rootDir, String projectID, String expressionAnalysisID)
	{
		return new File(
			rootDir + File.separator + 
			projectID + File.separator +
			"ExpressionAnalyses" + File.separator +
			expressionAnalysisID +  File.separator +
			expressionAnalysisID +  ".xml"
		);
	}
	
	/**
	 * extracts ExpressionAnalysis data from previously read XML-Document
	 * @return
	 */
	public ExpressionAnalysis getExpressionAnalysis()
	{
		ExpressionAnalysis res = new ExpressionAnalysis(); 
			res.id = XJDOM.getAttributeValue(doc, "ID");
			res.name = XJDOM.getAttributeValue(doc, "NAME");
			res.description = XJDOM.getAttributeValue(doc, "DESCRIPTION");
			res.result_id = extractResultID();		
			res.groups = getGroups();
		return res;
	}
	
	private String extractResultID() 
	{
		try
		{
			Element attscont = XJDOM.getChildren(doc, "EXPRESSION_ANALYSIS_RESULT_EXTERNAL_REF", false).get(0);
			List<Element> atts = XJDOM.getChildren(attscont, "REF_ATTRIBUTE", false);
			
			for(Element at :atts)
			{
				if(XJDOM.getAttributeValue(at, "NAME").equalsIgnoreCase("ID"))
					return XJDOM.getAttributeValue(at, "VALUE");
			}	
		}
		catch(Exception e){}
		return "";
	}

	/**
	 * recursively extracts groups.<br>
	 * each group contains a list of samples,<br>
	 * each sample contains a list of workflows,<br>
	 * ATTENTION: each workflow gets attributes: id, sample_tracking_id, replicate_name 
	 * their make possible to find the workflow file
	 * @return a list of groups
	 */
	public List<Group> getGroups()
	{
		List<Group> res = new ArrayList<Group>();
		List<Element> nodes = XJDOM.getChildren(doc, "EXPRESSION_ANALYSIS_EXPERIMENT_GROUP", false);
		for(Element e : nodes)
		{
			Group g = new Group();
			g.id = XJDOM.getAttributeValue(e, "ID");
			g.name = XJava.decURL( XJDOM.getAttributeValue(e, "NAME") );
			
			// get samples
			List<Element> sn = XJDOM.getChildren(e, "EXPRESSION_ANALYSIS_EXPERIMENT_SAMPLE", false);
			for(Element se : sn)
			{
				Sample sample = new Sample();
				sample.id = XJDOM.getAttributeValue(se, "ID");
				sample.name = XJava.decURL( XJDOM.getAttributeValue(se, "NAME") );
				
				// get workflows
				List<Element> wn = XJDOM.getChildren(se, "EXPRESSION_ANALYSIS_EXPERIMENT_REPLICATE");
				for(Element we : wn)
				{
					Workflow wrkfwl = new Workflow();
					wrkfwl.id =  XJDOM.getAttributeValue(we, "WORKFLOW_ID");
					wrkfwl.sample_tracking_id = XJDOM.getAttributeValue(we, "SAMPLE_TRACKING_ID");
					wrkfwl.replicate_name = XJava.decURL( XJDOM.getAttributeValue(we, "NAME") );
					sample.workflows.add( wrkfwl );
				}
				g.samples.add(sample);
			}
			res.add(g);
		}
		return res;
	}
}
