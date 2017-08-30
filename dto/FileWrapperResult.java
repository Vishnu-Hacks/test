package com.ctc.myct.search.dto;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FileWrapperResult extends BaseDTO{

	private List<File>				files;
	private List<Long>				fileIds;
	private boolean					internalOnly;
	private String					mimeType;
	private long 					organizationId;
	private boolean					storeOnly;
	private String					title;
	private Map<Locale, String> 	titleMap;
	

	public List<File>				getFiles()										{ return files; }
	public List<Long>				getFileIds()									{ return fileIds; }
	public boolean					isInternalOnly()								{ return internalOnly; }
	public String					getMimeType()									{ return mimeType; }
	public long						getOrganizationId()								{ return organizationId; }
	public boolean					isStoreOnly()									{ return storeOnly; }
	public String 					getTitle()										{ return title; } 	
	public Map<Locale, String> 		getTitleMap() 									{ return titleMap; }
	
	public void						setFiles( List<File> value )					{ files = value; }
	public void						setFileIds( List<Long> value )					{ fileIds = value; }
	public void 					setInternalOnly(boolean value)					{ internalOnly = value; }
	public void						setMimeType(String value)						{ mimeType = value; }
	public void 					setOrganizationId( long value )					{ organizationId = value; }
	public void						setStoreOnly(boolean value)						{ storeOnly = value; }
	public void						setTitle( String value ) 						{ title = value; }
	public void 					setTitleMap(Map<Locale, String> value) 			{ titleMap = value; }

}
