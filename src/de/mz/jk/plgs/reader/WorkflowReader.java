package de.mz.jk.plgs.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.jdom.DataConversionException;
import org.jdom.Element;

import de.mz.jk.jsix.libs.XFiles;
import de.mz.jk.jsix.libs.XJDOM;
import de.mz.jk.jsix.libs.XJava;
import de.mz.jk.plgs.data.*;
import de.mz.jk.plgs.data.Workflow.AcquisitionMode;

/**
 * WorkflowReader changed for using JDOM instead from DOM
 * ATTENTION: do not forget to recreate WorkflowReader before reading the next file!!! 
 * 
 * @author J.Kuharev
 * @since 2009-09-24
 */
public class WorkflowReader 
{
	private Element doc = null;
	private Workflow workflow = new Workflow();

// private int maxQMid=0;
	private int maxPEid = 0;

	public WorkflowReader(Workflow workflow)
	{
		this.workflow = workflow;
	}

	public WorkflowReader()
	{}

	/**
	 * assigns a workflow file to work with 
	 * by building its path from parameter attributes<br>
	 * using following pattern:<br>
	 * <B>[rootDir]/[Project_ID]/[SAMPLE_TRACKING_ID]/[SAMPLE_TRACKING_ID]_WorkflowResults/[WORKFLOW_ID].xml</B>
	 * @param project
	 * @param workflow
	 * @throws Exception 
	 */
	public static void readWorkflow(Project project, Workflow workflow) throws Exception
	{
		readWorkflow( new File(project.root), project.id, workflow );
	}
	
	/**
	 * assigns a workflow file to work with 
	 * by building its path from parameter attributes<br>
	 * using following pattern:<br>
	 * <B>[rootDir]/[Project_ID]/[SAMPLE_TRACKING_ID]/[SAMPLE_TRACKING_ID]_WorkflowResults/[WORKFLOW_ID].xml</B>
	 * @param rootDir
	 * @param projectID
	 * @param workflow
	 * @throws Exception 
	 */
	public static void readWorkflow(File rootDir, String projectID, Workflow workflow) throws Exception
	{
		WorkflowReader reader = new WorkflowReader( workflow );
		reader.fillWorkflow( getFile( rootDir, projectID, workflow.sample_tracking_id, workflow.id ), reader.workflow.acquisitionMode );
	}
	
	/**
	 * assigns a workflow file to work with 
	 * by building its path from parameter attributes<br>
	 * using following pattern:<br>
	 * <B>[workflowFolder]/[SAMPLE_TRACKING_ID]_WorkflowResults/[WORKFLOW_ID].xml</B>
	 * @param workflowFolder
	 * @param workflow
	 * @throws Exception
	 */
	public static void readWorkflow(File workflowFolder, Workflow workflow) throws Exception
	{
		WorkflowReader reader = new WorkflowReader( workflow );
		File resDir = new File( workflowFolder + File.separator + workflow.sample_tracking_id + "_WorkflowResults" );
		File runFile = new File( resDir + File.separator + workflow.id + ".xml" );
		reader.fillWorkflow( runFile, reader.workflow.acquisitionMode );
	}
	
	private void fillWorkflow( File xmlFile, Workflow.AcquisitionMode acqMode) throws Exception
	{
		try
		{
			doc = XJDOM.getJDOMRootElement( xmlFile );
		}
		catch (Exception e)
		{
			System.err.println( 
				e.getMessage() + "\n" +
				"SAXBuilder failed to parse XML from file:\n\t" + xmlFile  + "\n" +
				"reparsing using Tagsoup ..."
			);
			doc = XJDOM.getBadJDOMRootElement( xmlFile );
		}

        if(acqMode.equals(Workflow.AcquisitionMode.DIA)){
            readWF();
            readPE(Workflow.AcquisitionMode.DIA);
            readQM(Workflow.AcquisitionMode.DIA);
            readPR();
        }
        else if(acqMode.equals(Workflow.AcquisitionMode.DDA)){
            readWF_DDA();
            readPE(Workflow.AcquisitionMode.DDA);
            readQM(Workflow.AcquisitionMode.DDA);
            readPR_DDA();

        }
	}

    /**
     * read attributes:
     title
     sample_description
     input_file
     acquired_name
     abs_quan_response_factor
     * from xml-DOM into Workflow-object
     * @throws Exception
     */
    private void readWF_DDA() throws Exception {

        workflow.title = XJDOM.getAttributeValue(doc,"TITLE");

        Element massSpectrumDoc = XJDOM.getFirstChild( doc, "MASS_SPECTRUM" );
        workflow.acquired_name = XJava.decURL(XJDOM.getAttributeValue(massSpectrumDoc, "MS_DATA_NAME", ""));
        workflow.sample_description = XJDOM.getAttributeValue(doc,"TITLE"); //Same as workflow.title since DDA workflows have no params like description

		readMetaInfo_DDA();
	}

