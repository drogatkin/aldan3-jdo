/* Aldan3 - FromField.java
 * Copyright (C) 1999-2009 Dmitriy Rogatkin.  All rights reserved.
 *  $Id: FormField.java,v 1.25 2013/06/11 05:12:40 cvs Exp $                
 *  Created on Jun 8, 2009
 *  @author Dmitriy R
 */
package org.aldan3.annot;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.aldan3.data.util.FieldConverter;
import org.aldan3.data.util.FieldFiller;
import org.aldan3.data.util.FieldValidator;

/** Defines form field
 * 
 * @author Dmitriy
 *
 */
// TODO decide how to improve performance of converters and validators. Here are few options:
// 1. maintain a pool of such objects
// 2. maintain a static table of such objects considering they can be used in multi thread
// 3. use static methods (predefined as in interface, again have to be thread friendly
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD, ElementType.METHOD })
public @interface FormField {
	enum FieldType {
		Text, Editable, Password, Hidden, File, Readonly, RichText
	}
	
	//enum NormalizeType {
	//	Trim, Capital, Lower
	//}

	/** Tells if the field requires to have value
	 *  
	 * @return
	 */
	boolean required() default false;
	
	/** can be taken by validator classes to take extra parameters, like max length
	 * 
	 * @return
	 */
	String validationExpression() default "";

	/** Defines a field validator class
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	Class<? extends FieldValidator> validator() default FieldValidator.class;

	/** Define a field name of the class used for putting validation error message
	 * 
	 * @return
	 */
	String validationMessageTarget() default "";

	/** The data class field name where value of auto suggest has to be placed
	 * 
	 * @return
	 */
	String[] recalculateTargets() default {};
	
	/** Default field value to a value when no user input
	 * 
	 * @return
	 */
	String defaultTo() default "";

	/** Name of storage field corresponding to the form field
	 * 
	 * @return
	 */
	String dbFieldName() default "";

	/** Form field name if different than a field name
	 * 
	 * @return
	 */
	String formFieldName() default "";

	/** Class name used for conversion of user input to target field object type
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	Class<? extends FieldConverter> converter() default FieldConverter.class;

	/** Defines presentation attribute of a field
	 * 
	 * @return
	 */
	FieldType presentType() default FieldType.Editable;

	/** Defines presentation size
	 * 
	 */
	int presentSize() default -1;

	/** Defines a number of presentation rows
	 * 
	 * @return
	 */
	int presentRows() default -1;

	/** Defines a class producing a list of values which can be used for filling the field
	 * <br>
	 * If autosuggest is true, then the class is used to list suggested values and a field gets no list
	 * <p>
	 * DataFiller.class class can be specified to fill input list with query value, the query
	 * will run against datasource supplied in coordinator.
	 * @see fillQuery
	 * @see queryResultMap
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	Class<? extends FieldFiller> presentFiller() default FieldFiller.class;
	
	/** Enables auto suggest feature
	 * 
	 * @return
	 */
	boolean autosuggest() default false;
	
	/** Defines a list of other fields which presentation/value of the field depends on.
	 * When other fields got changed, the field has to be updated using <strong>recalculateFiller</strong>
	 * <p> if a dependency name can be a part of other dependency name, then names have to be ordered
	 * from longest name to shortest
	 * @return
	 */
	String[] dependencies() default {};
	
	/** Defines a class which is used for recalculate field value if it depends on other field and they get changed
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	Class<? extends FieldFiller> recalculateFiller() default FieldFiller.class;
	
	/** Fill query is used for
	 * <ol>
	 * <li>with standard DataFiller is used for
	 *  <ul>
	 *  <li> list fields, </li>
	 *  <li>cascading fields </li>
	 *  <li>and for auto-suggest.</li>
	 *  </ul> The query can include parameters in form
	 * :field, for example:
	 * <br>
	 * <pre>
	 * select value, label, help from some_table where key=:field1
	 * </pre></li>
	 * <li>upload service class</li>
	 * </ol>
	 * @return query string with parameters or UI service name
	 */
	String fillQuery() default "";
	
	/** Defines map of query result to standard names in the order:
	 * <br>
	 *  value, label, localized_label
	 *  <p>
	 *  for example:  queryResultMap = {"id", "name", ""}
	 * @return
	 */
	String[] queryResultMap() default {};
	
	/** used in conjunction with auto suggest, to specify max items to suggest
	 * 
	 * @return
	 */
	int maxSuggested() default 20;
	
	/** does normalization value, several normalization codes can be used
	 * <p>
	 * 'U' - to upper case<br>
	 * 'l' - to lover case<br>
	 * 'T', 't' - trim white spaces<br>
	 * 'C' -  to capital case, for example james smith -> James Smith
	 * 'Z' - no null, means if field didn't come from form, no null fill be placed in target field, that
	 * make preserve default value there.
	 * 
	 * @return string concatenation of normalization codes, for example "Ut" - to upper case and trim
	 * <p> normalization happens as at reading form value as at writing it. Normalization
	 * codes are case insensitive.
	 */
	String normalizeCodes() default "";
	
	/** defines optional CSS style for field, in this case other presentation values
	 * get ignored
	 * @return
	 */
	String presentStyle() default "";
	
	
	/** defines placeholder label as resource key or label itself when no labels
	 * specified
	 * @return
	 */
	String presentLabel() default "";
}
