package com.ctc.myct.search.dto;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.ctc.myct.search.dto.FileWrapperResult;

import com.ctc.myct.bo.model.Deal;

public class PublicationResult extends BaseDTO {

	private List<FileWrapperResult> fileWrappers;
	private long journalArticleId;
	private Date publicationDate;
	private String title;
	private String synopsis;

	public List<FileWrapperResult> getFileWrappers() {
		return fileWrappers;
	}

	public long getJournalArticleId() {
		return journalArticleId;
	}

	public Date getPublicationDate() {
		return publicationDate;
	}

	public String getTitle() {
		return title;
	}

	public void setFileWrappers(List<FileWrapperResult> value) {
		fileWrappers = value;
	}

	public void setJournalArticleId(long value) {
		journalArticleId = value;
	}

	public void setPublicationDate(Date value) {
		publicationDate = value;
	}

	public void setTitle(String value) {
		title = value;
	}

	public String getSynopsis() {
		return synopsis;
	}

	public void setSynopsis(String synopsis) {
		this.synopsis = synopsis;
	}

}