	private void readMetaInfo_DDA()
	{
		Element searchDoc = XJDOM.getFirstChild( doc, "DATABANK_SEARCH_QUERY_PARAMETERS" );
		// we may miss search parameters in result files produced by manually executed iaDBs.exe
		if (searchDoc == null) return;
        workflow.metaInfo.put( "SEARCH_ENGINE_TYPE",
                XJDOM.getFirstChildAttributeValue( searchDoc, "SEARCH_ENGINE_TYPE", "VALUE", "" ) );
        workflow.metaInfo.put( "DISSOCIATION_MODE",
                XJDOM.getFirstChildAttributeValue( searchDoc, "DISSOCIATION_MODE", "VALUE", "" ) );
        workflow.metaInfo.put( "SEARCH_DATABASE",
                XJDOM.getFirstChildAttributeValue( searchDoc, "SEARCH_DATABASE", "NAME", "" ) );
        workflow.metaInfo.put( "SEARCH_TYPE",
                XJDOM.getFirstChildAttributeValue( searchDoc, "SEARCH_TYPE", "NAME", "" ) );
        workflow.metaInfo.put( "PEP_MASS_TOL_UNIT",
                XJDOM.getFirstChildAttributeValue( searchDoc, "PEP_MASS_TOL", "UNIT", "" ) );
        Element sdSearch = XJDOM.getFirstChild(searchDoc, "PEP_MASS_TOL");
        workflow.metaInfo.put( "PEP_MASS_TOL",
                sdSearch.getTextTrim());
        workflow.metaInfo.put( "PEP_FRAG_TOL_UNIT",
                XJDOM.getFirstChildAttributeValue(searchDoc, "PEP_FRAG_TOL", "UNIT", "") );
        sdSearch = XJDOM.getFirstChild(searchDoc, "PEP_FRAG_TOL");
        workflow.metaInfo.put( "PEP_FRAG_TOL",
                sdSearch.getText());
        workflow.metaInfo.put( "MAX_HITS_TO_RETURN",
                searchDoc.getChildTextTrim("MAX_HITS_TO_RETURN"));
        workflow.metaInfo.put( "MIN_MASS_STANDARD_DEVIATION_UNIT",
                XJDOM.getFirstChildAttributeValue(searchDoc, "MIN_MASS_STANDARD_DEVIATION", "UNIT", "") );
        sdSearch = XJDOM.getFirstChild(searchDoc, "MIN_MASS_STANDARD_DEVIATION");
        workflow.metaInfo.put("MIN_MASS_STANDARD_DEVIATION",
                sdSearch.getText());
        sdSearch = XJDOM.getFirstChild(searchDoc, "MIN_PEPS_TO_MATCH_PROTEIN");
        workflow.metaInfo.put( "MIN_PEPS_TO_MATCH_PROTEIN",
                sdSearch.getText());
        workflow.metaInfo.put( "PROTEIN_MW_MIN",
                XJDOM.getFirstChildAttributeValue(searchDoc, "PROTEIN_MW", "FROM", "") );
        workflow.metaInfo.put( "PROTEIN_MW_MAX",
                XJDOM.getFirstChildAttributeValue(searchDoc, "PROTEIN_MW", "TO", "") );
        workflow.metaInfo.put( "PROTEIN_PI_MIN",
                XJDOM.getFirstChildAttributeValue(searchDoc, "PROTEIN_PI", "FROM", "") );
        sdSearch = XJDOM.getFirstChild(searchDoc, "MIN_PEPS_TO_MATCH_PROTEIN");
        workflow.metaInfo.put( "MIN_PEPS_TO_MATCH_PROTEIN",
                sdSearch.getText());
        workflow.metaInfo.put( "PROTEIN_PI_MAX",
                XJDOM.getFirstChildAttributeValue(searchDoc, "PROTEIN_PI", "TO", "") );
        workflow.metaInfo.put( "MISSED_CLEAVAGES",
                XJDOM.getFirstChildAttributeValue( searchDoc, "ANALYSIS_DIGESTOR", "MISSED_CLEAVAGES", "" ) );
        workflow.metaInfo.put( "AMINO_ACID_SEQUENCE_DIGESTOR",
                XJDOM.getFirstChildAttributeValue( searchDoc, "AMINO_ACID_SEQUENCE_DIGESTOR", "NAME", "" ) );
        sdSearch = XJDOM.getFirstChild(searchDoc, "ESTIMATED_PROT_SAMPLE");
        workflow.metaInfo.put("ESTIMATED_PROT_SAMPLE",
                sdSearch.getText());
        sdSearch = XJDOM.getFirstChild(searchDoc, "ESTIMATED_PROT_PROTEOME");
        workflow.metaInfo.put("ESTIMATED_PROT_PROTEOME",
                sdSearch.getText());
        sdSearch = XJDOM.getFirstChild(searchDoc, "MIN_CONFIDENCE");
        workflow.metaInfo.put("MIN_CONFIDENCE",
                sdSearch.getText());

        // parse modifiers
        List<Element> mods = XJDOM.getChildren( searchDoc, "ANALYSIS_MODIFIER" );
        List<String> modStrings = new ArrayList<>(mods.size());
        for ( Element mod : mods )
        {
            modStrings.add(
                    "name:" + XJDOM.getFirstChildAttributeValue( mod, "MODIFIER", "NAME", "" )
                            + ",status:" + XJDOM.getAttributeValue( mod, "STATUS", "" )
                            + ",applies_to:" + XJDOM.getFirstChildAttributeValue( mod, "MODIFIES", "APPLIES_TO", "" )
                            + ",delta_mass:" + XJDOM.getFirstChildAttributeValue( mod, "MODIFIES", "DELTA_MASS", "" )
            );
        }
        workflow.metaInfo.put( "MODIFICATIONS", XJava.joinList( modStrings, ";" ) );
        workflow.metaInfo.put( "ENTRIES_SEARCHED", XJDOM.getFirstChildAttributeValue( doc, "RESULT", "ENTRIES_SEARCHED", "" ) );
	}

