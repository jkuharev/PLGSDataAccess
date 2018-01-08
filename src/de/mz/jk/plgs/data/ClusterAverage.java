package de.mz.jk.plgs.data;

import java.util.ArrayList;
import java.util.List;

public class ClusterAverage 
{
	public int index=0;
	public int expression_analysis_index=0;
	public int cluster_id=0;
	public double ave_mhp=.0;
	public double std_mhp=.0;
	public double ave_rt=.0;
	public double std_rt=.0;
	public int ave_inten=0;
	public double std_inten=.0;
	public double ave_ref_rt=.0;
	public double std_ref_rt=.0;
	public double ave_charge=.0;
	public double std_charge=.0;
	public int total_rep_rate=0;
	
	public ExpressionAnalysis expressionAnalysis = null;
	public List<ClusteredEMRT> clusteredEMRT= new ArrayList<ClusteredEMRT>();
}
