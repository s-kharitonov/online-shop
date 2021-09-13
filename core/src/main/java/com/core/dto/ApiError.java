package com.core.dto;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ApiError(HttpStatus status, LocalDateTime date, List<String> messages) {

    private ApiError(Builder builder) {
        this(builder.status, builder.date, builder.messages);
    }

    public static class Builder {
        private HttpStatus status;
        private LocalDateTime date;
        private List<String> messages;

        public Builder status(HttpStatus status) {
            this.status = status;
            return this;
        }

        public Builder date(LocalDateTime date) {
            this.date = date;
            return this;
        }

        public Builder messages(List<String> messages) {
            this.messages = messages;
            return this;
        }

        public ApiError build() {
            return new ApiError(this);
        }
    }
}
