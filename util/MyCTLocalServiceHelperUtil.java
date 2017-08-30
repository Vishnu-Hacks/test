package com.ctc.myct.search.util;

import java.util.LinkedList;
import java.util.List;

import com.ctc.myct.bo.model.Deal;
import com.ctc.myct.bo.model.FileWrapper;
import com.ctc.myct.bo.model.FileWrapperFileEntryXRef;
import com.ctc.myct.bo.model.Publication;
import com.ctc.myct.bo.model.PublicationAttachment;
import com.ctc.myct.bo.service.DealPublicationLocalServiceUtil;
import com.ctc.myct.bo.service.FileWrapperFileEntryXRefLocalServiceUtil;
import com.ctc.myct.bo.service.FileWrapperLocalServiceUtil;
import com.ctc.myct.bo.service.PublicationAttachmentLocalServiceUtil;
import com.ctc.myct.bo.service.PublicationLocalServiceUtil;
import com.ctc.myct.search.configuration.ReadConfig;
import com.ctc.myct.search.dto.ArticleResult;
import com.ctc.myct.search.dto.File;
import com.ctc.myct.search.dto.FileWrapperResult;
import com.ctc.myct.search.dto.PublicationResult;
import com.ctc.myct.search.model.MultiSearchResult;
import com.ctc.myct.search.model.SearchResult;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalServiceUtil;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.service.ResourcePermissionLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;

public class MyCTLocalServiceHelperUtil {

	// fetches publication or journal articles from DB with respect to the Id
	// and types in the searchresult.
	public static void fetchContent(MultiSearchResult searchResult, ServiceContext serviceContext) {

		try {
			List<SearchResult> results = searchResult.getResult();
			for (SearchResult result : results) {

				if (result.getType().equalsIgnoreCase("publication")) {
					// fetching publication
					Publication publication = PublicationLocalServiceUtil.fetchPublication(result.getId());

					PublicationResult publicationResult = new PublicationResult();
					publicationResult.setAdditionalInformation(publication.getAdditionalInformation());
					publicationResult.setFirstName(publication.getFirstName());
					publicationResult.setId(publication.getPublicationId());
					publicationResult.setJournalArticleId(publication.getJournalArticleId());
					publicationResult.setLastName(publication.getLastName());
					publicationResult.setPublicationDate(publication.getPublicationDate());
					publicationResult.setTitle(publication.getTitle(serviceContext.getLocale()));
					publicationResult.setTitleMap(publication.getTitleMap());
					publicationResult.setSynopsis(publication.getSynopsis(serviceContext.getLocale()));
					publicationResult.setSectionId(publication.getSectionId());

					/**
					 * fetching role ID for the publication .:.A Publication has
					 * only one role.
					 */
					List<Role> publicationRoles = ResourcePermissionLocalServiceUtil.getRoles(
							serviceContext.getCompanyId(), Publication.class.getName(),
							ResourceConstants.SCOPE_INDIVIDUAL, String.valueOf(publication.getPublicationId()),
							ActionKeys.VIEW);
					publicationResult.setRoleId(publicationRoles.get(0).getRoleId());

					/**
					 * Fetching FileWrappers
					 */
					List<PublicationAttachment> attachments = PublicationAttachmentLocalServiceUtil
							.findByPublicationId(publication.getPublicationId());
					List<FileWrapperResult> fileWrappers = new LinkedList<>();
					for (PublicationAttachment a : attachments) {

						FileWrapper wrapper = FileWrapperLocalServiceUtil.getFileWrapper(a.getFileWrapperId());
						FileWrapperResult fileWrapper = new FileWrapperResult();
						List<File> fileEntry = new LinkedList<>();
						List<Long> fileIds = new LinkedList<>();

						List<FileWrapperFileEntryXRef> files = FileWrapperFileEntryXRefLocalServiceUtil
								.findByFileWrapperId(a.getFileWrapperId());

						for (FileWrapperFileEntryXRef f : files) {

							File file = new File();
							file.setId(f.getFileEntryId());
							file.setLanguage(f.getLanguage());
							file.setOrganizationId(f.getOrganizationId());

							fileEntry.add(file);
							fileIds.add(file.getId());

						}
						fileWrapper.setFiles(fileEntry);
						fileWrapper.setFileIds(fileIds);
						fileWrapper.setId(wrapper.getFileWrapperId());
						fileWrapper.setTitle(wrapper.getTitle(serviceContext.getLocale()));
						fileWrapper.setTitleMap(wrapper.getTitleMap());

						/**
						 * Values missing for FileWrapperResult DTO // TODO
						 */
						fileWrapper.setInternalOnly(false);
						fileWrapper.setMimeType(null);
						fileWrapper.setOrganizationId(0);
						fileWrapper.setStoreOnly(false);

						fileWrappers.add(fileWrapper);
					}
					publicationResult.setFileWrappers(fileWrappers);

					/**
					 * Fetching Deals 
					 */

					List<Deal> deals = DealPublicationLocalServiceUtil
							.findAssociatedDeals(publication.getPublicationId());
					
					long[] dealValue = new long[deals.size()];
					int i = 0;
					for (Deal d : deals) {
						dealValue[i] = d.getDealId();
						++i;
					}
					publicationResult.setAssociatedDeals(dealValue);
					publicationResult.setAssociatedDealObjects(deals.toArray(null));

					/**
					 * Values missing in publication model TODO
					 */
					publicationResult.setCriticalDuration(0);
					publicationResult.setKeywords(null);
					publicationResult.setIsCritical(false);
					publicationResult.setLinkedPublications(null);

					result.setContent((publicationResult));

				} else {
					try {
						JournalArticle article = JournalArticleLocalServiceUtil.getArticle(ReadConfig.getSiteId(),
								String.valueOf(result.getId()));

						ArticleResult articleResult = ArticleResultParserUtil.getSearchResult(result.getType(),
								article.getArticleId(), serviceContext);

						if (result.getType().contentEquals("rsrt"))
							articleResult.setDate(article.getCreateDate().getTime());

						articleResult.setDescription(article.getDescription(serviceContext.getLocale()));
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
