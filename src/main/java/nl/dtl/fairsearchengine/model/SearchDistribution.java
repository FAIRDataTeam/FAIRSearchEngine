package nl.dtl.fairsearchengine.model;

public class SearchDistribution {
	String accessURL;
	String download;
	String downloadHumanReadableSize;
	int downloadSize;
	String format;
	String licenseAcronym;
	String licenseURI;
	String title;
	
	public String getAccessURL() {
		return accessURL;
	}
	public void setAccessURL(String accessURL) {
		this.accessURL = accessURL;
	}
	public String getDownload() {
		return download;
	}
	public void setDownload(String download) {
		this.download = download;
	}
	public String getDownloadHumanReadableSize() {
		return downloadHumanReadableSize;
	}
	public void setDownloadHumanReadableSize(String downloadHumanReadableSize) {
		this.downloadHumanReadableSize = downloadHumanReadableSize;
	}
	public int getDownloadSize() {
		return downloadSize;
	}
	public void setDownloadSize(int downloadSize) {
		this.downloadSize = downloadSize;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public String getLicenseAcronym() {
		return licenseAcronym;
	}
	public void setLicenseAcronym(String licenseAcronym) {
		this.licenseAcronym = licenseAcronym;
	}
	public String getLicenseURI() {
		return licenseURI;
	}
	public void setLicenseURI(String licenseURI) {
		this.licenseURI = licenseURI;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
}
