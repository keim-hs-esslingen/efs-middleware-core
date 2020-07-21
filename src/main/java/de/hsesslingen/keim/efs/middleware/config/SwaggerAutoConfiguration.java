/*
 * MIT License
 * 
 * Copyright (c) 2020 Hochschule Esslingen
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE. 
 */
package de.hsesslingen.keim.efs.middleware.config;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Predicates;

import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Default Configuration for SwaggerUI to expose RestApis implemented in the middleware-library
 * @author k.sivarasah
 * 10 Oct 2019
 */

@Configuration
@EnableSwagger2
@ConditionalOnClass(Docket.class)
public class SwaggerAutoConfiguration {

	@Value("${spring.application.name:}")
	private String serviceName;
	
	@Value("${efs.middleware.provider-api.enabled:true}")
	private boolean providerEnabled;
	
	@Value("${efs.middleware.consumer-api.enabled:true}")
	private boolean consumerEnabled;
	
	public static final Tag CONSUMER_API_TAG = new Tag("Consumer Api", "(consumer) APIs provided for consuming mobility services", 1);
	public static final Tag BOOKING_API_TAG = new Tag("Booking Api", "(provider) Booking related APIs with CRUD functionality", 2);
	public static final Tag PLANNING_API_TAG = new Tag("Planning Api", "(provider) Gives information about transport asset availability and pricing", 3);
			
	public static final Tag[] PROVIDER_TAGS = new Tag[] { BOOKING_API_TAG, PLANNING_API_TAG };
	
	@Bean
	@ConditionalOnMissingBean
	public Docket api() {
		Docket docket = new Docket(DocumentationType.SWAGGER_2)
				.select()
				.apis(Predicates.or(RequestHandlerSelectors.basePackage("de.hsesslingen.keim.efs.middleware.controller"),
						RequestHandlerSelectors.basePackage("de.hsesslingen.keim.efs.adapter")))
				.paths(PathSelectors.any())
				.build()
				.globalOperationParameters(Arrays.asList(new ParameterBuilder()
						.name(ApiConstants.API_KEY_HEADER_NAME)
						.description(ApiConstants.API_KEY_DESC)
						.modelRef(new ModelRef("string"))
						.parameterType("header")
						.required(false).build()));
		
		return setTags(docket).apiInfo(apiInfo());
	}
	
	private Docket setTags(Docket docket) {
		if(providerEnabled && consumerEnabled) {
			return docket.tags(CONSUMER_API_TAG, PROVIDER_TAGS);
		} else if(providerEnabled) {
			return docket.tags(BOOKING_API_TAG, PLANNING_API_TAG);
		} else {
			return docket.tags(CONSUMER_API_TAG);
		}
	}
	
	public static Tag[] getTags() {
		return new Tag[] { CONSUMER_API_TAG, BOOKING_API_TAG, PLANNING_API_TAG };
	}
	
	
	protected ApiInfo apiInfo() {
		String serviceInfo = String.format("Middleware Service (%s)", serviceName);
		return new ApiInfo(serviceInfo,
				"API description of " + serviceInfo, "V0.1", null,
				new Contact("Hochschule Esslingen", "https://www.hs-esslingen.de", null), 
				null, null, Collections.emptyList());
	}

}