package nl.dtl.fairsearchengine.util.esClient;

import java.io.IOException;
import java.util.List;

public interface ESClient {
	
	List<String> doTextAnalysis(String analyser, String text);
	boolean documentExists(String id) throws IOException; //TODO create own exception
	<T> List<T>  search(String search, Class typeParameterClass);
	<T> List<T>  wordSuggest(String word, Class typeParameterClass);
	String search(String search);

}
