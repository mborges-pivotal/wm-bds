/*=========================================================================
 * Copyright (c) 2010-2014 Pivotal Software, Inc. All rights reserved.
 * This product is protected by U.S. and international copyright
 * and intellectual property laws. Pivotal products are covered by
 * one or more patents listed at http://www.gopivotal.com/patents.
 *=========================================================================
 */
package com.gopivotal.tola.wm.util.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component("PropertyMapper")
public class PropertyMapper {

    @Autowired
    ApplicationContext applicationContext;

    public Map<String, Object> startWith(String qualifier, String startWith) {
        return startWith(qualifier, startWith, false);
    }

    public Map<String, Object> startWith(String qualifier, String startWith, boolean removeStartWith) {
        Map<String, Object> result = new HashMap<String, Object>();

        Object obj = applicationContext.getBean(qualifier);
        if (obj instanceof Properties) {
            Properties mobileProperties = (Properties)obj;

            if (mobileProperties != null) {
                for (Entry<Object, Object> e : mobileProperties.entrySet()) {
                    Object oKey = e.getKey();
                    if (oKey instanceof String) {
                        String key = (String)oKey;
                        if (((String) oKey).startsWith(startWith)) {
                            if (removeStartWith) 
                                key = key.substring(startWith.length());
                            result.put(key, e.getValue());
                        }
                    }
                }
            }
        }

        return result;
    }
}
