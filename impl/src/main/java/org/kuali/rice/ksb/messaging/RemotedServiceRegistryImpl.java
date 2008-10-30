/*
 * Copyright 2007 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.ksb.messaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.rice.core.config.Config;
import org.kuali.rice.core.config.ConfigContext;
import org.kuali.rice.core.config.ConfigurationException;
import org.kuali.rice.core.util.RiceUtilities;
import org.kuali.rice.ksb.messaging.callforwarding.ForwardedCallHandler;
import org.kuali.rice.ksb.messaging.callforwarding.ForwardedCallHandlerImpl;
import org.kuali.rice.ksb.messaging.service.ServiceRegistry;
import org.kuali.rice.ksb.messaging.serviceexporters.ServiceExporterFactory;
import org.kuali.rice.ksb.service.KSBContextServiceLocator;
import org.kuali.rice.ksb.service.KSBServiceLocator;
import org.springframework.web.servlet.mvc.Controller;

import edu.emory.mathcs.backport.java.util.concurrent.ScheduledFuture;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;

public class RemotedServiceRegistryImpl implements RemotedServiceRegistry, Runnable {

	private static final Logger LOG = Logger.getLogger(RemotedServiceRegistryImpl.class);

	private Map<QName, ServerSideRemotedServiceHolder> publishedServices = Collections.synchronizedMap(new HashMap<QName, ServerSideRemotedServiceHolder>());

	private Map<QName, ServerSideRemotedServiceHolder> publishedTempServices = Collections.synchronizedMap(new HashMap<QName, ServerSideRemotedServiceHolder>());

	private boolean started;

	private ScheduledFuture future;

	protected ServiceRegistry enRoutingTableService;
	
	protected KSBContextServiceLocator serviceLocator;
	
	public void handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		((Controller) handler).handleRequest(request, response);
	}

	private void registerService(ServiceInfo entry, Object serviceImpl) throws Exception {
		ServerSideRemotedServiceHolder serviceHolder = ServiceExporterFactory.getServiceExporter(entry, serviceLocator).getServiceExporter(serviceImpl);
		this.publishedServices.put(entry.getQname(), serviceHolder);

	}

	private ServiceInfo getForwardHandlerServiceInfo(ServiceDefinition serviceDef) {
		ForwardedCallHandler callHandler = new ForwardedCallHandlerImpl();

		ServiceDefinition serviceDefinition = new JavaServiceDefinition();
		serviceDefinition.setBusSecurity(serviceDef.getBusSecurity());
		if (serviceDef.getLocalServiceName() == null && serviceDef.getServiceName() != null) {
			serviceDefinition.setServiceName(new QName(serviceDef.getServiceName().getNamespaceURI(), serviceDef.getServiceName().getLocalPart() + FORWARD_HANDLER_SUFFIX));
		} else {
			serviceDefinition.setLocalServiceName(serviceDef.getLocalServiceName() + FORWARD_HANDLER_SUFFIX);
			serviceDefinition.setServiceNameSpaceURI(serviceDef.getServiceNameSpaceURI());
		}
		serviceDefinition.setMessageExceptionHandler(serviceDef.getMessageExceptionHandler());
		serviceDefinition.setMillisToLive(serviceDef.getMillisToLive());
		serviceDefinition.setPriority(serviceDef.getPriority());
		serviceDefinition.setQueue(serviceDef.getQueue());
		serviceDefinition.setRetryAttempts(serviceDef.getRetryAttempts());
		serviceDefinition.setService(callHandler);
		serviceDefinition.validate();

		return new ServiceInfo(serviceDefinition);
	}

	@SuppressWarnings("unchecked")
	public void registerService(ServiceDefinition serviceDefinition, boolean forceRegistryRefresh) {
		if (serviceDefinition == null) {
			throw new RuntimeException("Service Definition is null");
		}
		List services = (List) ConfigContext.getCurrentContextConfig().getObject(Config.BUS_DEPLOYED_SERVICES);
		if (services == null) {
			services = new ArrayList();
			ConfigContext.getCurrentContextConfig().getObjects().put(Config.BUS_DEPLOYED_SERVICES, services);
		}
		services.add(serviceDefinition);
		// force an immediate registry of the service
		if (forceRegistryRefresh) {
			run();
		}
	}

	public void registerTempService(ServiceDefinition serviceDefinition, Object service) {
		ServiceInfo serviceInfo = new ServiceInfo(serviceDefinition);
		Object existingService = getService(serviceInfo.getQname());
		if (existingService != null) {
			throw new RuntimeException("Service with that name is already registered");
		}
		try {
			ServerSideRemotedServiceHolder serviceHolder = 
				ServiceExporterFactory.getServiceExporter(serviceInfo, serviceLocator).getServiceExporter(service);
			this.publishedTempServices.put(serviceInfo.getQname(), serviceHolder);
			LOG.debug("Registered temp service " + serviceDefinition.getServiceName());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public ServerSideRemotedServiceHolder getRemotedServiceHolder(QName qname) {
		final ServerSideRemotedServiceHolder serviceHolder = this.publishedServices.get(qname);
		return serviceHolder != null ? serviceHolder : this.publishedTempServices.get(qname);
	}

	public Object getService(QName qName, String url) {
		ServerSideRemotedServiceHolder serviceHolder = this.publishedServices.get(qName);
		if (serviceHolder != null && serviceHolder.getServiceInfo().getEndpointUrl().equals(url)) {
			return serviceHolder.getInjectedPojo();
		}

		serviceHolder = this.publishedTempServices.get(qName);
		if (serviceHolder != null && serviceHolder.getServiceInfo().equals(url)) {
			return serviceHolder.getInjectedPojo();
		}

		return null;
	}

	public Object getLocalService(QName serviceName) {
		ServerSideRemotedServiceHolder serviceHolder = this.publishedServices.get(serviceName);
		if (serviceHolder == null) {
			return null;
		}
		return serviceHolder.getInjectedPojo();
	}

	public Object getService(QName qname) {
		ServiceHolder serviceHolder = getRemotedServiceHolder(qname);
		if (serviceHolder != null) {
		    	try {
			Object service = serviceHolder.getService();
			return service;
		    	} catch (Exception e) {
		    	    this.removeRemoteServiceFromRegistry(qname);
		}
		}
		if (!StringUtils.isEmpty(qname.getNamespaceURI())) {
			return getService(new QName(qname.getLocalPart()));
		}
		return null;
	}

	public void removeRemoteServiceFromRegistry(QName serviceName) {
		this.publishedTempServices.remove(serviceName);
	}

	public void refresh() {
	    run();
	}

	public synchronized void run() {
	    	String serviceNamespace = ConfigContext.getCurrentContextConfig().getServiceNamespace();
		LOG.debug("Checking for newly published services on message entity " + serviceNamespace + " ...");

		String serviceServletUrl = (String) ConfigContext.getObjectFromConfigHierarchy(Config.SERVICE_SERVLET_URL);
		if (serviceServletUrl == null) {
			throw new RuntimeException("No service url provided to locate services.  This is configured in the KSBConfigurer.");
		}

		List javaServices = (List) ConfigContext.getCurrentContextConfig().getObject(Config.BUS_DEPLOYED_SERVICES);
		// convert the ServiceDefinitions into ServiceInfos for diff comparison
		List<ServiceInfo> configuredJavaServices = new ArrayList<ServiceInfo>();
		for (Iterator iter = javaServices.iterator(); iter.hasNext();) {
			ServiceDefinition serviceDef = (ServiceDefinition) iter.next();
			configuredJavaServices.add(new ServiceInfo(serviceDef));
			configuredJavaServices.add(getForwardHandlerServiceInfo(serviceDef));
		}

		List<ServiceInfo> configuredServices = new ArrayList<ServiceInfo>();
		configuredServices.addAll(configuredJavaServices);
		List<ServiceInfo> fetchedServices = null;

		if (ConfigContext.getCurrentContextConfig().getDevMode()) {
			fetchedServices = new ArrayList<ServiceInfo>();
		} else {
			//TODO we are not verifying that this read is not being done in dev mode in a test
			fetchedServices = this.getServiceInfoService().findLocallyPublishedServices(RiceUtilities.getIpNumber(), serviceNamespace);
		}

		RoutingTableDiffCalculator diffCalc = new RoutingTableDiffCalculator();
		diffCalc.setEnMessageHelper(serviceLocator.getMessageHelper());
		boolean needUpdated = diffCalc.calculateServerSideUpdateLists(configuredServices, fetchedServices);
		if (needUpdated) {
			if (!ConfigContext.getCurrentContextConfig().getDevMode()) {
				getServiceInfoService().save(diffCalc.getServicesNeedUpdated());
				getServiceInfoService().remove(diffCalc.getServicesNeedRemoved());
			}
			this.publishedServices.clear();
			publishServiceList(diffCalc.getMasterServiceList());
		} else if (this.publishedServices.isEmpty()) {
			publishServiceList(configuredServices);
		}
		LOG.debug("...Finished checking for remote services.");
	}

	private void publishServiceList(List<ServiceInfo> services) {
		for (ServiceInfo serviceInfo : services) {
			try {
				registerService(serviceInfo, serviceInfo.getServiceDefinition(serviceLocator.getMessageHelper()).getService());
			} catch (Exception e) {
				LOG.error("Encountered error registering service " + serviceInfo.getQname(), e);
				this.publishedServices.remove(serviceInfo);
				continue;
			}
		}
	}

	public boolean isStarted() {
		return this.started;
	}

	public synchronized void start() throws Exception {
		if (isStarted()) {
			return;
		}
		run();
		if (!ConfigContext.getCurrentContextConfig().getDevMode()) {
			int refreshRate = ConfigContext.getCurrentContextConfig().getRefreshRate();
			this.future = serviceLocator.getScheduledPool()==null?KSBServiceLocator.getScheduledPool().scheduleWithFixedDelay(this, 30, refreshRate, TimeUnit.SECONDS)
							:serviceLocator.getScheduledPool().scheduleWithFixedDelay(this, 30, refreshRate, TimeUnit.SECONDS);
		}
		this.started = true;
	}

	public void stop() throws Exception {
		// remove services from the bus
		if (this.future != null) {
			if (!this.future.cancel(false)) {
				LOG.warn("Failed to cancel the RemotedServiceRegistry.");
			}
			this.future = null;
		}
		List<ServiceInfo> fetchedServices = this.getServiceInfoService().findLocallyPublishedServices(RiceUtilities.getIpNumber(), ConfigContext.getCurrentContextConfig().getServiceNamespace());
		this.getServiceInfoService().markServicesDead(fetchedServices);
		this.publishedServices.clear();
		this.getPublishedTempServices().clear();
		this.started = false;
	}

	public String getContents(String indent, boolean servicePerLine) {
		String content = indent + "RemotedServiceRegistryImpl services=";

		for (ServiceHolder serviceHolder : this.publishedServices.values()) {
			if (servicePerLine) {
				content += indent + "+++" + serviceHolder.getServiceInfo().toString() + "\n";
			} else {
				content += serviceHolder.getServiceInfo().toString() + ", ";
			}
		}
		return content;
	}

	public ServiceRegistry getServiceInfoService() {
		return enRoutingTableService==null?KSBServiceLocator.getIPTableService():enRoutingTableService;
	}

	public Map<QName, ServerSideRemotedServiceHolder> getPublishedServices() {
		if (!isStarted()) {
			try {
				start();
			} catch (Exception e) {
				throw new ConfigurationException(e);
			}
		}
		return this.publishedServices;
	}

	public Map<QName, ServerSideRemotedServiceHolder> getPublishedTempServices() {
		return this.publishedTempServices;
	}

	/**
	 * @return the enRoutingTableService
	 */
	public ServiceRegistry getEnRoutingTableService() {
		return this.enRoutingTableService;
	}

	/**
	 * @param enRoutingTableService the enRoutingTableService to set
	 */
	public void setEnRoutingTableService(ServiceRegistry enRoutingTableService) {
		this.enRoutingTableService = enRoutingTableService;
	}

	/**
	 * @return the serviceLocator
	 */
	public KSBContextServiceLocator getServiceLocator() {
		return this.serviceLocator;
	}

	/**
	 * @param serviceLocator the serviceLocator to set
	 */
	public void setServiceLocator(KSBContextServiceLocator serviceLocator) {
		this.serviceLocator = serviceLocator;
	}

}