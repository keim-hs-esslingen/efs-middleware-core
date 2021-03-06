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
package de.hsesslingen.keim.efs.middleware.provider;

import de.hsesslingen.keim.efs.middleware.config.SwaggerAutoConfiguration;
import static de.hsesslingen.keim.efs.middleware.model.ICoordinates.parseAndValidate;
import de.hsesslingen.keim.efs.middleware.model.Place;
import io.swagger.annotations.Api;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author keim
 */
@Validated
@RestController
@ConditionalOnBean(IPlacesService.class)
@Api(tags = {SwaggerAutoConfiguration.PLACES_API_TAG})
public class PlacesApi extends ApiBase implements IPlacesApi {

    @Autowired
    private IPlacesService service;

    @Override
    public List<Place> searchPlaces(
            String query,
            String areaCenter,
            Integer radiusMeter,
            Integer limitTo,
            String token
    ) {
        logParams("searchPlaces", () -> array(
                "query", query,
                "areaCenter", areaCenter,
                "radiusMeter", radiusMeter,
                "limitTo", limitTo
        ));

        // Convert input params...
        var coordinates = parseAndValidate(areaCenter, () -> null);

        // Delegate search to user implemented PlacesService...
        var places = service.search(
                query, coordinates, radiusMeter, limitTo,
                parseToken(token)
        );

        logResult(places);

        return places;
    }

}
