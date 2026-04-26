package com.hft.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME, 
    include = JsonTypeInfo.As.PROPERTY, 
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = OrderAccepted.class, name = "OrderAccepted"),
    @JsonSubTypes.Type(value = OrderMatched.class, name = "OrderMatched"),
    @JsonSubTypes.Type(value = OrderCancelled.class, name = "OrderCancelled")
})
public interface DomainEvent {
}
