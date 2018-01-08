package de.mz.jk.plgs.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.mz.jk.plgs.Identifyable;

public class Sample extends Identifyable
{
	public int index=0;
	public int group_index=0;
	public String id = UUID.randomUUID().toString();
	public String name="";
	
	public Group group=null;
	public List<Workflow> workflows= new ArrayList<Workflow>();
	
	/** empty sample */
	public Sample(){}
	
	/** sample having given name */
	public Sample(String name){this.name=name;}
	
	/** sample as part of given group having given name */
	public Sample(Group g, String name)
	{
		this.name=name;
		g.addSample(this);
	}
	
	/** 
	 * sample as identical copy of given sample<br>
	 * workflows are not copied!!! 
	 * */
	public Sample( Sample s )
	{
		super(s);
		this.name=s.name; 
		this.group=s.group;
		this.id=s.id;
		this.index=s.index;
		this.group_index=s.group_index;
	}
	
	/**
	 * remove run from its old sample and add it to this sample
	 * @param w the run
	 */
	public void addWorkflow(Workflow w)
	{
		if( w.sample!=null ) w.sample.workflows.remove(w);
		w.sample = this;
		if( this.workflows.indexOf(w)<0 ) this.workflows.add(w); 
	}
	
	/**
	 * remove run from sample
	 * @param w
	 */
	public void removeWorkflow(Workflow w)
	{
		workflows.remove( w );
		w.sample = null;
	}
	
	/**
	 * create identical clone and optionally move all workflows to clone
	 * @param moveWorkflows
	 * @return
	 */
	public Sample getClone(boolean moveWorkflows)
	{
		Sample res = new Sample(this);
		if(moveWorkflows)
		{
			List<Workflow> ws = new ArrayList<Workflow>(this.workflows);
			for(Workflow w : ws) res.addWorkflow(w);
		}		
		return res;
	}
}
