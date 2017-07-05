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
	
	String title;

	public Theme(){
		
	}
	
	public String getTitle(){
		return title;	
	}
	
	public void setTitle(String title){
		this.title = title;
	}
	


}
