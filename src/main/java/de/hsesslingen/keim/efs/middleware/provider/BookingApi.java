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
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import de.hsesslingen.keim.efs.middleware.model.Booking;
import de.hsesslingen.keim.efs.middleware.model.BookingAction;
import de.hsesslingen.keim.efs.middleware.model.BookingState;
import de.hsesslingen.keim.efs.middleware.model.NewBooking;
import de.hsesslingen.keim.efs.middleware.validation.OnCreate;
import io.swagger.annotations.Api;
import de.hsesslingen.keim.efs.middleware.validation.ConsistentBookingDateParams;

/**
 * @author boesch, K.Sivarasah
 */
@Validated
@RestController
@ConditionalOnBean({IBookingService.class})
@Api(tags = {SwaggerAutoConfiguration.BOOKING_API_TAG})
public class BookingApi extends ApiBase implements IBookingApi {

    private static final String MISSING_TOKEN_MSG = "The Booking-API requires a token, but none was provided.";

    @Autowired
    private IBookingService service;

    @Override
    public List<Booking> getBookings(BookingState state, String token) {
        logParams("getBookings", () -> array(
                "state", state
        ));

        var bookings = service.getBookings(
                state,
                parseRequiredToken(token, MISSING_TOKEN_MSG)
        );

        logResult(bookings);

        return bookings;
    }

    @Override
    public Booking getBookingById(String id, String token) {
        logParams("getBookingById", () -> array(
                "id", id
        ));

        var result = service.getBookingById(
                id,
                parseRequiredToken(token, MISSING_TOKEN_MSG)
        );

        logResult(result);

        return result;
    }

    @Override
    public Booking createNewBooking(
            @Validated(OnCreate.class) @Valid @ConsistentBookingDateParams NewBooking newBooking,
            String optionReference,
            String token
    ) {
        logParamsWithBody("createNewBooking", newBooking, () -> array(
                "optionReference", optionReference
        ));

        var result = service.createNewBooking(
                newBooking, optionReference,
                parseRequiredToken(token, MISSING_TOKEN_MSG)
        );

        logResult(result);

        return result;
    }

    @Override
    public Booking modifyBooking(
            String id,
            @Valid @ConsistentBookingDateParams Booking booking,
            String token
    ) {
        logParamsWithBody("modifyBooking", booking, () -> array(
                "id", id
        ));

        var result = service.modifyBooking(
                id,
                booking,
                parseRequiredToken(token, MISSING_TOKEN_MSG)
        );

        logResult(result);

        return result;
    }

    @Override
    public Booking performAction(
            String bookingId,
            BookingAction action,
            String secret,
            String token
    ) {
        logParams("performAction", () -> array(
                "bookingId", bookingId,
                "action", action,
                "secret", obfuscateConditional(secret)
        ));

        var result = service.performAction(
                bookingId, action, secret,
                parseRequiredToken(token, MISSING_TOKEN_MSG)
        );

        logResult(result);

        return result;
    }

}
