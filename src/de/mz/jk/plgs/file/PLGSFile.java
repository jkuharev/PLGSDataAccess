package de.mz.jk.plgs.file;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Element;

import de.mz.jk.jsix.libs.XFiles;
import de.mz.jk.jsix.libs.XJDOM;

/**
 * <h3>{@link PLGSFileType}</h3>
 * @author jkuharev
 * @version Feb 9, 2018 1:28:01 PM
 */
public class PLGSFile
{
	private File file = null;
	private PLGSFileType type = PLGSFileType.UNKNOWN;
	private Map<String, String> params = new LinkedHashMap<>();

	/**
	 * @param file
	 */
	PLGSFile(File file)
	{
		this.file = file;
		read();
	}

	/**
	 * detect file type and read parameters
	 * @return
	 */
	public PLGSFile read()
	{
		try
		{
			this.type = guessType( file );
			this.params = getParams( file, type );
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return this;
	}

	public File getFile()
	{
		return file;
	}

	public PLGSFileType getType()
	{
		return type;
	}

	public Map<String, String> getParams()
	{
		return params;
	}

	/**
	 * @param file
	 * @param type if null type well be guessed automatically
	 * @return kv-map of parameters
	 * @throws Exception
	 */
	public static Map<String, String> getParams(File file, PLGSFileType type) throws Exception
	{
		Map<String, String> params = new LinkedHashMap<>();
		if (type == null) type = guessType( file );
		switch (type)
		{
			case APEX3D_XML:
			case PEPTIDE3D_XML:
			case IADBS_XML:
				String xml = XFiles.readUntilMatch( file, ".*\\<\\/PARAMS\\>.*", true );
				Element doc = XJDOM.getBadJDOMRootElement( xml );
				List<Element> paramTags = XJDOM.getChildren( doc, "PARAM" );
				for ( Element param : paramTags )
				{
					params.put( XJDOM.getAttributeValue( param, "NAME", "" ), XJDOM.getAttributeValue( param, "VALUE", "" ) );
				}
				break;
			case PROJECT_XML:
			case UNKNOWN:
		}
		return params;
	}

	/**
	 * guess PLGS result file type by reading a couple of lines and interpreting xml tags
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static PLGSFileType guessType(File file) throws Exception
	{
		String headLines = XFiles.readLines( file, 3 );
		
		if (!headLines.contains( "<?xml" ))
			return PLGSFileType.UNKNOWN;
		
		if (headLines.contains( "<APEX3D" ))
			return PLGSFileType.APEX3D_XML;
		
		if (headLines.contains( "<MASS_SPECTRUM" ))
			return PLGSFileType.PEPTIDE3D_XML;
		
		if (headLines.contains( "<RESULT" ))
			return PLGSFileType.IADBS_XML;
		
		if (headLines.contains( "<WORKFLOW" ))
			return PLGSFileType.IADBS_XML;
		
		if (headLines.contains( "<PROJECT" ))
			return PLGSFileType.PROJECT_XML;
		
		return PLGSFileType.UNKNOWN;
	}
}
