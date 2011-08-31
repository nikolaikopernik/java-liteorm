package com.liteorm.test;
/**
 * +---------+--------------+-------+----------+-----------+
|       0 | GArry Potter |   456 | Magic    |      4089 |
|       1 | E=MC^2       |   300 | Physics  |      4088 |
|       2 | Tuf Voyaging |  8590 | Fantasy  |      4093 |
 * @author kopernik
 *
 */
public class Book {
	private Integer id;
	private String title;
	private Short pages;
	private String category;
	private Author author;
	
	public Book() {
		// TODO Auto-generated constructor stub
	}
	
	public Book(String title, Short pages, String category, Author author) {
		this.title = title;
		this.pages = pages;
		this.category = category;
		this.author = author;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Short getPages() {
		return pages;
	}

	public void setPages(Short pages) {
		this.pages = pages;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Author getAuthor() {
		return author;
	}

	public void setAuthor(Author author) {
		this.author = author;
	}
	
	@Override
	public String toString() {
		return id+"|"+title+"|"+pages+"|"+category+"|"+"Author("+author+")";
	}
	
}
