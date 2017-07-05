package nl.dtl.fairsearchengine.util.license;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

public class LicenseTester {

	@Test
	public void test() {
		String url = "http://rdflicense.appspot.com/rdflicense/cc-by-nc-nd3.0";
		SoftwareLicenseFactory slc = new SoftwareLicenseFactory(); 
		try {
			slc.SoftwareLicense(new URL(url));
		} catch (MalformedURLException e) {
			fail("Malformed URL: " + e.getMessage());
		} catch (Exception e){
			fail("Ops, something went wrong:" + e.getMessage());
		}
		
		return;
	}

}
