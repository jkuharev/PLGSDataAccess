package de.mz.jk.plgs.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.mz.jk.plgs.Identifyable;

public class Group extends Identifyable
{
	public int index=0;
	public int expression_analysis_index=0;
	public String id = UUID.randomUUID().toString();
	public String name="";
		
	public ExpressionAnalysis expressionAnalysis = null;
	public List<Sample> samples= new ArrayList<Sample>();
	
	/** just an empty group */
	public Group(){}
	
	/** group with given name */
	public Group(String name)
	{
		this.name=name;
	}
	
	/** group as part of given EA and with given name */
	public Group(ExpressionAnalysis ea, String name)
	{
		this.name=name;
		ea.addGroup(this);
	}
	
	/** 
	 * group as identical copy of given group<br>
	 * samples are not copied!!! 
	 * */
	public Group(Group g)
	{
		super(g);
		this.name=g.name; 
		this.expressionAnalysis=g.expressionAnalysis;
		this.id=g.id;
		this.index=g.index;
	}
	
	/** 
	 * remove sample from its old group and add to this group
	 * @param s the sample 
	 */
	public void addSample(Sample s)
	{
		if(s.group!=null) s.group.removeSample(s);
		s.group = this;
		if(! samples.contains(s)) samples.add(s);
	}
	
	/**
	 * create identical clone and optionally move all samples to clone
	 * @param moveSamples
	 * @return
	 */
	public Group getClone(boolean moveSamples)
	{
		Group res = new Group(this);
		if(moveSamples)
		{
			List<Sample> ss = new ArrayList<Sample>(this.samples);
			for(Sample s : ss) res.addSample(s);
		}		
		return res;
	}

	/**
	 * remove sample
	 * @param s
	 */
	public void removeSample( Sample s )
	{
		samples.remove(s);
		s.group = null;
	}
}