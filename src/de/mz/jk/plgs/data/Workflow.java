package de.mz.jk.plgs.data;

import java.util.HashMap;
import java.util.Map;

import de.mz.jk.plgs.Identifyable;


public class Workflow extends Identifyable
{
	public int index=0;
	public int sample_index;

	public enum AcquisitionMode
	{
		DIA,
		DDA;
		
		/**
		 * interprete a mode string, e.g. "Electrospray-Shotgun", "DDA"
		 * @param modeString
		 * @return
		 */
		public static AcquisitionMode guess(String modeString)
		{
			String s = modeString.toLowerCase();
			if (s.contains( "dda" ))
				return DDA;
			else
				return DIA;
		}
	}

	/**
	 * from ExpressionAnalysis
	 */
	public String id="";
	public String sample_tracking_id = "";
	public String replicate_name="";

    //Default value for Acquisition mode is DIA. For DDA, it is changed when the project is read
    // (ProjectReader.getSamples), by using the static method WorkflowReader.getInstrumentMode
	public AcquisitionMode acquisitionMode = AcquisitionMode.DIA;

    /**
	 * Workflow...xml
	 */
	public String title="";	
	public String sample_description="";
	public String input_file="";
	public String acquired_name="";
	public double abs_quan_response_factor=0.0;	
	
	public Sample sample=null;
	
	public Map<Integer, Protein> 	proteins		= new HashMap<Integer, Protein>();
	public Map<Integer, Peptide> 	peptides		= new HashMap<Integer, Peptide>();
	public Map<Integer, QueryMass> 	queryMasses 	= new HashMap<Integer, QueryMass>();
	public Map<Integer, LowEnergy>	lowEnergies 	= new HashMap<Integer, LowEnergy>();
	public Map<Integer, MassPeak>	massPeaks 		= new HashMap<Integer, MassPeak>();
	public Map<String, String> 		metaInfo 		= new HashMap<String, String>();
	
	public int LeId2IndexShift = 0;
	public int QmId2IndexShift = 0;
	public int ProId2IndexShift = 0;
	public int PepId2IndexShift = 0;
	
	@Override public String toString()
	{
		return
			"id: " + id + "\n" +
			"replicate name: " + replicate_name + "\n" +
			"title: " + replicate_name + "\n" +
			"sample description: " + sample_description + "\n" +
			"acquired name: " + acquired_name + "\n" +
			"input file: " + input_file + "\n" +
			"xml file: " + xmlFilePath
		;
	}
	
	public String xmlFilePath = "";
}
