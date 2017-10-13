package nl.dtl.fairsearchengine.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LinkedDataResolver {
	
	private Hashtable<String, LinkedData2Web> map = null;
	
	
	
	
	public LinkedDataResolver(){
		init();
	}
	
	private void init(){
		this.map = new Hashtable<String, LinkedData2Web>();
		
		map.put("http://www.lexvo.org/", new LinkedData2Web("/http:\\/\\/lexvo\\.org\\/id\\/iso3166\\/([a-zA-Z]+)/g","h"));
		map.put("http://dbpedia.org/", new LinkedData2Web("http:\\/\\/dbpedia\\.org\\/data\\/(.+)", "http://dbpedia.org/data/{{0}}.n3"));
	}

	public URI resolve(URI uri) {
		
		 //System.out.println(uri.toString());
		
		 String UriPrefix = uri.getScheme() + "://" + uri.getHost() + "/";

		 LinkedData2Web l2w = map.get(UriPrefix);
		 
		 if(l2w==null) System.out.println("is null");
	
		 Pattern p = Pattern.compile("http:\\/\\/dbpedia\\.org\\/data\\/(.+)");
		 
		 Matcher m = p.matcher(uri.toASCIIString());
	
		 //System.out.println("applying " + l2w.getLinkedDataUri() + " to " +  uri.toASCIIString() );
		 
		 String webUri = null;
		 
		 while(m.find()) {
			 //System.out.println("xxx" + m.group()+ " " + m.groupCount());
		 
			 int size = m.groupCount();
			 webUri = l2w.getWebUri();
			 
			 for(int i = 1; i <= size; i++) {
				 //System.out.println( size + " " +i+ " = " + "\\{\\{"+i+"\\}\\}");
				 if(m.group(i)!=null) {
					 //System.out.println( m.group(i) + "<-" );
					 webUri = webUri.replaceAll("\\{\\{"+(i-1)+"\\}\\}", m.group(i));
				 } else {
					 System.out.println("isNull");
				 }
			 }
		 }
		 
	    URI returnUri = null;
		try {
			returnUri = new URI(webUri);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		 return returnUri;
	}
	
	
	

}
