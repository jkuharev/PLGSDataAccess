/** PLGSDataAccess, de.mz.jk.plgs.utils, Apr 27, 2018*/
package de.mz.jk.plgs.utils;

import java.util.ArrayList;
import java.util.List;

import de.mz.jk.plgs.data.ExpressionAnalysis;
import de.mz.jk.plgs.data.Group;
import de.mz.jk.plgs.data.Project;
import de.mz.jk.plgs.data.Sample;

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

	public static Project cloneProject(Project src, boolean copyStructure)
	{
		Project clone = new Project();
		clone.index = src.index;
		clone.id = src.id;
		clone.title = src.title;
		clone.root = src.root;
		clone.state = src.state;
		clone.info = src.info;
		clone.db = src.db;
		clone.titlePrefix = src.titlePrefix;
		clone.titleSuffix = src.titleSuffix;
		if (copyStructure)
		{
			for ( ExpressionAnalysis ea : src.expressionAnalyses )
			{}
		}
		return clone;
	}
}
