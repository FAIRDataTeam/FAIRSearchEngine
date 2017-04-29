package nl.dtl.fairsearchengine.util.esClient;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.searchbox.client.JestResult;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
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

}
