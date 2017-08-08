/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sybit.airtableandroid.converter;

import com.google.gson.internal.LinkedTreeMap;
import com.sybit.airtableandroid.vo.Thumbnail;
import javadz.beanutils.BeanUtils;
import javadz.beanutils.converters.AbstractConverter;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * org.apache.commons.beanutils.Converter implementaion
 * that handles conversion to and from Map&lt;String,T&gt; objects.
 *
 * <p>This implementation converts Map&lt;String,T&gt; to Map&lt;String,mapClass&gt;.
 * The mapClass can be set to the Class that is needed.</p>
 * 
 * @author fzr
 */
public class MapConverter extends AbstractConverter {

    private Class mapClass;

    /**
     * Converts the Input Object into a Map.
     * 
     *  
     * @param type The type of the Input Object
     * @param value The value of the Input Object
     * @return A Map
     * @throws Throwable 
     */

//    @Override
//    protected Object convertToType(Class aClass, Object o) throws Throwable {
//        return null;
//    }

    @Override
    protected Object convertToType(Class type, Object value) throws Throwable {
              
        Class sourceType = value.getClass();
        Map<String, Object> returnMap = new HashMap<>();
            
        if(value instanceof LinkedTreeMap){
            for (String key : ((LinkedTreeMap<String, Object>) value).keySet()) {
                Object instanz = this.mapClass.newInstance();
                Object val = ((LinkedTreeMap) value).get(key);   
                BeanUtils.setProperty(instanz,"name",key);
                for (String key2 : ((LinkedTreeMap<String, Object>) val).keySet()) {
                    Object val2 = ((LinkedTreeMap) val).get(key2);            
                    BeanUtils.setProperty(instanz,key2,val2);                                
                }           
                returnMap = toClassMap(sourceType,instanz,returnMap);
            }         
            return returnMap;
        }
        
        if(value instanceof String){
            return toStringMap(sourceType,value.toString(),returnMap);
        }
        
        final String stringValue = value.toString().trim();
        if (stringValue.length() == 0) {
            return handleMissing(type);
        }
            
        return toStringMap(sourceType,stringValue,returnMap);
    }
    
    /**
     * 
     * Default Conversion to specified Class.
     * 
     * @param type The Class of the type
     * @param value The value of the Object
     * @param returnMap A Map of all currently converted Objects
     * @return A Map
     */
    private Map<String,Object> toClassMap(final Class type, final Object value,Map<String, Object> returnMap) {
        
        if (type.equals(LinkedTreeMap.class)) {   
            if (value.getClass().equals(Thumbnail.class)) {
                returnMap.put(((Thumbnail)value).getName(),value);
            }     
            return returnMap;
        }
        
        return toStringMap(type,value.toString(),returnMap);
    }
    
    /**
     * 
     * Default toString Conversion.
     * 
     * @param type The Class of the type
     * @param value The String value 
     * @param returnMap A Map of all currently converted Objects
     * @return A Map
     */
    
     private Map<String,Object> toStringMap(final Class type, final String value,Map<String, Object> returnMap) {
                
        if (type.equals(String.class)) {      
            returnMap.put(value,value);
            return  returnMap;
        }
        
        returnMap.put(value,value);    
        return returnMap;
    }



    @Override
    protected Class<?> getDefaultType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Set-Method for the MapClass
     * 
     * @param aClass  The Parameter that is used
     */
    public void setMapClass(Class<Thumbnail> aClass) {
        this.mapClass = aClass;
    }
    
    /**
     * Get-Method for the MapClass
     * 
     * @return this.mapClass 
     */
    public Class getMapClass(){
        return this.mapClass;
    }
}
