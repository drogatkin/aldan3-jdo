/* aldan3 - DataFiller.java
 * Copyright (C) 1999-2009 Dmitriy Rogatkin.  All rights reserved.
 *  $Id: DataFiller.java,v 1.9 2014/04/07 07:22:26 cvs Exp $                
 *  Created on Jun 23, 2009
 *  @author Dmitriy
 */
package org.aldan3.data.util;

import java.lang.reflect.Field;
import java.util.Collection;

import org.aldan3.annot.FormField;
import org.aldan3.data.DOService;
import org.aldan3.model.Coordinator;
import org.aldan3.model.DataObject;
import org.aldan3.model.Log;
import org.aldan3.util.Sql;

public class DataFiller implements FieldFiller<Collection<DataObject>, Coordinator> {

	@Override
	public Collection<DataObject> fill(Coordinator modelObject, String filter) {
		if (filter == null)
			throw new IllegalArgumentException("null parameter for leading field");
		try {
			Field f = modelObject.getClass().getField(filter);
			FormField ff = f.getAnnotation(FormField.class);
			String q = ff.fillQuery();
			if (q.length() > 0) {
				for (String df : ff.dependencies()) 
					q = q.replaceAll(":" + df, getFieldSQLData(df, modelObject));				
				q = q.replaceAll(":" + filter, getFieldSQLData(filter, modelObject));
				// TODO add ":locale" substitution by current locale
				// Log.l.debug("Massaged q: %s%n", q);
				//System.err.printf("Massaged q: %s%n", q);
				// TODO possibly add result mapping object based on queryResultMap
				return ((DOService) modelObject.getService(DOService.NAME)).getObjectsByQuery(q, 0, ff.maxSuggested());
			}
		} catch (Exception e) {
			//e.printStackTrace();
			Log.l.error("Error in data filler for "+filter, e);
		}

		return null;
	}

	public static String getFieldSQLData(String name, Coordinator model) throws Exception {
		return Sql.toSqlValue(model.getClass().getField(name).get(model), ((DOService) model
				.getService(DOService.NAME)).getInlineDatePattern());
	}
}
