package de.mz.jk.plgs.reader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.jdom.Element;

import de.mz.jk.jsix.libs.XJDOM;
import de.mz.jk.plgs.data.MassPeak;

/**
 * DIAMassSpectrumReader reads PLGS's DIA MassSpectrum.xml files line by line
 * @author J.Kuharev
 */
public class DIAMassSpectrumReader extends IMassSpectrumReader {

	/**
	 * @param xml
	 * @throws Exception
	 */
	protected void readXMLData(String xml) throws Exception
	{
		Element doc = XJDOM.getBadJDOMRootElement(xml);

		List<Element> formats = XJDOM.getChildren(doc, "FORMAT");
		for(Element format : formats)
		if( XJDOM.getAttributeValue(format, "FRAGMENTATION_LEVEL").trim().equals("0") )
		{
			cols = getColumnIndexHashMap(format);
		}

		// read meta data
		metaInfo = new HashMap<String, String>(); // clear
		Element appDoc = XJDOM.getFirstChild( doc, "GeneratedBy" );
		metaInfo.put( "PROGRAM_NAME", XJDOM.getAttributeValue( appDoc, "Program", "" ) );
		metaInfo.put( "PROGRAM_VERSION", XJDOM.getAttributeValue( appDoc, "Version", "" ) );
		metaInfo.put( "PROGRAM_BUILD_DATE", XJDOM.getAttributeValue( appDoc, "CompileDate", "" ) + " " + XJDOM.getAttributeValue( appDoc, "CompileTime", "" ) );
		metaInfo.put( "PROGRAM_COMMAND_LINE", appDoc.getChildTextTrim( "CommandLine" ) );
		List<Element> apex3d_params = XJDOM.getFirstChild( doc, "APEX3D" ).getChildren();
		for ( Element param : apex3d_params )
		{
			metaInfo.put( "APEX3D_" + param.getName(), XJDOM.getAttributeValue( param, "VALUE", "" ) );
		}
		List<Element> pep3d_params = XJDOM.getChildren( doc, "PARAM" );
		for ( Element param : pep3d_params )
		{
			metaInfo.put( XJDOM.getAttributeValue( param, "NAME", "" ), XJDOM.getAttributeValue( param, "VALUE", "" ) );
		}
	}


	protected MassPeak getNext()
	{
		String line = "";
		String[] row = null;
		MassPeak mp = null;
		try{
			if( null==(line=reader.readLine()) || line.contains("</DATA") )
			{
				reader.close();
			}
			else
			{
				row = line.trim().split("\\s+");
				mp = new MassPeak();
					mp.Mass = Double.parseDouble( getCol(row, "Mass") );
					mp.Intensity = Long.parseLong( getCol(row, "Intensity") );
					mp.ADCResponse = Double.parseDouble( getCol(row, "ADCResponse") );
					mp.MassSD = Double.parseDouble( getCol(row, "MassSD") );
					mp.Mobility = Double.parseDouble( getCol(row, "Mobility") );
					mp.IntensitySD = Double.parseDouble( getCol(row, "IntensitySD") );
				    mp.id = Integer.parseInt( getCol( row, "LE_ID" ) );
					mp.AverageCharge = Double.parseDouble( getCol(row, "AverageCharge") );
					mp.Z = Byte.parseByte( getCol(row, "Z") );
					mp.RT = Double.parseDouble( getCol(row, "RT") );
					mp.RTSD = Double.parseDouble( getCol(row, "RTSD") );
					mp.FWHM = Double.parseDouble( getCol(row, "FWHM") );
					mp.ClusterLiftOffRT = Double.parseDouble( getCol(row, "ClusterLiftOffRT") );
					mp.LiftOffRT = Double.parseDouble( getCol(row, "LiftOffRT") );
					mp.InfUpRT = Double.parseDouble( getCol(row, "InfUpRT") );
					mp.InfDownRT = Double.parseDouble( getCol(row, "InfDownRT") );
					mp.TouchDownRT = Double.parseDouble( getCol(row, "TouchDownRT") );
					mp.ClusterTouchDownRT = Double.parseDouble( getCol(row, "ClusterTouchDownRT") );
					mp.fraction = Integer.parseInt( getCol(row, "Fraction") );
					
				peakCounter++;
				return mp;
			}
		}
		catch(Exception e)
		{
			System.err.println("processing MassSpectrum.xml file failed!");
			System.err.println("file: '" + msFile.getAbsolutePath() + "'");
			
			System.err.println("current row content: '"+line+"'");
			
			System.err.print("row splitting: ");
			for(int i=0; i<row.length; i++) System.err.print(i+": " + row[i] + "; ");
			System.err.println();
			
			System.err.print("precalculated row indexes: ");
			Set<String> keys = cols.keySet();
			for(String key : keys) System.err.print( cols.get(key) + "='"+key+"'; ");
			System.err.println();
			
			e.printStackTrace();
		}
		
		return null;
	}

	public static void main(String[] args) throws Exception 
	{
		// File file = new File("/Volumes/RAID0/PLGS2.5/root/Proj__13014668130450_9953185682477013/_13014989170370_20481690458337853/MassSpectrum.xml");
		File file = new File("/Volumes/RAID0/PLGS2.5/root/Proj__13014668130450_9953185682477013/_13014669869790_4129502310722458/MassSpectrum.xml");

		IMassSpectrumReader msr = new DIAMassSpectrumReader();
		msr.openFile(file);
		MassPeak mp=null;
		List<MassPeak> mps = new ArrayList<MassPeak>();
		
		while( msr.next() )
		{
			mp = msr.getMassPeak();
			mps.add(mp);
		}
		
		System.out.println(
			"index: " + msr.countValidPeaks() + "\nall size: " + mps.size()
		);
	}
}
