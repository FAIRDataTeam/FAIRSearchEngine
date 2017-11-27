package nl.dtl.fairsearchengine.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.impl.URIImpl;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.json.JSONArray;
import org.json.JSONObject;


import com.ipeirotis.readability.Readability;

import nl.dtl.fairmetadata4j.io.CatalogMetadataParser;
import nl.dtl.fairmetadata4j.io.DatasetMetadataParser;
import nl.dtl.fairmetadata4j.io.DistributionMetadataParser;
import nl.dtl.fairmetadata4j.io.FDPMetadataParser;
import nl.dtl.fairmetadata4j.io.MetadataParserException;
import nl.dtl.fairmetadata4j.model.CatalogMetadata;
import nl.dtl.fairmetadata4j.model.DatasetMetadata;
import nl.dtl.fairmetadata4j.model.DistributionMetadata;
import nl.dtl.fairmetadata4j.model.FDPMetadata;
import nl.dtl.fairsearchengine.crawler.Crawler;
import nl.dtl.fairsearchengine.util.esClient.ESClient;
import nl.dtl.fairsearchengine.util.esClient.JestESClient;

public class FdpParser {
	
	private static final Logger LOGGER = Logger.getLogger(FdpParser.class.getName());

	//TODO Change to URI object
	String uri;
	String output;
	
	public FdpParser() {}
	
