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
package de.hsesslingen.keim.efs.middleware.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Contains information about a mobility option, i.e. a possibility to travel
 * from A to B, whereas B can also be unset, meaning a trvel that starts from A
 * with an unknown destination at this point in time.
 *
 * @author boesch, K.Sivarasah
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Option implements Serializable {

    private static final long serialVersionUID = 2L;

    /**
     * The ID of the mobility service that this option originated from.
     */
    @NotNull
    @JsonProperty(required = true)
    private String serviceId;

    /**
     * This object contains the transport information of this option.
     */
    @Valid
    @NotNull
    @JsonProperty(required = true)
    private Leg leg;

    /**
     * Whether this option is started immediately upon creation of a booking.
     * The {@link BookingState.BOOKED} state is skipped in the created booking
     * and costs are counted from right after booking creation.
     */
    @NotNull
    @JsonProperty(required = true)
    private Boolean immediateStart;

    /**
     * A value that can be passed when creating a booking to reference this
     * {@link Option} unambiguously.
     */
    private String optionReference;

    /**
     * How long this option can be considered valid, i.e. useable for booking.
     */
    private ZonedDateTime validUntil;

    public Option(String serviceId, Leg leg, boolean immediateStart) {
        this.serviceId = serviceId;
        this.leg = leg;
        this.immediateStart = immediateStart;
    }

    @JsonIgnore
    public Option updateSelfFrom(Option other) {
        this.serviceId = other.serviceId;
        this.leg = other.leg;
        this.optionReference = other.optionReference;
        this.validUntil = other.validUntil;
        return this;
    }
}
