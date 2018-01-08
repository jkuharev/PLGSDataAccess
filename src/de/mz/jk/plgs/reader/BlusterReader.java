package de.mz.jk.plgs.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import de.mz.jk.plgs.data.ClusterAverage;
import de.mz.jk.plgs.data.ClusteredEMRT;
import de.mz.jk.plgs.data.ExpressionAnalysis;

/**
 * 
 * @author J.Kuharev
 * @since 2009-09-07
 */
public class BlusterReader 
{
/*
	public static void main(String[] args) 
	{
		String path = "D:\\PLGS2.4\\root\\Proj__12505921454590_3868760712547077\\ExpressionAnalyses\\_12511076716000_17984599795519318\\Results\\_12511077277910_42559850223138085\\BlusterOutput.cvf";
		BlusterReader br = new BlusterReader( new File(path), 0 );
		try 
		{
			br.open();
			ClusterAverage ca = null;
			int i=0;
			for(i=0; null!=(ca=br.next()); i++ ){}
			System.out.println(i);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
*/
	private File file=null;
	private String[] title = null;
	private BufferedReader reader = null;
	private int numberOfWorkflows = 0;
	private String[] workflowTitles = null;
	private int[] workflowIndexes = null;
	private int lineIndex;
	private int expressionAnalysisIndex = 0;
	
	private ClusterAverage lastCluster = null;
	
	public String[] getWorkflowTitles(){return workflowTitles;}
	public void setWorkflowIndex(String name, int index )
	{
		for(int i=0; i<workflowTitles.length; i++)
		{
			if( workflowTitles[i].equalsIgnoreCase(name) )
			{
				workflowIndexes[i] = index;
				return;
			}				                
		}
	}
	
	public int getNumberOfWorkflows(){return numberOfWorkflows;}
	public int getNumberOfClusters(){return lineIndex;}
	public ClusterAverage getClusterAverage(){return lastCluster;}
	
	/**
	 * constructs a File object from parameters
	 * @param rootDir
	 * @param ProjectID
	 * @param ExpressionAnalysisID 
	 * @param ResultID
	 * @return File object from constructed path
	 */
	public static File getFile(
		File rootDir, 
		String ProjectID, 
		String ExpressionAnalysisID,
		String ResultID)
	{
		return new File(
			rootDir.getAbsolutePath() + File.separator + 
			ProjectID + File.separator +
			"ExpressionAnalyses" + File.separator +
			ExpressionAnalysisID +  File.separator +
			"Results" +  File.separator +
			ResultID +  File.separator +
			"BlusterOutput.cvf"
		);
	}
	
	public BlusterReader(File file, int expressionAnalysisIndex)
	{
		this.file = file;
		this.expressionAnalysisIndex = expressionAnalysisIndex;
	}
	
	public BlusterReader(File rootDir, ExpressionAnalysis ea)
	{
		this(getFile(rootDir, ea.project.id, ea.id, ea.result_id), ea.index);
	}
	
	/**
	 * opens BlusterOutput.cvf file and reads the title line
	 * @throws Exception
	 */
	public void open() throws Exception
	{
		lineIndex=0;
		
		reader = new BufferedReader( new FileReader(file) );
		
		String line = "";
		if( null!=(line=reader.readLine()) )
		{
			String[] cols = line.split(",");
			title = new String[cols.length];
			
			for(int i=0; i<cols.length; i++)
			{
				title[i] = cols[i].trim();
			}
		}
		
		numberOfWorkflows = (title.length - 13)/9;
		workflowTitles = new String[numberOfWorkflows];
		workflowIndexes= new int[numberOfWorkflows];
		for(int j=0; j<numberOfWorkflows; j++)
		{
			workflowTitles[j] = title[13+j*9].trim();
		}
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
		lastCluster = getNext();
		return (lastCluster!=null);
	}
	
