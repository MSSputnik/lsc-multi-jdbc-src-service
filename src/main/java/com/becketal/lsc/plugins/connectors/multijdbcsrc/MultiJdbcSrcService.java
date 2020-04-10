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

 ** Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 ** Neither the name of the LSC Project nor the names of its
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

package com.becketal.lsc.plugins.connectors.multijdbcsrc;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.naming.CommunicationException;
import javax.naming.NamingException;
import javax.naming.directory.BasicAttribute;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.lsc.LscDatasetModification;
import org.lsc.LscDatasets;
import org.lsc.beans.IBean;
import org.lsc.configuration.ConnectionType;
import org.lsc.configuration.DatabaseConnectionType;
import org.lsc.configuration.PluginSourceServiceType;
import org.lsc.configuration.ServiceType.Connection;
import org.lsc.configuration.TaskType;
import org.lsc.exception.LscServiceConfigurationException;
import org.lsc.exception.LscServiceException;
import org.lsc.exception.LscServiceInitializationException;
import org.lsc.persistence.DaoConfig;
import org.lsc.service.IAsynchronousService;
import org.lsc.service.IService;
import org.lsc.utils.SetUtils;
import org.lsc.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.becketal.lsc.plugins.connectors.multijdbcsrc.generated.MultiJdbcSrcServiceConfig;
import com.becketal.lsc.plugins.connectors.multijdbcsrc.utils.ScriptingEvaluator;
import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * @author Martin Schmidt;
 * @author Jonathan Clarke &lt;jonathan@phillipoux.net&gt;
 *
 */
public class MultiJdbcSrcService implements IAsynchronousService, IService {

	protected static final Logger LOGGER = LoggerFactory.getLogger(MultiJdbcSrcService.class);
	protected SqlMapClient sqlMapper;

	private Class<IBean> beanClass;

	private final String requestNameForList;
	private final String requestNameForNextId;
	private final String requestNameForObject;
	private final String requestNameForClean;
	private final String multiValueHook;

	/** Period in (milliseconds) */
	private int interval;


