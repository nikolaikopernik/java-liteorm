package com.liteorm.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import com.liteorm.model.SqlQuery;

/**
 * Здесь происходят непосредственные запросы
 * TODO нужен пул коннектов, т.к. будут одновременные запросы
 * @author kopernik
 *
 */
public class SQL {
	public static final Logger logger = Logger.getLogger(SQL.class);
	private DataSource dataSource;
	private Connection connection;
	
	public SQL(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public int insert(SqlQuery query) throws SQLException{
		logger.debug("SQL: "+query.sql);
		PreparedStatement st =  getConnection().prepareStatement(query.sql);
		for(int i=0;i<query.args.length;i++){
			setParameter(i+1,st, query.args[i]);
		}
		st.executeUpdate();
		ResultSet set = st.getGeneratedKeys();
		set.first();
		return set.getInt(1);
	}
	
	public ResultSet insertBulk(SqlQuery query) throws SQLException{
		logger.debug("SQL: "+query.sql);
		PreparedStatement st = getConnection().prepareStatement(query.sql);
		for(int i=0;i<query.args.length;i++){
			setParameter(i+1,st, query.args[i]);
		}
		st.executeUpdate();
		ResultSet set = st.getGeneratedKeys();
		return set;
	}
	
	public int update(SqlQuery query) throws SQLException{
		logger.debug(query.sql);
		PreparedStatement st = getConnection().prepareStatement(query.sql);
		for(int i=0;i<query.args.length;i++){
			setParameter(i+1,st, query.args[i]);
		}
		return st.executeUpdate();
	}
	
	public ResultSet select(SqlQuery query) throws SQLException{
		logger.debug(query.sql);
		PreparedStatement st = getConnection().prepareStatement(query.sql);
		for(int i=0;i<query.args.length;i++){
			setParameter(i+1,st, query.args[i]);
		}
		return st.executeQuery();
	}
	
	
	private void setParameter(int i, PreparedStatement st, Object p) throws SQLException{
		st.setObject(i, p);
	}
	
	private Connection getConnection() throws SQLException{
		if(connection==null || connection.isClosed()){
			connection = dataSource.getConnection();
		}
		return connection;
	}
}
