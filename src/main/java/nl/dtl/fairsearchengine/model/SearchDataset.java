package nl.dtl.fairsearchengine.model;

import java.util.List;

public class SearchDataset {
	
	String FDPurl;
	String catalogTitle;
	String catalogURL;
	String datasetURL;
	String description;
	//List<SearchDistribution> searchDistributions;
	String landingPage;
	String repositoryTitle;
	String repositoryCountry;
	//String repositoryLocation;
	//String taxonomyList;
	
	public String getCatalogTitle() {
		return catalogTitle;
	}
	public void setCatalogTitle(String catalogTitle) {
		this.catalogTitle = catalogTitle;
	}
	public String getCatalogURL() {
		return catalogURL;
	}
	public void setCatalogURL(String catalogURL) {
		this.catalogURL = catalogURL;
	}
	public String getDatasetURL() {
		return datasetURL;
	}
	public void setDatasetURL(String datasetURL) {
		this.datasetURL = datasetURL;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public String getFDPurl() {
		return FDPurl;
	}
	public void setFDPUrl(String fDPUrl) {
		FDPurl = fDPUrl;
	}
	
/*	public List<SearchDistribution> getSearchDistributions() {
		return searchDistributions;
	}
	public void setSearchDistributions(List<SearchDistribution> searchDistributions) {
		this.searchDistributions = searchDistributions;
	}*/
	
	public String getLandingPage() {
		return landingPage;
	}
	public void setLandingPage(String landingPage) {
		this.landingPage = landingPage;
	}
	public String getRepositoryTitle() {
		return repositoryTitle;
	}
	public void setRepositoryTitle(String repositoryTitle) {
		this.repositoryTitle = repositoryTitle;
	}
	public String getRepositoryCountry() {
		return repositoryCountry;
	}
	public void setRepositoryCountry(String repositoryCountry) {
		this.repositoryCountry = repositoryCountry;
	}
	/*public String getRepositoryLocation() {
		return repositoryLocation;
	}
	public void setRepositoryLocation(String repositoryLocation) {
		this.repositoryLocation = repositoryLocation;
	}*/
}
