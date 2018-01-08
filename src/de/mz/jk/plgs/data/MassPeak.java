package de.mz.jk.plgs.data;

public class MassPeak 
{
	/** unique index in the experiment */
	public int index=0;
	/** relation to the parent object e.g. low_energy_index */
	public int parent_index = 0;
	
	public int workflow_index=0;
	public double Mass=0;
	public double Intensity=0;
	public double MassSD=0;
	public double IntensitySD=0;

	/** id in the source file */
	public int id = 0;
	/** mapping to the parent id in the source file, e.g. the id of an MS1 feature */
	public int parent_id = 0;

	public double AverageCharge=0;
	public byte Z=0;
	public double RT=0;
	public double RTSD=0;
	public double FWHM=0;
	public double ClusterLiftOffRT=0;
	public double LiftOffRT=0;
	public double InfUpRT=0;
	public double InfDownRT=0;
	public double TouchDownRT=0;
	public double ClusterTouchDownRT=0;

	/** detected in PLGS v2.5 */
	public double ADCResponse= 0;
	public double Mobility = .0;
	
	/** used for merged 2D gel fraction  */
	public int fraction = 0;
}