	private void readPR_DDA() throws Exception
	{
        Protein p = null;

        Element resDoc = XJDOM.getFirstChild(doc, "RESULT");

        List<Element> pn = XJDOM.getChildren(resDoc, "PROTEIN", false);
        Iterator<Element> pni = pn.iterator();


        // walk through proteins
        for(int pCnt=1; pni.hasNext(); pCnt++)
        {
            Element e = pni.next();
            // create new Protein
            p = new Protein();

            p.id = pCnt;
            p.auto_qc = XJDOM.getAttributeValue(e,"AUTO_QC");
            p.curated = XJDOM.getAttributeValue(e,"CURATED");
            p.coverage = XJDOM.getAttributeValue(e,"COVERAGE");
            p.score = XJDOM.getAttributeValue(e,"SCORE");
            p.rms_mass_error_prec = XJDOM.getAttributeValue(e,"RMS_MASS_ERROR_PREC");
            p.rms_mass_error_frag = XJDOM.getAttributeValue(e,"RMS_MASS_ERROR_FRAG");
            p.rms_rt_error_frag = XJDOM.getAttributeValue(e,"RMS_RT_ERROR_FRAG");

            Iterator<Element> cni = e.getChildren().iterator();
            for(int ci=0; cni.hasNext(); ci++)
            {
                try
                {
                    Element ce = cni.next();

                    if( ce.getName().equalsIgnoreCase("ENTRY") )
                        p.entry = ce.getTextTrim();
                    else if( ce.getName().equalsIgnoreCase("ACCESSION") )
                        p.accession = ce.getTextTrim();
                    else if( ce.getName().equalsIgnoreCase("DESCRIPTION") )
                        p.description = ce.getTextTrim();
                    else if( ce.getName().equalsIgnoreCase("MW") )
                        p.mw = ce.getTextTrim();
                    else if( ce.getName().equalsIgnoreCase("AQ_FMOLES") )
                        p.aq_fmoles = ce.getTextTrim();
                    else if( ce.getName().equalsIgnoreCase("AQ_NGRAMS") )
                        p.aq_ngrams = ce.getTextTrim();
                    else if( ce.getName().equalsIgnoreCase("PI") )
                        p.pi = ce.getTextTrim();
                    else if( ce.getName().equalsIgnoreCase("SEQUENCE") )
                        p.sequence = ce.getTextTrim();
                    else if( ce.getName().equalsIgnoreCase("SEQUENCE_MATCH") )
                    {
                        // peakCount peptides
                        p.peptides++;

                        int smid = XJDOM.getAttribute(ce,"ID").getIntValue();

						// Peptide pep = workflow.peptides.get( smid );
						List<Peptide> allPeps = pepByDDAId.get( smid );

						for ( Peptide pep : allPeps )
                        {
							// if this peptide was already assigned to another
							// protein
							if (pep.protein_id > 0)
							{
								// clone query mass and peptide
								QueryMass qm = QueryMass.clone( workflow.queryMasses.get( pep.id ) );
								pep = Peptide.clone( pep );
								pep.id = qm.id = ++maxPEid;
								// store clones
								workflow.queryMasses.put( qm.id, qm );
								workflow.peptides.put( pep.id, pep );
							}
							pep.protein_id = p.id;

							pep.start = XJDOM.getAttributeValue( ce, "START" );
							pep.end = XJDOM.getAttributeValue( ce, "END" );
							pep.coverage = XJDOM.getAttributeValue( ce, "COVERAGE" );
							pep.frag_string = XJDOM.getAttributeValue( ce, "FRAG_STRING" );
							pep.rms_mass_error_prod = XJDOM.getAttributeValue( ce, "RMS_MASS_ERROR_PROD" );
							pep.rms_rt_error_prod = XJDOM.getAttributeValue( ce, "RMS_RT_ERROR_PROD" );
							// peakCount fragment ions per peptide
							try
							{
								pep.products = XJDOM.getAttributeValue( ce.getChild( "FRAGMENT_ION" ), "IDS" ).split( "," ).length;
								p.products += pep.products;
							}
							catch (Exception ex)
							{}
						}
                    }
                }
                catch(Exception ex){}
            }

            // fix empty entries
            if( p.entry.length()<1 )
            {
                if( p.accession.length()<1 ) p.accession = "UNKNOWN_" + p.id;
                p.entry = p.accession;
            }
            else
            if( p.accession.length()<1 )
            {
                p.accession = p.entry;
            }

            workflow.proteins.put(p.id, p);
        }

// int normalPeps = 0;
// int badPeps = 0;
// for ( Peptide pep : workflow.peptides.values() )
// {
// if (pep.protein_id > 0)
// normalPeps++;
// else
// badPeps++;
// }
// System.out.println( "" );
    }

