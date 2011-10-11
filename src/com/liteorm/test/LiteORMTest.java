package com.liteorm.test;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

import com.liteorm.LiteORM;
import com.liteorm.LiteORMImpl;

/**
 * Схема для тестов - в папке res
 * 
 * @author kopernik
 *
 */
public class LiteORMTest extends TestCase{
	public static Logger logger = Logger.getLogger("test");
	private static String[] mapfiles = new String[]{"com/liteorm/test/res/catalogue.hbm.xml",
												"com/liteorm/test/res/host.hbm.xml",
												"com/liteorm/test/res/url.hbm.xml",
												"com/liteorm/test/res/qobject.hbm.xml",
												"com/liteorm/test/res/property.hbm.xml"};
	
	public void testLiteORM() throws Exception{
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://localhost/test");
		dataSource.setUsername("root");
		dataSource.setPassword("1111");
		
		LiteORM DB = new LiteORMImpl(mapfiles, dataSource);
		clear(dataSource);
		
		
		logger.info("** Start tests...");
		logger.info("*************************************************");
		
		simpleOperationsTest(DB);
		
		simpleBulkOperations(DB);
		
		one2manyTest(DB);
		
		logger.info("*************************************************");
		logger.info("** OK");
	}
	
	@SuppressWarnings("unchecked")
	private void simpleOperationsTest(LiteORM DB){
		logger.info("");
		logger.info("** Simple class operations...");
		logger.info("**** insert...");
		Host yandex = new Host("market.yandex.ru",(short)1);
		DB.insert(yandex);
		assertNotNull(yandex.getId());
		
		Host amazon = new Host("amazon.com",(short)1);
		DB.insert(amazon);
		assertNotNull(amazon.getId());
		
		Host ebay = new Host("ebay.com",(short)0);
		DB.insert(ebay);
		assertNotNull(amazon.getId());
		
		logger.info("**** select...");
		List<Host> hosts = DB.select("from Host");
		assertEquals(3,hosts.size());
		hosts = DB.select("from Host where status=?", 0);
		assertEquals(1,hosts.size());
		hosts = DB.select("from Host where host like ?", "%.com");
		assertEquals(2,hosts.size());
		
		logger.info("**** update...");
		yandex.setHost("yandex.ru");
		DB.update(yandex);
		hosts = DB.select("from Host where host=?", "market.yandex.ru");
		assertEquals(0,hosts.size());
		hosts = DB.select("from Host where host=?", yandex.getHost());
		assertEquals(1,hosts.size());
		assertEquals(yandex.getId(), hosts.get(0).getId());
		
		logger.info("**** delete...");
		DB.delete(ebay);
		hosts = DB.select("from Host where id=?", ebay.getId());
		assertEquals(0, hosts.size());
		hosts = DB.select("from Host where host=?", ebay.getHost());
		assertEquals(0, hosts.size());
	}
	
	@SuppressWarnings("unchecked")
	private void simpleBulkOperations(LiteORM DB){
		logger.info("");
		logger.info("** Simple bulk operations...");
		logger.info("**** bulk insert...");
		List<Host> hosts = new ArrayList<Host>(100);
		for(int i = 0;i<100;i++){
			hosts.add(new Host("host"+i+".qippo.com",(short)4));
		}
		DB.bulkInsert(hosts);
		for(Host host:hosts){
			assertNotNull("Null ID detected in simple bulk insert", host.getId());
		}
		hosts = null;
		hosts = DB.select("from Host where host like ?", "%qippo.com");
		assertEquals("Bulk saved objects count mismatch",100, hosts.size());
		
		logger.info("**** bulk update...");
		for(Host host:hosts){
			host.setHost(host.getHost()+"/incorrect");
		}
		DB.bulkUpdate(hosts);
		hosts = null;
		hosts = DB.select("from Host where host like ?", "%qippo.com");
		assertEquals("Error bulk update, some rows do not updated",0, hosts.size());
		hosts = DB.select("from Host where host like ?", "%qippo.com/incorrect");
		assertEquals("Error bulk update, some rows updated incorrect",100, hosts.size());
		
		logger.info("**** bulk delete...");
		DB.bulkDelete(hosts);
		hosts = DB.select("from Host where host like ?", "%qippo.com%");
		assertEquals("Error bulk delete, some rows not deleted",0, hosts.size());		
	}
	
	private void one2manyTest(LiteORM DB){
		logger.info("");
		logger.info("** One2many relation tests...");
		logger.info("*** insert...");
		Host yandex = new Host("market.yandex.ru",(short)1);
		DB.insert(yandex);
		Url url = new Url("http://testurl.ru", yandex);
		DB.insert(url);
		Object o = new Object("Test object", 10.99f, url);
		Set<Property> props = new HashSet<Property>();
		props.add(new Property("Picture","http://sdfsdfsdfsdfsdf"));
		props.add(new Property("Brand","Transcend"));
		props.add(new Property("Screen","15"));
		o.setProperties(props);
		DB.insert(o);
		List<Property> plist = DB.select("from Property where object=?", o.getObjectId());
		assertEquals(3, plist.size());
		
		logger.info("*** select...");
		List<Object> olist = DB.select("from Object, Property");
		assertTrue(olist.size()>0);
		Object saved = olist.get(0);
		assertNotNull(saved.getProperties());
		assertEquals(3, saved.getProperties().size());
		
		logger.info("*** bulk insert...");
		olist = new ArrayList<Object>();
		for(int i = 0; i<100;i++){
			o = new Object("Test object "+i, 10f*i, url);
			props = new HashSet<Property>();
			props.add(new Property("Picture","http://forobject-"+i));
			props.add(new Property("Brand","Transcend-"+i));
			props.add(new Property("Screen","15x"+i));
			o.setProperties(props);
			olist.add(o);
		}
		DB.bulkInsert(olist);
		plist = DB.select("from Property");
		assertTrue(plist.size()>100);
		
		logger.info("*** delete...");
		DB.delete(o);
		plist = DB.select("from Property where object=?", o.getObjectId());
		assertEquals(0, plist.size());
		
		
		
	}
	
	private void clear(DataSource ds) throws Exception{
		Connection c = ds.getConnection();
		c.prepareStatement("DELETE from catalogues").execute();
		c.prepareStatement("DELETE from properties").execute();
		c.prepareStatement("DELETE from objects").execute();
		c.prepareStatement("DELETE from urls").execute();
		c.prepareStatement("DELETE from hosts").execute();
	}

}
