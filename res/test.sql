
CREATE TABLE hosts (id int unsigned PRIMARY KEY AUTO_INCREMENT, host varchar(255) NOT NULL, status tinyint NOT NULL);
CREATE TABLE urls (id int unsigned PRIMARY KEY AUTO_INCREMENT, url varchar(255) NOT NULL,host_id int unsigned NOT NULL, 
  constraint fk_url_host foreign key (host_id) references hosts(id));
CREATE TABLE catalogues(cat_id int unsigned PRIMARY KEY AUTO_INCREMENT, title varchar(255) NOT NULL, status tinyint NOT NULL, parent_id int unsigned,
  constraint fk_cat_cat foreign key (parent_id) references catalogues(cat_id));
CREATE TABLE objects (
  object_id int(10) unsigned NOT NULL AUTO_INCREMENT,
  title varchar(255) NOT NULL,
  cat_id int(10) unsigned DEFAULT NULL,
  url_id int(10) unsigned NOT NULL,
  price float(10,2) NOT NULL,
  PRIMARY KEY (object_id),
  CONSTRAINT fk_obj_cat FOREIGN KEY (cat_id) REFERENCES catalogues (cat_id),
  CONSTRAINT fk_obj_url FOREIGN KEY (url_id) REFERENCES urls (id));
  
CREATE TABLE properties(property_id int unsigned PRIMARY KEY AUTO_INCREMENT, object_id int unsigned not null, name varchar(255) not null, value varchar(255) not null,
  constraint fk_prop_obj foreign key (object_id) references objects(object_id));