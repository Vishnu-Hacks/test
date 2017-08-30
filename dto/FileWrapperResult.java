package com.ctc.myct.search.dto;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FileWrapperResult extends BaseDTO{

	private List<File>				files;
	private List<Long>				fileIds;
	private String					mimeType;
	private String					title;
	

	public List<File>				getFiles()										{ return files; }
	public List<Long>				getFileIds()									{ return fileIds; }
	public String					getMimeType()									{ return mimeType; }
	public String 					getTitle()										{ return title; } 	
	
	public void						setFiles( List<File> value )					{ files = value; }
	public void						setFileIds( List<Long> value )					{ fileIds = value; }
	public void						setMimeType(String value)						{ mimeType = value; }
	public void						setTitle( String value ) 						{ title = value; }

}
