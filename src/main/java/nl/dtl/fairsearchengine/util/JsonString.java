package nl.dtl.fairsearchengine.util;

public class JsonString {
	String id;
	String json;
	
	public JsonString(String id, String json){
		this.id = id;
		this.json = json;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getJson() {
		return json;
	}
	public void setJson(String json) {
		this.json = json;
	}

}
