/*
 * Copyright 2007-2009 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.kim.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.kuali.rice.core.config.ConfigContext;
import org.kuali.rice.core.config.RiceConfigurer;
import org.kuali.rice.core.config.event.AfterStartEvent;
import org.kuali.rice.core.config.event.BeforeStopEvent;
import org.kuali.rice.core.config.event.RiceConfigEvent;
import org.kuali.rice.core.config.event.RiceConfigEventListener;
import org.kuali.rice.core.util.RiceConstants;
import org.kuali.rice.kim.bo.entity.dto.KimEntityDefaultInfo;
import org.kuali.rice.kim.bo.entity.dto.KimPrincipalInfo;
import org.kuali.rice.kim.bo.entity.impl.KimEntityDefaultInfoCacheImpl;
import org.kuali.rice.kim.service.IdentityArchiveService;
import org.kuali.rice.kim.util.KimConstants;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.KNSServiceLocator;
import org.kuali.rice.ksb.service.KSBServiceLocator;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;


/**
 * This is the default implementation for the IdentityArchiveService.
 * @see IdentityArchiveService
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
public class IdentityArchiveServiceImpl implements IdentityArchiveService, RiceConfigEventListener {
	private static final Logger LOG = Logger.getLogger( IdentityArchiveServiceImpl.class );

	private BusinessObjectService businessObjectService;

	private static final String EXEC_INTERVAL_SECS = "kim.identityArchiveServiceImpl.executionIntervalSeconds";
	private static final String MAX_WRITE_QUEUE_SIZE = "kim.identityArchiveServiceImpl.maxWriteQueueSize";

	private int executionIntervalSeconds = 600; // by default, flush the write queue this often
	private int maxWriteQueueSize = 300; // cache this many KEDI's before forcing write
	private final WriteQueue writeQueue = new WriteQueue();
	private final EntityArchiveWriter writer = new EntityArchiveWriter();

	// all this ceremony just decorates the writer so it logs a message first, and converts the Callable to Runnable
	private final Runnable maxQueueSizeExceededWriter =
		new CallableAdapter(new PreLogCallableWrapper<Boolean>(writer, Level.DEBUG, "max size exceeded, flushing write queue"));

	// ditto
	private final Runnable scheduledWriter =
		new CallableAdapter(new PreLogCallableWrapper<Boolean>(writer, Level.DEBUG, "scheduled write out, flushing write queue"));

	// ditto
	private final Runnable shutdownWriter =
		new CallableAdapter(new PreLogCallableWrapper<Boolean>(writer, Level.DEBUG, "rice is shutting down, flushing write queue"));

	public IdentityArchiveServiceImpl(Integer executionIntervalSeconds, Integer maxWriteQueueSize) {
		// register for RiceConfigEventS
		RiceConfigurer rice =
			(RiceConfigurer)ConfigContext.getCurrentContextConfig().getObject( RiceConstants.RICE_CONFIGURER_CONFIG_NAME );
		LOG.debug("registering for events...");
		rice.getKimConfigurer().registerConfigEventListener(this);

		if (executionIntervalSeconds != null) {
			this.executionIntervalSeconds = executionIntervalSeconds;
		}

		if (maxWriteQueueSize != null) {
			this.maxWriteQueueSize = maxWriteQueueSize;
		}
	}

	protected BusinessObjectService getBusinessObjectService() {
		if ( businessObjectService == null ) {
			businessObjectService = KNSServiceLocator.getBusinessObjectService();
		}
		return businessObjectService;
	}

	public KimEntityDefaultInfo getEntityDefaultInfoFromArchive( String entityId ) {
    	Map<String,String> criteria = new HashMap<String, String>(1);
    	criteria.put(KimConstants.PrimaryKeyConstants.ENTITY_ID, entityId);
    	KimEntityDefaultInfoCacheImpl cachedValue = (KimEntityDefaultInfoCacheImpl)getBusinessObjectService().findByPrimaryKey(KimEntityDefaultInfoCacheImpl.class, criteria);
    	return (cachedValue == null) ? null : cachedValue.convertCacheToEntityDefaultInfo();
    }

    public KimEntityDefaultInfo getEntityDefaultInfoFromArchiveByPrincipalId( String principalId ) {
    	Map<String,String> criteria = new HashMap<String, String>(1);
    	criteria.put("principalId", principalId);
    	KimEntityDefaultInfoCacheImpl cachedValue = (KimEntityDefaultInfoCacheImpl)getBusinessObjectService().findByPrimaryKey(KimEntityDefaultInfoCacheImpl.class, criteria);
    	return (cachedValue == null) ? null : cachedValue.convertCacheToEntityDefaultInfo();
    }

    @SuppressWarnings("unchecked")
	public KimEntityDefaultInfo getEntityDefaultInfoFromArchiveByPrincipalName( String principalName ) {
    	Map<String,String> criteria = new HashMap<String, String>(1);
    	criteria.put("principalName", principalName);
    	Collection<KimEntityDefaultInfoCacheImpl> entities = getBusinessObjectService().findMatching(KimEntityDefaultInfoCacheImpl.class, criteria);
    	return (entities == null || entities.size() == 0) ? null : entities.iterator().next().convertCacheToEntityDefaultInfo();
    }

    public void saveDefaultInfoToArchive( KimEntityDefaultInfo entity ) {
    	// if the max size has been reached, schedule now
    	if (maxWriteQueueSize <= writeQueue.offerAndGetSize(entity) /* <- this enqueues the KEDI */ &&
    			writer.requestSubmit()) {
    		KSBServiceLocator.getThreadPool().execute(maxQueueSizeExceededWriter);
    	}
    }

    /**
     * <h4>On events:</h4>
     * <p>{@link AfterStartEvent}: schedule the writer on the KSB scheduled pool
     * <p>{@link BeforeStopEvent}: flush the write queue immediately
     *
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    public void onEvent(final RiceConfigEvent event) {
    	if (event instanceof AfterStartEvent) {
    		// on startup, schedule this to run
    		LOG.info("scheduling writer...");
    		KSBServiceLocator.getScheduledPool().scheduleAtFixedRate(scheduledWriter,
    				executionIntervalSeconds, executionIntervalSeconds, TimeUnit.SECONDS);
    	} else if (event instanceof BeforeStopEvent) {
    		KSBServiceLocator.getThreadPool().execute(shutdownWriter);
    	}
    }

	/**
	 * store the person to the database, but do this an alternate thread to
	 * prevent transaction issues since this service is non-transactional
	 *
	 * @author Kuali Rice Team (rice.collab@kuali.org)
	 *
	 */
	private class EntityArchiveWriter implements Callable {

		// flag used to prevent multiple processes from being submitted at once
		AtomicBoolean currentlySubmitted = new AtomicBoolean(false);

		private final Comparator<Comparable> nullSafeComparator = new Comparator<Comparable>() {
			public int compare(Comparable i1, Comparable i2) {
				if (i1 != null && i2 != null) {
					return i1.compareTo(i2);
				} else if (i1 == null) {
					if (i2 == null) {
						return 0;
					} else {
						return -1;
					}
				} else { // if (entityId2 == null) {
					return 1;
				}
			};
		};

		/**
		 * Comparator that attempts to impose a total ordering on KimEntityDefaultInfo instances
		 */
		private final Comparator<KimEntityDefaultInfo> kediComparator = new Comparator<KimEntityDefaultInfo>() {
			/**
			 * compares by entityId value
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			public int compare(KimEntityDefaultInfo o1, KimEntityDefaultInfo o2) {
				String entityId1 = (o1 == null) ? null : o1.getEntityId();
				String entityId2 = (o2 == null) ? null : o2.getEntityId();

				int result = nullSafeComparator.compare(entityId1, entityId2);

				if (result == 0) {
					result = getPrincipalIdsString(o1).compareTo(getPrincipalIdsString(o2));
				}

				return result;
			}

			/**
			 * This method builds a newline delimited String containing the entity's principal IDs in sorted order
			 *
			 * @param entity
			 * @return
			 */
			private String getPrincipalIdsString(KimEntityDefaultInfo entity) {
				String result = "";
				if (entity != null) {
					List<KimPrincipalInfo> principals = entity.getPrincipals();
					if (principals != null) {
						if (principals.size() == 1) { // one
							result = principals.get(0).getPrincipalId();
						} else { // multiple
							String [] ids = new String [principals.size()];
							int insertIndex = 0;
							for (KimPrincipalInfo principal : principals) {
								ids[insertIndex++] = principal.getPrincipalId();
							}
							Arrays.sort(ids);
							result = StringUtils.join(ids, "\n");
						}
					}
				}
				return result;
			}
		};

		public boolean requestSubmit() {
			return currentlySubmitted.compareAndSet(false, true);
		}

		/**
		 * Call that tries to flush the write queue.
		 * @see Callable#call()
		 */
		public Object call() {
			try {
				// the strategy is to grab chunks of entities, dedupe & sort them, and insert them in a big
				// batch to reduce transaction overhead.  Sorting is done so insertion order is guaranteed, which
				// prevents deadlocks between concurrent writers to the database.
				PlatformTransactionManager transactionManager = KNSServiceLocator.getTransactionManager();
				TransactionTemplate template = new TransactionTemplate(transactionManager);
				template.execute(new TransactionCallback() {
					public Object doInTransaction(TransactionStatus status) {
						KimEntityDefaultInfo entity = null;
						ArrayList<KimEntityDefaultInfo> entitiesToInsert = new ArrayList<KimEntityDefaultInfo>(maxWriteQueueSize);
						Set<String> deduper = new HashSet<String>(maxWriteQueueSize);

						// order is important in this conditional so that elements aren't dequeued and then ignored
						while (entitiesToInsert.size() < maxWriteQueueSize && null != (entity = writeQueue.poll())) {
							if (deduper.add(entity.getEntityId())) entitiesToInsert.add(entity);
						}

						Collections.sort(entitiesToInsert, kediComparator);

						for (KimEntityDefaultInfo entityToInsert : entitiesToInsert) {
							getBusinessObjectService().save( new KimEntityDefaultInfoCacheImpl( entityToInsert ) );
						}
						return null;
					}
				});
			} finally { // make sure our running flag is unset, otherwise we'll never run again
				currentlySubmitted.compareAndSet(true, false);
			}

			return Boolean.TRUE;
		}
	}

	/**
	 * A class encapsulating a {@link ConcurrentLinkedQueue} and an {@link AtomicInteger} to
	 * provide fast offer(enqueue)/poll(dequeue) and size checking.  Size may be approximate due to concurrent
	 * activity, but for our purposes that is fine.
	 *
	 * @author Kuali Rice Team (rice.collab@kuali.org)
	 *
	 */
	private static class WriteQueue {
		AtomicInteger writeQueueSize = new AtomicInteger(0);
		ConcurrentLinkedQueue<KimEntityDefaultInfo> queue = new ConcurrentLinkedQueue<KimEntityDefaultInfo>();

		public int offerAndGetSize(KimEntityDefaultInfo entity) {
			queue.add(entity);
			return writeQueueSize.incrementAndGet();
		}

		private KimEntityDefaultInfo poll() {
			KimEntityDefaultInfo result = queue.poll();
			if (result != null) { writeQueueSize.decrementAndGet(); }
			return result;
		}

		private int getSize() {
			return writeQueueSize.get();
		}
	}

	/**
	 * decorator for a callable to log a message before it is executed
	 *
	 * @author Kuali Rice Team (rice.collab@kuali.org)
	 *
	 */
	private static class PreLogCallableWrapper<A> implements Callable<A> {

		private final Callable inner;
		private final Level level;
		private final String message;

		public PreLogCallableWrapper(Callable inner, Level level, String message) {
			this.inner = inner;
			this.level = level;
			this.message = message;
		}

		/**
		 * logs the message then calls the inner Callable
		 *
		 * @see java.util.concurrent.Callable#call()
		 */
		@SuppressWarnings("unchecked")
		public A call() throws Exception {
			LOG.log(level, message);
			return (A)inner.call();
		}
	}

	/**
	 * Adapts a Callable to be Runnable
	 *
	 * @author Kuali Rice Team (rice.collab@kuali.org)
	 *
	 */
	private static class CallableAdapter implements Runnable {

		private final Callable callable;

		public CallableAdapter(Callable callable) {
			this.callable = callable;
		}

		public void run() {
			try {
				callable.call();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}