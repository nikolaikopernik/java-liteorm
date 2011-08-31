package com.liteorm.test;

public class Author {
	private Integer id;
	private String name;
	private Integer birth;
	private Boolean alive;
	
	public Author() {
		// TODO Auto-generated constructor stub
	}

	public Author(String name, Integer birth, Boolean alive) {
		this.name = name;
		this.birth = birth;
		this.alive = alive;
	}

	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getBirth() {
		return birth;
	}

	public void setBirth(Integer birth) {
		this.birth = birth;
	}
	
	public Boolean getAlive() {
		return alive;
	}
	
	public void setAlive(Boolean alive) {
		this.alive = alive;
	}
	
	@Override
	public String toString() {
		return id + "|" + name + "|" + birth + "|" + alive;
	}
}
