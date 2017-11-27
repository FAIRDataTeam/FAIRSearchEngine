package nl.dtl.fairsearchengine.crawler;

//import static org.junit.Assert.fail;

import org.eclipse.rdf4j.model.impl.URIImpl;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
//import org.openrdf.rio.Rio;
//import org.openrdf.rio.UnsupportedRDFormatException;

//import com.github.kburger.revolver.Resolver;
//import com.github.kburger.revolver.ResolverImpl;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import com.ipeirotis.readability.Readability;

import org.eclipse.rdf4j.model.Model;


import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
//import java.util.Optional;
import java.util.Vector;

import javax.activation.MimeType;

import nl.dtl.fairmetadata4j.io.CatalogMetadataParser;
import nl.dtl.fairmetadata4j.io.CatalogMetadataParserTest;
import nl.dtl.fairmetadata4j.io.DatasetMetadataParser;
import nl.dtl.fairmetadata4j.io.DistributionMetadataParser;
import nl.dtl.fairmetadata4j.io.FDPMetadataParser;
import nl.dtl.fairmetadata4j.io.MetadataParserException;
import nl.dtl.fairmetadata4j.model.CatalogMetadata;
import nl.dtl.fairmetadata4j.model.DatasetMetadata;
import nl.dtl.fairmetadata4j.model.DistributionMetadata;
import nl.dtl.fairmetadata4j.model.FDPMetadata;
import nl.dtl.fairmetadata4j.utils.ExampleFilesUtils;
import nl.dtl.fairsearchengine.util.FdpParser;
import nl.dtl.fairsearchengine.util.HttpURLConnect;
import nl.dtl.fairsearchengine.util.JsonString;
import nl.dtl.fairsearchengine.util.Theme.Theme;
import nl.dtl.fairsearchengine.util.esClient.ESClient;
import nl.dtl.fairsearchengine.util.esClient.JestESClient;
import nl.dtl.fairsearchengine.util.license.License;
import nl.dtl.fairsearchengine.util.license.SoftwareLicenseFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.json.JSONArray;
import org.json.JSONObject;

//import org.openrdf.model.Statement;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.impl.URIImpl;

public class Crawler {
	
	//static String FDPUri = "http://dev-vm.fair-dtls.surf-hosted.nl:8082/fdp";
	//static String FDPUri = "http://semlab1.liacs.nl:8080/fdp";
	
	private static String ElasticSearchServer = "http://127.0.0.1:9200/";
	
	//static String FDPCatalogURI = "http://dev-vm.fair-dtls.surf-hosted.nl:8082/fdp/biobank";
	//static String FDPCatalogURI = "";
	
	//private static String DEFAULTURI = "https://lorentz.fair-dtls.surf-hosted.nl/fdp/";
	//private static String DEFAULTURI = "http://dev-vm.fair-dtls.surf-hosted.nl:8082/fdp/";
	//private static String DEFAULTURI = "http://fdp.wikipathways.org/fdp/";
	private static String DEFAULTURI = "http://136.243.4.200:8087/fdp";
	
	private String fdpuri;
	
	public static void main(String argv[]) throws URISyntaxException{
		
		Options options = new Options();
		options.addOption("f", true, "FAIR data point to parse");
		options.addOption("o", true, "Leave empty for stdout, \"-o example.txt\" to write to file, \"-o http://127.0.0.1/_bulk\" to load to an elasticsearch instance ");
		
		CommandLineParser parser = new DefaultParser();
		
		
		Crawler c = new Crawler();
		
		try {
			CommandLine cmd = parser.parse( options, argv);
		

			String output;
			
			if(cmd.hasOption("o") && cmd.getOptionValue("o").isEmpty()) {
				output = null;
			} else if(cmd.hasOption("o")) {
				output = cmd.getOptionValue("o");
			} else {
				output = "http://127.0.0.1:9200/_bulk";
			}
			
			if(cmd.hasOption("f")){
				FdpParser fdpparser = new FdpParser();
				fdpparser.parse(cmd.getOptionValue("f"), output);
			} else {
				FdpParser fdpparser = new FdpParser();
				fdpparser.parse(DEFAULTURI, output);
			}
		
							
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
		//c.parse2();
		
	/*	try {
			if(false)
				c.loadFDP();
		} catch (RDFParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedRDFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		//c.parse3();
		
		/*Resolver r = new ResolverImpl();
		//Optional o = r.resolve(new java.net.URI("https://rdflicense.appspot.com/rdflicense/cc-by-nc-nd3.0"));
		//Optional o = r.resolve(new java.net.URI("http://dev-vm.fair-dtls.surf-hosted.nl:8082/fdp/"));
		Optional o = r.resolve(new java.net.URI("http://www.w3.org/2000/01/rdf-schema#seeAlso"));
		*/	
	
		
		//System.out.println(o.get());
	} 
	
	private void setElasticSearchServer(String address) {
		this.ElasticSearchServer  = address;
	}

	public static void parse2(){
		
		
		/*try {
			IRI iri = new URIImpl("http://dbpedia.org/data/The_Lord_of_the_Rings");
			Theme t = new Theme(iri);
			t.getLabel();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		*/
		
	}

	
	public void parse3(){
		//if(argv.length==1)
		//	new Crawler().parse1(FDPUri);
		//else
		//	new Crawler().parse1(argv[1]);
		String url = "https://rdflicense.appspot.com/rdflicense/cc-by-nc-nd3.0";
		SoftwareLicenseFactory slc = new SoftwareLicenseFactory(); 
		try {
			URL urlObj = new URL(url);
			//slc.SoftwareLicense(urlObj);
		} catch (MalformedURLException e) {
			System.out.println(e.getMessage());
			//fail("Malformed URL: " + e.getMessage());
		} catch (Exception e){
			System.err.println("Ops, something went wrong:" + e.getMessage());
		}
		
	}

	
	public void parse4(){
		//if(argv.length==1)
		//	new Crawler().parse1(FDPUri);
		//else
		//	new Crawler().parse1(argv[1]);
		String url = "https://rdflicense.appspot.com/rdflicense/cc-by-nc-nd3.0";
		SoftwareLicenseFactory slc = new SoftwareLicenseFactory(); 
		try {
			URL urlObj = new URL(url);
			slc.SoftwareLicense(urlObj);
		} catch (MalformedURLException e) {
			System.out.println(e.getMessage());
			//fail("Malformed URL: " + e.getMessage());
		} catch (Exception e){
			System.err.println("Ops, something went wrong:" + e.getMessage());
		}
		
	}
	
	
    public static String getFileContentAsString(String fileName)  {        
        String content = "";  
        try {
            URL fileURL = Crawler.class.getResource(fileName);
            content = Resources.toString(fileURL, Charsets.UTF_8);
        } catch (IOException ex) {
            //LOGGER.error("Error getting turle file",ex);          
        }        
        return content;
    } 
    
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
    
    public static String convertLicenseURIToAcronym(String uri){
    	Map<String, String> map = new HashMap<String, String>();
    	
    	map.put("https://creativecommons.org/licenses/by-nc-nd/3.0/", "CC BY-NC-ND 3.0");
    	
    	return map.get(uri);
		    	    	
    }
    
	
}
