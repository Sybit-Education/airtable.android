/*
 * The MIT License (MIT)
 * Copyright (c) 2017 Sybit GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 */
package com.sybit.airtableandroid;

import com.google.gson.annotations.SerializedName;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.sybit.airtableandroid.exception.AirtableException;
import com.sybit.airtableandroid.exception.HttpResponseExceptionHandler;
import com.sybit.airtableandroid.vo.*;
import javadz.beanutils.BeanUtils;
import javadz.beanutils.BeanUtilsBean;
import javadz.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Representation Class of Airtable Tables.
 *
 * @since 0.1
 */
public class Table<T> {

    private static final Logger LOG = LoggerFactory.getLogger( Table.class );

    private final String name;
    private final Class<T>  type;

    private Base parent;

    /**
     *
     * @param name name of table.
     * @param type class to represent table row
     */
    public Table(String name, Class<T> type) {
        assert name != null : "name was null";
        assert type != null : "type was null";

        this.name = name;
        this.type = type;
    }

    /**
     *
     * @param name
     * @param type
     * @param base
     */
    @SuppressWarnings("WeakerAccess")
    public Table(String name, Class<T> type, Base base){
        this(name, type);
        setParent(base);
    }

    /**
     *
     * @param parent
     */
    public void setParent(Base parent) {
        this.parent = parent;
    }

    /**
     * Select all rows of table.
     *
     * @return List of all items.
     * @throws AirtableException
     */
    public List<T> select() throws AirtableException {
        return select(new Query() {
            @Override
            public Integer getMaxRecords() {
                return null;
            }

            @Override
            public String getView() {
                return null;
            }

            @Override
            public List<Sort> getSort() {
                return null;
            }

            @Override
            public String filterByFormula() {
                return null;
            }

            @Override
            public String[] getFields() {
                return null;
            }

            @Override
            public Integer getPageSize() {
                return null;
            }
        });

    }

    /**
     * Select List of data of table with defined Query Parameters.
     *
     * @param query defined query
     * @return list of table items
     * @throws AirtableException
     */
    @SuppressWarnings("WeakerAccess")
    public List<T> select(Query query) throws AirtableException {
        HttpResponse<Records> response;
        try {
            GetRequest request = Unirest.get(getTableEndpointUrl())
                    .header("accept", "application/json")
                    .header("Authorization", getBearerToken());
            if(query.getFields() != null && query.getFields().length > 0){ 
                String[] fields = query.getFields();
                for (int i = 0; i < fields.length; i++) {
                    request.queryString("fields[]",fields[i]);     
                }      
            }
            if(query.getMaxRecords() != null) {
                request.queryString("maxRecords", query.getMaxRecords());
            }
            if(query.getView() != null) {
                request.queryString("view", query.getView());
            }
            if(query.filterByFormula() != null) {
                request.queryString("filterByFormula", query.filterByFormula());
            }
            if(query.getPageSize() != null){
                if (query.getPageSize() > 100) {
                    request.queryString("pageSize",100);
                } else {
                    request.queryString("pageSize",query.getPageSize());
                }      
            }
            if(query.getSort() != null) {
                int i = 0;
                for (Sort sort : query.getSort()) {
                    request.queryString("sort[" + i + "][field]", sort.getField());
                    request.queryString("sort[" + i + "][direction]", sort.getDirection());
                }
            }

            LOG.debug("URL=" + request.getUrl());

            response = request.asObject(Records.class);
        }
        catch (UnirestException e) {
            throw new AirtableException(e);
        }

        int code = response.getStatus();
        List<T> list = null;
        if(200 == code) {
            list = getList(response);
        } else {
            HttpResponseExceptionHandler.onResponse(response);
        }

        return list;
    }