    /**
	 * 
	 * @param p the project
	 * @param w the workflow object
	 * @param fullRead if true detailed information about query masses/peptides/proteins is included 
	 * @return
	 * @throws Exception
	 */
	public static Workflow getWorkflow(Project p, Workflow w, boolean fullRead, Workflow.AcquisitionMode wfMode) throws Exception
	{
		File file = getFile(new File(p.root), p.id, w.sample_tracking_id, w.id);

        return getWorkflow(file, fullRead, wfMode);
	}

	/**
	 * read a workflow xml file automatically guessing the acquisition mode
	 * @param workflowXMLFile
	 * @param fullRead if true detailed information about query masses/peptides/proteins is included 
	 * @return
	 * @throws Exception
	 */
	public static Workflow getWorkflow(File workflowXMLFile, boolean fullRead) throws Exception
	{
		AcquisitionMode mode = Workflow.AcquisitionMode.guess( WorkflowReader.getInstrumentMode( workflowXMLFile ) );
		return getWorkflow( workflowXMLFile, fullRead, mode );
	}

    /**
	 * 
	 * @param workflowXMLFile
	 * @param fullRead if true detailed information about query masses/peptides/proteins is included 
	 * @return
	 * @throws Exception
	 */
	public static Workflow getWorkflow(File workflowXMLFile, boolean fullRead, Workflow.AcquisitionMode wfMode) throws Exception
	{
		WorkflowReader r = new WorkflowReader();
		Workflow w = r.workflow;

        w.acquisitionMode = wfMode;

		try{w.sample_tracking_id = workflowXMLFile.getParentFile().getParentFile().getName();} catch (Exception e) {}
		w.id = XFiles.getBaseName( workflowXMLFile );
		w.xmlFilePath = workflowXMLFile.getAbsolutePath();
		// replicate name origins from expression analysis
		w.replicate_name = r.workflow.title;
		// store the same as meta info
		w.metaInfo.put( "WORKFLOW_XML_FILE_PATH", w.xmlFilePath );
		w.metaInfo.put( "WORKFLOW_REPLICATE_NAME", w.replicate_name );
		w.metaInfo.put( "WORKFLOW_ID", w.id );
		// do we full read?
		if(fullRead)
		{
			// read everything from xml file
			r.fillWorkflow( workflowXMLFile, w.acquisitionMode); // Workflow.AcquisitionMode.DIA );
		}
		else
		{
			// read only overview slice from xml file
			// read until tag: PRODUCT or PEPTIDE or QUERY_MASS or HIT or FRAGMENTATION
			String xml = XFiles.readUntilMatch(
				workflowXMLFile,
                ".*("+
					"(<PRODUCT)"+
					"|(<PEPTIDE)"+
					"|(<QUERY_MASS)"+
					"|(<HIT)"+
				")\\s+.*",
                ".*(" +
                        "(<FRAGMENTATION)" +
                        "|(</FRAGMENTATION)" +
                        "|(<DATA CORRECTED)" +
                 ").*",
				false);
			r.doc = (Element) XJDOM.getBadJDOMRootElement( xml );
			r.readWF();
		}

		return r.workflow;
	}
	
	/**
	 * read instrument mode parameter from workflow xml file
	 * @param p the project (must define p.root, p.id)
	 * @param w the workflow (must define w.sample_tracking_id, w.id))
	 * @return the instrument mode string
	 */
	public static String getInstrumentMode(Project p, Workflow w)
	{
		return getInstrumentMode( getFile(new File(p.root), p.id, w.sample_tracking_id, w.id) );
	}
	
	/**
	 * read instrument mode parameter from workflow xml file
	 * @param workflowXMLFile the file to read
	 * @return the instrument mode string
	 */
	public static String getInstrumentMode( File workflowXMLFile )
	{
		BufferedReader r = null;
		try
		{
			r = new BufferedReader( new FileReader( workflowXMLFile ) );
			String res = "", line = "";
			while( ((line=r.readLine())!=null) ) 
			{
				line = line.trim();
				if( line.matches(".*SEARCH_TYPE.+NAME=\".+\".*") )
				{
					// DIA mode

					// <SEARCH_TYPE NAME="Electrospray-Shotgun" />
					// <SEARCH_TYPE NAME="MSMS" />
					// break before string -> break right part after string -> take left part
					return line.split("NAME=\"")[1].split("\"")[0];
				}
				else if( line.matches( ".*DISSOCIATION_MODE.+VALUE=\".+\".*" ) )
				{
					// DDA mode
					return "DDA";
				}
				else if( line.contains("</DATABANK_SEARCH_QUERY_PARAMETERS>") )
				{
					return "Unknown";
				}
				else if( line.startsWith( "<RESULT" ) )
				{
					return "Unknown";
				}
			}
		}
		catch(Exception e)
		{
			System.err.println("Error reading file '"+workflowXMLFile.getAbsolutePath()+"'");
			e.printStackTrace();
		}
		finally
		{
			try{r.close();} catch (IOException e){}
		}
		
		return "Unknown";
	}
	
