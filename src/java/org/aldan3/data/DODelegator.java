/* aldan3 - DODelegator.java
 * Copyright (C) 1999-2009 Dmitriy Rogatkin.  All rights reserved.
 *  $Id: DODelegator.java,v 1.30 2013/04/20 05:25:55 cvs Exp $                
 *  Created on Jun 18, 2009
 *  @author Dmitriy
 */
package org.aldan3.data;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.aldan3.annot.DBField;
import org.aldan3.annot.DataRelation;
import org.aldan3.annot.FormField;
import org.aldan3.data.util.AnnotField;
import org.aldan3.data.util.FieldConverter;
import org.aldan3.model.Coordinator;
import org.aldan3.model.DOFactory;
import org.aldan3.model.DataObject;
import org.aldan3.model.Field;
import org.aldan3.model.Log;

// TODO use converter from DBField annotation
// TODO define persistent services interface and implement it here
// TODO rename to DOFacade
// the  interface have to relay on DOService implementation taken from coordinator 
// as DOService
public class DODelegator<T> implements DataObject, DOFactory {
	protected T principal;

	protected HashMap<String, java.lang.reflect.Field> fieldsMap;

	protected String storage;

	protected HashSet<String> selectedData;

	protected TimeZone timeZone;

	protected Locale locale;

	/**
	 * Creates DataObject from annotated model object Some parameters give
	 * flexibility of customization
	 * 
	 * @param model
	 *            object
	 * @param name
	 *            name of storage table (optional)
	 * @param exclusion
	 *            comma separated list of fields excluded from DataObject
	 * @param inclusion
	 *            comma separated list of fields containing data for select
	 *            operations, usually keys forming where clause
	 */
	public DODelegator(T model, String name, String exclusion, String inclusion) {
		principal = model;
		storage = name;
		fieldsMap = new HashMap<String, java.lang.reflect.Field>();
		for (java.lang.reflect.Field f : principal.getClass().getFields())
			if (f.getAnnotation(DBField.class) != null) {
				FormField ff = f.getAnnotation(FormField.class);
				String dbFiledName = ff != null && ff.dbFieldName().length() > 0 ? ff.dbFieldName() : f.getName();
				if (fieldsMap.containsKey(dbFiledName))
					throw new IllegalArgumentException("Ambiguous db fields " + f + " and "
							+ fieldsMap.get(dbFiledName));
				fieldsMap.put(normilizeFieldName(dbFiledName), f);
			}
		if (exclusion != null) {
			StringTokenizer st = new StringTokenizer(exclusion, ",");
			while (st.hasMoreTokens())
				fieldsMap.remove(normilizeFieldName(st.nextToken()));
		}
		if (inclusion != null) {
			selectedData = new HashSet<String>();
			StringTokenizer st = new StringTokenizer(inclusion, ",");
			while (st.hasMoreTokens()) {
				name = normilizeFieldName(st.nextToken());
				if (fieldsMap.containsKey(name))
					selectedData.add(name);
				else
					Log.l.debug("The field %s was claimed as key wasn't in fields list and ignored", name);
			}
		}
	}

	/** Creates data object from annotated POJO
	 * it takes all DB fields and use no key, suitable for add operation mostly
	 * @param model
	 */
	public DODelegator(T model) {
		this(model, null, (String) null, null);
	}

	/** Creates data object from annotated POJO
	 * 
	 * @param model
	 * @param name of table/data object
	 * @param exclusion POJO annotated used for forming exclusion fields
	 */
	public DODelegator(T model, String name, Class exclusion) {
		this(model, name, listOfFields(exclusion, null), null);
		if (model.getClass() == exclusion)
			throw new IllegalArgumentException("Model class and exclusion class are the same");
	}

	/** Creates data object from annotated POJO
	 * 
	 * @param model
	 * @param name
	 * @param exclusion
	 * @param inclusion
	 */
	public DODelegator(T model, String name, Class exclusion, String inclusion) {
		this(model, name, listOfFields(exclusion, null), inclusion);
		if (model.getClass() == exclusion)
			throw new IllegalArgumentException("Model class and exclusion class are the same");
	}

