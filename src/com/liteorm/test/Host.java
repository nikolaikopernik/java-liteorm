package com.liteorm.test;

public class Host {
	private Integer id;
	private String host;
	private Short status;
	
	public Host() {
		super();
	}

	public Host(final String host, final Short status) {
		this.host = host;
		this.status = status;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Short getStatus() {
		return status;
	}

	public void setStatus(Short status) {
		this.status = status;
	}																	
}