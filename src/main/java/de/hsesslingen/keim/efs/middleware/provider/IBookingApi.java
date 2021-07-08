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

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import de.hsesslingen.keim.efs.middleware.model.Booking;
import de.hsesslingen.keim.efs.middleware.model.BookingState;
import de.hsesslingen.keim.efs.middleware.model.BookingAction;
import de.hsesslingen.keim.efs.middleware.model.NewBooking;
import de.hsesslingen.keim.efs.middleware.model.Option;
import static de.hsesslingen.keim.efs.middleware.provider.ITokensApi.TOKEN_DESCRIPTION;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.RequestHeader;
import javax.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import de.hsesslingen.keim.efs.middleware.validation.ConsistentBookingDateParams;
import de.hsesslingen.keim.efs.middleware.validation.OnCreate;
import de.hsesslingen.keim.efs.mobility.config.EfsSwaggerApiResponseSupport;
import de.hsesslingen.keim.efs.mobility.service.MobilityService;
import de.hsesslingen.keim.efs.mobility.requests.MiddlewareRequest;
import static de.hsesslingen.keim.efs.mobility.requests.MiddlewareRequest.TOKEN_HEADER;
import de.hsesslingen.keim.efs.mobility.requests.MiddlewareRequestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;

/**
 * This API provides endpoints for creating and managing bookings at mobility
 * service providers.
 * <p>
 * <h3>Additional note:</h3>
 * This interface also provides static methods for building HTTP requests, that
 * match the endpoints defined in it. They are build upon the
 * {@link MiddlewareRequest} class.
 *
 * @author k.sivarasah 17 Oct 2019
 */