	/** Creates data object from annotated POJO
	 * 
	 * @param model
	 * @param name
	 * @param exclusion class with DB annotations foeming exclusion list
	 * @param exclusioninclusion reduces exclusion list for specified values
	 * @param inclusion key fields
	 */
	public DODelegator(T model, String name, Class exclusion, String exclusioninclusion, String inclusion) {
		this(model, name, listOfFields(exclusion, exclusioninclusion), inclusion);
	}

	@Override
	public boolean containData(String name) {
		name = normilizeFieldName(name);
		if (selectedData != null)
			return selectedData.contains(name);
		return fieldsMap.containsKey(name);
	}

	@Override
	public boolean meanFieldFilter(String name) {
		return containData(name);
	}

	@Override
	public Field defineField(Field field) {
		throw new UnsupportedOperationException("No new fields can be added " + field);
	}

	@Override
	public Object get(String name) {
		name = normilizeFieldName(name);
		try {
			java.lang.reflect.Field f = fieldsMap.get(name);
			DBField ff = f.getAnnotation(DBField.class);
			if (ff == null || ff.converter() == FieldConverter.class)
				return f.get(principal);
			// TODO poor performance, converter classes have to be cached unless not thread friendly
			// another solution is implementing converter interface in model itself
			// or reference a field of model holding instantiated converter instance
			// and finally provide predefined thread friendly static method in specified class
			return create(ff.converter()).deConvert(f.get(principal), timeZone, locale);
		} catch (Exception e) {
			Log.l.error("Error in retrieving field:" + name, e);
		}
		return null;
	}

	@Override
	public Object getField(String name) {
		return get(normilizeFieldName(name));
	}

	@Override
	public Field getField(Field field) {
		String n = normilizeFieldName(field.getName());
		if (fieldsMap.containsKey(n))
			return new AnnotField(fieldsMap.get(n));
		return null;
	}

	@Override
	public Set<String> getFieldNames() {
		return fieldsMap.keySet();
	}

	@Override
	public Set<Field> getFields() {
		HashSet<Field> result = new HashSet<Field>(fieldsMap.size());
		for (String n : getFieldNames()) {
			result.add(new AnnotField(fieldsMap.get(n)));
		}
		return result;
	}

	@Override
	public String getName() { // not thread safe
		if (storage == null)
			synchronized (this) {
				Class<?> principalCLass = principal.getClass();
				DataRelation dr = principalCLass.getAnnotation(DataRelation.class);
				if (dr != null && dr.table().length() > 0)
					storage = dr.table();
				else {
					storage = principal.getClass().getName();
					int pos = storage.lastIndexOf('.');
					if (pos > 0)
						storage = storage.substring(pos + 1);
				}
			}
		return storage;
	}

