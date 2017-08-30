package com.ctc.myct.search.dto;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.ctc.myct.search.dto.FileWrapperResult;

import com.ctc.myct.bo.model.Deal;

public class PublicationResult extends BaseDTO {

	private String additionalInformation;
	private List<FileWrapperResult> fileWrappers;
	private String firstName;
	private long journalArticleId;
	private String[] keywords;
	private String lastName;
	private String[] linkedPublications;
	private Date publicationDate;
	private long roleId;
	private long sectionId;
	private String title;
	private Map<Locale, String> titleMap;
	private boolean isCritical;
	private int criticalDuration;
	private long[] associatedDeals;
	private Deal[] associatedDealObjects;
	private String synopsis;

	public String getAdditionalInformation() {
		return additionalInformation;
	}

	public List<FileWrapperResult> getFileWrappers() {
		return fileWrappers;
	}

	public String getFirstName() {
		return firstName;
	}

	public String[] getKeywords() {
		return keywords;
	}

	public String getLastName() {
		return lastName;
	}

	public String[] getLinkedPublications() {
		return linkedPublications;
	}

	public long getJournalArticleId() {
		return journalArticleId;
	}

	public Date getPublicationDate() {
		return publicationDate;
	}

	public long getRoleId() {
		return roleId;
	}

	public long getSectionId() {
		return sectionId;
	}

	public String getTitle() {
		return title;
	}

	public Map<Locale, String> getTitleMap() {
		return titleMap;
	}

	public boolean getIsCritical() {
		return isCritical;
	}

	public int getCriticalDuration() {
		return criticalDuration;
	}

	public long[] getAssociatedDeals() {
		return associatedDeals;
	}

	public Deal[] getAssociatedDealObjects() {
		return associatedDealObjects;
	}

	public void setAdditionalInformation(String value) {
		additionalInformation = value;
	}

	public void setFileWrappers(List<FileWrapperResult> value) {
		fileWrappers = value;
	}

	public void setFirstName(String value) {
		firstName = value;
	}

	public void setKeywords(String[] value) {
		keywords = value;
	}

	public void setLastName(String value) {
		lastName = value;
	}

	public void setLinkedPublications(String[] value) {
		linkedPublications = value;
	}

	public void setJournalArticleId(long value) {
		journalArticleId = value;
	}

	public void setPublicationDate(Date value) {
		publicationDate = value;
	}

	public void setRoleId(long value) {
		roleId = value;
	}

	public void setSectionId(long value) {
		sectionId = value;
	}

	public void setTitle(String value) {
		title = value;
	}

	public void setTitleMap(Map<Locale, String> map) {
		titleMap = map;
	}

	public void setIsCritical(boolean value) {
		isCritical = value;
	}

	public void setCriticalDuration(int value) {
		criticalDuration = value;
	}

	public void setAssociatedDeals(long[] value) {
		associatedDeals = value;
	}

	public void setAssociatedDealObjects(Deal[] value) {
		associatedDealObjects = value;
	}

	public String getSynopsis() {
		return synopsis;
	}

	public void setSynopsis(String synopsis) {
		this.synopsis = synopsis;
	}

}
