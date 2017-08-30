package com.ctc.myct.search.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.ctc.myct.bo.model.Publication;
import com.ctc.myct.search.dto.PublicationResult;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

public class PublicationMapper {
	
	private static final Log _log = LogFactoryUtil.getLog(PublicationMapper.class);
	
	public PublicationResult mapBOtoDTO(Publication bo, Locale userLocale){

		PublicationResult p = new PublicationResult(); 
		
		p.setId(bo.getPublicationId());
		p.setTitle(bo.getTitle(userLocale));
		p.setJournalArticleId(bo.getJournalArticleId());
		p.setSynopsis(bo.getSynopsis(userLocale));
		p.setPublicationDate(bo.getPublicationDate());

		return p;
	}
	
	
	public List<PublicationResult> mapBOstoDTOs(List<Publication> bos, Locale userLocale){
		
		
		List<PublicationResult> publications = new ArrayList<>();
		
		for (com.ctc.myct.bo.model.Publication curPublication : bos) {
			publications.add(mapBOtoDTO(curPublication, userLocale));
		}
		
		return publications;
	}

}
