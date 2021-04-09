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
package middleware.provider;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import static java.time.ZonedDateTime.now;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import de.hsesslingen.keim.efs.middleware.provider.credentials.AbstractCredentials;
import de.hsesslingen.keim.efs.middleware.provider.credentials.CredentialsUtils;
import static de.hsesslingen.keim.efs.middleware.model.BookingState.BOOKED;
import de.hsesslingen.keim.efs.middleware.model.NewBooking;
import de.hsesslingen.keim.efs.middleware.model.Leg;
import de.hsesslingen.keim.efs.middleware.model.Place;
import static de.hsesslingen.keim.efs.middleware.model.Place.fromCoordinates;
import static de.hsesslingen.keim.efs.mobility.requests.MiddlewareRequest.TOKEN_HEADER;
import static de.hsesslingen.keim.efs.mobility.service.Mode.BICYCLE;
import java.time.LocalDateTime;
import middleware.MiddlewareTestApplication;
import middleware.MiddlewareTestBase;
import middleware.provider.credentials.TestCredential;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import org.springframework.test.context.ActiveProfiles;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author k.sivarasah 5 Oct 2019
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MiddlewareTestApplication.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BookingAndOptionsApiTest extends MiddlewareTestBase {

    /**
     * JSON-value of type {@link TestCredential}.
     */
    private static final String TOKEN_VALUE = "{\"id\":\"foobar\"}";

    @Autowired
    MockMvc mockMvc;

    @Test
    public void getOptionsTest_200() throws Exception {
        mockMvc.perform(get(OPTIONS_PATH).param("from", "1.234,2.345")
                .header("x-credentials", CredentialsUtils.toJsonString(getDummyCredentials())))
                .andExpect(status().is2xxSuccessful())
                .andDo(print());
    }

    @Test
    public void getOptionsTestIso_200() throws Exception {
        // Testing incomplete zoned date time decplarations...
        mockMvc.perform(get(OPTIONS_PATH).param("from", "1.234,2.345").param("startTime", LocalDateTime.now().plusHours(1).toString().substring(0, 16))
                .header("x-credentials", CredentialsUtils.toJsonString(getDummyCredentials())))
                .andExpect(status().is2xxSuccessful())
                .andDo(print());
    }

    @Test
    public void getOptionsTest_Invalid_Position_400() throws Exception {
        mockMvc.perform(get(OPTIONS_PATH).param("from", "1.234,2345"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.code", is("400")))
                .andDo(print());
    }

    @Test
    public void getOptionsTest_400() throws Exception {
        mockMvc.perform(get(OPTIONS_PATH))
                .andExpect(status().is4xxClientError())
                .andDo(print());
    }

    @Test
    public void getBookingByStateTest_200() throws Exception {
        mockMvc.perform(get(BOOKINGS_PATH).param("state", BOOKED.toString()).header(TOKEN_HEADER, TOKEN_VALUE))
                .andExpect(status().is2xxSuccessful())
                .andDo(print());
    }

    @Test
    public void getBookingByStateTest_400() throws Exception {
        mockMvc.perform(get(BOOKINGS_PATH).param("state", "UNKNOWN").header(TOKEN_HEADER, TOKEN_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("400")))
                .andDo(print());
    }

    @Test
    public void getBookingById_200() throws Exception {
        mockMvc.perform(get(BOOKINGS_PATH + "/{id}", "booking_id_001").header(TOKEN_HEADER, TOKEN_VALUE))
                .andExpect(status().is2xxSuccessful())
                .andDo(print());
    }

    @Test
    public void getBookings_200() throws Exception {
        mockMvc.perform(get(BOOKINGS_PATH).header(TOKEN_HEADER, TOKEN_VALUE))
                .andExpect(status().is2xxSuccessful())
                .andDo(print());
    }

    @Test
    public void putBookingWithoutId_MethodNotAllowed_405() throws Exception {
        mockMvc.perform(put(BOOKINGS_PATH).header(TOKEN_HEADER, TOKEN_VALUE))
                .andExpect(status().isMethodNotAllowed())
                .andDo(print());
    }

    @Test
    public void postBooking_Missing_RequestBody_400() throws Exception {
        mockMvc.perform(post(BOOKINGS_PATH).header(TOKEN_HEADER, TOKEN_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("400")))
                .andDo(print());
    }

    @Test
    public void postBooking_Invalid_Booking_400() throws Exception {
        NewBooking newBooking = new NewBooking();

        mockMvc.perform(
                post(BOOKINGS_PATH)
                        .content(mapper.writeValueAsBytes(newBooking))
                        .contentType(APPLICATION_JSON)
                        .header(TOKEN_HEADER, TOKEN_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.code", is("400")))
                .andDo(print());
    }

    @Test
    public void postBooking_Valid_Booking_200() throws Exception {
        NewBooking newBooking = getDummyNewBooking();

        mockMvc.perform(
                post(BOOKINGS_PATH)
                        .content(mapper.writeValueAsBytes(newBooking))
                        .contentType(APPLICATION_JSON)
                        .header(TOKEN_HEADER, TOKEN_VALUE))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    private NewBooking getDummyNewBooking() {
        Place dummyPlace = fromCoordinates("1.23,2.34");

        Leg dummyLeg = new Leg()
                .setMode(BICYCLE)
                .setFrom(dummyPlace)
                .setTo(dummyPlace)
                .setStartTime(now().plusSeconds(1000))
                .setEndTime(now().plusSeconds(10000));

        return new NewBooking().setLeg(dummyLeg);
    }

    private AbstractCredentials getDummyCredentials() {
        return new TestCredential("uuid_001", "demoLoginKey_101010");
    }
}
