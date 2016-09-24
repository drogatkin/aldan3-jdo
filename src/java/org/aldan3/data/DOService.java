/* aldan3 - DOService.java
 * Copyright (C) 1999-2009 Dmitriy Rogatkin.  All rights reserved.
 *  $Id: DOService.java,v 1.54 2014/02/01 09:22:55 cvs Exp $                
 *  Created on Jun 8, 2009
 *  @author Dmitriy R
 */
package org.aldan3.data;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import javax.sql.DataSource;

import org.aldan3.data.util.SimpleField;
import org.aldan3.model.DOFactory;
import org.aldan3.model.DataObject;
import org.aldan3.model.Log;
import org.aldan3.model.ProcessException;
import org.aldan3.model.ServiceProvider;
import org.aldan3.model.Field;
import org.aldan3.util.Sql;

public class DOService implements ServiceProvider<DOService> {
	// TODO add batch operations
	public static final String NAME = "DOService";

	DataSource dataSource;

	public DOService(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/** Retrieves an object from storage with fields as in parameter
	 *  A retrieved object will have fields filled which were empty in request object.
	 *  Only one matching object is expected, otherwise an exception will be thrown.
	 * @param dataObject used as request and response storage
	 * @return the parameter data object with filled fields
	 * @throws ProcessException
	 */
	public DataObject getObjectLike(final DataObject dataObject) throws ProcessException {
		StringBuffer q = new StringBuffer("select ");
		StringBuffer wc = new StringBuffer();
		makeQueryParts(dataObject, q, wc);
		Connection con = null;
		Statement stm = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			stm = con.createStatement();
			rs = stm.executeQuery(q.append(" from ").append(dataObject.getName()).append(wc).toString());
			if (rs.next()) {
				DataObject result = Sql.createDO(rs, dataObject instanceof DOFactory ? (DOFactory) dataObject
						: new DOFactory() {
							@Override
							public DataObject create() {
								return dataObject;
							}
						});
				if (rs.next())
					throw new ProcessException("Query: " + q + " returned multiple objects like " + dataObject);
				//Log.l.log(Log.ERROR, "", "q: %s = ", null, q);
				return result;
			}
			return null;
		} catch (SQLException e) {
			Log.l.log(Log.ERROR, "", "q: %s = ", e, q);
			throw new ProcessException("SQL exception happened in " + q, e);
		} catch (IllegalArgumentException iae) {
			throw new ProcessException("Can't obtain a connection", iae);
		} finally {
			release(con, stm, rs);
			//System.err.println("q:"+q);
		}
	}

