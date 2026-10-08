package com.smartspace.dev;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

public class MutableClock extends Clock {

    private final ZoneId zone;
    private Duration offset = Duration.ZERO;

    public MutableClock(ZoneId zone) {
        this.zone = zone;
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        if (zone.equals(this.zone)) {
            return this;
        }
        MutableClock newClock = new MutableClock(zone);
        newClock.offset = this.offset;
        return newClock;
    }

    @Override
    public Instant instant() {
        return Clock.system(zone).instant().plus(offset);
    }

    public void advanceBy(Duration duration) {
        this.offset = this.offset.plus(duration);
    }
    
    public void reset() {
        this.offset = Duration.ZERO;
    }
}
