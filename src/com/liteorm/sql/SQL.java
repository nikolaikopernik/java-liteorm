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
	
	public int insert(SqlQuery query) throws SQLException{
		logger.debug("SQL: "+query.getSql());
		Connection connection = getConnection();
		PreparedStatement st =  connection.prepareStatement(query.getSql());
		for(int i=0;i<query.getArgs().length;i++){
			setParameter(i+1,st, query.getArgs()[i]);
		}
		st.executeUpdate();
		ResultSet set = st.getGeneratedKeys();
		releaseConnection(connection);		
		set.first();
		return set.getInt(1);
	}
	
	public SqlRowSet insertBulk(SqlQuery query) throws SQLException{
		logger.debug("SQL: "+query.getSql());
		Connection connection = getConnection();
		PreparedStatement st =  connection.prepareStatement(query.getSql());
		for(int i=0;i<query.getArgs().length;i++){
			setParameter(i+1,st, query.getArgs()[i]);
		}
		st.executeUpdate();
		ResultSet result = st.getGeneratedKeys();
		SqlRowSet set =  (SqlRowSet) new SqlRowSetResultSetExtractor().extractData(result);
		releaseConnection(connection);
		return set;
	}
	
	public int update(SqlQuery query) throws SQLException{
		logger.debug(query.getSql());
		Connection connection = getConnection();
		PreparedStatement st =  connection.prepareStatement(query.getSql());
		for(int i=0;i<query.getArgs().length;i++){
			setParameter(i+1,st, query.getArgs()[i]);
		}
		int idx = st.executeUpdate();
		releaseConnection(connection);
		return idx;
	}
	
	public SqlRowSet select(SqlQuery query) throws SQLException{
		logger.debug(query.getSql());
		Connection connection = getConnection();
		PreparedStatement st =  connection.prepareStatement(query.getSql());
		for(int i=0;i<query.getArgs().length;i++){
			setParameter(i+1,st, query.getArgs()[i]);
		}
		ResultSet result = st.executeQuery();
		SqlRowSet set =  (SqlRowSet) new SqlRowSetResultSetExtractor().extractData(result);
		releaseConnection(connection);
		return set;
	}
	
	
	private void setParameter(int i, PreparedStatement st, Object p) throws SQLException{
		st.setObject(i, p);
	}
	
	private Connection getConnection() throws SQLException{
		return DataSourceUtils.getConnection(dataSource);
	}
	
	private void releaseConnection(Connection c){
		DataSourceUtils.releaseConnection(c, dataSource);
	}
}
