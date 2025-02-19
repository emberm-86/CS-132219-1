package com.test.assessment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LogEvent {

  @JsonProperty String id;
  @JsonProperty String state;
  @JsonProperty String type;
  @JsonProperty String host;
  @JsonProperty Long timestamp;

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    String id;
    String state;
    String type;
    String host;
    Long timestamp;

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder state(String state) {
      this.state = state;
      return this;
    }

    public Builder type(String type) {
      this.type = type;
      return this;
    }

    public Builder host(String host) {
      this.host = host;
      return this;
    }

    public Builder timestamp(Long timestamp) {
      this.timestamp = timestamp;
      return this;
    }

    public LogEvent build() {
      final LogEvent logEvent = new LogEvent();
      logEvent.setId(id);
      logEvent.setState(state);
      logEvent.setType(type);
      logEvent.setHost(host);
      logEvent.setTimestamp(timestamp);
      return logEvent;
    }
  }
}
