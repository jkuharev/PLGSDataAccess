package de.mz.jk.plgs.data;

public class Peptide 
{
	public int index=0;
	public int id=0;
	
	public String mass="";
	public String sequence="";
	public String type="";
	public String modifier="";
	
	/**
	 * from SEQUENCE_MATCH in PROTEIN
	 */
	public String start="0";
	public String end="0";
	public String coverage="0";
	public String frag_string="";
	public String rms_mass_error_prod="0";
	public String rms_rt_error_prod="0";
	
	/**
	 * from MASS_MATCH in QUERY_MASS
	 */
	public String auto_qc="0";
	public String curated="0";
	public String mass_error="0";
	public String mass_error_ppm="0";
	public String score="0";
	
	public int products=0;
	
	public int protein_id = 0;
	public int workflow_index = 0;
	public int protein_index = 0;
	public int query_mass_index = 0;

	/**
	 * cloning by copying all properties 
	 * @param src
	 * @return
	 */
	public static Peptide clone(Peptide src)
	{
		Peptide tar = new Peptide();
		tar.index = src.index;
		tar.id = src.id;
		tar.mass = src.mass;
		tar.sequence = src.sequence;
		tar.type = src.type;
		tar.modifier = src.modifier;
		tar.start = src.start;
		tar.end = src.end;
		tar.coverage = src.coverage;
		tar.frag_string = src.frag_string;
		tar.rms_mass_error_prod = src.rms_mass_error_prod;
		tar.rms_rt_error_prod = src.rms_rt_error_prod;
		tar.auto_qc = src.auto_qc;
		tar.curated = src.curated;
		tar.mass_error = src.mass_error;
		tar.mass_error_ppm = src.mass_error_ppm;
		tar.score = src.score;
		tar.products = src.products;
		tar.protein_id = src.protein_id;
		tar.workflow_index = src.workflow_index;
		tar.protein_index = src.protein_index;
		tar.query_mass_index = src.query_mass_index;
		return tar;
	}
}