	/**
	 * create workflow object from a valid workflow folder.<br>
	 * <b>Attention:</b> no produkt/peptide/protein information read
	 * @param workflowFolder 
	 * @param fullRead if true detailed information about query masses/peptides/proteins is included 
	 */
	public static List<Workflow> getWorkflows( File workflowFolder, boolean fullRead ) throws Exception
	{
		List<Workflow> res = new ArrayList<Workflow>();
		String SamleTrackingID	= workflowFolder.getName();
		File resDir = new File( workflowFolder + File.separator + SamleTrackingID + "_WorkflowResults" );
		List<File> runFiles = XFiles.getFileList(resDir, ".xml", 0, false);
		
		for(File runFile : runFiles)
		{
            // TODO: check that this method is really not used. Remove if so, and if not, adjust acquisition modes.
			res.add( getWorkflow(runFile, fullRead, Workflow.AcquisitionMode.DIA) );
		}

		return res;
	}
	
	/**
	 * construct a File object using following pattern:<br>
	 * <B>[rootDir]/[Project_ID]/[SAMPLE_TRACKING_ID]/[SAMPLE_TRACKING_ID]_WorkflowResults/[WORKFLOW_ID].xml</B>
	 * @param rootDir
	 * @param ProjectID
	 * @param SampleTrackingID
	 * @param WorkflowID
	 * @return resulting file object
	 */
	public static File getFile(File rootDir, String ProjectID, String SampleTrackingID, String WorkflowID)
	{
		return new File(
			rootDir.getAbsolutePath() + File.separator +
			ProjectID + File.separator +
			SampleTrackingID + File.separator +
			SampleTrackingID + "_WorkflowResults" + File.separator +
			WorkflowID +  ".xml"
		);
	}


    private void readWF() throws Exception {
        if(workflow.acquisitionMode == Workflow.AcquisitionMode.DIA)
            readWF_DIA();
        else if(workflow.acquisitionMode == Workflow.AcquisitionMode.DDA)
            readWF_DDA();
    }


        /**
         * read attributes:
                            title
                            sample_description
                            input_file
                            acquired_name
                            abs_quan_response_factor
         * from xml-DOM into Workflow-object
         * @throws Exception
         */
	private void readWF_DIA() throws Exception
	{
		workflow.title = XJDOM.getAttributeValue(doc,"TITLE");
		
		Iterator<Element> params = XJDOM.getChildren(doc, "PARAM", false).iterator();
		for(int i=0; params.hasNext(); i++)
		{
			Element pare = params.next();
			String parName = XJDOM.getAttributeValue(pare, "NAME");
			String parValue = XJDOM.getAttributeValue(pare, "VALUE");

			switch (parName.toLowerCase())
			{
				case "inputfile":
					workflow.input_file = parValue;
					break;
				case "sampledescription":
					workflow.sample_description = parValue;
					break;
				case "acquiredname":
					workflow.acquired_name = parValue;
					break;
				default:
			}
			// store as meta info
			workflow.metaInfo.put( parName, parValue );
		}

		Iterator<Element> on = XJDOM.getChildren(doc, "OUTPUT", false).iterator();
		for(int i=0; on.hasNext(); i++)
		{
			Element one = on.next();
			if( XJDOM.getAttributeValue(one, "NAME").equals("AbsQuanResponseFactor") )
			{
				workflow.abs_quan_response_factor = XJava.parseNumber( XJDOM.getAttributeValue( one, "VALUE" ), 0.0 );
				break;
			}
		}

		readMetaInfo_DIA();
	}