	/**
	 * Simple JDBC source service that gets SQL request names from lsc.properties
	 * and calls the appropriate SQL requests defined in sql-map-config.d
	 * 
	 * @param task Initialized task containing all necessary pieces of information to initiate connection
	 * 				and load settings 
	 * @throws LscServiceInitializationException 
	 */
	@SuppressWarnings("unchecked")
	public MultiJdbcSrcService(final TaskType task) throws LscServiceConfigurationException {
		// get the plugin configuration
		// try to get my configuration. Should be of type databaseConnection
		if (task != null) {
			PluginSourceServiceType pluginSourceServiceType = task.getPluginSourceService();
			if (pluginSourceServiceType != null) {
				// Initialize the connection object
				Connection connection = pluginSourceServiceType.getConnection();
				if (connection != null) {
					ConnectionType connectionType = connection.getReference();
					if (connectionType != null) {
						LOGGER.debug("Found connectionType: " + String.valueOf(connectionType));
						if (connectionType instanceof DatabaseConnectionType) {
							sqlMapper = DaoConfig.getSqlMapClient((DatabaseConnectionType)connectionType);

							try {
								this.beanClass = (Class<IBean>) Class.forName(task.getBean());

							} catch (ClassNotFoundException e) {
								throw new LscServiceConfigurationException(e);
							}
						} else {
							LOGGER.debug("connectionType is not instanceOf DatabaseConnectionType");
							throw new LscServiceConfigurationException("Unable to identify the MultiJdbcSrcService service configuration " + "inside the plugin source node of the task: " + task.getName());
						}
					} else {
						LOGGER.debug("connectionType is null");
						throw new LscServiceConfigurationException("Unable to identify the MultiJdbcSrcService service configuration " + "inside the plugin source node of the task: " + task.getName());
					}
				} else {
					LOGGER.debug("connection is null");
					throw new LscServiceConfigurationException("Unable to identify the MultiJdbcSrcService service configuration " + "inside the plugin source node of the task: " + task.getName());
				}
				// find our configuration
				MultiJdbcSrcServiceConfig config = null;
				for (Object o : task.getPluginSourceService().getAny()) {
					LOGGER.debug("getAny Object Type is : " + o.getClass().getName());
					if (o instanceof MultiJdbcSrcServiceConfig) {
						config = (MultiJdbcSrcServiceConfig)o;
						break;
					}
				}
				if (config != null) {
					// Continue to read the configuration from the pluginSourceServiceType
					LOGGER.debug("configType is " + pluginSourceServiceType.getClass().getName());
					LOGGER.debug("Read config from " + pluginSourceServiceType.getName());
					LOGGER.debug("ConfigurationClass: " + pluginSourceServiceType.getConfigurationClass());
					LOGGER.debug("ImplementationClass: " + pluginSourceServiceType.getImplementationClass());

					requestNameForList = config.getRequestNameForList();
					requestNameForObject = config.getRequestNameForObject();
					requestNameForNextId = config.getRequestNameForNextId();
					requestNameForClean = config.getRequestNameForClean();
					multiValueHook = config.getMultivaluehook();
					if(requestNameForClean == null) {
						LOGGER.warn("No clean request has been specified for task=" + task.getName() + ". During the clean phase, LSC wouldn't be able to get the right entries and may delete all destination entries !");
					}

					interval = (config.getInterval() != null ? config.getInterval().intValue() : 5) * 1000;
				} else {
					LOGGER.debug("MultiJdbcSrcServiceConfig not found");
					throw new LscServiceConfigurationException("Unable to identify the MultiJdbcSrcService service configuration " + "inside the plugin source node of the task: " + task.getName());
				}
			} else {
				LOGGER.debug("pluginSourceServiceType is null");
				throw new LscServiceConfigurationException("Unable to identify the MultiJdbcSrcService service configuration " + "inside the plugin source node of the task: " + task.getName());
			}
		} else {
			LOGGER.debug("task object is null");
			throw new LscServiceConfigurationException("task object is null");
		}
	}

	// Functions from the AbstractJdbcService

	/**
	 * The simple object getter according to its identifier.
	 * 
	 * @param pivotName Name of the entry to be returned, which is the name returned by
	 *            {@link #getListPivots()} (used for display only)
	 * @param pivotAttributes Map of attribute names and values, which is the data identifier in the
	 *            source such as returned by {@link #getListPivots()}. It must identify a unique
	 *            entry in the source.
	 * @return The bean, or null if not found
	 * @throws LscServiceException May throw a embedded {@link CommunicationException} if an SQLException is encountered 
	 */
	public IBean getBean(String pivotName, LscDatasets pivotAttributes) throws LscServiceException {
		Map<String, Object> attributeMap = pivotAttributes.getDatasets();
		try {
			return (IBean) sqlMapper.queryForObject(getRequestNameForObject(), attributeMap);
		} catch (SQLException e) {
			LOGGER.warn("Error while looking for a specific entry with id={} ({})", pivotName, e);
			LOGGER.debug(e.toString(), e);
			// TODO This SQLException may mean we lost the connection to the DB
			// This is a dirty hack to make sure we stop everything, and don't risk deleting everything...
			throw new LscServiceException(new CommunicationException(e.getMessage()));
		}
	}

	/**
	 * Execute a database request to get a list of object identifiers. This request
	 * must be a very simple and efficient request because it will get all the requested
	 * identifiers.
	 * @return Map of all entries names that are returned by the directory with an associated map of
	 *         attribute names and values (never null)
	 */
	@SuppressWarnings("unchecked")
	public Map<String, LscDatasets> getListPivots() {
		/* TODO: This is a bit of a hack - we use ListOrderedMap to keep order of the list returned,
		 * since it may be important when coming from a database.
		 * This is really an API bug, getListPivots() should return a List, not a Map.
		 */
		Map<String, LscDatasets> ret = new ListOrderedMap();

		try {
			List<HashMap<String, Object>> ids = (List<HashMap<String, Object>>) sqlMapper.queryForList(getRequestNameForList());
			Iterator<HashMap<String, Object>> idsIter = ids.iterator();
			Map<String, Object> idMap;

			for (int count = 1; idsIter.hasNext(); count++) {
				idMap = idsIter.next();
				ret.put(getMapKey(idMap, count), new LscDatasets(idMap));
			}
		} catch (SQLException e) {
			LOGGER.warn("Error while looking for the entries list: {}", e.toString());
			LOGGER.debug(e.toString(), e);
		}

		return ret;
	}

