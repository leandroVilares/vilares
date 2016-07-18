package com.ml.logistica.romaneio.dao;

import java.util.Calendar;

public class PackingListFilter {
	/**
	 * Id Romaneio
	 */
	private Long Id = null;
	/**
	 * Data romaneio
	 */
	private Calendar startDate = null;
	private Calendar endDate = null;
	// --

	/**
	 * Filial
	 */
	private Long branch = null;

	public PackingListFilter() {
		super();
	}

	public Long getId() {
		return Id;
	}

	public void setId(Long id) {
		Id = id;
	}

	public Calendar getStartDate() {
		return startDate;
	}

	public void setStartDate(Calendar startDate) {
		this.startDate = startDate;
	}

	public Calendar getEndDate() {
		return endDate;
	}

	public void setEndDate(Calendar endDate) {
		this.endDate = endDate;
	}

	public Long getBranch() {
		return branch;
	}

	public void setBranch(Long branch) {
		this.branch = branch;
	}

	@Override
	public String toString() {
		return "PackingListFilter [Id=" + Id + ", startDate=" + (startDate == null ? null : startDate.getTime())
				+ ", endDate=" + (endDate == null ? null : endDate.getTime()) + ", branch=" + branch + "]";
	}

}