	/**
		 * 
		 */
	private void readMetaInfo_DIA()
	{
		Element searchDoc = XJDOM.getFirstChild( doc, "DATABANK_SEARCH_QUERY_PARAMETERS" );
		// we may miss search parameters in result files produced by manually executed iaDBs.exe
		if (searchDoc == null) return;
		workflow.metaInfo.put( "SEARCH_ENGINE_TYPE",
				XJDOM.getFirstChildAttributeValue( searchDoc, "SEARCH_ENGINE_TYPE", "VALUE", "" ) );
		workflow.metaInfo.put( "SEARCH_DATABASE",
				XJDOM.getFirstChildAttributeValue( searchDoc, "SEARCH_DATABASE", "NAME", "" ) );
		workflow.metaInfo.put( "SEARCH_TYPE",
				XJDOM.getFirstChildAttributeValue( searchDoc, "SEARCH_TYPE", "NAME", "" ) );
		workflow.metaInfo.put( "FASTA_FORMAT",
				XJDOM.getFirstChildAttributeValue( searchDoc, "FASTA_FORMAT", "VALUE", "" ) );
		workflow.metaInfo.put( "NUM_BY_MATCH_FOR_PEPTIDE_MINIMUM",
				XJDOM.getFirstChildAttributeValue( searchDoc, "NUM_BY_MATCH_FOR_PEPTIDE_MINIMUM", "VALUE", "" ) );
		workflow.metaInfo.put( "NUM_PEPTIDE_FOR_PROTEIN_MINIMUM",
				XJDOM.getFirstChildAttributeValue( searchDoc, "NUM_PEPTIDE_FOR_PROTEIN_MINIMUM", "VALUE", "" ) );
		workflow.metaInfo.put( "NUM_BY_MATCH_FOR_PROTEIN_MINIMUM",
				XJDOM.getFirstChildAttributeValue( searchDoc, "NUM_BY_MATCH_FOR_PROTEIN_MINIMUM", "VALUE", "" ) );
		workflow.metaInfo.put( "MISSED_CLEAVAGES",
				XJDOM.getFirstChildAttributeValue( searchDoc, "ANALYSIS_DIGESTOR", "MISSED_CLEAVAGES", "" ) );
		workflow.metaInfo.put( "AMINO_ACID_SEQUENCE_DIGESTOR",
				XJDOM.getFirstChildAttributeValue( searchDoc, "AMINO_ACID_SEQUENCE_DIGESTOR", "NAME", "" ) );
		// parse modifiers
		List<Element> mods = XJDOM.getChildren( searchDoc, "ANALYSIS_MODIFIER" );
		List<String> modStrings = new ArrayList<>( mods.size() );
		for ( Element mod : mods )
		{
			modStrings.add(
					"name:" + XJDOM.getFirstChildAttributeValue( mod, "MODIFIER", "NAME", "" )
							+ ",status:" + XJDOM.getAttributeValue( mod, "STATUS", "" )
							+ ",enriched:" + XJDOM.getAttributeValue( mod, "ENRICHED", "" )
							+ ",applies_to:" + XJDOM.getFirstChildAttributeValue( mod, "MODIFIES", "APPLIES_TO", "" )
							+ ",delta_mass:" + XJDOM.getFirstChildAttributeValue( mod, "MODIFIES", "DELTA_MASS", "" ) );
		}
		workflow.metaInfo.put( "MODIFICATIONS", XJava.joinList( modStrings, ";" ) );
		workflow.metaInfo.put( "ENTRIES_SEARCHED", XJDOM.getFirstChildAttributeValue( doc, "RESULT", "ENTRIES_SEARCHED", "" ) );
		Element appDoc = XJDOM.getFirstChild( doc, "GeneratedBy" );
		workflow.metaInfo.put( "PROGRAM_NAME", XJDOM.getAttributeValue( appDoc, "Program", "" ) );
		workflow.metaInfo.put( "PROGRAM_VERSION", XJDOM.getAttributeValue( appDoc, "Version", "" ) );
		workflow.metaInfo.put( "PROGRAM_BUILD_DATE",
				XJDOM.getAttributeValue( appDoc, "CompileDate", "" ) + " " + XJDOM.getAttributeValue( appDoc, "CompileTime", "" ) );
		workflow.metaInfo.put( "PROGRAM_COMMAND_LINE", appDoc.getChildTextTrim( "CommandLine" ) );
	}

	/**
	 * read QueryMass and LowEnergy
	 */
	private void readQM(Workflow.AcquisitionMode wfMode) throws Exception
	{
        if(wfMode.equals(Workflow.AcquisitionMode.DIA))
            readQM_DIA();
        else if(wfMode.equals(Workflow.AcquisitionMode.DDA))
            readQM_DDA();

	}

    private void readQM_DDA() {

        QueryMass qm = null;
        LowEnergy le = null;

        Element resDoc = XJDOM.getFirstChild(doc, "RESULT");
        if(resDoc==null)
            return;

        List<Element> qmn = XJDOM.getChildren(resDoc, "QUERY_MASS", false);
        Iterator<Element> qmi = qmn.iterator();

        while( qmi.hasNext() )
        {
            Element e = qmi.next();

            // create new QM
            qm = new QueryMass();
            // fill its attributes
            try{qm.low_energy_id = XJDOM.getAttribute(e,"ID").getIntValue();}catch(Exception ex){}

            /** Intensity type changed to double on 2009-09-30 */
            qm.intensity = 100000; // TODO: This constant value should be handled in a more elegant way.

            // if LE with QM.LE_ID does not exist
            if( !workflow.lowEnergies.containsKey(qm.low_energy_id) )
            {
                // create new LE
                le = new LowEnergy();
                // fill its attributes
                le.id = qm.low_energy_id;
                le.charge = XJDOM.getAttributeValue(e,"CHARGE");
                le.mass = XJDOM.getAttributeValue(e,"MASS");
                try{le.retention_time = XJDOM.getAttribute(e,"RETENTION_TIME").getDoubleValue();}catch(Exception ex){}
                // le.retention_time_rounded = ((double)((int)(le.retention_time * 100)))/100;
                le.retention_time_rounded = Math.round(le.retention_time*100)/100.0;
                // add LE to workflow
                workflow.lowEnergies.put(le.id, le);
            }

            Iterator<Element> mmi = XJDOM.getChildren(e, "MASS_MATCH", false).iterator();

            // if QUERY_MASS contains MASS_MATCH
            if( mmi.hasNext() )
            {
                Element mme = mmi.next();
                // Peptide must already exist!!!
                int mmeid = -1;
                try { mmeid = XJDOM.getAttribute(mme, "ID").getIntValue(); } catch (DataConversionException e1) {}

				// in DDA workflows, peptide index is stored at MASS_MATCH "ID"
                Peptide p = workflow.peptides.get( mmeid ); 

				if (pepByDDAId.containsKey( mmeid ))
				{
					// clone query mass
					qm = QueryMass.clone( qm );

					// clone peptide
					p = Peptide.clone( p );
					p.id = ++maxPEid;

					// store clone
					workflow.peptides.put( p.id, p );
				}
				else
				{
					// no clone
					List<Peptide> pepList4DDAId = new ArrayList<>();
					pepByDDAId.put( mmeid, pepList4DDAId );
				}
                
				pepByDDAId.get( mmeid ).add( p );
                p.auto_qc = XJDOM.getAttributeValue(mme,"AUTO_QC");
                p.curated = XJDOM.getAttributeValue(mme,"CURATED");
                p.score = XJDOM.getAttributeValue(mme,"SCORE");

				qm.id = p.id;
                // add QM to workflow
                workflow.queryMasses.put(qm.id, qm);
            }

        }

    }