	protected String getMapKey(Map<String, Object> idMap, int count) {

		String key;
		// the key of the result Map is usually the DN
		// since we don't have a DN from a database, we use a concatenation of:
		//     - all pivot attributes
		//     - a count of all objects (to make sure the key is unique)
		// unless there's only one pivot, to be backwards compatible
		if (idMap.values().size() == 1) {
			key = idMap.values().iterator().next().toString();
		} else {
			key = StringUtils.join(idMap.values().iterator(), ", ") + " (" + count + ")";
		}
		return key;
	}

	/**
	 * Override default AbstractJdbcSrcService to get a SimpleBean
	 * @throws LscServiceException 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public IBean getBean(String id, LscDatasets attributes, boolean fromSource) throws LscServiceException {
		IBean srcBean = null;
		try {
			Map<String, Object> record = null;
			srcBean = beanClass.newInstance();
			List<?> records = sqlMapper.queryForList((fromSource ? getRequestNameForObject() : getRequestNameForClean()), getAttributesMap(attributes));
			switch (records.size()) {
			case 0:
				return null;
			case 1:
				record =  (Map<String, Object>) records.get(0);
				break;
			default:
				// multiple records found.
				record = multiRecordSelector(null, records);
				if (record == null)
					throw new LscServiceException("Multi Record handler did not return single record! " +
							"For id=" + id + ", there are " + records.size() + " records !");
			}

			for(Entry<String, Object> entry: record.entrySet()) {
				if(entry.getValue() != null) {
					srcBean.setDataset(entry.getKey(), SetUtils.attributeToSet(new BasicAttribute(entry.getKey(), entry.getValue())));
				} else {
					srcBean.setDataset(entry.getKey(), SetUtils.attributeToSet(new BasicAttribute(entry.getKey())));
				}
			}
			srcBean.setMainIdentifier(id);
			return srcBean;
		} catch (InstantiationException e) {
			LOGGER.error("Unable to get static method getInstance on {} ! This is probably a programmer's error ({})",
					beanClass.getName(), e.toString());
			LOGGER.debug(e.toString(), e);
		} catch (IllegalAccessException e) {
			LOGGER.error("Unable to get static method getInstance on {} ! This is probably a programmer's error ({})",
					beanClass.getName(), e.toString());
			LOGGER.debug(e.toString(), e);
		} catch (SQLException e) {
			LOGGER.warn("Error while looking for a specific entry with id={} ({})", id, e);
			LOGGER.debug(e.toString(), e);
			// TODO This SQLException may mean we lost the connection to the DB
			// This is a dirty hack to make sure we stop everything, and don't risk deleting everything...
			throw new LscServiceException(new CommunicationException(e.getMessage()));
		} catch (NamingException e) {
			LOGGER.error("Unable to get handle cast: " + e.toString());
			LOGGER.debug(e.toString(), e);
		}
		return null;
	}


	public static Map<String, Object> fillAttributesMap(
			Map<String, Object> datasets, IBean destinationBean) {
		for(String attributeName : destinationBean.datasets().getAttributesNames()) {
			if(!datasets.containsKey(attributeName)) {
				if(destinationBean.getDatasetById(attributeName) != null && destinationBean.getDatasetById(attributeName).size() > 0) {
					datasets.put(attributeName, destinationBean.getDatasetById(attributeName).iterator().next().toString());
				}
			}
		}
		return datasets;
	}

	public static Map<String, Object> getAttributesMap(
			List<LscDatasetModification> lscAttributeModifications) {
		Map<String, Object> values = new HashMap<String, Object>();
		for(LscDatasetModification lam : lscAttributeModifications) {
			if(lam.getValues().size() > 0) {
				values.put(lam.getAttributeName(), lam.getValues().get(0));
			} else {
				// deleted items get the value null
				values.put(lam.getAttributeName(), null);
			}
		}
		return values;
	}

	public static Map<String, String> getAttributesMap(
			LscDatasets lscAttributes) {
		Map<String, String> values = new HashMap<String, String>(lscAttributes.getDatasets().size());
		for(Entry<String, Object> entry : lscAttributes.getDatasets().entrySet()) {
			if(entry.getValue() != null) {
				values.put(entry.getKey(), getValue(entry.getValue()));
			} else {
				// deleted items get the value null
				values.put(entry.getKey(), null);
			}
		}
		return values;
	}

	public static String getValue(Object value) {
		if(value instanceof List) {
			return ((List<?>)value).iterator().next().toString();
		} else if(value instanceof Set) {
			return ((Set<?>)value).iterator().next().toString();
		} else {
			return value.toString();
		}
	}

	/**
	 * select the right record when sql returned multiple records.
	 * @param records
	 * @return selected record
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> multiRecordSelector(Task task, List<?> records) {
		LOGGER.debug("Entering multiRecordSelector");
		System.out.println("Entering multiRecordSelector");
		Map<String, Object> result = null;
		if (records != null) {
			System.out.println("**** MultiRecordSelector List size: " + String.valueOf(records.size()));
			
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("srcBeanList", records);
			try {
				Object forceValue = ScriptingEvaluator.evalToObject(multiValueHook, params);
				if (forceValue != null) {
					LOGGER.debug("ForceValue Return Type: " + forceValue.getClass().getName());
					if (forceValue instanceof Map) {
						result = (Map<String, Object>) forceValue;
					} else {
						LOGGER.error("Unsupported RETURN type for multiJdbcSrcService multihook: " + forceValue.getClass().getName());
					}
				} else {
					LOGGER.debug("ForceValues == null");
				}
			} catch (LscServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (LOGGER.isDebugEnabled()) {
			System.out.println("Selected record: " + result);
			LOGGER.debug("Selected record: " + result);
		}
		return result;
	}


	// Functions from the SimpleJdbcSrcServcie


	/* (non-Javadoc)
	 * @see org.lsc.service.AbstractJdbcService#getRequestNameForList()
	 */
	public String getRequestNameForList() {
		return requestNameForList;
	}

