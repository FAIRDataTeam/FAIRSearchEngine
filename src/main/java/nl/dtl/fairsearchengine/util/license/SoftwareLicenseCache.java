package nl.dtl.fairsearchengine.util.license;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import org.openrdf.model.URI;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;

import com.google.common.collect.ImmutableList;

import nl.dtl.fairsearchengine.util.HttpURLConnect;

import org.openrdf.model.vocabulary.*;

public class SoftwareLicenseCache implements SoftwareLicense {

	private String URI;
	private String licenseDescription;
	private String licenseAcronym;
	private String liceseLabel;
	
	public void SoftwareLicense(URL url) {
		String data = "<empty>";
		InputStream is;
		String doc;
		Model model = null; //step
		try {
			URL url2 = new URL(url.toString() + ".ttl");
			
			HttpURLConnect httpcon = new HttpURLConnect();
			
			try {
				
				doc = httpcon.sendGet(url.toString() + ".ttl");
				System.out.println(doc);
				
				//is = this.httpGetInputStream(url2);
				model = Rio.parse( new StringReader(doc) , "", RDFFormat.TURTLE);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // this.httpGet(url2);
			
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedRDFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		System.out.println("step2");
		
		
		Iterator<Statement> it = model.iterator();
        List<Statement> statements = ImmutableList.copyOf(it);
        
        for (Statement st : statements) {
            Resource subject = st.getSubject();
            URI predicate = st.getPredicate();
            Value object = st.getObject();
       
            // System.out.println(subject.stringValue() + " =? " + distributionURI + " " + predicate.stringValue() + " =? " + DCAT.FORMAT);
            if (subject.equals(distributionURI)) {
                if (predicate.equals(DCAT.ACCESS_URL)) {
                    metadata.setAccessURL((URI) object);
                } else if (predicate.equals(DCAT.DOWNLOAD_URL)) {
                    metadata.setDownloadURL((URI) object);
                } else if (predicate.equals(DCAT.FORMAT)) {                   
                     metadata.setFormat(new LiteralImpl(object.stringValue(), 
                             XMLSchema.STRING));
                } else if (predicate.equals(DCAT.BYTE_SIZE)) {                                  
                     metadata.setByteSize(new LiteralImpl(object.stringValue(), 
                             XMLSchema.STRING));
                } else if (predicate.equals(DCAT.MEDIA_TYPE)) {                   
                     metadata.setMediaType(new LiteralImpl(object.stringValue(), 
                             XMLSchema.STRING));
                }
            }
        }
        
		// TODO improve
		//for (Resource r : model.filter(null, RDFS.LABEL, null).subjects()) {				
		//	Model firstNameTriples2 = model.filter(r, RDFS.LABEL, null);
		//	//this.(firstNameTriples2.objectString());
		//}
	
	}

	@Override
	public String getLicenseDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLicenseAcronym() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLicenseURI() {
		// TODO Auto-generated method stub
		return null;
	}
	
	  private InputStream httpGetInputStream(URL url) throws MalformedURLException{
		  
			//URL URL2 = new URL(url.toString()+".ttl");
		    InputStream is = null;
		    BufferedReader br;

		    try {	   
		    
		        is = url.openStream();  // throws an IOException
		        
		        return is;
		        
		       
		    } catch (MalformedURLException mue) {
		         mue.printStackTrace();
		    } catch (IOException ioe) {
		         ioe.printStackTrace();
		    } finally {
		        //try {
		            //if (is != null) is.close();
		        //} catch (IOException ioe) {
		            // nothing to see here
		        //}
		    }
		    return null;
	  }
	
	  /*private String httpGet(URL url) throws IOException{
		    InputStream is = httpGetInputStream(url);
		  	BufferedReader br = new BufferedReader(new InputStreamReader(is));
		    String line, doc = "";
	        while ((line = br.readLine()) != null) {
	            doc += line;
	        }
	        
	        return doc;
	  }*/

}
