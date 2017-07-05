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
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;

import com.google.common.collect.ImmutableList;

import nl.dtl.fairmetadata4j.model.FDPMetadata;
import nl.dtl.fairmetadata4j.utils.RDFUtils;
import nl.dtl.fairsearchengine.util.HttpURLConnect;

import org.openrdf.model.vocabulary.*;

public class SoftwareLicenseFactory {
	
	//TODO improve and make a cache
	
	public static License  getLicense(URI uri){
				HttpURLConnect httpcon = new HttpURLConnect();
				License license = new License();
				String rdfLicense = null;
				try {
					rdfLicense = httpcon.sendGet(uri.stringValue() + ".ttl");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			    //List<Statement> statements = RDFUtils.getStatements(
			    //		rdfLicense, null, RDFFormat.TURTLE);
			    
			    Model model = null;
				try {
					model = Rio.parse(new StringReader(rdfLicense), uri.stringValue() , RDFFormat.TURTLE);
				} catch (RDFParseException | UnsupportedRDFormatException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    
		        //IRI fdpURI = (IRI) statements.get(0).getSubject();
			    for(Statement statement: model){
			    	//System.out.println(RDFS.LABEL+"x"+statement.getPredicate());
			    	if(statement.getPredicate().stringValue().equals(RDFS.LABEL)){
			    		System.out.println(statement.getObject().stringValue());
			    		license.setName( statement.getObject().stringValue() );
			    		return license;
			    	}
			    }
		        
		        
				
				return null;
				
	}
	
}
