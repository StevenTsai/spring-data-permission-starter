package com.github.steventsai.permission.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.assertj.core.api.Assertions.assertThat;

class MissingPermissionExceptionTest {

    @Test
    void responseStatusAnnotation_mapsToBadRequest() {
        ResponseStatus responseStatus = MissingPermissionException.class.getAnnotation(ResponseStatus.class);

        assertThat(responseStatus).isNotNull();
        assertThat(responseStatus.value()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void constructors_preserveMessageAndCause() {
        RuntimeException cause = new RuntimeException("boom");
        MissingPermissionException exception = new MissingPermissionException("missing", cause);

        assertThat(new MissingPermissionException("missing")).hasMessage("missing");
        assertThat(exception).hasMessage("missing").hasCause(cause);
    }
}