	public static void main(String[] args) throws Exception
	{
		WorkflowReader r = new WorkflowReader();
		File file = new File(
				"/Volumes/users/Pedro/temp/2015-036_PhosphoDIA_OneWFOnly/root/Proj__14351466666970_8293863831364245/PEAKS_DDA1/PEAKS_DDA1_WorkflowResults/3d3665d2-041f-4cb8-b75e-188df0dd8669.xml" );
		Workflow run = getWorkflow( file, true, AcquisitionMode.DDA );
		System.out.println( "proteins: " + run.proteins.size() );
		System.out.println( "peptides: " + run.peptides.size() );
		System.out.println( "qms: " + run.queryMasses.size() );

	}

    private void readQM_DIA() {

        QueryMass qm = null;
        LowEnergy le = null;

        List<Element> qmn = XJDOM.getChildren(doc, "QUERY_MASS", false);
        Iterator<Element> qmi = qmn.iterator();

        while(qmi.hasNext())
        {
            Element e = qmi.next();

            // create new QM
            qm = new QueryMass();
            // fill its attributes
            try{qm.id = XJDOM.getAttribute(e,"ID").getIntValue();}catch(Exception ex){}

            /** Intensity type changed to double on 2009-09-30 */
            try{qm.intensity = XJDOM.getAttribute(e,"INTENSITY").getDoubleValue();}catch(Exception ex){}
            try{qm.low_energy_id = XJDOM.getAttribute(e,"LE_ID").getIntValue();}catch(Exception ex){}


            // if LE with QM.LE_ID does not exist
            if( !workflow.lowEnergies.containsKey(qm.low_energy_id) )
            {
                // create new LE
                le = new LowEnergy();
                // fill its attributes
                le.id = qm.low_energy_id;
                le.charge = XJDOM.getAttributeValue(e,"CHARGE");
                le.mass = XJDOM.getAttributeValue(e,"MASS");
                try{le.retention_time = XJDOM.getAttribute(e,"RETENTION_TIME").getDoubleValue();}catch(Exception ex){}
                le.retention_time_rounded = Math.round(le.retention_time*100)/100.0;
                try{le.drift_time = XJDOM.getAttribute(e,"DRIFT").getDoubleValue();}catch(Exception ex){}
                // add LE to workflow
                workflow.lowEnergies.put(le.id, le);
            }

            Iterator<Element> mmn = XJDOM.getChildren(e, "MASS_MATCH", false).iterator();

            // if QUERY_MASS contains MASS_MATCH
            if(mmn.hasNext())
            {
                Element mme = mmn.next();
                // Peptide must already exist!!!
                Peptide p = workflow.peptides.get( qm.id );
                p.auto_qc = XJDOM.getAttributeValue(mme,"AUTO_QC");
                p.curated = XJDOM.getAttributeValue(mme,"CURATED");
                p.mass_error = XJDOM.getAttributeValue(mme,"MASS_ERROR");
                p.mass_error_ppm = XJDOM.getAttributeValue(mme,"MASS_ERROR_PPM");
                p.score = XJDOM.getAttributeValue(mme,"SCORE");
            }

            // add QM to workflow
            workflow.queryMasses.put(qm.id, qm);
        }
    }

