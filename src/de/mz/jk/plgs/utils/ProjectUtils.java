/** PLGSDataAccess, de.mz.jk.plgs.utils, Apr 27, 2018*/
package de.mz.jk.plgs.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.mz.jk.plgs.data.*;

/**
 * <h3>{@link ProjectUtils}</h3>
 * @author jkuharev
 * @version Apr 27, 2018 3:27:50 PM
 */
public class ProjectUtils
{
	public static void packageLooseSamples(Project p)
	{
		List<Sample> looseSamples = new ArrayList( p.samples.size() );
		for ( Sample s : p.samples )
		{ // collect samples that are not mapped to any sample group
			if (s.group == null) looseSamples.add( s );
		}

		if (looseSamples.size() > 0)
		{ // we have unmapped samples in this project
			ExpressionAnalysis ea = new ExpressionAnalysis( p, "default analysis" );
			Group g = new Group( ea, "default sample group" );
			g.samples.addAll( looseSamples );
		}
	}

	/**
	 * construct a search result file path as follows
	 * <B>[rootDir]/[Project_ID]/[SAMPLE_TRACKING_ID]/[SAMPLE_TRACKING_ID]_WorkflowResults/[WORKFLOW_ID].xml</B>
	 * @param rootDir
	 * @param ProjectID
	 * @param SampleTrackingID
	 * @param WorkflowID
	 * @return resulting file path
	 */
	public static String suggestPLGSPathForWorkflowXML(String rootDir, String projectID, String sampleTrackingID, String workflowID)
	{
		String path = rootDir + File.separator +
				projectID + File.separator +
				sampleTrackingID + File.separator +
				sampleTrackingID + "_WorkflowResults" + File.separator +
				workflowID + ".xml";
		return path;
	}

	/**
	 * construct a search result file path as follows
	 * <B>[rootDir]/[Project_ID]/[SAMPLE_TRACKING_ID]/[SAMPLE_TRACKING_ID]_WorkflowResults/[WORKFLOW_ID].xml</B>
	 * @param project
	 * @param run
	 * @return resulting file path
	 */
	public static String suggestPLGSPathForWorkflowXML(Project project, Workflow run)
	{
		String path = suggestPLGSPathForWorkflowXML( new File( project.root ).getAbsolutePath(), project.id, run.sample_tracking_id, run.id );
		return path;
	}

	public static String suggestPLGSPathForMassSpectrumXML(String rootDir, String projectID, String sampleTrackingID)
	{
		String path = rootDir + File.separator +
				projectID + File.separatorChar +
				sampleTrackingID + File.separatorChar + "MassSpectrum.xml";
		return path;
	}

	public static String suggestPLGSPathForMassSpectrumXML(Project project, Workflow run)
	{
		String path = suggestPLGSPathForMassSpectrumXML( new File( project.root ).getAbsolutePath(), project.id, run.sample_tracking_id );
		return path;
	}
}
