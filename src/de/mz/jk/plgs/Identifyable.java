package de.mz.jk.plgs;
/** ISOQuant, isoquant.kernel.plgs, 07.04.2011*/


/**
 * <h3>{@link Identifyable}</h3>
 * @author Joerg Kuharev
 * @version 07.04.2011 13:41:46
 */
public class Identifyable
{
	private static long objectIdentifierCounter = 0;
	private long objectIdentifier = 0;
	
	/**
	 * create object with new identity
	 */
	public Identifyable()
	{
		setUniqueIdentity();
	}
	
	/**
	 * create object using the identity of identic object
	 * @param identicObject
	 */
	public Identifyable(Identifyable identicObject)
	{
		objectIdentifier = identicObject.objectIdentifier;
	}
	
	/**
	 * check if given object has the same identity
	 * @param obj
	 * @return
	 */
	public boolean isIdentic( Identifyable obj )
	{
		return this.objectIdentifier == obj.objectIdentifier; 
	}
	
	/**
	 * own the identity from given object
	 * @param obj
	 */
	public void setIdentic( Identifyable obj )
	{ 
		this.objectIdentifier = obj.objectIdentifier; 
	}
	
	/**
	 * make this object have unique identity 
	 */
	public void setUniqueIdentity()
	{
		objectIdentifier = ++objectIdentifierCounter;
	}
	
	@Override public boolean equals(Object obj)
	{
		return 
			(obj instanceof Identifyable) 
			? this.isIdentic((Identifyable) obj) 
			: super.equals(obj);
	}
}
