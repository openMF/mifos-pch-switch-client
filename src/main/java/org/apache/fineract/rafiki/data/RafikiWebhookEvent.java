package org.apache.fineract.rafiki.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RafikiWebhookEvent {

    private String id;
    private String type;
    private Map<String, Object> data;
}
