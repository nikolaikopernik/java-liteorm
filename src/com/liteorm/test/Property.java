package com.liteorm.test;

public class Property {

	private Integer propertyId;
	private Object object;
	private String name;
	private String value;
	
	public Property() {
		// TODO Auto-generated constructor stub
	}
	
	public Property(String name, String value) {
        this.name = name;
		this.value = value;
	}
	
	public Property(String name, String value, Object object) {
        setName(name);
		this.value = value;
		this.object = object;
	}
	
	/**
	 * @return the propertyId
	 */
	public Integer getPropertyId() {
		return propertyId;
	}
	/**
	 * @param propertyId the propertyId to set
	 */
	public void setPropertyId(Integer propertyId) {
		this.propertyId = propertyId;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
    
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
    }
    
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	public Object getObject() {
		return object;
	}
	public void setObject(Object object) {
		this.object = object;
	}
}