    /**
     * select with Parameter maxRecords
     * 
     * @param maxRecords
     * @return
     * @throws AirtableException
     */
    public List<T> select(final Integer maxRecords) throws AirtableException {
        return select(new Query() {
            @Override
            public Integer getMaxRecords() {
                return maxRecords;
            }

            @Override
            public String getView() {
                return null;
            }

            @Override
            public List<Sort> getSort() {
                return null;
            }

            @Override
            public String filterByFormula() {
                return null;
            }

            @Override
            public String[] getFields() {
                return null;
            }

            @Override
            public Integer getPageSize() {
                return null;
            }
        });
    }

    /**
     * Select data of table by definied view.
     * @param view
     * @return
     * @throws AirtableException
     */
    public List<T> select(final String  view) throws AirtableException {
        return select(new Query() {
            @Override
            public Integer getMaxRecords() {
                return null;
            }

            @Override
            public String getView() {
                return view;
            }

            @Override
            public List<Sort> getSort() {
                return null;
            }

            @Override
            public String filterByFormula() {
                return null;
            }

            @Override
            public String[] getFields() {
                return null;
            }

            @Override
            public Integer getPageSize() {
                return null;
            }
        });
    }

    /**
     *select Table data with defined sortation
     * 
     * @param sortation
     * @return
     * @throws AirtableException
     */
    public List<T> select(Sort sortation) throws AirtableException {
        final List<Sort> sortList = new ArrayList<>();
        sortList.add(sortation);

        return select(new Query() {
            @Override
            public Integer getMaxRecords() {
                return null;
            }

            @Override
            public String getView() {
                return null;
            }

            @Override
            public List<Sort> getSort() {
                return sortList;
            }

            @Override
            public String filterByFormula() {
                return null;
            }

            @Override
            public String[] getFields() {
                return null;
            }

            @Override
            public Integer getPageSize() {
                return null;
            }
        });
    }
    
    /**
     * Select only Table data with defined fields.
     *
     * @param fields array of requested fields.
     * @return list of item using only requested fields.
     * @throws AirtableException
     */
    public List<T> select(final String[] fields) throws AirtableException {

        return select(new Query() {
            @Override
            public Integer getMaxRecords() {
                return null;
            }

            @Override
            public String getView() {
                return null;
            }

            @Override
            public List<Sort> getSort() {
                return null;
            }

            @Override
            public String filterByFormula() {
                return null;
            }

            @Override
            public String[] getFields() {
                return fields;
            }

            @Override
            public Integer getPageSize() {
                return null;
            }
        });
    }

    /**
     * Get List of records of response.
     *
     * @param response
     * @return
     */
    private List<T> getList(HttpResponse<Records> response) {

        final Records records = response.getBody();
        final List<T> list = new ArrayList<>();

        for(Map<String, Object> record : records.getRecords()) {
            T item = null;
            try {
                item = transform(record, this.type.newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                LOG.error(e.getMessage(), e);
            }
            list.add(item);
        }
        return list;
    }

    /**
     * Find record by given id.
     *
     * @param id id of record.
     * @return searched record.
     * @throws AirtableException
     */
    public T find(String id) throws AirtableException {

        RecordItem body;

        HttpResponse<RecordItem> response;
        try {
            response = Unirest.get( getTableEndpointUrl() + "/" + id)
                .header("accept", "application/json")
                .header("Authorization", getBearerToken())
                .asObject(RecordItem.class);
        } catch (UnirestException e) {
            throw new AirtableException(e);
        }
        int code = response.getStatus();

        if(200 == code) {
            body = response.getBody();
        } else {
            HttpResponseExceptionHandler.onResponse(response);
            body = null;
        }

        try {
            return transform(body, this.type.newInstance() );
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
            throw new AirtableException(e);
        }
    }
    
    /**
     * Create Record of given Item
     * 
     * @param item the item to be created
     * @return the created item
     * @throws AirtableException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException 
     */
    public T create(T item) throws AirtableException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
               
        RecordItem responseBody = null;
        
        checkProperties(item);
                                      
        PostRecord body = new PostRecord<T>();
        body.setFields(item);
              
        HttpResponse<RecordItem> response;
        try {
            response = Unirest.post( getTableEndpointUrl())
                .header("accept", "application/json")
                .header("Authorization", getBearerToken())
                .header("Content-type","application/json")
                .body(body)
                .asObject(RecordItem.class);
        } catch (UnirestException e) {
            throw new AirtableException(e);
        }
             
        int code = response.getStatus();

        if(200 == code) {
            responseBody = response.getBody();
        } else {
            HttpResponseExceptionHandler.onResponse(response);
        }

        try {
            return transform(responseBody, this.type.newInstance() );
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
            LOG.error(e.getMessage(), e);
        }
        
        return null;
    }

