package com.icm.alert_api.specs;

import com.icm.alert_api.models.AlertModel;
import org.springframework.data.jpa.domain.Specification;

import java.time.ZonedDateTime;
import java.util.Collection;

public final class AlertSpecifications {

    private AlertSpecifications() {}

    public static Specification<AlertModel> companyId(Long companyId) {
        return (root, query, cb) -> cb.equal(root.get("company").get("id"), companyId);
    }

    public static Specification<AlertModel> vehicleCodeIn(Collection<String> vehicleCodes) {
        return (root, query, cb) -> root.get("vehicleCode").in(vehicleCodes);
    }

    public static Specification<AlertModel> alertTypeIn(Collection<String> types) {
        return (root, query, cb) -> root.get("alertType").in(types);
    }

    public static Specification<AlertModel> acknowledged(Boolean ack) {
        return (root, query, cb) -> cb.equal(root.get("acknowledged"), ack);
    }

    public static Specification<AlertModel> eventTimeFrom(ZonedDateTime fromInclusive) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("eventTime"), fromInclusive);
    }

    public static Specification<AlertModel> eventTimeTo(ZonedDateTime toExclusive) {
        return (root, query, cb) -> cb.lessThan(root.get("eventTime"), toExclusive);
    }
}