@EfsSwaggerApiResponseSupport
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public interface IBookingApi {

    String PATH = "/bookings";

    /**
     * Returns a list of bookings associated with the account that is
     * represented by the given token.
     *
     * @param state An optional state by which to filter the bookings.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ITokensApi} for more
     * details on tokens. This value is almost certainly required by all
     * mobility service providers for querying the {@link IBookingApi}.
     * @return List of {@link Booking}
     */
    @GetMapping(PATH)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Get Bookings", notes = "Returns a list of Booking optionally filtered by their state.")
    List<Booking> getBookings(
            @ApiParam("An optional state by which to filter the bookings.")
            @RequestParam(required = false) BookingState state,
            //
            @ApiParam(value = TOKEN_DESCRIPTION)
            @RequestHeader(name = TOKEN_HEADER, required = false) String token
    );

    /**
     * Gets a particular {@link Booking} using the booking id.
     *
     * @param id The ID of the booking which shall be retrieved.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ITokensApi} for more
     * details on tokens. This value is almost certainly required by all
     * mobility service providers for querying the {@link IBookingApi}.
     * @return The {@link Booking} object
     */
    @GetMapping(PATH + "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Get Booking by Id", notes = "Returns the Booking with the given unique booking id")
    Booking getBookingById(
            @ApiParam("The ID of the booking which shall be retrieved.")
            @PathVariable String id,
            //
            @ApiParam(value = TOKEN_DESCRIPTION)
            @RequestHeader(name = TOKEN_HEADER, required = false) String token
    );

    /**
     * Creates a new booking and returns it.
     *
     * @param newBooking The {@link NewBooking} that should be created.
     * @param optionReference An optional reference to an {@link Option} that
     * unambiguously references this option for booking. This reference is
     * sometimes given in instances of {@link Option}.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ITokensApi} for more
     * details on tokens. This value is almost certainly required by all
     * mobility service providers for querying the {@link IBookingApi}.
     * @return {@link Booking} that was created
     */
    @PostMapping(PATH)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Create a new Booking", notes = "Creates a new Booking for a service-provider in BOOKED or STARTED state using the provided NewBooking object and returns it")
    Booking createNewBooking(
            @ApiParam("The booking that should be created.")
            @RequestBody @Validated(OnCreate.class) @Valid @ConsistentBookingDateParams NewBooking newBooking,
            //
            @ApiParam("An optional reference to an \"Option\" that unambiguously references this option for booking. This reference is sometimes given in instances of \"Option\".")
            @RequestParam(required = false) String optionReference,
            //
            @ApiParam(value = TOKEN_DESCRIPTION)
            @RequestHeader(name = TOKEN_HEADER, required = false) String token
    );

    /**
     * Updates an existing {@link Booking} with new details.
     *
     * @param id The ID of the booking that shall be modified.
     * @param booking The {@link Booking} object containing modified data
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ITokensApi} for more
     * details on tokens. This value is almost certainly required by all
     * mobility service providers for querying the {@link IBookingApi}.
     * @return the modified {@link Booking} object
     */
    @PutMapping(PATH + "/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ApiOperation(value = "Modify a Booking", notes = "Updates an existing Booking with the provided details")
    Booking modifyBooking(
            @ApiParam("The ID of the booking that shall be modified.")
            @PathVariable String id,
            //
            @ApiParam("The Booking object containing the modified data.")
            @RequestBody @Valid @ConsistentBookingDateParams Booking booking,
            //
            @ApiParam(value = TOKEN_DESCRIPTION)
            @RequestHeader(name = TOKEN_HEADER, required = false) String token
    );

    /**
     * Can be used to perform actions on bookings.
     *
     * @param bookingId The ID of the booking on which to perform the action.
     * @param action The action that should be performed on the booking with the
     * given {@link bookingId}.
     * @param secret An optional secret that might be required by some mobility
     * service providers to perform this action. (e.g. a PIN)
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ITokensApi} for more
     * details on tokens. This value is almost certainly required by all
     * mobility service providers for querying the {@link IBookingApi}.
     * @return
     */
    @PostMapping(PATH + "/{bookingId}/action/{action}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Perform an action on a booking", notes = "Performs the given action on a booking.")
    Booking performAction(
            @ApiParam("The ID of the booking on which to perform the action.")
            @PathVariable String bookingId,
            //
            @ApiParam("The action that should be performed on the booking with the given \"bookingId\".")
            @PathVariable BookingAction action,
            //
            @ApiParam("An optional secret that might be required by some mobility service providers to perform this action. (e.g. a PIN)")
            @RequestParam(required = false) String secret,
            @ApiParam(value = TOKEN_DESCRIPTION)
            @RequestHeader(name = TOKEN_HEADER, required = false) String token
    );

    /**
     * Assembles a request, matching the {@code GET /bookings} endpoint, for the
     * service with the given url using the given token. See
     * {@link IBookingApi#getBookings(BookingState, String)} for JavaDoc on that
     * endpoint.
     * <p>
     * The returned request can be sent using {@code request.go()} which will
     * return a {@link ResponseEntity}.
     *
     * @param serviceUrl The base url of the mobility service that should be
     * queried. Use {@link MobilityService#getServiceUrl()} to get this url.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ITokensApi} for more
     * details on tokens. This value is almost certainly required by all
     * mobility service providers for querying the {@link IBookingApi}.
     * @param requestTemplate The template that should be used as foundation for
     * building the request.
     * @return
     */
    static MiddlewareRequest<List<Booking>> buildGetBookingsRequest(
            String serviceUrl,
            String token,
            MiddlewareRequestTemplate requestTemplate
    ) {
        return requestTemplate
                .get(serviceUrl + PATH)
                .token(token)
                .expect(new ParameterizedTypeReference<>() {
                });
    }

    /**
     * Assembles a request, matching the {@code GET /bookings} endpoint, for the
     * service with the given url using the given token. See
     * {@link IBookingApi#getBookings(de.hsesslingen.keim.efs.middleware.model.BookingState, String)}
     * for JavaDoc on that endpoint.
     * <p>
     * The returned request can be sent using {@code request.go()} which will
     * return a {@link ResponseEntity}.
     *
     * @param serviceUrl The base url of the mobility service that should be
     * queried. Use {@link MobilityService#getServiceUrl()} to get this url.
     * @param state An optional state by which to filter the bookings.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ITokensApi} for more
     * details on tokens. This value is almost certainly required by all
     * mobility service providers for querying the {@link IBookingApi}.
     * @param requestTemplate The template that should be used as foundation for
     * building the request.
     * @return
     */
    static MiddlewareRequest<List<Booking>> buildGetBookingsRequest(
            String serviceUrl,
            BookingState state,
            String token,
            MiddlewareRequestTemplate requestTemplate
    ) {
        return buildGetBookingsRequest(serviceUrl, token, requestTemplate).query("state", state);
    }

    /**
     * Assembles a request, matching the {@code GET /bookings/{bookingId}}
     * endpoint, for the service with the given url using the given token. See
     * {@link IBookingApi#getBookingById(String, String)} for JavaDoc on that
     * endpoint.
     * <p>
     * The returned request can be sent using {@code request.go()} which will
     * return a {@link ResponseEntity}.
     *
     * @param serviceUrl The base url of the mobility service that should be
     * queried. Use {@link MobilityService#getServiceUrl()} to get this url.
     * @param id The ID of the booking which shall be retrieved.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ITokensApi} for more
     * details on tokens. This value is almost certainly required by all
     * mobility service providers for querying the {@link IBookingApi}.
     * @param requestTemplate The template that should be used as foundation for
     * building the request.
     * @return
     */
    static MiddlewareRequest<Booking> buildGetBookingByIdRequest(
            String serviceUrl,
            String id,
            String token,
            MiddlewareRequestTemplate requestTemplate
    ) {
        return requestTemplate
                .get(serviceUrl + PATH + "/" + id)
                .token(token)
                .expect(Booking.class);
    }

    /**
     * Assembles a request, matching the {@code POST /bookings} endpoint, for
     * the service with the given url using the given token. See
     * {@link IBookingApi#createNewBooking(de.hsesslingen.keim.efs.middleware.model.NewBooking, String)}
     * for JavaDoc on that endpoint.
     * <p>
     * The returned request can be sent using {@code request.go()} which will
     * return a {@link ResponseEntity}.
     *
     * @param serviceUrl The base url of the mobility service that should be
     * queried. Use {@link MobilityService#getServiceUrl()} to get this url.
     * @param newBooking The {@link NewBooking} that should be created.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ITokensApi} for more
     * details on tokens. This value is almost certainly required by all
     * mobility service providers for querying the {@link IBookingApi}.
     * @param requestTemplate The template that should be used as foundation for
     * building the request.
     * @return
     */
    static MiddlewareRequest<Booking> buildCreateNewBookingRequest(
            String serviceUrl,
            NewBooking newBooking,
            String token,
            MiddlewareRequestTemplate requestTemplate
    ) {
        return requestTemplate
                .post(serviceUrl + PATH)
                .token(token)
                .body(newBooking)
                .expect(Booking.class);
    }

    /**
     * Assembles a request, matching the {@code POST /bookings} endpoint, for
     * the service with the given url using the given token.See
     * {@link IBookingApi#createNewBooking(de.hsesslingen.keim.efs.middleware.model.NewBooking, String)}
     * for JavaDoc on that endpoint.<p>
     * The returned request can be sent using {@code request.go()} which will
     * return a {@link ResponseEntity}.
     *
     * @param serviceUrl The base url of the mobility service that should be
     * queried. Use {@link MobilityService#getServiceUrl()} to get this url.
     * @param newBooking The {@link NewBooking} that should be created.
     * @param optionReference An optional reference to an {@link Option} that
     * unambiguously references this option for booking. This reference is
     * sometimes given in instances of {@link Option}.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ITokensApi} for more
     * details on tokens. This value is almost certainly required by all
     * mobility service providers for querying the {@link IBookingApi}.
     * @param requestTemplate The template that should be used as foundation for
     * building the request.
     * @return
     */
    static MiddlewareRequest<Booking> buildCreateNewBookingRequest(
            String serviceUrl,
            NewBooking newBooking,
            String optionReference,
            String token,
            MiddlewareRequestTemplate requestTemplate
    ) {
        return buildCreateNewBookingRequest(serviceUrl, newBooking, token, requestTemplate)
                .query("optionReference", optionReference);
    }

    /**
     * Assembles a request, matching the {@code PUT /bookings/{bookingId}}
     * endpoint, for the service with the given url using the given token. See
     * {@link IBookingApi#modifyBooking(String, de.hsesslingen.keim.efs.middleware.model.Booking, String)}
     * for JavaDoc on that endpoint.
     * <p>
     * The returned request can be sent using {@code request.go()} which will
     * return a {@link ResponseEntity}.
     *
     * @param serviceUrl The base url of the mobility service that should be
     * queried. Use {@link MobilityService#getServiceUrl()} to get this url.
     * @param booking The {@link Booking} object containing modified data
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ITokensApi} for more
     * details on tokens. This value is almost certainly required by all
     * mobility service providers for querying the {@link IBookingApi}.
     * @param requestTemplate The template that should be used as foundation for
     * building the request.
     * @return
     */
    static MiddlewareRequest<Booking> buildModifyBookingRequest(
            String serviceUrl,
            Booking booking,
            String token,
            MiddlewareRequestTemplate requestTemplate
    ) {
        return requestTemplate
                .put(serviceUrl + PATH + "/" + booking.getId())
                .token(token)
                .body(booking)
                .expect(Booking.class);
    }

    /**
     * Assembles a request, matching the
     * {@code POST /bookings/{bookingId}/action/{action}} endpoint, for the
     * service with the given url using the given token. See
     * {@link IBookingApi#performAction(String, de.hsesslingen.keim.efs.middleware.model.BookingAction, String, String)}
     * for JavaDoc on that endpoint.
     * <p>
     * The returned request can be sent using {@code request.go()} which will
     * return a {@link ResponseEntity}.
     *
     * @param serviceUrl The base url of the mobility service that should be
     * queried. Use {@link MobilityService#getServiceUrl()} to get this url.
     * @param bookingId The ID of the booking on which to perform the action.
     * @param action The action that should be performed on the booking with the
     * given {@link bookingId}.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ITokensApi} for more
     * details on tokens. This value is almost certainly required by all
     * mobility service providers for querying the {@link IBookingApi}.
     * @param requestTemplate The template that should be used as foundation for
     * building the request.
     * @return
     */
    static MiddlewareRequest<Booking> buildPerformActionRequest(
            String serviceUrl,
            String bookingId,
            BookingAction action,
            String token,
            MiddlewareRequestTemplate requestTemplate
    ) {
        return requestTemplate
                .post(serviceUrl + PATH + "/" + bookingId + "/action/" + action.toString())
                .token(token)
                .expect(Booking.class);
    }

    /**
     * Assembles a request, matching the
     * {@code POST /bookings/{bookingId}/action/{action}} endpoint, for the
     * service with the given url using the given token. See
     * {@link IBookingApi#performAction(String, de.hsesslingen.keim.efs.middleware.model.BookingAction, String, String)}
     * for JavaDoc on that endpoint.
     * <p>
     * The returned request can be sent using {@code request.go()} which will
     * return a {@link ResponseEntity}.
     *
     * @param serviceUrl The base url of the mobility service that should be
     * queried. Use {@link MobilityService#getServiceUrl()} to get this url.
     * @param bookingId The ID of the booking on which to perform the action.
     * @param action The action that should be performed on the booking with the
     * given {@link bookingId}.
     * @param secret An optional secret that might be required by some mobility
     * service providers to perform this action. (e.g. a PIN)
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ITokensApi} for more
     * details on tokens. This value is almost certainly required by all
     * mobility service providers for querying the {@link IBookingApi}.
     * @param requestTemplate The template that should be used as foundation for
     * building the request.
     * @return
     */
    static MiddlewareRequest<Booking> buildPerformActionRequest(
            String serviceUrl,
            String bookingId,
            BookingAction action,
            String secret,
            String token,
            MiddlewareRequestTemplate requestTemplate
    ) {
        return buildPerformActionRequest(serviceUrl, bookingId, action, token, requestTemplate)
                .query("secret", secret);
    }
}
