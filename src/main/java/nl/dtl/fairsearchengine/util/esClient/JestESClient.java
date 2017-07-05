package nl.dtl.fairsearchengine.util.esClient;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
//import com.hp.hpl.jena.query.Dataset;

import io.searchbox.client.JestResult;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Get;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.Suggest;
import io.searchbox.core.Suggest.Builder;
import io.searchbox.core.SuggestResult;
import io.searchbox.indices.Analyze;

public class JestESClient implements ESClient {

	@Override
	public List<String> doTextAnalysis(String analyser, String text) {
		JestClient jc = this.getJestClient();
		
		String query = "";
		
		Analyze analyzer = new Analyze.Builder()
							.analyzer(analyser)
							.text(text)
							.build();
		
		List<String> tokenList = new Vector();
		String data = analyzer.getData(new Gson());
		
		try {
			JestResult jestresult = jc.execute(analyzer);
			JsonObject jsonobject = jestresult.getJsonObject();
			System.out.println(jestresult.getJsonString());
			JsonArray jsonarray = jsonobject.getAsJsonArray("tokens");
			
			for(int i = 0; i <jsonarray.size(); i++){
				//System.out.println( jsonarray.get(i).getAsJsonObject().getAsJsonObject("token").getAsString() );
				tokenList.add(jsonarray.get(i).getAsJsonObject().get("token").getAsString());
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//System.out.println(analyzer.get);
		
		System.out.println("Data: "+data);
		
		return tokenList;
	}
	
	
	private JestClient getJestClient(){
		 JestClientFactory factory = new JestClientFactory();
		 factory.setHttpClientConfig(new HttpClientConfig
		                        .Builder("http://localhost:9200")
		                        .multiThreaded(true)
					//Per default this implementation will create no more than 2 concurrent connections per given route
					.defaultMaxTotalConnectionPerRoute(1)
					// and no more 20 connections in total
					.maxTotalConnection(1)
		                        .build());
		 JestClient client = factory.getObject();
		 return client;
	}


	@Override
	public boolean documentExists(String id) throws IOException {
		/*TODO optimize for head only - no need to retrieve body */
		JestClient jc = this.getJestClient();
		
		Get get = new  Get.Builder("dataset", id).type("dataset").build();
		
		DocumentResult result = jc.execute(get);
		
		if(result.getResponseCode()==200) return true;
		else return false;
	}


	@Override
	public <T> List<T> search(String searchstring, Class typeParameterClass) {
		
		SearchResult result = jestSearch(searchstring);
		
		List<T> searchResult = result.getSourceAsObjectList( typeParameterClass );
		
		return searchResult;
		
	}
	
	private SearchResult jestSearch(String searchstring){
		JestClient jc = this.getJestClient();
		
		
/*		String query = "{\n" +
	            "    \"id\": \"myTemplateId\"," +
	            "    \"params\": {\n" +
	            "        \"query_string\" : \"" + searchstring + "\"" +
	            "    }\n" +
	            "}";
*/
		String query = "\"query\": {"+
				       "\"match\" : {"+
				       "	\"_all\" : \""+searchstring+"\""+
					   "}"+
					   "}";
		
		String query2 = "{\n" +
	            "    \"query\": {\n" +
	            "        \"filtered\" : {\n" +
	            "            \"query\" : {\n" +
	            "                \"query_string\" : {\n" +
	            "                    \"query\" : \""+searchstring+"\"\n" +
	            "                }\n" +
	            "            }\n" +
//	            "            ,\"filter\" : {\n" +
//	            "                \"term\" : { \"user\" : \"kimchy\" }\n" +
//	            "            }\n" +
	            "        }\n" +
	            "    }\n" +
	            "}";
		
		System.out.println(query);
		
		Search search = new Search.Builder(query2)
	                // multiple index or types can be added.
	                .addIndex("dataset")
	                .addType("dataset")
	                .build();
		
		SearchResult result = null;
		try {
			result =  jc.execute(search);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}


	@Override
	public String search(String searchstring) {
		SearchResult result = jestSearch(searchstring);
		return result.getJsonString();
	}


	@Override
	public <T> List<T> wordSuggest(String word, Class typeParameterClass) {
				JestClient jc = this.getJestClient();
		
				String query = "{"+
					"\"name_suggest\":{"+
			        "\"text\":\""+word+"\","+
			        	"\"completion\": {"+
			            	"\"field\" : \"suggest\""+
			        	  "}"+
			    		"}"+
					"}";
				
				System.out.println(query);
				
				Suggest suggest = new Suggest.Builder(query)
							.addIndex("dataset")
							.build();
				
				SuggestResult result = null;
				
				try {
					result =  jc.execute(suggest);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				return result.getSourceAsObjectList(typeParameterClass);
	}

}