    public T update(T item) throws AirtableException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        
        RecordItem responseBody = null;
        
        String id = getIdOfItem(item);
            
        PostRecord body = new PostRecord<T>();
        body.setFields(filterFields(item));
              
        HttpResponse<RecordItem> response;
        try {
            response = Unirest.patch( getTableEndpointUrl()+"/"+id)
                .header("accept", "application/json")
                .header("Authorization", getBearerToken())
                .header("Content-type","application/json")
                .body(body)
                .asObject(RecordItem.class);
        } catch (UnirestException e) {
            throw new AirtableException(e);
        }
             
        int code = response.getStatus();

        if(200 == code) {
            responseBody = response.getBody();
        } else {
            HttpResponseExceptionHandler.onResponse(response);
        }

        try {
            return transform(responseBody, this.type.newInstance() );
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
            LOG.error(e.getMessage(), e);
        }
        
        return null;

    }

    public T replace(T item) {

        throw new UnsupportedOperationException("not yet implemented");
    }
    
    /**
     * Delete Record by given id
     * 
     * @param id
     * @throws AirtableException 
     */

    public void destroy(String id) throws AirtableException {
        
        Delete body = null;

        HttpResponse<Delete> response;
        try {
            response = Unirest.delete(getTableEndpointUrl() + "/" + id)
                .header("accept", "application/json")
                .header("Authorization", getBearerToken())
                .asObject(Delete.class);
        } catch (UnirestException e) {
            throw new AirtableException(e);
        }
        int code = response.getStatus();

        if(200 == code) {
            body = response.getBody();
        } else {
            HttpResponseExceptionHandler.onResponse(response);
        }

        if(!body.isDeleted()){
            throw new AirtableException("Record id: "+body.getId()+" could not be deleted.");
        }      
    }

    /**
     *
     * @return
     */
    private Base base() {
        return parent;
    }

    /**
     * Get the endpoint for the specified table.
     *
     * @return URL of tables endpoint.
     */
    private String getTableEndpointUrl() {

        return base().airtable().endpointUrl() + "/" + base().name() + "/" + this.name;
    }

    /**
     * Get Bearer Token for Authentication Header.
     *
     * @return
     */
    private String getBearerToken() {
        return "Bearer " + base().airtable().apiKey();
    }

    /**
     *
     * @param record
     * @param retval
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private T transform(Map<String, Object> record, T retval) throws InvocationTargetException, IllegalAccessException {
        for(String key: record.keySet()) {
            if("fields".equals(key)) {
                //noinspection unchecked
                retval = transform((Map<String, Object>)record.get("fields"), retval);
            } else {
                setProperty(retval, key, record.get(key));
            }
        }

        return retval;
    }

    /**
     *
     * @param record
     * @param retval
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private T transform(RecordItem record, T retval) throws InvocationTargetException, IllegalAccessException {
        setProperty(retval, "id", record.getId());
        setProperty(retval, "createdTime", record.getCreatedTime());

        retval = transform(record.getFields(), retval);

        return retval;
    }

    /**
     *
     * @param retval
     * @param key
     * @param value
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private void setProperty(T retval, String key, Object value) throws IllegalAccessException, InvocationTargetException {
        String property = key2property(key);

        for (Field f: this.type.getDeclaredFields()) {
            final SerializedName annotation = f.getAnnotation(SerializedName.class);

            if(annotation != null && property.equalsIgnoreCase(annotation.value())){
               property = f.getName();
               break;
            }
        }

        if (propertyExists(retval, property)) {
            BeanUtils.setProperty(retval, property, value);
        }else {
            LOG.warn(retval.getClass() + " does not support public setter for existing property [" + property + "]");
        }
    }

    /**
     * Convert AirTable ColumnName to Java PropertyName.
     *
     * @param key
     * @return
     */
    private String key2property(String key) {
        
        if(key.contains(" ") || key.contains("-") ) {
            LOG.warn( "Annotate columns having special characters by using @SerializedName for property: [" + key + "]");
        }
        String property = key.trim();
        property = property.substring(0,1).toLowerCase() + property.substring(1, property.length());
        
        return property;
    }

    /**
     * Check if writable property exists.
     *
     * @param bean bean to inspect
     * @param property name of property
     * @return true if writable property exists.
     */
    private static boolean propertyExists (Object bean, String property) {
        return PropertyUtils.isReadable(bean, property) &&
                PropertyUtils.isWriteable(bean, property);
    }

    /**
     * Checks if the Property Values of the item are valid for the Request.
     * 
     * @param item
     * @throws AirtableException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException 
     */
    private void checkProperties(T item) throws AirtableException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        
        if(propertyExists(item,"id") || propertyExists(item,"createdTime")){
            Field[] attributes = item.getClass().getDeclaredFields();
            for (Field attribute : attributes) {
                String name = attribute.getName();
                if (name.equals("id") || name.equals("createdTime")) {
                    if(BeanUtils.getProperty(item,attribute.getName()) != null){
                        throw new AirtableException("Property "+name+" should be null!");
                    }           
                } else if (name.equals("photos")){
                    List<Attachment> obj = (List<Attachment>) BeanUtilsBean.getInstance().getPropertyUtils().getProperty(item, "photos");
                    checkPropertiesOfAttachement(obj);
                }                 
            }
        }
        
    }
    
    private void checkPropertiesOfAttachement(List<Attachment> attachements) throws AirtableException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
        
        if(attachements != null){
            for (int i = 0; i < attachements.size(); i++) {
                if(propertyExists(attachements.get(i),"id") || propertyExists(attachements.get(i),"size") || propertyExists(attachements.get(i), "type") || propertyExists(attachements.get(i), "filename")){
                    Field[] attributesPhotos = attachements.getClass().getDeclaredFields();
                    for (Field attributePhoto : attributesPhotos) {
                        String namePhotoAttribute = attributePhoto.getName();
                        if (namePhotoAttribute.equals("id") || namePhotoAttribute.equals("size") || namePhotoAttribute.equals("Tpye") ||  namePhotoAttribute.equals("filename")) {
                            if(BeanUtils.getProperty(attachements.get(i),namePhotoAttribute) != null){
                                throw new AirtableException("Property "+namePhotoAttribute+" should be null!");
                            }           
                        }
                    }
                }
            }  
        }               
    }
    
    /**
     * 
     * Get the String Id from the item.
     * 
     * @param item
     * @return
     * @throws AirtableException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException 
     */

    private String getIdOfItem(T item) throws AirtableException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        
        if(propertyExists(item,"id")){
            String id = BeanUtils.getProperty(item, "id");
            if(id != null){
                return id;
            }                
        }
        throw new AirtableException("Id of "+item+" not Found!");
    }

    /**
     * 
     * Filter the Fields of the PostRecord Object. Id and created Time are set to null so Object Mapper doesent convert them to JSON.
     * 
     * @param item
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException 
     */
    private T filterFields(T item) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
       
        System.out.println(item);
        
        Field[] attributes = item.getClass().getDeclaredFields();
        
        for (Field attribute : attributes) {
            String name = attribute.getName();
            if ((name.equals("id") || name.equals("createdTime")) && (BeanUtils.getProperty(item,name) != null)) {
                BeanUtilsBean.getInstance().getPropertyUtils().setProperty(item, name, null);       
            }
        }
            
        return item;
    }
}
