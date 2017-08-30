package com.ctc.myct.search.dto;

public class File extends BaseDTO {

	private long			fileId;
	private long 			organizationId;
	private String			language;
	
	public long				getFileId()						{ return fileId; }
	public long				getOrganizationId()				{ return organizationId; }
	public String 			getLanguage()						{ return language; } 	

	
	public void				setFileId(long value)			{ fileId = value; }
	public void				setOrganizationId(long value)	{ organizationId = value; }
	public void 			setLanguage(String value)			{ language = value; } 	

	
	
}
