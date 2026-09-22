package com.smartspace.booking.exception;

import com.smartspace.common.exception.DomainException;

public class SlotUnavailableException extends DomainException {
    public SlotUnavailableException() {
        super("SLOT_UNAVAILABLE", "One or more requested time slots are already booked or blocked.");
    }
}
