package com.ctc.myct.search.util;

import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.ctc.myct.search.configuration.ReadConfig;
import com.ctc.myct.search.dto.ArticleResult;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.DocumentException;
import com.liferay.portal.kernel.xml.Node;
import com.liferay.portal.kernel.xml.SAXReaderUtil;


public class ArticleResultParserUtil  {
	
	@SuppressWarnings("unused")
	private static final Log _log = LogFactoryUtil.getLog(ArticleResultParserUtil.class);
	
	

	public static ArticleResult getSearchResult( String type,  String id,ServiceContext context) {
		
		ArticleResult result = null;
		JournalArticle article = null;
		
		try {
			switch( type ) {
			
				case "rsrt":					
					article = JournalArticleLocalServiceUtil.getArticle(ReadConfig.getSiteId(), id);
					result = parseRSRTArticle(article, context.getLocale());
					break;
				case "ctu":
					article = JournalArticleLocalServiceUtil.getArticle(ReadConfig.getSiteId(), id);
					result = parseCTUArticle(article,context.getLocale());					
					break;
				case "vimeo":				
					article = JournalArticleLocalServiceUtil.getArticle(ReadConfig.getSiteId(), id);
					result = parseVimeoArticle(article, context.getLocale());					
					break;
				case "techTraining":				
					article = JournalArticleLocalServiceUtil.getArticle(ReadConfig.getSiteId(), id);
					result = parseRSRTArticle(article, context.getLocale());					
					break;
					
			}
					
			
		} catch (PortalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			return null;
		}
		
		
		return result;
		
	}
	
	private static ArticleResult parseCTUArticle( JournalArticle article, Locale locale ) {
		
		ArticleResult ar = new ArticleResult();
		
		//Get the title.
		ar.setTitle( article.getTitle(locale));
		
		//Now to parse the content.
		String content = article.getContentByLocale(locale.getLanguage());
		Document document = null;
		
		try {
			
			document = SAXReaderUtil.read(new StringReader(content));
			
			String url = "/root/dynamic-element[@name='url']";
			String modifiedDate = "/root/dynamic-element[@name='modifiedDate']";
			
			Node node = document.selectSingleNode(url);
			
			if(node != null) {
				Node nodeContent = node.selectSingleNode("dynamic-content");
				ar.setURL(nodeContent.getText());
			}
			
			node = document.selectSingleNode(modifiedDate);
			
			if(node != null) {
				Node nodeContent = node.selectSingleNode("dynamic-content");
				
				//We need to convert the string date to a Calendar date. 
				DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
				
				if( !nodeContent.getText().trim().isEmpty() )
				{
				
					try {
						Date date = formatter.parse(nodeContent.getText());
						
						Calendar calendar = Calendar.getInstance();
						calendar.setTime(date);										
						
						ar.setDate(calendar.getTimeInMillis());
					}
					catch (ParseException e) {
						  e.printStackTrace();
					}
				}
			}						
			
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		
		return ar;
		
	}
	
	private static ArticleResult parseRSRTArticle( JournalArticle article, Locale locale ) {
		
		ArticleResult ar = new ArticleResult();
		
		//Get the title.
		ar.setTitle( article.getTitle(locale));
		
		//Now to parse the content.
		String content = article.getContentByLocale(locale.getLanguage());
		Document document = null;
		
		try {
			
			document = SAXReaderUtil.read(new StringReader(content));
			
			String url = "/root/dynamic-element[@name='url']";
			
			Node node = document.selectSingleNode(url);
			
			if(node != null) {
				Node nodeContent = node.selectSingleNode("dynamic-content");
				ar.setURL(nodeContent.getText());
			}			
			
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		
		return ar;
		
	}
	
	private static ArticleResult parseVimeoArticle( JournalArticle article, Locale locale ) {
		
		ArticleResult ar = new ArticleResult();
		
		//Get the title.
		ar.setTitle( article.getTitle(locale));
		
		//Now to parse the content.
		String content = article.getContentByLocale(locale.getLanguage());
		Document document = null;
		
		try {
			
			document = SAXReaderUtil.read(new StringReader(content));
			
			String url = "/root/dynamic-element[@name='url']";
			String uploadDate = "/root/dynamic-element[@name='uploadDate']";
			
			Node node = document.selectSingleNode(url);
			
			if(node != null) {
				Node nodeContent = node.selectSingleNode("dynamic-content");
				ar.setURL(nodeContent.getText());
			}
			
			node = document.selectSingleNode(uploadDate);
			
			if(node != null) {
				Node nodeContent = node.selectSingleNode("dynamic-content");
				
				//We need to convert the string date to a Calendar date. 
				DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
				
				if( !nodeContent.getText().trim().isEmpty() )
				{
				
					try {
						Date date = formatter.parse(nodeContent.getText());
						
						Calendar calendar = Calendar.getInstance();
						calendar.setTime(date);										
						
						ar.setDate(calendar.getTimeInMillis());
					}
					catch (ParseException e) {
						  e.printStackTrace();
					}
				}
			}			
			
			
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		
		return ar;
		
	}

}