	public DataObject getObjectLikeWhenExists(final DataObject dataObject, DataObject existObj) throws ProcessException {
		StringBuffer q = new StringBuffer("select ");
		StringBuffer wc = new StringBuffer();
		makeQueryParts(dataObject, q, wc);
		StringBuffer ewc = new StringBuffer(" and exists (select 1 from ").append(existObj.getName());
	
		makeQueryParts(existObj, new StringBuffer(), ewc);
		ewc.append(')');
		Connection con = null;
		Statement stm = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			stm = con.createStatement();
			rs = stm.executeQuery(q.append(" from ").append(dataObject.getName()).append(wc).append(ewc).toString());
			//System.err.println("q:"+q);
			if (rs.next()) {
				DataObject result = Sql.createDO(rs, dataObject instanceof DOFactory ? (DOFactory) dataObject
						: new DOFactory() {
							@Override
							public DataObject create() {
								return dataObject;
							}
						});
				if (rs.next())
					throw new ProcessException("Query: " + q + " returned multiple objects like " + dataObject);
				return result;
			}
			return null;
		} catch (SQLException e) {
			Log.l.log(Log.ERROR, "", "q: %s = ", e, q);
			throw new ProcessException("SQL exception happened in " + q, e);
		} catch (IllegalArgumentException iae) {
			throw new ProcessException("Can't obtain a connection", iae);
		} finally {
			release(con, stm, rs);
		}
	}
	
	public DataObject getObjectByQuery(String query, DOFactory doFactory) throws ProcessException {
		Connection con = null;
		Statement stm = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			stm = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			rs = stm.executeQuery(query);
			if (rs.next()) {
				DataObject result = Sql.createDO(rs, doFactory);
				if (rs.next())
					throw new ProcessException("Multiple objects matching the query " + query);
				return result;
			} //System.err.printf("No records for %s, %s%n",  query, con);
			return null;
		} catch (SQLException e) {
			Log.l.log(Log.ERROR, "", "q: %s = ", e, query);
			throw new ProcessException("SQL exception happened in " + query, e);
		} catch (IllegalArgumentException iae) {
			throw new ProcessException("Can't obtain a connection", iae);
		} finally {
			release(con, stm, rs);
		}
	}

	public Collection<DataObject> getObjectsNotLike(DataObject dataObject, long from, int size) throws ProcessException {
		return getObjectsLike(dataObject, from, size, true, null);
	}

	public Collection<DataObject> getObjectsByQuery(String query, long from, int size) throws ProcessException {
		return getObjectsByQuery(query, from, size, null);
	}

	/** read data objects 
	 * 
	 * @param query
	 * @param from
	 * @param size
	 * @param doFactory
	 * @return
	 * @throws ProcessException
	 */
	public <D extends DataObject> Collection<D> getObjectsByQuery(String query, long from, int size,
			DOFactory<D> doFactory) throws ProcessException {
		Connection con = null;
		Statement stm = null;
		ResultSet rs = null;
		ArrayList<D> result = new ArrayList<D>(size > 0 ? size : 10);
		try {
			con = getConnection();
			stm = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			if (size > 0)
				stm.setMaxRows(size); // TODO fetch size
			String limit = getLimit(from, size);
			if (limit != null && limit.length() > 0)
				rs = stm.executeQuery(query + limit);
			else {
				rs = stm.executeQuery(query);
				if (from != 0)
					scrollTo(rs, from);
			}
			while (rs.next()) {
				result.add(Sql.createDO(rs, doFactory));
			}
			return result;
		} catch (SQLException e) {
			Log.l.log(Log.ERROR, "", "q: %s = ", e, query);
			throw new ProcessException("SQL exception happened in " + query, e);
		} catch (IllegalArgumentException iae) {
			throw new ProcessException("Can't obtain a connection", iae);
		} finally {
			release(con, stm, rs);
		}
	}

	public <D extends DataObject> Collection<D> getObjectsLike(D dataObject, long from, int size)
			throws ProcessException {
		return getObjectsLike(dataObject, from, size, false, null);
	}

	/** queries objects by an object prototype
	 * 
	 * @param dataObject
	 * @param from
	 * @param size
	 * @param inverse
	 * @param factory for objects creation
	 * @return
	 * @throws ProcessException
	 */
	public <D extends DataObject> Collection<D> getObjectsLike(D dataObject, long from, int size, boolean inverse,
			DOFactory<D> factory) throws ProcessException {
		StringBuffer q = new StringBuffer("select ");
		StringBuffer wc = new StringBuffer(128);
		makeQueryParts(dataObject, q, wc, inverse);
		return getObjectsByQuery(q.append(" from ").append(dataObject.getName()).append(wc).toString(), from, size,
				factory != null ? factory : dataObject instanceof DOFactory ? (DOFactory) dataObject : null);
	}

	/** creates persistent storage (table) based on data object definition
	 * 
	 * @param dataObject
	 * @throws ProcessException
	 */
	public void createStorageFor(DataObject dataObject) throws ProcessException {
		createStorageFor(dataObject.getName(), dataObject.getFields());
	}

	/** Creates a table based on fields description
	 * 
	 * @param name
	 * @param fields
	 * @throws ProcessException
	 */
	// TODO figure out how to pass simply class, like createStorageFor(Class<?> object 
	// TODO make sure adding some default size for varchar, since without size isn't allowed
	// TODO double has to be defined with precision
	public void createStorageFor(String name, Set<Field> fields) throws ProcessException {
		if (name == null || name.length() == 0 || fields == null || fields.size() == 0)
			throw new IllegalArgumentException("Null or empty parameters are specified");
		StringBuffer q = new StringBuffer(512);
		HashSet<String> indexCols = isCreateIndex()?new HashSet<String>():null;
		q.append("create table IF NOT EXISTS ").append(name).append(" (");
		boolean first = true;
		StringBuffer c = new StringBuffer(256);
		for (Field f : fields) {
			if (first)
				first = false;
			else
				q.append(", ");
			if (f.isKey())
				c.append(", primary key(").append(f.getStoredName()).append(')');
			else if (f.isIndex() && !inlineConstraints()) {
				if (indexCols == null)
					c.append(", index(").append(f.getStoredName()).append(')');
				else
					indexCols.add(f.getStoredName());
			}
			if (f.isUnique() && !inlineConstraints())
				c.append(", UNIQUE(").append(f.getStoredName()).append(')');
			
			if (f.getSql() != null && f.getSql().length() > 0)
				q.append(f.getSql());
			else {
				q.append(f.getStoredName()).append(' ').append(f.getType());
				if (f.getSize() > 0 && f.autoIncremented() < 1) {
					q.append('(').append(f.getSize());
					int prec = f.getPrecision();
					if (prec > 0)
						q.append(',').append(prec);
					q.append(')');
				}
				if (inlineConstraints()) {
					if (f.isUnique())
						q.append(" UNIQUE");
				}
			}
		}
		// TODO add constraints, check foreign, primary, key, unique
		if (c.length() > 0)
			q.append(c);
		q.append(")");
		//System.err.printf("sql:%s%n", q);
		if (updateQuery(q.toString()) < 0)
			throw new ProcessException("Storage " + name + " has not been created.\n" + q);
		if (indexCols != null && indexCols.size() > 0) {
			q.setLength(0);
			q.append("CREATE INDEX IF NOT EXISTS ").append("IDX_").append(name).append(" ON ").append(name).append('(');
			Iterator<String> it = indexCols.iterator();
			if (it.hasNext()) 
				q.append(it.next());
			while(it.hasNext())
				q.append(',').append(it.next());
			q.append(')');
			if (updateQuery(q.toString()) < 0)
				throw new ProcessException("Index IDX_" + name + " has not been created.\n" + q);
		}
	}
	
	/** Modifies storage to possible description change
	 * It generates and execute ALTER statements
	 * @param name
	 * @param fields new fields set for existing storage
	 * @throws ProcessException
	 */
	public void modifyStorageFor(String name, Set<Field> fields) throws ProcessException {
		Set<Field> currentFields = getStorageDescription(name);
		StringBuilder alterAdd = null;
		StringBuilder alterModif = null;
		StringBuilder alterDrop = null;
		if (currentFields.size() == 0) { // no storage
			//System.err.printf("No description found for %s%n", name);
			createStorageFor(name, fields);
			return;
		}
		// TODO ALTER seems DB vendor specific, some configurability is required
		for (Field f : fields) {
			Field cf = getFieldByName(normalizeElementName(f.getName()), currentFields);
			if (cf == null) {
				if (alterAdd == null) {
					alterAdd = new StringBuilder("ALTER TABLE ").append(name).append(" ADD (");
				} else
					alterAdd.append(", ");
				try {
					appendFieldDescription(alterAdd, f);
				} catch (IOException e) {
					
				}
			} else {
				if(cf.getSize() != f.getSize()) {
					if(!("varchar".equals(cf.getType()) && "varchar".equals(f.getType())))
						continue;	
					if (alterModif == null)
						alterModif = new StringBuilder("ALTER TABLE ").append(name).append(" MODIFY ");
					else
						alterModif.append(", ");
					try {
						appendFieldDescription(alterModif, f);
					} catch (IOException e) {
						
					}
                } 
			}
		}
		if (alterAdd != null)
			alterAdd.append(")");
		for (Field f : currentFields) {
			if (getFieldByNameNormalized(f.getName(), fields) == null) {
				if (alterDrop == null)
					alterDrop = new StringBuilder("ALTER TABLE ").append(name).append(" DROP COLUMN ");
				else
					alterDrop.append(", "); // TODO consider adding DROP COLUMN for each elements since syntax inconsistent
				alterDrop.append(normalizeElementName(f.getName()));
			}
		}
		//System.err.printf("Alter %s, modif %s, drop %s%n", alterAdd, alterModif, alterDrop);
		if (alterModif != null)
			if (updateQuery(alterModif.toString()) < 0)
				throw new ProcessException("Alter modify  for " + name + " failed\n" + alterModif);
		if (alterAdd != null)
			if (updateQuery(alterAdd.toString()) < 0)
				throw new ProcessException("Alter add for " + name + " failed\n" + alterAdd);
		if (alterDrop != null)
			if (updateQuery(alterDrop.toString()) < 0)
				throw new ProcessException("Alter drop for " + name + " failed\n" + alterDrop);
	}
	
	protected  Field getFieldByName(String name, Set<Field> fields) {
		// TODO analyze if stored name has to e used
		//System.err.printf("Looking for %s%n", name);
		for(Field f: fields) { //f.getStoredName()
			//System.err.printf("Comparing to %s%n", f.getName());
			if (name.equals(f.getName()))
				return f;
		}
		return null;
	}
	
	protected  Field getFieldByNameNormalized(String name, Set<Field> fields) {
		// TODO analyze if stored name has to e used
		//System.err.printf("Looking for %s%n", name);
		for(Field f: fields) { //f.getStoredName()
			//System.err.printf("Comparing to %s%n", f.getName());
			if (name.equals(normalizeElementName(f.getName())))
				return f;
		}
		return null;
	}
	
	protected Appendable appendFieldDescription(Appendable a, Field f) throws IOException {
		if (f.getType() == null)
			throw new IllegalArgumentException("Can't resolve field type for "+f.getStoredName());
		a.append(f.getStoredName()).append(' ').append(f.getType());
		if (f.getSize() > 0) {
			a.append('(').append(String.valueOf(f.getSize()));
			int prec = f.getPrecision();
			if (prec > 0)
				a.append(',').append(String.valueOf(prec));
			a.append(')');
		}
		return a;
	}
	
	/** Retrieves current storage fields description
	 * 
	 * @param name
	 * @return
	 * @throws ProcessException
	 */
	public Set<Field> getStorageDescription(String name) throws ProcessException {
		Connection con = null;
		DatabaseMetaData  dmd = null;
		ResultSet rs = null;
		HashSet<Field> result = new HashSet<Field>();
		try {
			con = getConnection();
			dmd = con.getMetaData();
			rs = dmd.getColumns(null, null, normalizeElementName(name), null);
			//System.err.printf("requested for table %s %s %s%n",  name, con.getCatalog(), con.getSchema());
			while(rs.next()) {				
				result.add(SimpleField.create(rs.getString("COLUMN_NAME"), rs.getString("TYPE_NAME"), 
						rs.getInt("COLUMN_SIZE"), rs.getInt("DECIMAL_DIGITS"), null, false, false, false, "YES".equals(rs.getString("IS_AUTOINCREMENT"))?1:0, rs.getInt("DATA_TYPE")));
			}
		} catch(SQLException se) {
			throw new ProcessException("An exception at column description retrieval", se);
		} finally {
			release(con, /*dmd,*/ rs);
		}
		return result;
	}
	
	/** retrieves all available individual storages in provided scope
	 * 
	 * @param schema - specifies scope
	 * @return a collection of storages definitions as name and type (TABLE_NAME and TABLE_TYPE for SQL storages), or null if nothing found
	 * @throws ProcessException if a problem encountered durign the operation
	 */
	public Collection<DataObject> getAllStorages(String schema) throws ProcessException {
		Connection con = null;
		DatabaseMetaData  dmd = null;
		ResultSet rs = null;
		Collection<DataObject> result = null;
		try {
			con = getConnection();
			dmd = con.getMetaData();
			rs = dmd.getTables(null, normalizeElementName(schema), "%", null);
			if (rs.next()) {
				result = new ArrayList<DataObject>();
				result.add(Sql.createDO(rs, null));
				while(rs.next()) {
					result.add(Sql.createDO(rs, null));
				}
			}
		} catch(SQLException se) {
			throw new ProcessException("An exception at column description retrieval", se);
		} finally {
			release(con, /*dmd,*/ rs);
		}
		return result;
		
	}
	
	/** updates records specified by keys part to values of rest filled fields
	 * 
	 * @param dataObjectWithKeys
	 * @return
	 * @throws ProcessException
	 */
	public int updateObjectsLike(DataObject dataObjectWithKeys) throws ProcessException {
		return updateObjectsLike(dataObjectWithKeys, false);
	}
	
	public int updateObjectsNotLike(DataObject dataObjectWithKeys) throws ProcessException {
		return updateObjectsLike(dataObjectWithKeys, true);
	}
	
	public int updateObjectsLike(DataObject dataObjectWithKeys, boolean invert) throws ProcessException {
		// TODO implement using prepared statement
		StringBuffer q = new StringBuffer("");
		StringBuffer wc = new StringBuffer(128);
		q.append("update ").append(dataObjectWithKeys.getName()).append(" set ");
		Set<Field> fields = makeUpdateQueryParts(dataObjectWithKeys, q, wc, invert);
		q.append(wc);
		Connection con = null;
		PreparedStatement stm = null;
		try {
			con = getConnection();
			System.err.printf("Prepared q: %s%n", q);
			stm = con.prepareStatement(q.toString());
			int c = 1;
			for (Field f : fields) {
				String name = f.getName();
				Object obj = dataObjectWithKeys.get(name);
				// TODO rise an exception if not singular
				if (dataObjectWithKeys.isOperational(name)) {
					String sql = f.getSql();
					if (sql != null && sql.length() > 0)
						continue;
					if (obj == null)
						stm.setNull(c++, f.getJDBCType());
					else
						stm.setObject(c++, Sql.toPreparedSqlValue(obj));
				} else {
					if (obj == null)
						stm.setNull(c++, f.getJDBCType());
					else
						stm.setObject(c++, Sql.toPreparedSqlValue(obj));
				}
			}
			return stm.executeUpdate();
		} catch (Exception e) {
			Log.l.log(Log.ERROR, "", "q: %s = ", e, q);
			throw new ProcessException("SQL exception happened in " + q, e);
		} finally {
			release(con, stm);
		}
	}

	/** updates table records not matching pattern object by data object
	 * 
	 * @param pattern
	 * @param dataObject
	 * @return number of updated records
	 * @throws ProcessException
	 */
	public int updateObjectsNotLike(DataObject pattern, DataObject dataObject) throws ProcessException {
		return updateObjectsLike(pattern, dataObject, true);
	}

	/** updates table records matching pattern object by data object
	 * 
	 * @param pattern
	 * @param dataObject
	 * @return number of updated records
	 * @throws ProcessException
	 */
	public int updateObjectsLike(DataObject pattern, DataObject dataObject) throws ProcessException {
		return updateObjectsLike(pattern, dataObject, false);
	}

	/** updates table records matching pattern object by data object
	 * 
	 * @param pattern
	 * @param dataObject
	 * @param match/mismatch flag
	 * @return number of updated records
	 * @throws ProcessException
	 */
	public int updateObjectsLike(DataObject pattern, DataObject dataObject, boolean invert) throws ProcessException {
		StringBuffer q = new StringBuffer("");
		StringBuffer wc = new StringBuffer(128);
		makeQueryParts(pattern, q, wc, invert);
		q.setLength(0);
		q.append("update ").append(dataObject.getName()).append(" set ");
		fillKeyValuePairs(dataObject, q);
		//System.err.printf("Update:%s >< %s%n", q, wc);
		q.append(wc);
		return updateQuery(q.toString());
	}

	/** adds object unless it does exist. Keys (mean) are not included.
	 * 
	 * @param dataObject
	 * @param existObject
	 * @param keys
	 * @return
	 * @throws ProcessException
	 */
	public int addObject(DataObject dataObject, DataObject existObject, String keys) throws ProcessException {
		// TODO add retrieving keys
		// TODO test for H2 and Oracle
		/*
		 * 
		 * INSERT INTO table_listnames (name, address, tele)
		SELECT * FROM (SELECT 'Rupert', 'Somewhere', '022') AS tmp
		WHERE NOT EXISTS (
		SELECT name FROM table_listnames WHERE name = 'Rupert'
		) LIMIT 1;
		 */
		StringBuffer i = new StringBuffer(512);
		StringBuffer d = new StringBuffer(512);
		StringBuffer s = new StringBuffer(512);
		StringBuffer w = new StringBuffer(512);
		Set<Field> fields = dataObject.getFields();
		i.append("INSERT INTO ").append(dataObject.getName()).append(" (");
		d.append(") SELECT * FROM (SELECT ");
		s.append(") AS tmp WHERE NOT EXISTS (SELECT ");
		w.append(" FROM ").append(dataObject.getName()).append(" WHERE ");
		boolean first = true;
		for (Field f : fields) {
			if (/*dataObject.meanFieldFilter(f.getName()) ||*/ f.autoIncremented()!=0) // excluding keys (mean)
				continue;
			if (first)
				first = false;
			else {
				i.append(", ");
				d.append(", ");
			}
			if (f.getSql() != null && f.getSql().length() > 0)
				d.append(f.getSql());
			else
				d.append(Sql.toSqlString(dataObject.get(f.getName()), getInlineDatePattern())).append(" AS ")
						.append(f.getStoredName());
			i.append(f.getStoredName());
		}
		if (first)
			throw new ProcessException("No fields to add");
		fields = existObject.getFields();
		first = true;
		for (Field f : fields) {
			if (dataObject.isOperational(f.getName()) == false) // processing keys
				continue;
			if (first)
				first = false;
			else {
				s.append(", ");
				w.append(" AND ");
			}
			String sql = existObject.getSql(f);
			if (sql != null && sql.length() > 0)
				w.append(sql);
			else
				w.append(f.getStoredName()).append('=')
						.append(Sql.toSqlString(dataObject.get(f.getName()), getInlineDatePattern()));
			s.append(f.getStoredName());
		}
		if (first)
			throw new ProcessException("No exists fields");
		i.append(d).append(s).append(w).append(") "); 
		String limit = getLimit(0, 1);
		if (limit != null)
			i.append(limit);
		System.err.println("add if not exists:" + i);
		if (keys == null || keys.isEmpty())
			return updateQuery(i.toString());
		else
			throw new ProcessException("Retrieving keys isn't implemented");
	}
	
	/** adds object unconditionally and no auto generated keys requested. 
	 * All fields have to be marked as keys (mean)
	 *  
	 * @param dataObject
	 * @return
	 * @throws ProcessException 
	 */
	public int addObject(DataObject dataObject) throws ProcessException {
		StringBuffer q = new StringBuffer(512);
		StringBuffer v = new StringBuffer(512);
		Set<Field> fields = dataObject.getFields();
		q.append("insert into ").append(dataObject.getName()).append(" (");
		boolean first = true;
		for (Field f : fields) {
			if ( f.autoIncremented()!=0 /*dataObject.meanFieldFilter(f.getName()) == false*/)
				continue;
			if (first)
				first = false;
			else {
				q.append(", ");
				v.append(", ");
			}
			if (f.getSql() != null && f.getSql().length() > 0)
				v.append(f.getSql());
			else
				v.append(Sql.toSqlString(dataObject.get(f.getName()), getInlineDatePattern()));
			q.append(f.getStoredName());
		}
		if (first)
			throw new ProcessException("No operational fields to add");
		q.append(") select ").append(v).append(getSelectValuesTable());
		//System.err.println("Added:"+q);
		return updateQuery(q.toString());
	}

	/** adds an object and return generated auto keys
	 * 
	 * @param dataObject
	 * @param keys
	 * @return
	 * @throws ProcessException
	 */
	public int addObject(DataObject dataObject, String keys) throws ProcessException {
		return addObject(dataObject, keys, null);
	}
	
	static private String start_ius [] = {"insert into ", "MERGE INTO ", "MERGE INTO "};
	/** Insert records in table and retrieves auto generated keys back, it can also
	 * setup on duplicate update, when uniqueness constraints violation happens
	 * it is covered by merge into for some databases
	 * @param dataObject to insert
	 * @param keys to retrieve back
	 * @param updateObject used for update if duplicated condition
	 * @return number of inserted or updated records
	 * @throws ProcessException
	 */
	public int addObject(DataObject dataObject, String keys, DataObject updateObject) throws ProcessException {
		StringBuffer q = new StringBuffer(512);
		int var = getInsertUpdateVariant();
		q.append(updateObject==null?start_ius[0]:start_ius[var]).append(dataObject.getName()).append(" (");
		StringBuffer v = new StringBuffer(512);
		Set<Field> fields = dataObject.getFields();
		boolean first = true;
		LinkedList<Field> addedKeySet = null;
		for (Field f : fields) {
			// excluding keys, if auto incremented
			// TODO the exclusion of keys is db depend
			if (dataObject.isOperational(f.getName()) == true /*&& var != 0*/ && f.autoIncremented()!=0 && updateObject==null
					|| var == 2 && f.autoIncremented()!=0)
				continue;
			if (first)
				first = false;
			else {
				q.append(", ");
				v.append(", ");
			}
			if (f.getSql() != null && f.getSql().length() > 0)
				v.append(f.getSql());
			else
				v.append('?');
			q.append(f.getStoredName());
		}
		if (first)
			throw new ProcessException("The inserted object "+fields+" doesn't contain any data");
		Set<Field> updFields = null;
		switch(var) {
		case 0: // MySQL 
			q.append(") values (").append(v).append(')');
			if (updateObject != null) {
				v.setLength(0);
				//fillKeyValuePairs(updateObject, v);
				fillPreparedKeyValuePairs(updateObject, updFields = updateObject.getFields(),  v);
				if (v.length() > 0)
					q.append(" ON DUPLICATE KEY UPDATE ").append(v);
			}
			break;
		case 1:
			break;
		case 2:
			// TODO if keys "" or null, then can be calculated as operational  from updateObject
			if (updateObject != null) {
				StringBuffer k = new StringBuffer(512);
				addedKeySet = 
						appendKeys(updateObject, k, v);
				if (k.length() > 0)
					q.append(',').append(k);
				if (k.length() > 0) {
					q.append(") KEY (");
					q.append(k);
				}
			}
			q.append(") VALUES (").append(v).append(')');
		}
		
		// for Oracle
		//MERGE INTO sgrc_analyzer_track t "
		// USING (SELECT ? rec_id, ? instance_id, ? row_id from dual) s " + " ON (t.rec_id = s.rec_id) "
		// WHEN MATCHED THEN UPDATE SET t.instance_id = s.instance_id, t.row_id = s.row_id"
		// WHEN NOT MATCHED THEN INSERT (rec_id, instance_id, row_id) VALUES (s.rec_id, s.instance_id, s.row_id)";
		// H2
		// MERGE INTO AUTHOR (FIRST_NAME, LAST_NAME)
		// KEY (LAST_NAME)
		// VALUES ('John', 'Hitchcock')
		// http://www.jooq.org/doc/2.6/manual/sql-building/sql-statements/merge-statement/
		Connection con = null;
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			//System.err.println("prepare statement:" + q);
			String[] ka = null;
			stm = keys == null || keys.isEmpty() ? con.prepareStatement(q.toString()) : con.prepareStatement(q.toString(), ka = keys
					.split(","));
			int c = 1;
			for (Field f : fields) {
				//System.err.printf("check %d%n",c);
				// excluding what are keys 
				if (f.getSql() != null && f.getSql().length() > 0 ||
						dataObject.isOperational(f.getName()) == true && f.autoIncremented()!=0 && updateObject==null
						|| var == 2 && f.autoIncremented()!=0)
					continue;
				Object obj = dataObject.get(f.getName());
				//System.err.printf("set paramd %d for %s = %s%n", c, f.getName(), obj);
				if (obj == null)
					stm.setNull(c++, f.getJDBCType());
				else
					stm.setObject(c++, Sql.toPreparedSqlValue(obj));
			}
			switch(var) {
			case 0:
				if (updateObject != null) {
					for (Field f : updFields) {
						if (f.getSql() != null && f.getSql().length() > 0)
							continue;
						Object obj = updateObject.get(f.getName());
						//System.err.printf("set upd %d for %s = %s%n", c, f.getName(), obj);
						if (obj == null)
							stm.setNull(c++, f.getJDBCType());
						else
							stm.setObject(c++, Sql.toPreparedSqlValue(obj));
					}
				}
				break;
			case 2:
				if (updateObject != null)
					for (Field f : addedKeySet) {
						if (updateObject.isOperational(f.getName())) //{System.err.printf(">>>>%s%n", f.getName());
							stm.setObject(c++, Sql.toPreparedSqlValue(updateObject.get(f.getName())));//}
					}
				break;
			}
			int result = stm.executeUpdate();
			if (ka != null) {
				rs = stm.getGeneratedKeys();
				if (rs.next()) {
					int ki = 1;
					for (String k : ka) {
						Object dobj;
						dataObject.put(k, dobj=rs.getObject(ki++));
						//System.err.println("Set key "+k+" to "+dobj);
					}
				} else {
					//System.err.printf("No autoincremented keys retrieved:%s%n", keys);
					//for (String k : ka) 
						//dataObject.modifyField(k, null);
					//throw new ProcessException("Can't retrieve autoincremented keys:" + keys);
				}
			}
			return result;
		} catch (SQLException e) {
			Log.l.log(Log.ERROR, "", "q: %s = ", e, q);
			throw new ProcessException("SQL exception happened in " + q, e);
		} finally {
			release(con, stm, rs);
		}		
	}

	public int deleteObjectLike(DataObject dataObject) throws ProcessException {
		StringBuffer q = new StringBuffer("");
		StringBuffer wc = new StringBuffer(128);
		makeQueryParts(dataObject, q, wc);
		q.setLength(0);
		q.append("delete from ").append(dataObject.getName()).append(wc);
		return updateQuery(q.toString());
	}

	public void deleteStorageFor(DataObject dataObject) throws ProcessException {
		StringBuffer q = new StringBuffer(512);
		q.append("drop table ").append(dataObject.getName());
		updateQuery(q.toString());
		// delete indices if any
		if (isCreateIndex()) {
			boolean dropIdx = false;
			for(Field f:dataObject.getFields()) {
				if (f.isIndex()) {
					dropIdx = true;
					break;
				}
			}
			if (dropIdx) {
				q.setLength(0);
				q.append("DROP INDEX IF EXISTS IDX_").append(dataObject.getName());
				updateQuery(q.toString());
			}
		}

	}

	/** Bring all subsequential DO operations in the current thread as a part of
	 * one transaction
	 * @throws ProcessException
	 */
	public void startTransaction() throws ProcessException {
		Connection con = transactionContext.get();
		if (con == null) {
			try {
				con = dataSource.getConnection();
				con.setAutoCommit(false);
			} catch (SQLException e) {
				throw new ProcessException("", e);
			}
			transactionContext.set(con);
		}
	}

	public void commitTransaction() throws ProcessException {
		finishTransaction(false);
	}

	public void rollbackTransaction() throws ProcessException {
		finishTransaction(true);
	}

	public void spawnTransactionToThread(Thread t) throws ProcessException {
		throw new UnsupportedOperationException();
	}

	protected Connection getConnection() throws SQLException {
		Connection con = transactionContext.get();
		if (con != null)
			return con;
		//System.err.printf("Pool status: %s%n", dataSource);
		return dataSource.getConnection();
	}

	private void finishTransaction(boolean rollback) throws ProcessException {
		Connection con = transactionContext.get();
		if (con == null) {
			if (rollback == false)
				throw new ProcessException("Not in transaction context", new IllegalStateException());
			else
				return;
		}
		synchronized (con) {
			try {
				if (rollback)
					con.rollback();
				else
					con.commit();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				//transactionContext.set(null);
				transactionContext.remove();
				try {
					con.setAutoCommit(true);
					con.close();
				} catch (SQLException e) {

				}
			}
		}
	}

	private void release(Object... cs) {
		if (cs != null)
			for (Object resource : cs)
				if (resource != null)
					if (resource instanceof Connection)
						release((Connection) resource, (Statement) null, (ResultSet) null);
					else if (resource instanceof AutoCloseable)
						try {
							((AutoCloseable) resource).close();
						} catch (Exception e) {

						}
					else
						throw new IllegalArgumentException("Attempt to release non closeable resource: " + resource);
	}
	
	private void release(Connection con, Statement stm, ResultSet rs) {
		try {
			if (rs != null)
				rs.close();
		} catch (SQLException e) {
		}
		try {
			if (stm != null)
				stm.close();
		} catch (SQLException e) {
		}

		Connection con1 = transactionContext.get();
		if (con1 == null)
			try {
				if (con != null)
					con.close();
			} catch (SQLException e) {
			}
		else if (con != null && con != con1)
			throw new IllegalStateException(
					"Trying to release connection not matching a connection in transactional context");
	}

	private void scrollTo(ResultSet rs, long pos) throws SQLException {
		for (; (pos - 1) > 0 && rs.next(); pos--)
			;
		if (pos > 1) // Exhausted
			throw new SQLException("Exhausted result set before " + pos);
	}

	private Set<Field> makeQueryParts(DataObject dataObject, StringBuffer q, StringBuffer wc) {
		return makeQueryParts(dataObject, q, wc, false);
	}

	private Set<Field> makeQueryParts(DataObject dataObject, StringBuffer q, StringBuffer wc, boolean inverse) {
		Set<Field> fields = dataObject.getFields();
		boolean first = true, firstClause = true;
		String eq = inverse ? "!=" : "=";
		String in = inverse ? " not in (" : " in (";
		for (Field f : fields) {
			String name = f.getName();
			if (first == false)
				q.append(',');

			if (f.getSql() != null && f.getSql().length() > 0) {
				if (firstClause == false)
					wc.append(" and ");
				else
					wc.append(" where ");
				wc.append(f.getSql()); // TODO think if it should look like wc.append(name).append(eq).append(f.getSql()); 
				if (firstClause)
					firstClause = false;
			} else if (dataObject.isOperational(name)) {
				Object value = dataObject.get(name);
				if (value != null) {
					if (firstClause == false)
						wc.append(" and ");
					else
						wc.append(" where ");
					Class<?> vc = value.getClass();
					if (vc.isArray()) {
						if (vc.getComponentType().isPrimitive()) {
							if (vc.getComponentType() == int.class) {
								throw new IllegalArgumentException("Primitive types " + vc.getComponentType()
										+ " not supported in arrays yet");
							} else if (vc.getComponentType() == char.class) {
								wc.append(name).append(" = ").append(Sql.toSqlString(value, null));
								//wc.append(name).append(" like ").append(Sql.toSqlString(value, null));
							} else
								throw new IllegalArgumentException("Primitive types " + vc.getComponentType()
										+ " not supported in arrays yet");
						} else {
							Object values[] = (Object[]) value;
							int n = values.length;
							if (n > 0) {
								wc.append(name).append(in);
								wc.append(Sql.toSqlString(values[0], getInlineDatePattern()));
								for (int i = 1; i < n; i++)
									wc.append(',').append(Sql.toSqlString(values[i], getInlineDatePattern()));
								wc.append(')');
							}
						}
					} else if (Collection.class.isAssignableFrom(value.getClass())) {
						Collection values = (Collection) value;
						int n = values.size();
						if (n > 0) {
							wc.append(name).append(in);
							boolean fr = true;
							for (Object v : values) {
								if (fr)
									wc.append(',');
								wc.append(Sql.toSqlString(v, getInlineDatePattern()));
								fr = false;
							}
							wc.append(']');
						}
					} else
						wc.append(name).append(eq).append(Sql.toSqlString(value, getInlineDatePattern()));
					if (firstClause)
						firstClause = false;
				} else
					//  else field isnull
					Log.l.error("Field " + name + " is NULL, although is claimed containing data", null);
			}
			q.append(f.getStoredName());
			if (first)
				first = false;
		}
		return fields;
	}
	
	private Set<Field> makeUpdateQueryParts(DataObject jdo, StringBuffer q, StringBuffer wc, boolean inverse) {
		Set<Field> fields = jdo.getFields();
		boolean first = true, firstClause = true;
		String eq = inverse ? "!=" : "=";
		for (Field f : fields) {
			String name = f.getName();
			String sname = f.getStoredName();
			if (sname == null || sname.length() == 0)
				sname = name;		
			if (jdo.isOperational(name)) {
				if (firstClause == false)
					wc.append(" and ");
				else
					wc.append(" where ");
				String sql = f.getSql();
				if (sql != null && sql.length() > 0) 
					wc.append(jdo.getSql(f));
				else
				wc.append(sname).append(eq).append('?');
				if (firstClause)
					firstClause = false;
			} else {
				if (first == false)
					q.append(',');
				// TODO think of  String sql = f.getSql();
				q.append(sname).append("=?");
				if (first)
					first = false;
			}
		}
		return fields;
	}
	
	private StringBuffer fillKeyValuePairs(DataObject dataObject, StringBuffer q) {
		Set<Field> fields = dataObject.getFields();
		boolean first = true;
		for (Field f : fields) {
			if (first)
				first = false;
			else {
				q.append(", ");
			}
			if (f.getSql() != null && f.getSql().length() > 0)
				q.append(f.getSql());
			else
				q.append(f.getStoredName()).append('=').append(
						Sql.toSqlString(dataObject.get(f.getName()), getInlineDatePattern()));
		}
		return q;
	}
	
	private StringBuffer fillPreparedKeyValuePairs(DataObject dataObject, Set<Field> fields, StringBuffer q) {
		boolean first = true;
		for (Field f : fields) {
			if (first)
				first = false;
			else {
				q.append(", ");
			}
			String s = dataObject.getSql(f); 
			if (s != null && s.length() > 0)
				q.append(s);
			else
				q.append(f.getStoredName()).append("=?");
		}
		return q;
	}
	
	private LinkedList<Field> appendKeys(DataObject dataObject, StringBuffer q, StringBuffer v) {
		if (dataObject == null)
			return null;
		LinkedList<Field> result = new LinkedList<Field>();
		boolean first = q.length() == 0;
		boolean firstVal = v.length() == 0;
		for (Field f : dataObject.getFields()) {
			if (dataObject.isOperational(f.getName())) {
				if (first == false) {
					q.append(',');	
				} else
					first = false;
				if (firstVal == false) {
					v.append(',');	
				} else
					firstVal = false;
				q.append(f.getStoredName());
				v.append('?');
				result.add(f);
			}
		}
		return result;
	}

	public int updateQuery(String q) throws ProcessException {
		Connection con = null;
		Statement stm = null;
		try {
			con = getConnection();
			stm = con.createStatement(); // ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE			
			return stm.executeUpdate(q); //Statement.RETURN_GENERATED_KEYS
			//stm.getGeneratedKeys()
		} catch (SQLException e) {
			Log.l.log(Log.ERROR, "", "q: %s = ", e, q);
			throw new ProcessException("SQL exception happened in " + q, e);
		} finally {
			release(con, stm, null);
		}
	}

	@Override
	public String getPreferredServiceName() {
		return NAME;
	}

	@Override
	public DOService getServiceProvider() {
		return this;
	}

	private static final ThreadLocal<Connection> transactionContext = new ThreadLocal<Connection>();

	//////////////////////////////  SQL Specific  ///////////////////////////////////////////
	/** defines extra select if select value, value... suffix
	 * for example from DUAL
	 * @return suffix
	 */
	protected String getSelectValuesTable() {
		return "";
	}

	/** defines a query addition to get records from certain range
	 * it is database specific, so if not supported, then method returns null or empty string
	 * @param start
	 * @param size
	 * @return
	 */
	protected String getLimit(long start, int size) {
		if (size > 0)
			return " LIMIT " + start + ", " + size;
		return null;
	}
	
	/** specifies format of query to insert a new record when key isn't found
	 * and update existing using the key
	 * @return number of variant as <ul>
	 * <li>0 - on duplicate key update MySQL syntax</li>
	 * <li>1 - merge into ..  WHEN (NOT) MATCHED THEN Oracle syntax</li>
	 * <li>2 - merge into .. key H2 syntax</li></ul>
	 * 
	 */
	protected int getInsertUpdateVariant() {
		return 0;
	}
	
	/** specifies request to create requested indices explicitly
	 * 
	 * @return
	 */
	protected boolean isCreateIndex() {
		return false;
	}

	/** defines a date time field pattern when used inline
	 *  
	 * @return pattern, like <code>to_date('''yyyy/MM/dd:hh:mm:ssa''', ''yyyy/mm/dd:hh:mi:ssam'')'</code>
	 */
	public String getInlineDatePattern() {
		return "'CONVERT('''yyyy-MM-dd HH:mm:ss''', DATETIME)'"; // MySQL
	}

	public String getSQLDateTimePattern() {
		return "''yyyy-MM-dd HH:mm:ss''";
	}
	

	/** Override this method is storage (table) name needs to be normalized, for example 
	 * converted to upper case
	 * @param name
	 * @return
	 */
	public String normalizeElementName(String name) {
		return name;
	}
	
	/** Sorts of using certain constraints as inline or as 
	 * a separate entry , for example<br>
	 * <ul>
	 *  <li>field type constraint
	 *  <li>constraint(field)
	 *  </ul>
	 * @return
	 */
	protected boolean inlineConstraints() {
		// TODO actually provide list of constraints
		return true;
	}

}
