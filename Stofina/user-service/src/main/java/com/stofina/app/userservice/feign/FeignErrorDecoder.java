package com.stofina.app.userservice.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stofina.app.commondata.exception.InternalServerException;
import com.stofina.app.commondata.response.ErrorResponse;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@RequiredArgsConstructor
public class FeignErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        ErrorResponse errorResponse = null;
        try (InputStream bodyIs = response.body().asInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            errorResponse = mapper.readValue(bodyIs, ErrorResponse.class);
        } catch (IOException e) {
            log.error("Error decoding error response: {}", e.getMessage());
        }

        String message = errorResponse != null ? errorResponse.getMessage() : "Unknown error";
        log.error("Error from service: {}", message);

        switch (response.status()) {

            case 500: return new InternalServerException(message);
            default: return defaultErrorDecoder.decode(methodKey, response);
        }
    }
}
