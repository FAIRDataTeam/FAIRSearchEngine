package nl.dtl.fairsearchengine.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

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

	//TODO Change to URI object
	String uri; 
	
	public FdpParser() {}
	
	public void parse(String uri){
		
	this.uri = uri;	
		
	System.out.println("NParser  v0.1");
		
	CatalogMetadata catalogMetadata = null;
	DatasetMetadata datasetMetadata = null;
	DistributionMetadata distributionMetdada = null;
	
	List<JsonString> jsonList = new Vector();
	     
     int ci = 0,  di  = 0 , dii = 0;
	 List<IRI> catalogList;
			try {
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
	       
	       
	    HttpURLConnect httpCon = new HttpURLConnect();
	    int i = 0;
	    
	    ESClient esclient = new JestESClient();
	    
	    for(JsonString jsonElem: jsonList){
	    
	    String data = "";
	    	
		    try{
		    	//add force import and force update
		    	
		    	if ( esclient.documentExists(jsonElem.getId()) ) {
		    		data = "{ \"update\": { \"_index\": \"dataset\", \"_type\": \"dataset\", \"_id\": \""+jsonElem.getId()+"\"}}\n";  	
		    		data += "{ \"doc\" : " + jsonElem.getJson() + " } \n";
		    	} else {
		    		data = "{ \"create\": { \"_index\": \"dataset\", \"_type\": \"dataset\", \"_id\": \""+jsonElem.getId()+"\"}}\n";
		    		data += jsonElem.getJson() + "\n";
		    	}
		    	System.out.println("\n"+data);
		    	httpCon.sendPost("http://127.0.0.1:9200/_bulk", data);
		    	
		    } catch(IOException e){
		    	System.out.println(e.getMessage());
		    	System.out.println("Skipping "+jsonElem.getId()+". Error found while checking if data exists.");
		    } catch (Exception e) {
		    	System.out.println(e.getMessage());
		    	System.out.println("Skipping "+jsonElem.getId()+". Error inserting data.");
			}
    
	    }
    
	}
	
	private void setupJSON(FDPMetadata fdpmetadata, CatalogMetadata catalogMetadata, DatasetMetadata datasetMetadata, DistributionMetadata distributionMetdada, URI ctURI, List jsonList) throws MalformedURLException, MetadataParserException, IOException, MimeTypeException{
		System.out.println("parsing datasets");
		
		List<IRI> dataset = catalogMetadata.getDatasets();
		//System.out.println("> "+ci++ + " " + ctURI.toString() + " " + dataset.size());
		
		System.out.println("parsed " +dataset.size()+ " datasets");
		
		int  di = 0;
		for(URI uri : dataset){
			System.out.println(uri  +">> "+di++);
			
			
			try {
				datasetMetadata = this.doDatasetMetadaParser(uri);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
			
			
			List<IRI> distribution = datasetMetadata.getDistributions();
				JSONObject es = new JSONObject();
				
				//es.put("_id", uri.stringValue());
				
				//if(catalogMetadata.getHomepage()!=null) es.put("latndingPage");
				if(datasetMetadata.getTitle()!=null) es.put("title", datasetMetadata.getTitle().stringValue());
				if(datasetMetadata.getDescription()!=null){
					es.put("description", datasetMetadata.getDescription().stringValue());
					
					Readability r = new Readability(datasetMetadata.getDescription().stringValue());
					
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
				if(catalogMetadata.getTitle()!=null) es.put("catalogTitle", catalogMetadata.getTitle().stringValue());
				if(datasetMetadata.getLandingPage()!=null) es.put("landingPage", datasetMetadata.getLandingPage());
				
				List<Literal> keywordList = datasetMetadata.getKeywords();
				List<String> keywordStringList = new Vector<String>();
				
				for(Literal keyword: keywordList){
					keywordStringList.add(keyword.stringValue());
				}
				
				es.put("keyword", keywordStringList);
				
				IRI institutionCountry  = fdpmetadata.getInstitutionCountry();
				List<IRI> catalogThemes = datasetMetadata.getThemes();
				
				es.put("repositoryTitle", fdpmetadata.getTitle().stringValue() );
				es.put("repositoryCountry", "NL"); //TODO improve
				
				//es.put("description_suggest", datasetMetadata.getDescription().stringValue() );
				
				//es.put("suggest", new JSONObject().put("input", datasetMetadata.getDescription().stringValue() ));
				List<String> suggest = new Vector();
				
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