	public void parse(String uri, String output){
		
	setup(uri, output);

		
	LOGGER.info("NParser  v0.42");
		
	CatalogMetadata catalogMetadata = null;
	DatasetMetadata datasetMetadata = null;
	DistributionMetadata distributionMetdada = null;
	
	List<JsonString> jsonList = new Vector();
	     
     int ci = 0,  di  = 0 , dii = 0;
	 List<IRI> catalogList;
			try {
				//TODO make it a log
				System.out.println("Parsing "+uri.toString());
				System.out.println("Sending to "+output);
				
				FDPMetadata fdpmetadata = this.loadFDP(uri);
				
			    List<IRI> catalogURI = fdpmetadata.getCatalogs();
			    List<IRI> catalogURI2 = new ArrayList();
			      
			    catalogList = new Vector();
			    
			    for(int i = 0; i < catalogURI.size(); i++){
			    	  
			    	  catalogList.add(catalogURI.get(i));
			    }
			      
				
				for(IRI ctURI : catalogList){
    
		    	   try{
		    		  System.out.println("\n Identified and parsing catalog " + ctURI.stringValue());
		    	   	  catalogMetadata = this.doCatalogMetadataParser(ctURI);
		    	   	 
		    	   	  setupJSON(fdpmetadata, catalogMetadata, datasetMetadata, distributionMetdada, ctURI, jsonList);
		    	   } catch(IOException | MetadataParserException | MimeTypeException e){
		    		  e.printStackTrace();
		    	   } catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
				}
	       
		} catch (RDFParseException | UnsupportedRDFormatException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	       
		ESClient esclient = null;
		HttpURLConnect httpCon = null;
		
		//setup connection	
		if(send2es()) {   
			httpCon = new HttpURLConnect();		    
		    esclient = new JestESClient();
		}
		
		if(send2file()) {
			trucateFile(this.output);
		}
	    
	    int i = 0;
	    for(JsonString jsonElem: jsonList){
	    	
	    	String dataPreamble = "{ \"create\": { \"_index\": \"dataset\", \"_type\": \"dataset\", \"_id\": \""+jsonElem.getId()+"\"} }\n";
	    	//String payload = "{ \"doc\" : " + jsonElem.getJson() + " } \n";
	    	String payload = jsonElem.getJson();
	    	String data = "";
	    	
		    try{
		    	//add force import and force update
		    	if(send2es()) {
			    	if ( esclient.documentExists(jsonElem.getId()) ) {
			    		dataPreamble = "{ \"update\": { \"_index\": \"dataset\", \"_type\": \"dataset\", \"_id\": \""+jsonElem.getId()+"\"}}\n";
			    		data = "\n{ \"doc\" : " + payload +" }";
			    		data = dataPreamble + payload + "\n";
			    		httpCon.sendPost(output, data); //TODO get from command line
			    	} else {
			    		data = dataPreamble + payload + "\n";
			    		httpCon.sendPost(output, data); //TODO get from command line
			    	}
			    	System.out.println("DATA:" + data);
		    	} 
		    	
		    	if(send2file()) {
		    		data = dataPreamble + payload + "\n";
		    		writeToFile(output, data);
		    	}
		    	
		    	if(send2stdout()) {
		    		data = dataPreamble + payload;
		    		System.out.println(data);
		    	}
		    	
		    			
		    	
		    } catch(IOException e){
		    	System.err.println(e.getMessage());
		    	System.err.println("Skipping "+jsonElem.getId()+". Error found while checking if data exists.");
		    } catch (Exception e) {
		    	System.err.println(e.getMessage());
		    	System.err.println("Skipping "+jsonElem.getId()+". Error inserting data.");
			}
    
	    }
    
	}
	
	private void setup(String uri, String output) {
		this.uri = uri;
	    this.output = output;
	}
	
	private boolean send2stdout(){
		if(output == null) return true;
		else return false;
	}
	
	private boolean send2es(){
		if(output.toLowerCase().startsWith("http")) return true;
		else return false;
	}
	
	private boolean send2file() {
		if(!output.toLowerCase().startsWith("http")) return true;
		else return false;
	}
	
	private void trucateFile(String file) {
		FileChannel fc;
		try {
			fc = new FileOutputStream(file, true).getChannel();
		    fc.truncate(0);
		    fc.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void writeToFile(String elasticSearch, String data) {
		try {
		    Files.write(Paths.get(elasticSearch), data.getBytes(), StandardOpenOption.APPEND , StandardOpenOption.CREATE);
		}catch (IOException e) {
			e.printStackTrace();
		    System.err.println(e.getMessage());
		}	
	}

	private void setupJSON(FDPMetadata fdpmetadata, CatalogMetadata catalogMetadata, DatasetMetadata datasetMetadata, DistributionMetadata distributionMetdada, URI ctURI, List jsonList) throws MalformedURLException, MetadataParserException, IOException, MimeTypeException, URISyntaxException{

		List<IRI> dataset = catalogMetadata.getDatasets();
				
		int  di = 0;
		for(URI uri : dataset){
			System.out.println(uri +" >> "+di++);
			
			try {
				datasetMetadata = this.doDatasetMetadaParser(uri);
			} catch (Exception e) {
				System.err.println("Error found in "+uri);
				e.printStackTrace();
				continue; //TODO improve; skips to next iteration 
			}
			

			List<IRI> distribution = datasetMetadata.getDistributions();
			JSONObject es = new JSONObject();
				
			List<String> suggest = new Vector();
			//es.put("_id", uri.stringValue());
			
			//if(catalogMetadata.getHomepage()!=null) es.put("latndingPage");
			if(datasetMetadata.getTitle()!=null) es.put("title", datasetMetadata.getTitle().stringValue());
			if(datasetMetadata.getDescription()!=null){
				es.put("description", datasetMetadata.getDescription().stringValue());
					
				Readability r;
				
				if(false) {
					r = new Readability(datasetMetadata.getDescription().stringValue());
						
					Map<String,Double> readabilityMetrics = new HashMap();
					
					readabilityMetrics.put("ARI", new Double(r.getARI()));
					readabilityMetrics.put("ColemanLiau", new Double(r.getColemanLiau()));
					readabilityMetrics.put("Complex", new Double(r.getComplex()));
					readabilityMetrics.put("FleschKincaidGradeLevel", new Double(r.getFleschKincaidGradeLevel()));
					readabilityMetrics.put("FleschReadingEase", new Double(r.getFleschReadingEase()));
					readabilityMetrics.put("GunningFog", new Double(r.getGunningFog()));
					readabilityMetrics.put("SMOG", new Double(r.getSMOG()));
					readabilityMetrics.put("SMOGIndex", new Double(r.getSMOGIndex()));
					es.put("readabilityMetrics", readabilityMetrics);
				}
				
				
			}
			if(catalogMetadata.getTitle()!=null) es.put("catalogTitle", catalogMetadata.getTitle().stringValue());
			if(datasetMetadata.getLandingPage()!=null) es.put("landingPage", datasetMetadata.getLandingPage());
			
			
			List<Literal> keywordList = datasetMetadata.getKeywords();
			List<String> keywordStringList = new Vector<String>();
			
			for(Literal keyword: keywordList){
				keywordStringList.add(keyword.stringValue());
			}
			
			
			
			IRI institutionCountry  = fdpmetadata.getInstitutionCountry();
			List<IRI> catalogThemes = datasetMetadata.getThemes();
			
			List<String> themeList = new Vector<String>();
			
			for(IRI catalogTheme : catalogThemes) {
				System.out.println(catalogTheme.stringValue());
				Uri2Label uri2label = new Uri2Label(new java.net.URI(catalogTheme.stringValue()));
				
				List<String> labelListen = uri2label.getLabels("en");
				for(String label : labelListen) {
					String encLabel = URLEncoder.encode(label, "UTF-8");
					themeList.add(encLabel);
					suggest.add(encLabel);
				}
			
				List<String> labelListnl = uri2label.getLabels("nl");

				for(String label : labelListnl) {
					String encLabel = URLEncoder.encode(label, "UTF-8");
					themeList.add(encLabel);
					suggest.add(encLabel);
				}
				
				themeList.add(catalogTheme.stringValue()); //add to synonims
				

				suggest.add(catalogTheme.stringValue());
			}
			
			//todo add to model
			es.put("theme", themeList);
			
			es.put("repositoryTitle", fdpmetadata.getTitle().stringValue() );
			es.put("repositoryCountry", "NL"); //TODO improve
			
			//timestamp
			Long timestamp =  (new Date().getTime())/1000;
			es.put("updateTimestamp", timestamp);
			
			//es.put("description_suggest", datasetMetadata.getDescription().stringValue() );
			
			//es.put("suggest", new JSONObject().put("input", datasetMetadata.getDescription().stringValue() ));
			
			
			JestESClient jec = new JestESClient();
			List<String> tokenList = jec.doTextAnalysis("english", datasetMetadata.getTitle().stringValue());
			
			//adding tokenized list from english
			for(String token : tokenList)	
						suggest.add( token );
			//adding list of keywords
			for(String keyword : keywordStringList)
						suggest.add( keyword );
			

			es.put("suggest", new JSONObject().put("input", suggest ));

			
			JSONArray coords = new JSONArray();
				coords.put(new Double(52.13263));
				coords.put(new Double(5.29126));
			es.put("repositoryLocation", coords);
			
			List<IRI> taxonomyList = catalogMetadata.getThemeTaxonomys();
			List<String> taxonomyStringList = new Vector();
			
			for(URI taxonomyUri: taxonomyList){ //adicionar
				taxonomyStringList.add(taxonomyUri.stringValue());
			}
			
			es.put("taxonomyList", taxonomyStringList);
			

			List<Map> distributionList = new Vector();
					
			for(URI distributionUri : distribution){
				
					   Map<String,String> distributionMap = new HashMap();
					   //System.out.println(">>> "+dii++);
					   try {
						distributionMetdada = this.doDistributionMetadataParser(distributionUri);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					   
					   if(distributionMetdada.getTitle()!=null)
					     distributionMap.put("title", distributionMetdada.getTitle().stringValue());
					   if(distributionMetdada.getDownloadURL()!=null)
					     distributionMap.put("download",    distributionMetdada.getDownloadURL().stringValue());

					   if(distributionMetdada.getLicense()!=null){
						   distributionMap.put("licenseURI", distributionMetdada.getLicense().stringValue());
						  // if(Crawler.convertLicenseURIToAcronym(distributionMetdada.getLicense().stringValue())!=null)
						  // distributionMap.put("licenseAcronym", Crawler.convertLicenseURIToAcronym(distributionMetdada.getLicense().stringValue()));
						   distributionMap.put("licenseAcronym", "APACHE");
					   }
					   
			
					   if(distributionUri.stringValue()!=null)
						   distributionMap.put("distributionURI", distributionUri.stringValue());
					   
					  // distributionMetdada.
					   
					   if(distributionMetdada.getDownloadURL()!=null){
					   		int size = this.getFileSize(new URL( distributionMetdada.getDownloadURL().stringValue() ));
					   		//int size = this.getFileSize(new URL( "http://www.sapo.pt/" ));
					   		System.out.println("********* ******* ******** SIZE: "+size);
					   		distributionMap.put( "downloadSize", size + "" );
					   		distributionMap.put( "downloadHumanReadableSize", Crawler.humanReadableByteCount(size, false) );
					   }
					   
					   
					   if(distributionMetdada.getAccessURL()!=null)
					     distributionMap.put("accessURL",   distributionMetdada.getAccessURL().stringValue());
					   if(distributionMetdada.getFormat()!=null)
						 distributionMap.put("format", distributionMetdada.getFormat().stringValue());
					   else{
						   if(distributionMetdada.getMediaType()!=null && distributionMetdada.getFormat()==null){
							   String sourceMymeType = distributionMetdada.getMediaType().stringValue();
							   MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
							   //TODO treat exception here
							   org.apache.tika.mime.MimeType mimeType = allTypes.forName(sourceMymeType);
							   String fileExtension = mimeType.getExtension();
							   distributionMap.put("format", fileExtension);
						   } else distributionMap.put("format", "unknown");
					   }
					    // distributionMap.put("xxx", "xxxxxx");
					   
					   distributionList.add(distributionMap);
			}	
			es.put("distribution", distributionList);
			
			//improve structure
			es.put("FDPurl", this.uri);
			es.put("catalogURL", ctURI);
			es.put("datasetURL", uri);
			
			es.put("keyword", keywordStringList);
			
			//jsonList.add("datasetURL")
			
			System.out.println("JSON: "+es.toString());
			jsonList.add(new JsonString(uri.stringValue() , es.toString()));
		}
	}
	
	private  FDPMetadata loadFDP(String uri) throws RDFParseException, UnsupportedRDFormatException, IOException {
		
		HttpURLConnect con = new HttpURLConnect();
		 
		String data = null;
		
		try {
			data = con.sendGet(uri);
			System.out.println(data);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
			  
		  
	    FDPMetadataParser fdpMetadataParser = new FDPMetadataParser();
	    FDPMetadata fdpMetadata = null;
		try {
			fdpMetadata = fdpMetadataParser.parse(data, new URIImpl(uri), RDFFormat.TURTLE);
		} catch (MetadataParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	               
	    //mudar nome de variavel
	    //TODO unccoment x 4
	    
	      

	    //List<URI> catalogURI2 = new Vector();
	      
	     // catalogURI2.add(new URIImpl("http://dev-vm.fair-dtls.surf-hosted.nl:8082/fdp/biobank"));
	     // catalogURI2.add(new URIImpl("http://dev-vm.fair-dtls.surf-hosted.nl:8082/fdp/comparativeGenomics"));
	     // catalogURI2.add(new URIImpl("http://dev-vm.fair-dtls.surf-hosted.nl:8082/fdp/patient-registry"));
	     // catalogURI2.add(new URIImpl("http://dev-vm.fair-dtls.surf-hosted.nl:8082/fdp/textmining"));
	     // catalogURI2.add(new URIImpl("http://dev-vm.fair-dtls.surf-hosted.nl:8082/fdp/transcriptomics"));
	      
	     // catalogURI2.add(new URIImpl("http://semlab1.liacs.nl:8080/fdp/biobank"));
	     // catalogURI2.add(new URIImpl("http://semlab1.liacs.nl:8080/fdp/registry"));
	      	       
		/*
	       catalogURI2.add(new URIImpl("http://semlab1.liacs.nl:8080/fdp/biobank/173631-collection1"));
	       catalogURI2.add(new URIImpl("http://semlab1.liacs.nl:8080/fdp/biobank/44001-collection1"));
	       catalogURI2.add(new URIImpl("http://semlab1.liacs.nl:8080/fdp/biobank/45274-collection1"));
	       catalogURI2.add(new URIImpl("http://semlab1.liacs.nl:8080/fdp/biobank/45401-collection1"));
	  
	       catalogURI2.add(new URIImpl("http://semlab1.liacs.nl:8080/fdp/biobank/45401-collection1"));
	       catalogURI2.add(new URIImpl("http://semlab1.liacs.nl:8080/fdp/biobank/62316-collection1"));
	       catalogURI2.add(new URIImpl("http://semlab1.liacs.nl:8080/fdp/biobank/76957-collection1"));
	       catalogURI2.add(new URIImpl("http://semlab1.liacs.nl:8080/fdp/biobank/77088-collection1"));
	       catalogURI2.add(new URIImpl("http://semlab1.liacs.nl:8080/fdp/biobank/77219-collection1"));
	       catalogURI2.add(new URIImpl("http://semlab1.liacs.nl:8080/fdp/biobank/77350-collection1"));
	       catalogURI2.add(new URIImpl("http://semlab1.liacs.nl:8080/fdp/biobank/77489-collection1"));
	       
	       catalogURI2.add(new URIImpl("http://semlab1.liacs.nl:8080/fdp/biobank/77630-collection1"));
	       catalogURI2.add(new URIImpl("http://semlab1.liacs.nl:8080/fdp/biobank/77761-collection1"));
	       catalogURI2.add(new URIImpl("http://semlab1.liacs.nl:8080/fdp/biobank/87919-collection1"));
	       catalogURI2.add(new URIImpl("http://semlab1.liacs.nl:8080/fdp/biobank/88051-collection1")); */
	       
	       return fdpMetadata;
	}
	
	private DatasetMetadata doDatasetMetadaParser(URI uri) throws Exception {
		System.out.println(uri.stringValue());
		DatasetMetadataParser dmp = new DatasetMetadataParser();
		
		HttpURLConnect httpConnect = new HttpURLConnect();
		
		//InputStream in = new URL( uri.stringValue() ).openStream();
		
		DatasetMetadata dm = dmp.parse(httpConnect.sendGet(uri.stringValue()), new URIImpl(uri.toString()), RDFFormat.TURTLE);
		
		//System.out.println("-"+dm.getDescription());
		//System.out.println("-"+dm.getTitle());
		//System.out.println("-"+dm.getDescription());
		
		return dm;
				
	}

	public CatalogMetadata doCatalogMetadataParser(IRI ctURI) throws Exception{
		 //System.out.println("-> "+ctURI.toString());
		   
		  HttpURLConnect httpcon = new HttpURLConnect();
		  String rdf = httpcon.sendGet(ctURI.toString());
		
		  CatalogMetadataParser parser = new CatalogMetadataParser();
		  
		  // alternative way to get data
	     /* 	
	       
	       InputStream in;
	       //in = new URL( "http://dev-vm.fair-dtls.surf-hosted.nl:8082/fdp/biobank" ).openStream();
	       in = new URL( ctURI.toString() ).openStream();
	        
	         String rdf = IOUtils.toString(in);*/
		  
	        IRI cURI = ctURI;
	        IRI fURI = new URIImpl(this.uri);   
	        
	        System.out.println(rdf);
	        
	        CatalogMetadata metadata = parser.parse(
	                 rdf, cURI, fURI, 
	                RDFFormat.TURTLE);
	        
	        return metadata;
	}
	
	private DistributionMetadata doDistributionMetadataParser(URI distributionUri) throws Exception {
		//System.out.println("=== "+distributionUri.stringValue());
		
		DistributionMetadataParser disMP = new DistributionMetadataParser();
		
		HttpURLConnect httpCon = new HttpURLConnect();
		
		//InputStream in2 = new URL(distributionUri.stringValue()).openStream();
		
		DistributionMetadata distributionMetadata = disMP.parse(httpCon.sendGet(distributionUri.stringValue()), new URIImpl(distributionUri.toString()), RDFFormat.TURTLE );
	
		//System.out.println("======Des "+distributionMetadata.getDescription());
		//System.out.println("======Dow "+distributionMetadata.getDownloadURL());
		//System.out.println("======Acc "+distributionMetadata.getAccessURL());
		
		
		return distributionMetadata;
		//System.out.println("json> " + es.toString());
	}
	
	private int getFileSize(URL url) {
	    HttpURLConnection conn = null;
	    try {
	        conn = (HttpURLConnection) url.openConnection();
	        conn.setRequestMethod("HEAD");
	        conn.getInputStream();
	        return conn.getContentLength();
	    } catch (IOException e) {
	    	System.out.println(e.getMessage());
	        return -1;
	    } finally {
	        conn.disconnect();
	    }
	}
	
}
