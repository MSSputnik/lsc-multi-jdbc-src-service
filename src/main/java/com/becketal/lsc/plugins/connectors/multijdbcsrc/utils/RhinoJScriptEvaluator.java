/*
 ****************************************************************************
 * Ldap Synchronization Connector provides tools to synchronize
 * electronic identities from a list of data sources including
 * any database with a JDBC connector, another LDAP directory,
 * flat files...
 *
 *                  ==LICENSE NOTICE==
 * 
 * Copyright (c) 2008 - 2011 LSC Project 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:

 *    * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of the LSC Project nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *                  ==LICENSE NOTICE==
 *
 *               (c) 2008 - 2011 LSC Project
 *         Sebastien Bahloul <seb@lsc-project.org>
 *         Thomas Chemineau <thomas@lsc-project.org>
 *         Jonathan Clarke <jon@lsc-project.org>
 *         Remy-Christophe Schermesser <rcs@lsc-project.org>
 ****************************************************************************
 */
package com.becketal.lsc.plugins.connectors.multijdbcsrc.utils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.lsc.exception.LscServiceException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.UniqueTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the Rhino Java Script evaluation context.
 * copy of lsc.utils.RhinoJScriptEvaluator
 * adapted to the needs for the MultiJdbcSrcService
 *  
 * @author Sebastien Bahloul &lt;seb@lsc-project.org&gt;
 * @author Martin Schmidt
 */
public final class RhinoJScriptEvaluator implements ScriptableEvaluator {

	// Logger
	private static final Logger LOGGER = LoggerFactory.getLogger(RhinoJScriptEvaluator.class);

	/** The local Rhino context. */
	private Context             cx;

	/** debug flag */
	private boolean debug;

	/**
	 * Default public constructor.
	 */
	public RhinoJScriptEvaluator(boolean debug) {
		this.debug = debug;
	}

	/** {@inheritDoc} */
	@Override
	public Object evalToObject(final String expression, final Map<String, Object> params)
			throws LscServiceException {
		try {
			return convertJsToJava(instanceEval(expression, params));
		} catch (EvaluatorException e) {
			throw new LscServiceException(e);
		}
	}
	
	/**
	 * Local instance evaluation.
	 * 
	 * @param expression the expression to eval
	 * @param params the keys are the name used in the
	 * @return the evaluation result
	 * @throws LscServiceException
	 */
	private Object instanceEval(final String expression, final Map<String, Object> params)
			throws LscServiceException {

		RhinoDebugger rhinoDebugger = null;
		Map<String, Object> localParams = new HashMap<String, Object>();
		if (params != null) {
			localParams.putAll(params);
		}

		/* Allow to have shorter names for function in the package org.lsc.utils.directory */
		String expressionImport = 
				"with (new JavaImporter(Packages.org.lsc.utils.directory)) {"
						+ "with (new JavaImporter(Packages.org.lsc.utils)) {\n" 
						+ expression + "\n}}";

		ContextFactory factory = new ContextFactory();

		if(debug) {
			rhinoDebugger = new RhinoDebugger(expressionImport, factory);
		}

		cx = factory.enterContext();

		//        if(debug) {
		//            cx.setGeneratingDebug(true);
		//            cx.setGeneratingSource(true);
		//            cx.setOptimizationLevel(-1);
		//        }

		Scriptable scope = cx.initStandardObjects();
		Script script = cx.compileString(expressionImport, "<cmd>", 1, null);



		for (Entry<String, Object> entry : localParams.entrySet()) {
			Object jsObj = Context.javaToJS(entry.getValue(), scope);
			ScriptableObject.putProperty(scope, entry.getKey(), jsObj);
		}

		Object ret = null;
		try {
			if(debug) {
				rhinoDebugger.initContext(cx, scope, script);
				Object jsObj = Context.javaToJS(rhinoDebugger, scope);
				ScriptableObject.putProperty(scope, "rhinoDebugger", jsObj);
				ret = rhinoDebugger.exec();
			} else {
				ret = script.exec(cx, scope);
			}
		} catch (EcmaError e) {
			LOGGER.error(e.toString());
			LOGGER.debug(e.toString(), e);
			return null;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			LOGGER.error(e.toString());
			LOGGER.debug(e.toString(), e);
			return null;
		} finally {
			if(debug) {
				rhinoDebugger.run();
			}
			Context.exit();
		}
		return ret;
	}

	private static Object convertJsToJava(Object src) {
		if (src == null) {
			return null;
		} else {
			LOGGER.debug("rjs:convertJsToJava:src:" + src.getClass().getName());
			if (src.getClass().getName().equals("sun.org.mozilla.javascript.internal.NativeJavaObject") || src.getClass().getName().equals("org.mozilla.javascript.NativeJavaObject")) {
				Object o = Context.jsToJava(src, Object.class);
				LOGGER.debug("rjs:convertJsToJava:jsToJava:" + o.getClass().getName());
				return o;
			} else if (src.getClass().getName().equals("sun.org.mozilla.javascript.internal.NativeArray")) {
				try {
					Method getMethod = src.getClass().getMethod("get", int.class, Class.forName("sun.org.mozilla.javascript.internal.Scriptable"));
					Object length = src.getClass().getMethod("getLength").invoke(src);
					Object[] retarr = new Object[Integer.parseInt(length.toString())];
					for (int index = 0; index < retarr.length; index++) {
						retarr[index] = getMethod.invoke(src, index, null);
					}
					return retarr;
				} catch (Exception e) {
					LOGGER.error(e.toString());
					LOGGER.debug(e.toString(), e);
				}
			} else if (src == UniqueTag.NOT_FOUND || src == UniqueTag.NULL_VALUE) {
				return null;
			}
		}
		return src;
	}
}
