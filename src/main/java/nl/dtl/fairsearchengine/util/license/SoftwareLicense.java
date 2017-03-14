package nl.dtl.fairsearchengine.util.license;

import java.net.URL;

public interface SoftwareLicense {
		
	public void SoftwareLicense(URL uri);
	
	public String getLicenseDescription();
	
	public String getLicenseAcronym();
	
	public String getLicenseURI();
	
}
