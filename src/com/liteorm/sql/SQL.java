package com.liteorm.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.SqlRowSetResultSetExtractor;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.liteorm.query.SqlQuery;

/**
 * Здесь происходят непосредственные запросы
 * TODO нужен пул коннектов, т.к. будут одновременные запросы
 * @author kopernik
 *
 */
public class SQL {
	public static final Logger logger = Logger.getLogger(SQL.class);
	private DataSource dataSource;
	
	public SQL(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public int insert(SqlQuery query, Connection connection) throws SQLException{
		logger.debug("SQL: "+query.getSql());
		PreparedStatement st =  connection.prepareStatement(query.getSql());
		for(int i=0;i<query.getArgs().length;i++){
			setParameter(i+1,st, query.getArgs()[i]);
		}
		st.executeUpdate();
		ResultSet set = st.getGeneratedKeys();
		return set.getInt(1);
	}
	
	public ResultSet insertBulk(SqlQuery query, Connection connection) throws SQLException{
		logger.debug("SQL: "+query.getSql());
		PreparedStatement st =  connection.prepareStatement(query.getSql());
		for(int i=0;i<query.getArgs().length;i++){
			setParameter(i+1,st, query.getArgs()[i]);
		}
		st.executeUpdate();
		return st.getGeneratedKeys();
	}
	
	public int update(SqlQuery query, Connection connection) throws SQLException{
		logger.debug(query.getSql());
		PreparedStatement st =  connection.prepareStatement(query.getSql());
		for(int i=0;i<query.getArgs().length;i++){
			setParameter(i+1,st, query.getArgs()[i]);
		}
		int idx = st.executeUpdate();
		return idx;
	}
	
	public ResultSet select(SqlQuery query, Connection connection) throws SQLException{
		logger.debug(query.getSql());
		PreparedStatement st =  connection.prepareStatement(query.getSql());
		for(int i=0;i<query.getArgs().length;i++){
			setParameter(i+1,st, query.getArgs()[i]);
		}
		ResultSet result = st.executeQuery();
		return result;
	}
	
	
	private void setParameter(int i, PreparedStatement st, Object p) throws SQLException{
		st.setObject(i, p);
	}
	
	public Connection getConnection() throws SQLException{
		return DataSourceUtils.getConnection(dataSource);
	}
	
	public void releaseConnection(Connection c){
		DataSourceUtils.releaseConnection(c, dataSource);
	}
}
