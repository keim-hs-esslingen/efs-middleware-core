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

import de.hsesslingen.keim.efs.middleware.utils.AbstractEfsRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

/**
 * @author k.sivarasah 26 Sep 2019
 */
@Configuration
public class RestUtilsAutoConfiguration {

    @Bean("restTemplateLoadBalanced")
    @LoadBalanced
    public RestTemplate restTemplateLoadBalanced(RestTemplateBuilder restTemplateBuilder, ResponseErrorHandler responseErrorHandler) {
        return restTemplateBuilder
                .errorHandler(responseErrorHandler)
                .build();
    }

    @Bean("restTemplateSimple")
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder, ResponseErrorHandler responseErrorHandler) {
        return restTemplateBuilder
                .errorHandler(responseErrorHandler)
                .build();
    }

    @Bean
    public Object efsRequest(@Qualifier("restTemplateLoadBalanced") RestTemplate restTemplateLoadBalanced,
            @Qualifier("restTemplateSimple") RestTemplate restTemplateSimple,
            @Value("${efs.security.api-key.value:null}") String apiKey) {
        AbstractEfsRequest.configure(restTemplateLoadBalanced, restTemplateSimple, apiKey);
        return null;
    }

    @Bean
    @ConditionalOnMissingBean
    public ResponseErrorHandler responseErrorHandler() {
        return new DefaultResponseErrorHandler();
    }

}