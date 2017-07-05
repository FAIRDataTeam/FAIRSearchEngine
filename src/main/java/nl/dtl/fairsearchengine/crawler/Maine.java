package nl.dtl.fairsearchengine.crawler;

import java.util.List;

import org.eclipse.rdf4j.model.URI;

import org.eclipse.rdf4j.model.impl.URIImpl;

import com.ipeirotis.readability.Readability;

import nl.dtl.fairsearchengine.model.SearchDataset;
import nl.dtl.fairsearchengine.model.WordSuggest;
import nl.dtl.fairsearchengine.util.esClient.ESClient;
import nl.dtl.fairsearchengine.util.esClient.JestESClient;
import nl.dtl.fairsearchengine.util.license.License;
import nl.dtl.fairsearchengine.util.license.SoftwareLicenseFactory;

//import org.eclipse.rdf4j.model.URI;

public class Maine {
	
	public static void main(String argv[]) {
		//new Maine().readability();
		//new Maine().testSearch("aa");
		new Maine().testSuggestion("d");
		
	}
	
	void readability(){
		//URI uri = new URIImpl("http://dbpedia.org/data/France");
		//LDCrawl ldc = new LDCrawl( uri, "http://dbpedia.org/data/France.n3" );
		
		Readability r = new Readability("hello world! I like cool people. Nice Nice!! The athom is 1.32 score. I \"love much\" chocolate");
		System.out.println("FleschReadingEase  (100/0)"+r.getFleschReadingEase());
		System.out.println("gunningfog         (0/17) "+r.getGunningFog());
		System.out.println("feschkinkaidgrade  (100/0)"+r.getFleschKincaidGradeLevel());
		
		
		
		
		
		License l = SoftwareLicenseFactory.getLicense(new URIImpl("http://purl.org/NET/rdflicense/cc-zero1.0"));		
		System.out.println("ok"+l.getName());
		
		if(true) return;
	}
	
	void testSearch(String s){
		ESClient esclient = new JestESClient();
		List<SearchDataset> searchDatasetList = esclient.search("dataset", SearchDataset.class);
		
		for(SearchDataset searchDataset : searchDatasetList){
			System.out.println(searchDataset.getCatalogTitle());						
		}
	}
	
	void testSuggestion(String s){
		ESClient esclient = new JestESClient();	
		List<WordSuggest> wsList = esclient.wordSuggest(s, WordSuggest.class);  
		for(WordSuggest suggestion : wsList){
			suggestion.getText();
		}
	}
	
	void generateFPD(List<URI> sourceURI, List<URI> destiny ){
		for(URI uri: sourceURI){
			
		}
	}
	

}
