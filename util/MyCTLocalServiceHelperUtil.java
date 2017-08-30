package com.ctc.myct.search.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import com.ctc.myct.bo.model.FileWrapperFileEntryXRef;
import com.ctc.myct.bo.model.Publication;
import com.ctc.myct.bo.model.PublicationAttachment;
import com.ctc.myct.bo.service.FileWrapperFileEntryXRefServiceUtil;
import com.ctc.myct.bo.service.FileWrapperLocalServiceUtil;
import com.ctc.myct.bo.service.PublicationAttachmentLocalServiceUtil;
import com.ctc.myct.bo.service.PublicationServiceUtil;
import com.ctc.myct.search.configuration.ReadConfig;
import com.ctc.myct.search.dto.ArticleResult;
import com.ctc.myct.search.dto.File;
import com.ctc.myct.search.dto.FileWrapperResult;
import com.ctc.myct.search.dto.PublicationResult;
import com.ctc.myct.search.mapper.FileWrapperMapper;
import com.ctc.myct.search.mapper.PublicationMapper;
import com.ctc.myct.search.model.MultiSearchResult;
import com.ctc.myct.search.model.SearchResult;
import com.liferay.document.library.kernel.service.DLAppLocalServiceUtil;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalServiceUtil;
import com.liferay.portal.kernel.exception.ResourceActionsException;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.ServiceContext;

public class MyCTLocalServiceHelperUtil {

	// fetches publication or journal articles from DB with respect to the Id
	// and types in the searchresult.
	public static void fetchContent(MultiSearchResult searchResult, ServiceContext serviceContext, Locale locale ) {		
		
		try {
			List<SearchResult> results = searchResult.getResult();
			for (SearchResult result : results) {

				if (result.getType().equalsIgnoreCase("publication")) {					

					//Init the mappers
					PublicationMapper pMapper = new PublicationMapper();	
					FileWrapperMapper fwMapper = new FileWrapperMapper();
						
					List<PublicationAttachment> publicationAttachments;
					List<FileWrapperFileEntryXRef> fileWrapperFileEntryXRefs;
						
					try {
						//Get the Publication Object from the database.
						//We need to use the remote service because that is where the permission checks are.
						Publication publication = PublicationServiceUtil.getPublication(result.getId(), serviceContext);
	
						//Build a PublicationResult using the Publication Mapper.
						PublicationResult publicationResult = pMapper.mapBOtoDTO(PublicationServiceUtil.getPublication(result.getId(), serviceContext), locale);
	
						/**
						 * Fetching FileWrappers
						 */
						publicationAttachments = PublicationAttachmentLocalServiceUtil.findByPublicationId(publication.getPublicationId());
						
						List<FileWrapperResult> fileWrappers = new LinkedList<>();
						
						for (PublicationAttachment a : publicationAttachments) {
	
							FileWrapperResult wrapper = fwMapper.mapBOtoDTO( FileWrapperLocalServiceUtil.getFileWrapper(a.getFileWrapperId()), locale);
							
							List<File> fileEntry = new LinkedList<>();
							List<Long> fileIds = new LinkedList<>();
	
							List<FileWrapperFileEntryXRef> fwfeXRefs = FileWrapperFileEntryXRefServiceUtil
									.findByFileWrapperId(a.getFileWrapperId(),serviceContext);
	
							for (FileWrapperFileEntryXRef fwfeXRef : fwfeXRefs) {
								
								FileEntry fe = DLAppLocalServiceUtil.getFileEntry(fwfeXRef.getFileEntryId());
	
								wrapper.setMimeType(fe.getExtension());
								fileIds.add(fe.getFileEntryId());
	
							}
							
							if( fileIds.size() > 0 )
							{
								wrapper.setFileIds(fileIds);
								fileWrappers.add(wrapper);
							}
						}
						
						publicationResult.setFileWrappers(fileWrappers);
	
						result.setContent((publicationResult));
					}
					catch(ResourceActionsException ex){
						continue;
					}

				} else {
					try {
						JournalArticle article = JournalArticleLocalServiceUtil.getArticle(ReadConfig.getSiteId(),
								String.valueOf(result.getId()));

						ArticleResult articleResult = ArticleResultParserUtil.getSearchResult(result.getType(),
								article.getArticleId(), serviceContext);

						if (result.getType().contentEquals("rsrt"))
							articleResult.setDate(article.getCreateDate().getTime());

						articleResult.setDescription(article.getDescription(locale));
						articleResult.setId(new Long(article.getArticleId()));

						result.setContent(articleResult);

					} catch (Exception e) {
						e.printStackTrace();
						// TODO
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			// log error TODO
		}

	}

}
