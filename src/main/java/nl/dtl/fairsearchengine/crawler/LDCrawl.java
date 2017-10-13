package nl.dtl.fairsearchengine.crawler;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.openrdf.model.vocabulary.RDFS;

import nl.dtl.fairsearchengine.util.HttpURLConnect;
import nl.dtl.fairsearchengine.util.license.License;

public class LDCrawl {
	
	/*list all Entities*/
	/*get all subjects for a given entity*/
	/*get all subject predicate object*/
         //index literals
	     //resolve entities and fill the result (or/and add the object?)
	
	public LDCrawl(URI seedURI, String location){
		HttpURLConnect httpcon = new HttpURLConnect();
		String doc = null;
		try {
			doc = httpcon.sendGet(location);
			
			Model model = null;
		 
			try {
				model = Rio.parse(new StringReader(doc), seedURI.stringValue() , RDFFormat.TURTLE);
			} catch (RDFParseException | UnsupportedRDFormatException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
	        //IRI fdpURI = (IRI) statements.get(0).getSubject();
		    for(Statement statement: model){
		    	//System.out.println(RDFS.LABEL+"x"+statement.getPredicate());
		    	if(statement.getPredicate().stringValue().equals(RDFS.LABEL)){
		    		System.out.println(statement.getObject().stringValue());
		    		//license.setName( statement.getObject().stringValue() );
		    		//return license;
		    	}
		    }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	List<URI> autoSeed(){
		//List all concepts
		return null;
	}
	
}