	/* (non-Javadoc)
	 * @see org.lsc.service.AbstractJdbcService#getRequestNameForObject()
	 */
	public String getRequestNameForObject() {
		return requestNameForObject;
	}

	/* (non-Javadoc)
	 * @see org.lsc.service.AbstractJdbcService#getRequestNameForNextId()
	 */
	public String getRequestNameForNextId() {
		return requestNameForNextId;
	}

	/* (non-Javadoc)
	 * @see org.lsc.service.AbstractJdbcService#getRequestNameForClean()
	 */
	public String getRequestNameForClean() {
		return requestNameForClean;
	}

	static int count = 0;

	@SuppressWarnings("unchecked")
	public Entry<String, LscDatasets> getNextId() {
		Map<String, Object> idMap;
		try {
			idMap = (Map<String, Object>) sqlMapper.queryForObject(getRequestNameForNextId());
			String key = getMapKey(idMap, count++);
			Map<String, LscDatasets> ret = new HashMap<String, LscDatasets>();
			ret.put(key, new LscDatasets(idMap));
			return ret.entrySet().iterator().next();
		} catch (SQLException e) {
			LOGGER.warn("Error while looking for next entry ({})", e);
			LOGGER.debug(e.toString(), e);
		}

		return null;
	}

	public long getInterval() {
		return interval;
	}
}

