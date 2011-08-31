package com.liteorm.test;

import java.util.Set;

/**
 * Object container.
 */
public class Object {
	private Integer objectId = null;
	private String title;
	private Catalogue catalogue;
	private Url url;
    private Float price;
    
    private Set<Property> properties;

	public Object() {
	}

	public Object(String title,Float price, Url url) {
		this.title = title;
		this.price = price;
		this.url = url;
	}

	public Integer getObjectId() {
		return objectId;
	}

	public void setObjectId(Integer objectId) {
		this.objectId = objectId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Catalogue getCatalogue() {
		return catalogue;
	}

	public void setCatalogue(Catalogue catalogue) {
		this.catalogue = catalogue;
	}

	public Url getUrl() {
		return url;
	}

	public void setUrl(Url url) {
		this.url = url;
	}

	public Float getPrice() {
		return price;
	}

	public void setPrice(Float price) {
		this.price = price;
	}
	
	public Set<Property> getProperties() {
		return properties;
	}
	
	public void setProperties(Set<Property> properties) {
		this.properties = properties;
	}
	
}
