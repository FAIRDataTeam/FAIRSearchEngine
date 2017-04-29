package nl.dtl.fairsearchengine.util.esClient;

import java.util.List;

public interface ESClient {
	
	List<String> doTextAnalysis(String analyser, String text);


}
