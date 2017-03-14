package nl.dtl.fairsearchengine.util;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

public class LinkedDataResolver {
	
	Hashtable<String, String> l;
	
	
	
	
	public void LinkedDataResolver(){
		init();
	}
	
	private void init(){
		l = new Hashtable<String, String>();
		
		l.add("http://www.lexvo.org/", /http:\\/\\/lexvo.org\\/id\\/iso3166\\/([A-Z]+)/g");
		l.add("http://dbpedia.org/page/The_Lord_of_the_Rings", "/http://dbpedia.org/data/The_Lord_of_the_Rings/g");
	}
	
	
	

}
