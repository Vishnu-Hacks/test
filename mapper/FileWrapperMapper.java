package com.ctc.myct.search.mapper;

import java.util.Locale;

import com.ctc.myct.bo.model.FileWrapper;
import com.ctc.myct.search.dto.FileWrapperResult;

public class FileWrapperMapper {

	public FileWrapperResult mapBOtoDTO(FileWrapper bo, Locale userLocale) {
		
		FileWrapperResult fw = new FileWrapperResult(); 
		
		fw.setId(bo.getFileWrapperId());
		fw.setTitle( bo.getTitle(userLocale));
		
		return fw;
	}	
	
}
