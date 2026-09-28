package com.healthcare.vitalsservice.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Writes mutating operations to a dedicated AUDIT logger so they can be shipped to an audit sink. */
@Component
public class AuditLogger {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void record(String action, String detail) {
        AUDIT.info("service=vitals-service action={} detail={}", action, detail);
    }
}
