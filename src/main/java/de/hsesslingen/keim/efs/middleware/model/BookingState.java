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

/**
 * The life-cycle state of a booking.
 *
 * @author boesch
 */
public enum BookingState {
    NEW, // Followed by: BOOKED | STARTED | UPDATEREQUESTED
    BOOKED, // Followed by: CANCELLED | STARTED | UPDATEREQUESTED
    CANCELLED, // Closed
    UPDATEREQUESTED, // Kept as before
    STARTED, // Followed by: PAUSED | ABORTED | FINISHED | UPDATEREQUESTED
    ABORTED, // Closed
    FINISHED, // Closed
    PAUSED; // Followed by: STARTED | ABORTED | FINISHED | UPDATEREQUESTED

    public boolean canAdvanceTo(BookingState next) {
        if (next == null) {
            return false;
        }

        switch (this) {
            case NEW:
                return next == UPDATEREQUESTED || next == BOOKED || next == STARTED;
            case BOOKED:
                return next == UPDATEREQUESTED || next == CANCELLED || next == STARTED;
            case STARTED:
                return next == UPDATEREQUESTED || next == PAUSED || next == ABORTED || next == FINISHED;
            case PAUSED:
                return next == UPDATEREQUESTED || next == STARTED || next == ABORTED || next == FINISHED;
            case UPDATEREQUESTED:
                return true;
            case CANCELLED:
            case FINISHED:
            case ABORTED:
            default:
                return false;
        }
    }

}