    /**
	 * @since 2009-09-11 15:00
	 */
	private void readPE(Workflow.AcquisitionMode wfMode) throws Exception
	{
		Peptide p = null;
		
		List<Element> pn = XJDOM.getChildren(doc, "PEPTIDE", false);
//        if(wfMode.equals(Workflow.AcquisitionMode.DDA))
//        {
//            Element plQueryDoc = XJDOM.getFirstChild(doc, "PROTEINLYNX_QUERY");
//            Element resDoc = XJDOM.getFirstChild(plQueryDoc, "RESULT");
//
//            if(resDoc==null)
//                return;
//            pn = XJDOM.getChildren(resDoc, "PEPTIDE", false);
//        }
		Iterator<Element> pni = pn.iterator();
		
		// walk through all peptides
		while(pni.hasNext())
		{		
			Element e = pni.next();
			// create new Peptide
			p = new Peptide();
			
			try{p.id = XJDOM.getAttribute(e,"ID").getIntValue();}catch(Exception ex){}
  			p.mass = XJDOM.getAttributeValue(e,"MASS");
			p.sequence = XJDOM.getAttributeValue(e,"SEQUENCE");

            if(wfMode == Workflow.AcquisitionMode.DIA) {
                try{p.protein_id = XJDOM.getAttribute(e,"PROT_ID").getIntValue();}catch(Exception ex){}
                p.type = XJDOM.getAttributeValue(e, "TYPE");
            }else{
                p.type = "DDA";
            }

			Iterator<Element> modn = XJDOM.getChildren(e, "MATCH_MODIFIER", false).iterator();
			for(int j=0; modn.hasNext(); j++)
			{
				Element em = modn.next();
				p.modifier += 
					((j>0)?", ":"") +
					XJDOM.getAttributeValue(em,"NAME") + "(" +
					XJDOM.getAttributeValue(em,"POS") + ")"					
				;
			}
			
			// add Peptide to workflow
			workflow.peptides.put(p.id, p);
			maxPEid = Math.max( p.id, maxPEid );
		}
	}
	
	private Map<Integer, List<Peptide>> pepByDDAId = new HashMap<>();

	/**
	 * @since 2009-09-11 18:00
	 */
	private void readPR() throws Exception
	{
		Protein p = null;
		
		List<Element> pn = XJDOM.getChildren(doc, "PROTEIN", false);
		Iterator<Element> pni = pn.iterator();
		
		int pCnt=100000;
		// walk through proteins
		while(pni.hasNext())
		{
			pCnt++;
			Element e = pni.next();
			// create new Protein
			p = new Protein();

			try{p.id = XJDOM.getAttribute(e,"ID").getIntValue();}catch(Exception ex){p.id=pCnt;}
			p.auto_qc = XJDOM.getAttributeValue(e,"AUTO_QC");
			p.curated = XJDOM.getAttributeValue(e,"CURATED");
			p.coverage = XJDOM.getAttributeValue(e,"COVERAGE");
			p.score = XJDOM.getAttributeValue(e,"SCORE");
			p.rms_mass_error_prec = XJDOM.getAttributeValue(e,"RMS_MASS_ERROR_PREC");
			p.rms_mass_error_frag = XJDOM.getAttributeValue(e,"RMS_MASS_ERROR_FRAG");
			p.rms_rt_error_frag = XJDOM.getAttributeValue(e,"RMS_RT_ERROR_FRAG");
			
			Iterator<Element> cni = e.getChildren().iterator();
			for(int ci=0; cni.hasNext(); ci++)
			{
				try
				{
					Element ce = cni.next();
	
					if( ce.getName().equalsIgnoreCase("ENTRY") )
						p.entry = ce.getTextTrim();
					else if( ce.getName().equalsIgnoreCase("ACCESSION") )
						p.accession = ce.getTextTrim();
					else if( ce.getName().equalsIgnoreCase("DESCRIPTION") )
						p.description = ce.getTextTrim();
					else if( ce.getName().equalsIgnoreCase("MW") )
						p.mw = ce.getTextTrim();
					else if( ce.getName().equalsIgnoreCase("AQ_FMOLES") )
						p.aq_fmoles = ce.getTextTrim();
					else if( ce.getName().equalsIgnoreCase("AQ_NGRAMS") )
						p.aq_ngrams = ce.getTextTrim();
					else if( ce.getName().equalsIgnoreCase("PI") )
						p.pi = ce.getTextTrim();
					else if( ce.getName().equalsIgnoreCase("SEQUENCE") )
						p.sequence = ce.getTextTrim();
					else if( ce.getName().equalsIgnoreCase("SEQUENCE_MATCH") )
					{
						// peakCount peptides
						p.peptides++;
						
						int smid = XJDOM.getAttribute(ce,"ID").getIntValue();
						Peptide pep = workflow.peptides.get( smid );
						pep.start = XJDOM.getAttributeValue(ce,"START");
						pep.end = XJDOM.getAttributeValue(ce,"END");
						pep.coverage = XJDOM.getAttributeValue(ce,"COVERAGE");
						pep.frag_string = XJDOM.getAttributeValue(ce,"FRAG_STRING");
						pep.rms_mass_error_prod = XJDOM.getAttributeValue(ce,"RMS_MASS_ERROR_PROD");
						pep.rms_rt_error_prod = XJDOM.getAttributeValue(ce,"RMS_RT_ERROR_PROD");
						
						// peakCount fragment ions per peptide
						try{
							pep.products = XJDOM.getAttributeValue(ce.getChild("FRAGMENT_ION"), "IDS").split(",").length;
							p.products += pep.products;
						}catch(Exception ex){}
					}
				}
				catch(Exception ex){}
			}			
			
			// fix empty entries
			if( p.entry.length()<1 )
			{
				if( p.accession.length()<1 ) p.accession = "UNKNOWN_" + p.id;
				p.entry = p.accession;
			}
			else
			if( p.accession.length()<1 )
			{
				p.accession = p.entry;
			}
			
			workflow.proteins.put(p.id, p);
		}
	}
}
