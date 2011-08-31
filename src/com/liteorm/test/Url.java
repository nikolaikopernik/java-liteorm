package com.liteorm.test;

public class Url {
	private Integer id;
	private Host host;
	private String url;

	public Url() {

	}
	
	public Url(String url, Host host) {
		this.url = url;
		this.host = host;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Host getHost() {
		return host;
	}

	public void setHost(Host host) {
		this.host = host;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