	@Override
	public Object modifyField(String name, Object value) {
		name = normilizeFieldName(name);
		try {
			java.lang.reflect.Field f = fieldsMap.get(name);
                        //name = normilizeFieldName(name);
			if (f != null) {
				Object result = f.get(principal);
				try {
					f.set(principal, value);
				} catch (IllegalArgumentException iae) {
					DBField ff = f.getAnnotation(DBField.class);
					if (ff != null && ff.converter() != FieldConverter.class) {
						f.set(principal, create(ff.converter()).convert(value.toString(),
								timeZone, locale));
						return result;
					}
					Class t = f.getType();
					Class vc = value == null ? null : value.getClass();
					Log.l.debug("Required data conversion for %s to %s of %s != %s", name, value, vc, t);
					//System.err.printf("required data conversion for %s to %s of %s != %s%n", name, value, vc, t);
					if (vc == null) {
						if (t.isPrimitive()) {
							f.set(principal, 0);
						} else
							Log.l.debug("Can't set null value for %s", t);
					} else if (vc == String.class) {
						String sv = (String) value;
						if (sv.length() > 0) {
							if (t.isArray()) {
								if (sv.charAt(0) == '[')
									sv = sv.substring(1, sv.length() - 1);
								String[] svs = sv.split(",");
								if (t == String[].class)
									f.set(principal, svs);
								else if (t == int[].class) {
									int[] ivs = new int[svs.length];
									for (int i = 0, n = svs.length; i < n; i++)
										ivs[i] = Integer.parseInt(svs[i].trim());
									f.set(principal, ivs);
								}
							} else if (t == int.class) {
								f.set(principal, new Integer(sv));
							} else if (t == char.class) {
								//System.err.printf("Setting char %s for %s%n", sv, name);
								f.set(principal, sv.charAt(0));
							} else if (t == boolean.class || t == Boolean.class) {
								f.set(principal, sv.charAt(0) == 'T');
							} else if (t.isEnum()) {
								f.set(principal, Enum.valueOf(t, sv));
							} else if (t == TimeZone.class) {
								f.set(principal, TimeZone.getTimeZone(sv));
							}
						} else { // empty string
							Log.l.debug("Empty string can't be converted, so field %s wasn't set", name);
						}
					} else if (vc == BigDecimal.class) {
						if (t == float.class || t == Float.class)
							f.set(principal, ((BigDecimal) value).floatValue());
					} else if (vc == Long.class) {
						if (t == int.class)
							f.set(principal, ((Long) value).intValue());
					} else if (vc == Integer.class) {
					} else if (vc == Double.class) {

					} else
						Log.l.debug("Field %s wasn't updated to %s", name, value);
				}
				return result;
			} else
				Log.l.error(String.format("A field wasn't found for name %s to set to %s", name, value), null);
		} catch (Exception e) {
			Log.l.error("Problem is settings " + name + " to " + value, e);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Object modifyField(Field field, Object value) {
		return modifyField(field.getName(), value);
	}

	@Override
	public Field removeField(Field field) {
		if (fieldsMap.remove(normilizeFieldName(field.getName())) != null)
			return field;
		return null;
	}

	/**
	 * build all data field of data object with exclusion <br>
	 * can be used for building reverse exclusion lists
	 * 
	 * @param modelClass
	 * @param exclusion
	 * @return
	 */
	public static String listOfFields(Class modelClass, String exclusion) {
		HashSet<String> fields = new HashSet<String>();
		for (java.lang.reflect.Field f : modelClass.getDeclaredFields()) {
			if (f.getAnnotation(DBField.class) != null) {
				FormField ff = f.getAnnotation(FormField.class);
				if (ff != null && ff.dbFieldName().length() > 0)
					fields.add(ff.dbFieldName());
				else
					fields.add(f.getName());
			}
		}
		if (exclusion != null) {
			StringTokenizer st = new StringTokenizer(exclusion, ",");
			while (st.hasMoreTokens())
				fields.remove(st.nextToken());
		}

		int n = fields.size();
		if (n > 0) {
			StringBuffer result = new StringBuffer(128);
			Iterator<String> i = fields.iterator();
			result.append(i.next());
			while (i.hasNext())
				result.append(',').append(i.next());
			return result.toString();
		}
		return null;
	}

	protected String normilizeFieldName(String fieldName) {
		if (principal instanceof Coordinator) {
			return ((DOService)((Coordinator)principal).getService(Coordinator.DOSERVICE)).normalizeElementName(fieldName);
		}
		return fieldName;
	}

	public T getPrincipal() {
		return principal;
	}

	@Override
	public DataObject create() {
		return this;
	}

	private FieldConverter create(Class<? extends FieldConverter> class1) throws InstantiationException, IllegalAccessException {
		if (principal instanceof Coordinator) {
			try {
				return class1.getConstructor(Object.class).newInstance(((Coordinator) principal).getModel(null));
			} catch (IllegalArgumentException e) {

			} catch (InvocationTargetException e) {

			} catch (NoSuchMethodException e) {

			} catch (SecurityException e) {

			}
		}
		return class1.newInstance();
	}

}
