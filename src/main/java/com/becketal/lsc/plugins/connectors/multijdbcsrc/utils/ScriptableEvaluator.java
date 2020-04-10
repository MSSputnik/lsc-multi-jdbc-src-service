package com.becketal.lsc.plugins.connectors.multijdbcsrc.utils;

import java.util.Map;

import org.lsc.exception.LscServiceException;

/**
 * Copy of org.lsc.utils.ScriptableEvaluator
 * but removal of the Task option.
 * @author Martin Schmidt
 *
 */
public interface ScriptableEvaluator {

	/**
	 * Evaluate your script expression to a boolean value
	 * @param expression the expression to evaluate
	 * @param params the keys are the name used in the
	 * @return the evaluation result, null if nothing
	 * @throws LscServiceException thrown when a technical error is encountere
	 */
	public Object evalToObject(final String expression, final Map<String, Object> params)
			throws LscServiceException;
}
