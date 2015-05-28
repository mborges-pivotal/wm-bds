/*=========================================================================
 * Copyright (c) 2010-2014 Pivotal Software, Inc. All rights reserved.
 * This product is protected by U.S. and international copyright
 * and intellectual property laws. Pivotal products are covered by
 * one or more patents listed at http://www.gopivotal.com/patents.
 *=========================================================================
 */
package com.gopivotal.tola.wm.boot;


import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Appllication - Delta console
 * 
 * @author mborges
 *
 */
@ComponentScan({"com.gopivotal.tola.wm.boot","com.gopivotal.tola.wm.util.service"})
@SpringBootApplication
public class Application {
	
    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(Application.class, args);


        /*
        System.out.println("Let's inspect the beans provided by Spring Boot:");
        String[] beanNames = ctx.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
            System.out.println(beanName);
        }
        */
    }

}
