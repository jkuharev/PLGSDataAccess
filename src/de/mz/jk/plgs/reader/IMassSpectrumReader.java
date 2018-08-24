package de.mz.jk.plgs.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Element;

import de.mz.jk.jsix.libs.XJDOM;
import de.mz.jk.plgs.data.MassPeak;
import de.mz.jk.plgs.data.Workflow;

/**
 * Created by napedro on 12/11/15.
 */
public abstract class IMassSpectrumReader {

    protected File msFile = null;
    public int peakCounter=0;
    public BufferedReader reader=null;
    protected MassPeak lastMP=null;
    public Map<String, Integer> cols = null;
    public Map<String, String> metaInfo = null;

    protected abstract MassPeak getNext();
    protected abstract void readXMLData(String xml) throws Exception;


    /**
     * @return how many valid peaks already read
     */
    public int countValidPeaks(){ return peakCounter; }

    /**
     * open a mass spectrum file
     * @param msFile file to open
     * @throws Exception
     */
    public void openFile(File msFile) throws Exception
    {
        this.msFile  = msFile;
        peakCounter=0;
        reader = new BufferedReader( new FileReader(msFile) );

        // skip lines until <DATA>-Tag
        String line = "";
        String xml = "";
        while( null!=(line=reader.readLine()))
        {
            if(line.contains("<DATA "))
                break; // return;
            else
                xml += line + "\n";
        }

        readXMLData(xml);
    }

    /**
     * @param format
     * @return
     */
    protected Map<String, Integer> getColumnIndexHashMap(Element format)
    {
        HashMap<String, Integer> res = new HashMap<String, Integer>();
        List<Element> fields = XJDOM.getChildren(format, "FIELD");
        for(Element field : fields)
        {
            try
            {
                res.put(
                        XJDOM.getAttributeValue(field, "NAME"),
                        Integer.parseInt( XJDOM.getAttributeValue(field, "POSITION") ) -1
                );
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return res;
    }

    /**
     * @return opened File
     */
    public File getMassSpectrumFile(){return msFile;}

    /**
     * open a mass spectrum file by constructing its
     * path as follows:<br>
     * [project directory]/[sample tracking id]/MassSpectrum.xml
     * @param prjDir
     * @param sampleTrackingID
     * @throws Exception
     */
    public void openMassSpectrum(String prjDir, String sampleTrackingID) throws Exception
    {
        openFile( new File(
                prjDir + File.separatorChar +
                        sampleTrackingID + File.separatorChar +
                        "MassSpectrum.xml"
        ));
    }

    /**
	 * open a mass spectrum file
	 * @param run
	 * @throws Exception
	 */
	public void openMassSpectrum(Workflow run) throws Exception
	{
		File xmlFile = new File( run.massSpectrumXMLFilePath );
		if (!xmlFile.exists()) { throw new FileNotFoundException( "missing file: " + xmlFile.getAbsolutePath() ); }
		openFile( xmlFile );
	}

	/**
	 * checks if next mass peak is valid and reads it<br>
	 * usage:<pre>
	 * while(reader.next())
	 * {
	 * 		MassPeak mp = reader.getMassPeak();
	 * 		...
	 * }
	 * </pre>
	 * @return true if next mass peak is valid, false if not
	 */
    public boolean next()
    {
        lastMP = getNext();
        return (lastMP!=null);
    }

    protected String getCol(String[] row, String colName)
    {
        return (cols.containsKey(colName)) ? row[cols.get(colName)] : "0";
    }

    public Map<String, String> getMetaInfo()
    {
        return metaInfo;
    }

    /**
     * @return last read mass peak
     */
    public MassPeak getMassPeak(){ return lastMP; }

}
