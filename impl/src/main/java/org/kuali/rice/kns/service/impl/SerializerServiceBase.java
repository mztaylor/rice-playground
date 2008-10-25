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
package org.kuali.rice.kns.service.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ojb.broker.core.proxy.ListProxyDefaultImpl;
import org.apache.ojb.broker.core.proxy.ProxyHelper;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kns.document.Document;
import org.kuali.rice.kns.service.DocumentSerializerService;
import org.kuali.rice.kns.service.PersistenceService;
import org.kuali.rice.kns.service.SerializerService;
import org.kuali.rice.kns.service.XmlObjectSerializerService;
import org.kuali.rice.kns.util.TypedArrayList;
import org.kuali.rice.kns.util.documentserializer.AlwaysTruePropertySerializibilityEvaluator;
import org.kuali.rice.kns.util.documentserializer.PropertySerializabilityEvaluator;
import org.kuali.rice.kns.util.documentserializer.PropertyType;
import org.kuali.rice.kns.util.documentserializer.SerializationState;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.converters.reflection.ObjectAccessException;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * Default implementation of the {@link DocumentSerializerService}.  If no &lt;workflowProperties&gt; have been defined in the
 * data dictionary for a document type (i.e. {@link Document#getDocumentPropertySerizabilityEvaluator()} returns an instance of 
 * {@link AlwaysTruePropertySerializibilityEvaluator}), then this service will revert to using the {@link XmlObjectSerializerService}
 * bean, which was the old way of serializing a document for routing.  If workflowProperties are defined, then this implementation
 * will selectively serialize items.
 */
public abstract class SerializerServiceBase implements SerializerService  {
	protected static final Log LOG = LogFactory.getLog(SerializerServiceBase.class);
    
    protected PersistenceService persistenceService;
    protected XmlObjectSerializerService xmlObjectSerializerService;
    
    protected XStream xstream;
    protected ThreadLocal<SerializationState> serializationStates;
    protected ThreadLocal<PropertySerializabilityEvaluator> evaluators;
    
    public SerializerServiceBase() {
        serializationStates = new ThreadLocal<SerializationState>();
        evaluators = new ThreadLocal<PropertySerializabilityEvaluator>();
        
        xstream = new XStream(new ProxyAndStateAwareJavaReflectionProvider());
        xstream.registerConverter(new ProxyConverter(xstream.getMapper(), xstream.getReflectionProvider() ));
        xstream.registerConverter(new PersonConverter(xstream.getMapper(), new PersonReflectionProvider()));
        xstream.addDefaultImplementation(ArrayList.class, ListProxyDefaultImpl.class);
        //xstream.registerConverter(new TypedArrayListConverter(xstream.getMapper()));
    }
        
    public class ProxyConverter extends ReflectionConverter {
        public ProxyConverter(Mapper mapper, ReflectionProvider reflectionProvider) {
            super(mapper, reflectionProvider);
        }
        public boolean canConvert(Class clazz) {
            return clazz.getName().indexOf("CGLIB") > -1 || clazz.getName().equals("org.apache.ojb.broker.core.proxy.ListProxyDefaultImpl");
        }

        public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context) {
            if (obj instanceof ListProxyDefaultImpl) { 
                List copiedList = new ArrayList(); 
                List proxiedList = (List) obj; 
                for (Iterator iter = proxiedList.iterator(); iter.hasNext();) { 
                    copiedList.add(iter.next()); 
                } 
                context.convertAnother( copiedList );
            } 
            else { 
                super.marshal(getPersistenceService().resolveProxy(obj), writer, context);
            }           
        }

        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            return null;
        }
    }
    
    public class ProxyAndStateAwareJavaReflectionProvider extends PureJavaReflectionProvider {
        @Override
        public void visitSerializableFields(Object object, Visitor visitor) {
            SerializationState state = serializationStates.get();
            PropertySerializabilityEvaluator evaluator = evaluators.get();
            
            for (Iterator iterator = fieldDictionary.serializableFieldsFor(object.getClass()); iterator.hasNext();) {
                Field field = (Field) iterator.next();
                if (!fieldModifiersSupported(field)) {
                    continue;
                }
                
                if (ignoreField(field)) {
                    continue;
                }
                
                validateFieldAccess(field);
                
                initializeField(object, field);
                
                Object value = null;
                try {
                    value = field.get(object);
                } catch (IllegalArgumentException e) {
                    throw new ObjectAccessException("Could not get field " + field.getClass() + "." + field.getName(), e);
                } catch (IllegalAccessException e) {
                    throw new ObjectAccessException("Could not get field " + field.getClass() + "." + field.getName(), e);
                }
                
                if (evaluator.isPropertySerializable(state, object, field.getName(), value)) {
                    if (value != null && ProxyHelper.isProxy(value)) {
                        // resolve proxies after we determine that it's serializable
                        value = getPersistenceService().resolveProxy(value);
                    }
                    PropertyType propertyType = evaluator.determinePropertyType(value);
                    state.addSerializedProperty(field.getName(), propertyType);
                    visitor.visit(field.getName(), field.getType(), field.getDeclaringClass(), value);
                    state.removeSerializedProperty();
                }
            }
        }
        
        protected boolean ignoreField(Field field) {
            return false;
        }
        
        protected void initializeField(Object object, Field field) {
        }
    }

    public class PersonConverter extends ReflectionConverter {
        private PersonReflectionProvider reflectionProvider;
        
        public PersonConverter(Mapper mapper, ReflectionProvider reflectionProvider) {
            super(mapper, reflectionProvider);
            this.reflectionProvider = (PersonReflectionProvider) reflectionProvider;
        }
        
        @Override
        public boolean canConvert(Class type) {
            return Person.class.isAssignableFrom(type);
        }
    }
    
    public class PersonReflectionProvider extends ProxyAndStateAwareJavaReflectionProvider {
        private Set<String> primitivePropertiesPopulatedByServices;
        private PropertyUtilsBean propertyUtilsBean;
        
        public PersonReflectionProvider() {
            initializePrimitivePropertiesPopulatedByServices();
            propertyUtilsBean = new PropertyUtilsBean();
        }
        
        @Override
        protected void initializeField(Object object, Field field) {
            Person user = (Person) object;
            String fieldName = field.getName();
            
            if (primitivePropertiesPopulatedByServices.contains(fieldName)) {
                try {
                    // some universal user properties are initialized by calling the getter method for the appropriate method
                    // so, here, we use property utils bean/introspection to call the getter method, so that it will be populated
                    // when the reflection provider attempts to access these properties
                    propertyUtilsBean.getProperty(user, fieldName);
                } catch (Exception e) {
                    LOG.error("Cannot use getter method for Person property: " + fieldName, e);
                }
            }
        }

        protected void initializePrimitivePropertiesPopulatedByServices() {
            primitivePropertiesPopulatedByServices = new HashSet<String>();
            primitivePropertiesPopulatedByServices.add("moduleProperties");
            primitivePropertiesPopulatedByServices.add("moduleUsers");
        }
    }
    
    public PersistenceService getPersistenceService() {
        return this.persistenceService;
    }

    public void setPersistenceService(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }
    
    public XmlObjectSerializerService getXmlObjectSerializerService() {
        return this.xmlObjectSerializerService;
    }

    public void setXmlObjectSerializerService(XmlObjectSerializerService xmlObjectSerializerService) {
        this.xmlObjectSerializerService = xmlObjectSerializerService;
    }
    
    protected SerializationState createNewDocumentSerializationState(Document document) {
        return new SerializationState();
    }
    
    public class TypedArrayListConverter extends CollectionConverter {

    	public TypedArrayListConverter(Mapper mapper){
    		super(mapper);
    	}

    	public boolean canConvert(Class clazz) {
    		return clazz.equals(TypedArrayList.class);
        }

    }
    
    
}

