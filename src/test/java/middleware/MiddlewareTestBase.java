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
package middleware;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.hsesslingen.keim.efs.middleware.common.LegBaseItem;
import de.hsesslingen.keim.efs.middleware.common.Options;
import de.hsesslingen.keim.efs.middleware.common.Place;
import de.hsesslingen.keim.efs.middleware.common.TypeOfAsset;
import de.hsesslingen.keim.efs.mobility.service.MobilityService;

/**
 * @author k.sivarasah 28 Nov 2019
 */
public class MiddlewareTestBase {

    public static final String BOOKINGS_PATH = "/api/bookings";
    public static final String OPTIONS_PATH = "/api/bookings/options";

    public static final String CONSUMER_API_BOOKINGS = "/consumer/api/bookings";
    public static final String CONSUMER_API_OPTIONS = "/consumer/api/bookings/options";

    public static final String SERVICE_ID = "demo";

    public static final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public Options getDummyOption(String serviceId, String from) {
        return new Options()
                .setLeg(new LegBaseItem().setServiceId(serviceId)
                        .setStartTime(Instant.now()).setEndTime(Instant.now())
                        .setFrom(new Place(from)))
                .setMeta(new TypeOfAsset().setName("dummy").setTypeID("type_id_001"));
    }

    public List<Options> getDummyOptions(String serviceId, String from) {
        return Arrays.asList(getDummyOption(serviceId, from));
    }

    public MobilityService getMobilityService(String serviceId) {
        return new MobilityService().setId(serviceId)
                .setProviderName("dummy-provider")
                .setServiceName("dummy-service")
                .setServiceUrl("http://dummy-service-url/api");
    }

    public <T> T convert(String content, Class<T> toClass) {
        try {
            return mapper.readValue(content, toClass);
        } catch (IOException e) {
            return null;
        }
    }

    public String toString(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (IOException e) {
            return null;
        }
    }
}