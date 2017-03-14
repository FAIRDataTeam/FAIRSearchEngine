package nl.dtl.fairsearchengine.util.Theme;

import java.io.IOException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.URIImpl;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;

import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.google.common.collect.ImmutableList;
import nl.dtl.fairmetadata4j.io.MetadataParserException;
import nl.dtl.fairmetadata4j.model.FDPMetadata;
import nl.dtl.fairsearchengine.util.HttpURLConnect;

public class Theme {
	
	IRI iri;

	public Theme(IRI iri){
		this.iri = iri;
	}
	
	public String getLabel(){
		
		HttpURLConnect httpcon = new HttpURLConnect();
		String doc = "";
		
		try {
			
			doc = httpcon.sendGet(this.iri.toString() + ".n3");
			System.out.println(doc);
			
			/*Model model = Rio.parse(new StringReader(doc),
		    		this.iri, RDFFormat.TURTLE);*/

            Model model = Rio.parse(new StringReader(doc), this.iri.stringValue() , RDFFormat.TURTLE);
                   
			Model m  = model.filter(this.iri, RDFS.LABEL, null);
		
		    Iterator<Statement> it = m.iterator();
			
			List<Statement> statements = ImmutableList.copyOf(it);
			
			for (Statement st: m) {
				System.out.println(st.toString());
			}
			
			
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			System.out.println("step2");
		
		return null;
	}
	


}
