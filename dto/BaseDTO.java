package com.ctc.myct.search.dto;

/*import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;*/

//@XmlRootElement(name="BaseDTO")
//@JsonRootName(value="BaseDTO")
public class BaseDTO {

	// @JsonProperty("id")
	// @JsonView(Views.Dealer.class)
	private long id;

	public long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
