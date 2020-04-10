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

import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptEngine;

import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the Groovy Script evaluation context.
 * copy of lsc.utils.GroovyEvaluator
 * adapted to the needs for the MultiJdbcSrcService
 * @author Sebastien Bahloul &lt;seb@lsc-project.org&gt;
 * @author Martin Schmidt
 */
public final class GroovyEvaluator implements ScriptableEvaluator {

	// Logger
	private static final Logger LOGGER = LoggerFactory.getLogger(GroovyEvaluator.class);

	private GroovyScriptEngineImpl engine;
	
	/**
	 * Default public constructor.
	 */
	public GroovyEvaluator(ScriptEngine se) {
		this.engine = (GroovyScriptEngineImpl) se;
	}

	@Override
	public Object evalToObject(final String expression, final Map<String, Object> params) {
		return instanceEval(expression, params);
	}


	/**
	 * Local instance evaluation.
	 *
	 * @param expression
	 *                the expression to eval
	 * @param params
	 *                the keys are the name used in the
	 * @return the evaluation result
	 */
	private Object instanceEval(final String expression,
					final Map<String, Object> params) {
		Bindings bindings = engine.createBindings();


		/* Allow to have shorter names for function in the package org.lsc.utils.directory */
		String expressionImport =
//						"import static org.lsc.utils.directory.*\n" +
//						"import static org.lsc.utils.*\n" + 
						expression;

		if(params != null) {
			for(String paramName: params.keySet()) {
				bindings.put(paramName, params.get(paramName));
			}
		}
		
		Object ret = null;
		try {
			ret = engine.eval(expressionImport, bindings);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			LOGGER.error(e.toString());
			LOGGER.debug(e.toString(), e);
			return null;
		}

		return ret;
	}
}
