package de.mz.jk.plgs.data;

public class QueryMass 
{
	public int index = 0;
	public int id = 0;
	public double intensity = 0;
	public int low_energy_id = 0;
	public int workflow_index = 0;
	public int low_energy_index = 0;

	/**
	 * cloning by copying all properties
	 * @param src
	 * @return
	 */
	public static QueryMass clone(QueryMass src)
	{
		QueryMass tar = new QueryMass();
		tar.index = src.index;
		tar.id = src.id;
		tar.intensity = src.intensity;
		tar.low_energy_id = src.low_energy_id;
		tar.workflow_index = src.workflow_index;
		tar.low_energy_index = src.low_energy_index;
		return tar;
	}
}
