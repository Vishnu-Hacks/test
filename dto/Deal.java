package com.ctc.myct.search.dto;

public class Deal extends BaseDTO {
	
	private long dealId;
	private int number;
	private int dealYear;
	private String name;
	
	public long getDealId() {
		return dealId;
	}

	public void setDealId(long dealId) {
		this.dealId = dealId;
	}
	
	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}
	
	public int getDealYear() {
		return dealYear;
	}

	public void setDealYear(int dealYear) {
		this.dealYear = dealYear;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
