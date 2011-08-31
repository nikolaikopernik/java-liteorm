package com.liteorm.test;


/**
 * Catalogue container. Catalogue uses "nested set" structure for storing
 * elements
 */
public class Catalogue {
	private Integer catId;
	private String title;
	private Catalogue parent;
	private Short 	status;

	public Catalogue() {

	}

	public Catalogue(String title) {
		this.title = title;
	}
	
	public Catalogue(final String title, Short status) {
		this.title = title;
		this.status = status;
	}

	public Integer getCatId() {
		return catId;
	}

	public void setCatId(Integer catId) {
		this.catId = catId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Catalogue getParent() {
		return parent;
	}

	public void setParent(Catalogue parent) {
		this.parent = parent;
	}

	public Short getStatus() {
		return status;
	}

	public void setStatus(Short status) {
		this.status = status;
	}	
}
