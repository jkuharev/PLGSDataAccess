package de.mz.jk.plgs.data;

public class ClusteredEMRT 
{
	/** emrt index from db */
	public int index=0;
	/** workflow index from db */
	public int workflow_index=0;
	/** cluster index from db */
	public int cluster_index=0;
	/** expression analysis index from db */
	public int expression_analysis_index=0;
	
	/** PLGS internal cluster number */
	public int cluster_id=0;
	
	public double mass=0.0;
	public double sd_mhp=0.0;
	public int spec_index=0;
	public double charge=0.0;
	public double rt=0.0;
	public double sd_rt=0.0;
	public double ref_rt=0.0;
	public int precursor_type=0;
	
	public int low_energy_id=0;
	public int low_energy_index=0;

	/** original intensity from PLGS */
	public double inten=0.0;
	/** cluster average of intensity */
	public double ave_inten = 0.0;
	/** inten_log2ratio = LOG2( inten / ave_inten ) */
	public double inten_log2ratio = 0.0;
	
	/** intensity after correction step */
	public double cor_inten=0;
	/** cluster average of corrected intensity */
	public int ave_cor_inten = 0;
	/** inten_log2ratio = LOG2( inten / ave_inten ) */
	public double cor_inten_log2ratio = 0.0;
	
	/** link to workflow */
	public Workflow workflow=null;
	
	/** link to cluster */
	public ClusterAverage clusterAverage=null;
}
