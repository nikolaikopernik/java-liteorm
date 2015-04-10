# Lite ORM #

### 1. Полная совместимость с конфигами hibernate ###
(позволяет не трогать уже созданные конфиги + паралельная работа LiteORM + Hibernate во время постепенной интеграции)
```
	public class Author {
	  private Integer id;
	  private String name;
	  private Integer birth;
	  private Boolean alive;
	  ...
```
```
    <?xml version="1.0"?>
      <hibernate-mapping package="com.test">
	<class name="Author" table="author" lazy="false">
		<id name="id" column="id">
			<generator class="native" />
		</id>
		<property name="name" />
		<property name="birth" column="birth_day" type="integer"/>
		<property name="alive" column="is_alive" type="boolean" />
	</class>
</hibernate-mapping>
```


### 2. Простота. Полная управляемость кодом. Никаких кешей и скрытых телодвижений. ###
Только
> Object --> SQL

> SQL  --> Object



### 3. Манипуляции данными ###
Создание:
```
Author author = new Author("Albert Einstein", 12, false);    
liteORM.insert(author);    

    SQL: INSERT INTO author(name,birth_day,is_alive)VALUES(?,?,?)
```
Изменение:
```
author.setBirth(144);    
liteORM.update(author);	

	SQL: UPDATE author SET name=?,birth_day=?,is_alive=? WHERE id=?
```
Удаление
```
liteORM.delete(author);    

    SQL: DELETE FROM author WHERE id=?	
```

### 4. Язык запросов ###
```
    from targetClass[,relationClass, relationClass] [where exp1] [(order by|limit) exp2]
```
targetClass - таргетовый класс, который, собственно, и загружается
relationClass - перечисление возможных ссылок с таргетого класса, для которых нужно загружать полные данные. Порядок не важен, важно наличие или отсутствие класса. Тут перечисляются не только ссылочные классы для таргетого, если есть поле вложеностью более 1, то алгоритм тот же - если есть в даннм перечислении классов класс этого поля, то он загружается полность. Реализуется все это путем построения дерева зависимостей. Лишние классы не учавствуют в выборке.
exp1 - ограничение в стиле SQL, но в терминах полей классов.Преобразуется в SQL выражение путем простой замены полей на колонки таблиц.
exp2 - аналогично exp1, переносится в sql практически без изменений

### 5. Пакетная обработка ###
Вставка:
```
List<Author> list = new LinkedList<Author>();    
list.add(new Author(...))    
...    
DB.bulkInsert(list);    

    SQL: INSERT INTO author(name,birth_day,is_alive)VALUES(?,?,?),(?,?,?),(?,?,?),...
```
Обновление:
```
DB.bulkUpdate(list);

    SQL: REPLACE INTO author (id,name,birth_day,is_alive) VALUES (?,?,?,?),(?,?,?,?),... 
```

### 6. Many-to-one отношение ###
```
    public class Book {    
      private Integer id;
      private String title;
      private Short pages;
      private String category;
      private Author author;
      ...
```
Задается в коде и в xml конфиге так же как и прежде.
Запросы:
Инициализация - только lazy (загружено будет только то, что явно указано). Например,
```
DB.select("from Book where title like ?","%Potter%"); //нет указания загружать класс Author
    SELECT book_id,title,pages,category,author_id FROM books _a50 WHERE _a50.title like ?	
```
результат - 0|GArry Potter|456|Magic|Author(4089|null|null|null) - поле author заполнилось фейковым объектом, который имеет только id
, а вот если указать явно класс Author в запросе
{{{DB.select("from Book b,Author a where b.title like ?","%Potter%");
> SELECT b.book\_id,b.title,b.pages,b.category,b.author\_id,id,name,birth\_day,is\_alive FROM books b,author a WHERE  b.author\_id=a.id AND b.title like ?
Book potter = (Book)DB.selectFirst("from Book,Author where title like ?", "%Potter%");
System.out.println(potter);
potter.getAuthor().setName("Darth Vader");
DB.update(potter);     //обновляется только книга
//если хотим обновить автора
DB.update(potter.getAuthor());
> 3|Garry Potter|342|category1|Author(4271|Joanne Rowling|45|true)
> SQL: UPDATE books SET title=?,pages=?,category=?,author\_id=? WHERE book\_id=?
> SQL: UPDATE author SET name=?,birth\_day=?,is\_alive=? WHERE id=?DB.select("from Book where title like ?","%Potter%"); //нет указания загружать класс Author
    SELECT book_id,title,pages,category,author_id FROM books _a50 WHERE _a50.title like ?	
}}}
результат - 0|GArry Potter|456|Magic|Author(4089|Joanne Rowling|45|true) - поле author загружено полностью

Добавление/изменение/удаление:    
Ввиду открытости и простоты работы ни одно из этих действий не распространяется по связям many-to-one (т.е. чтоб обновить автора книги нужно явно это сделать - через обновление книги автор ее не обновится). Пример:    
{{{
}}}

=== 7. Что еще осталось ===
1. связи one-to-many
2. подклассы (subclasses)
3. негенерируемые и составные id```