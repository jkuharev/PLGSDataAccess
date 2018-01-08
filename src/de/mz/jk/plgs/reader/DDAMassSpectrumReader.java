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
 * DDAMassSpectrumReader reads PLGS's DDA MassSpectrum.xml files line by line
 * @author J.Kuharev
 */
public class DDAMassSpectrumReader extends IMassSpectrumReader
{
	private int peakId = 0;

    /**
	* @param xml
	* @throws Exception
	*/
	protected void readXMLData(String xml) throws Exception
	{
		peakId = 0;
		Element doc = XJDOM.getBadJDOMRootElement(xml);

		List<Element> formats = XJDOM.getChildren(doc, "FORMAT");
		for(Element format : formats)
		if( XJDOM.getAttributeValue(format, "FRAGMENTATION_LEVEL").trim().equals("0") )
		{
			cols = getColumnIndexHashMap(format);
		}

		// read meta data
		metaInfo = new HashMap<String, String>(); // clear

        List<Element> levelParams = XJDOM.getChildren(doc, "LEVEL");
        for( Element level : levelParams ){
            String frgLevel = XJDOM.getAttributeValue(level, "FRAGMENTATION_LEVEL");

            Element mass_measure = XJDOM.getFirstChild(level, "MASS_MEASURE");

            //TODO: Fix this at the XWorkflow generation, or ignore it completely.
            /*Element bgs = XJDOM.getFirstChild(mass_measure, "BACKGROUND_SUBTRACT");
            metaInfo.put ("FRAGLEVEL" + frgLevel + "_BACKGROUND_SUBTRACT_BELOW_CURVE", XJDOM.getAttributeValue(bgs, "BELOW_CURVE"));
            metaInfo.put ("FRAGLEVEL" + frgLevel + "_BACKGROUND_SUBTRACT_POLYNOMIAL_ORDER", XJDOM.getAttributeValue(bgs, "POLYNOMIAL_ORDER"));
            metaInfo.put ("FRAGLEVEL" + frgLevel + "_BACKGROUND_SUBTRACT_POLYNOMIAL_TYPE", XJDOM.getAttributeValue(bgs, "TYPE"));
            */

            Element peakDetection = XJDOM.getFirstChild(mass_measure, "PEAK_DETECTION");
            Element smooth = XJDOM.getFirstChild(peakDetection, "SMOOTH");
            metaInfo.put ("FRAGLEVEL" + frgLevel + "_PEAKDETECTION_SMOOTH_METHOD", XJDOM.getAttributeValue(smooth, "METHOD"));
            metaInfo.put ("FRAGLEVEL" + frgLevel + "_PEAKDETECTION_SMOOTH_NUMBER", XJDOM.getAttributeValue(smooth, "NUMBER"));
            metaInfo.put ("FRAGLEVEL" + frgLevel + "_PEAKDETECTION_SMOOTH_WINDOW", XJDOM.getAttributeValue(smooth, "WINDOW"));
            Element centroid = XJDOM.getFirstChild(peakDetection, "CENTROID");
            metaInfo.put ("FRAGLEVEL" + frgLevel + "_PEAKDETECTION_CENTROID_TOP", XJDOM.getAttributeValue(centroid, "CENTROID_TOP"));
            metaInfo.put ("FRAGLEVEL" + frgLevel + "_PEAKDETECTION_MIN_PEAK_WIDTH", XJDOM.getAttributeValue(centroid, "MIN_PEAK_WIDTH"));

            Element calibrate = XJDOM.getFirstChild(level, "CALIBRATE");
            metaInfo.put ("FRAGLEVEL" + frgLevel + "_CALIBRATE_LOCKSPRAY_AVG_SCANS", XJDOM.getAttributeValue(calibrate, "LOCKSPRAY_AVG_SCANS"));
            metaInfo.put ("FRAGLEVEL" + frgLevel + "_CALIBRATE_LOCK_MASS_EXT", XJDOM.getAttributeValue(calibrate, "LOCK_MASS_EXT"));
            metaInfo.put ("FRAGLEVEL" + frgLevel + "_CALIBRATE_TOF_RESOLUTION", XJDOM.getAttributeValue(calibrate, "TOF_RESOLUTION"));
            metaInfo.put ("FRAGLEVEL" + frgLevel + "_CALIBRATE_TOLERANCE", XJDOM.getAttributeValue(calibrate, "TOLERANCE"));

            Element deisotope = XJDOM.getFirstChild(level, "DEISOTOPE");
            metaInfo.put ("FRAGLEVEL" + frgLevel + "_DEISOTOPE_METHOD", XJDOM.getAttributeValue(deisotope, "METHOD"));
        }

	}

    protected MassPeak getNext()
	{
		String line = "";
		String[] row = null;
		MassPeak mp = null;
		try{
			while (null != ( line = reader.readLine() ))
			{
				line = line.trim();
				// we are at the end of data block
				if (line.contains( "</DATA" )) break;
				// ignore empty lines
				if (line.length() < 1) continue;
				peakId++;
				row = line.split( "\\s+" );
				mp = new MassPeak();
				mp.Mass = Double.parseDouble( getCol( row, "Mass" ) );
				mp.Intensity = Double.parseDouble( getCol( row, "Intensity" ) );
				mp.Z = Byte.parseByte( getCol( row, "Charge" ) );
				mp.RT = Double.parseDouble( getCol( row, "RT" ) );
				mp.id = peakId; // Integer.parseInt(getCol(row, "StartScan"));
				mp.fraction = Integer.parseInt( getCol( row, "Fraction" ) );
				peakCounter++;
				return mp;
			}
			reader.close();
		}
		catch(Exception e)
		{
			System.err.println("processing MassSpectrum.xml file failed!");
			System.err.println("file: '" + msFile.getAbsolutePath() + "'");
			
			System.err.println("current row content: '"+line+"'");
			
			System.err.print("row splitting: ");
			for(int i=0; i<row.length; i++) System.err.print(i+": " + row[i] + "; ");
			System.err.println();
			
			System.err.print("precalculated row indices: ");
			Set<String> keys = cols.keySet();
			for(String key : keys) System.err.print( cols.get(key) + "='"+key+"'; ");
			System.err.println();
			
			e.printStackTrace();
		}
		
		return null;
	}

	public static void main(String[] args) throws Exception 
	{
		String fname1 = "/Volumes/DAT/Users/jkuharev/Desktop/Pedro_test_DDAwithMobilityEnabled/root/Proj__14522484716920_19899030331524759/_14522487244930_875962760885909/MassSpectrum.xml";
        String fname2 = "/Users/napedro/tmp_data/Pedro_test_DDAwithMobilityEnabled/root/Proj__14522484716920_19899030331524759/_14522487244930_875962760885909/MassSpectrum.xml";
        String fname3 = "/Users/napedro/tmp_data/2015-036_PhosphoDIA_OneWFOnly/root/Proj__14351466666970_8293863831364245/PEAKS_DDA1/MassSpectrum.xml";
		String fname4 = "/Volumes/users/Pedro/PROJECTS/DIAoverDDA/2016-010 PLGS_DIA/root/Proj__14544155466130_2836545356307507/PEAKS_DDA_A1/MassSpectrum.xml";
		File file = new File( fname4 );


        IMassSpectrumReader msr = new DDAMassSpectrumReader();
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