	/**
	 * reads next line from BlusterOutput.cvf file and 
	 * @return ClusterAverage object
	 */
	private ClusterAverage getNext()
	{
		try{
			String line = "";
			if( null==(line=reader.readLine()) )
			{
				reader.close();
			}
			else
			{
				// appended ',0' at the end of line enforces all elements be splitted
				// due to String.split() does not create 
				// elements from empty separators at the end of string
				String[] values = (line+",0").split(",");
				clearCluster();		
				lastCluster = new ClusterAverage();
		
				lastCluster.expression_analysis_index = expressionAnalysisIndex;
				
				/*
				 Field 	Type 	Null 	Key 	Default 	Extra
index 	int(11) 	NO 	PRI 	NULL 	auto_increment
expression_analysis_index 	int(11) 	YES 	MUL 	NULL 	 
cluster_id 	int(11) 	YES 	MUL 	NULL 	 
ave_mhp 	double 	YES 	  	NULL 	 
std_mhp 	double 	YES 	  	NULL 	 
ave_rt 	double 	YES 	  	NULL 	 
std_rt 	double 	YES 	  	NULL 	 
ave_inten 	int(11) 	YES 	MUL 	NULL 	 
std_inten 	int(11) 	YES 	  	NULL 	 
ave_ref_rt 	double 	YES 	  	NULL 	 
std_ref_rt 	double 	YES 	  	NULL 	 
ave_charge 	double 	YES 	  	NULL 	 
std_charge 	double 	YES 	  	NULL 	 
total_rep_rate 	tinyint(4) 	YES 	  	NULL 	 
recluster_id 	int(11) 	YES 	MUL 	NULL 	 
ave_cor_inten 	double 	NO 	  	NULL 	 
*/
				
				lastCluster.cluster_id		= Integer.parseInt(numString(values[0].trim()));
				lastCluster.ave_mhp			= Double.parseDouble(numString(values[2].trim()));
				lastCluster.std_mhp			= Double.parseDouble(numString(values[3].trim()));
				lastCluster.ave_rt			= Double.parseDouble(numString(values[4].trim()));
				lastCluster.std_rt			= Double.parseDouble(numString(values[5].trim()));
				lastCluster.ave_inten		= Integer.parseInt(numString(values[6].trim()));
				lastCluster.std_inten		= Double.parseDouble(numString(values[7].trim()));
				lastCluster.ave_ref_rt		= Double.parseDouble(numString(values[8].trim()));
				lastCluster.std_ref_rt		= Double.parseDouble(numString(values[9].trim()));
				lastCluster.ave_charge		= Double.parseDouble(numString(values[10].trim()));
				lastCluster.std_charge		= Double.parseDouble(numString(values[11].trim()));
				lastCluster.total_rep_rate	= Integer.parseInt(numString(values[12].trim()));
				
				for(int j=0; j<numberOfWorkflows; j++)
				{
					// start index for this EMRT
					int i = 13 + j*9;
					// exclude clustering gaps recognized by zero mass
					if(values[i].trim().length()>0)
					try
					{
						ClusteredEMRT emrt =  new ClusteredEMRT();
						emrt.workflow_index = workflowIndexes[j];
						emrt.mass			= Double.parseDouble(numString(values[i].trim()));
						emrt.sd_mhp			= Double.parseDouble(numString(values[i+1].trim()));
						emrt.inten			= Integer.parseInt(numString(values[i+2].trim()));
						emrt.spec_index		= Integer.parseInt(numString(values[i+3].trim()));
						emrt.charge			= Double.parseDouble(numString(values[i+4].trim()));
						emrt.rt				= Double.parseDouble(numString(values[i+5].trim()));
						emrt.sd_rt			= Double.parseDouble(numString(values[i+6].trim()));
						emrt.ref_rt			= Double.parseDouble(numString(values[i+7].trim()));
						emrt.precursor_type	= Integer.parseInt(numString(values[i+8].trim()));
						lastCluster.clusteredEMRT.add( emrt );
					}
					catch(Exception e)
					{
						System.out.println(
							"  line=" + lineIndex + "\n" +
							"clusid=" + lastCluster.cluster_id + "\n" +
							"length=" + values.length + "\n" +
							"workfl=" + j + "\n" +
							"column=" + i + "\n"
						);
						
						throw e;
					}
				}
				lineIndex++;
				return lastCluster;
			}
		}catch(Exception e){ e.printStackTrace(); }
		return null;
	}
	
	private String numString(String value) 
	{
		return (value==null || value.length()<1) ? "0" : value;
	}
	
	/**
	 * clearing Cluster for freeing memory from ballast
	 */
	private void clearCluster() 
	{
		if(lastCluster!=null)
		{
			lastCluster.clusteredEMRT.clear();
			lastCluster=null;
		}
	}
}
