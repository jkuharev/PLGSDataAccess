package de.mz.jk.plgs.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.mz.jk.plgs.Identifyable;

public class ExpressionAnalysis extends Identifyable
{
	public int index=0;
	public int project_index=0;
	public String id=UUID.randomUUID().toString();
	public String name="";
	public String description="";
	
	public Project project=null;
	public List<Group> groups= new ArrayList<Group>();
	public List<ClusterAverage> clusterAverage= new ArrayList<ClusterAverage>();
	
	/**
	 * EXPRESSION_ANALYSIS_RESULT ID
	 * 
	 *	Results/[ID]/[ID].xml
	 *	Results/[ID]/BlusterOutput.cvf
	 */
	public String result_id="";
	
	/** empty expression analysis */
	public ExpressionAnalysis(){}
	
	/** expression analysis having given name */
	public ExpressionAnalysis(String name)
	{
		this.name = name;
	}

	/** expression analysis as part of given project and having given name */
	public ExpressionAnalysis(Project p, String name)
	{
		this.name = name;
		this.project_index = p.index;
		p.addExpressionAnalysis( this );
	}
	
	/** 
	 * expression analysis as identical copy of another one<br>
	 * groups and cluster averages are not copied!!!
	 */
	public ExpressionAnalysis(ExpressionAnalysis ea)
	{
		super(ea);
		this.name = ea.name;
		this.id = ea.id;
		this.description = ea.description;
		this.project_index = ea.project_index;
		this.project = ea.project;
	}
	
	/**
	 * remove given group from its old expression analysis 
	 * and add it to this expression analysis
	 * @param g the group
	 */
	public void addGroup(Group g)
	{
		if(g.expressionAnalysis!=null) g.expressionAnalysis.groups.remove(g);
		g.expressionAnalysis = this;
		if(! groups.contains(g)) groups.add(g);
	}

	/**
	 * @param node
	 */
	public void removeGroup(Group g)
	{
		g.expressionAnalysis = null;
		groups.remove(g);
	}
}
