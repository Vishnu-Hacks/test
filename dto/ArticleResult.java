package com.ctc.myct.search.dto;

public class ArticleResult extends BaseDTO{
	

	//(title, description, date, url (or info to create url)

	private String 					description;
	private long 					date;
	private String 					title;
	private String 					url;

	public String 					getDescription() 						{ return description; }
	public long 					getDate() 								{ return date; }
	public String 					getTitle() 								{ return title; }
	public String 					getURL() 								{ return url; }

	public void 					setDescription( String value ) 			{ description = value; }
	public void 					setDate( long value ) 					{ date = value; }
	public void 					setTitle( String value ) 				{ title = value; }
	public void 					setURL( String value ) 					{ url = value; }
	
	
}
