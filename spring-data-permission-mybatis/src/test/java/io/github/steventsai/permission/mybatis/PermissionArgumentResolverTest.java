package io.github.steventsai.permission.mybatis;

import io.github.steventsai.permission.PermissionRequest;
import io.github.steventsai.permission.exception.MissingPermissionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PermissionArgumentResolverTest {

    private PermissionArgumentResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new PermissionArgumentResolver();
    }

    @Test
    void supportsParameter_withAnnotation_returnsTrue() throws NoSuchMethodException {
        MethodParameter param = MethodParameter.forExecutable(
                SampleController.class.getMethod("handle", PermissionRequest.class), 0);

        assertThat(resolver.supportsParameter(param)).isTrue();
    }

    @Test
    void supportsParameter_withoutAnnotation_returnsFalse() throws NoSuchMethodException {
        MethodParameter param = MethodParameter.forExecutable(
                SampleController.class.getMethod("other", String.class), 0);

        assertThat(resolver.supportsParameter(param)).isFalse();
    }

    @Test
    void resolveArgument_validHeader_returnsRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Id", "42");
        NativeWebRequest webRequest = new ServletWebRequest(request);

        Object result = resolver.resolveArgument(null, null, webRequest, null);

        assertThat(result).isInstanceOf(PermissionRequest.class);
        assertThat(((PermissionRequest) result).operatorUserId()).isEqualTo(42L);
    }

    @Test
    void resolveArgument_missingHeader_throwsException() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        NativeWebRequest webRequest = new ServletWebRequest(request);

        assertThatThrownBy(() -> resolver.resolveArgument(null, null, webRequest, null))
                .isInstanceOf(MissingPermissionException.class)
                .hasMessageContaining("Missing user ID");
    }

    @Test
    void resolveArgument_blankHeader_throwsException() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Id", "  ");
        NativeWebRequest webRequest = new ServletWebRequest(request);

        assertThatThrownBy(() -> resolver.resolveArgument(null, null, webRequest, null))
                .isInstanceOf(MissingPermissionException.class)
                .hasMessageContaining("Missing user ID");
    }

    @Test
    void resolveArgument_invalidNumber_throwsException() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Id", "not-a-number");
        NativeWebRequest webRequest = new ServletWebRequest(request);

        assertThatThrownBy(() -> resolver.resolveArgument(null, null, webRequest, null))
                .isInstanceOf(MissingPermissionException.class)
                .hasMessageContaining("Invalid user ID");
    }

    @Test
    void resolveArgument_customHeader_usesCustomHeader() {
        PermissionArgumentResolver customResolver = new PermissionArgumentResolver("X-Custom-User");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Custom-User", "99");
        NativeWebRequest webRequest = new ServletWebRequest(request);

        Object result = customResolver.resolveArgument(null, null, webRequest, null);

        assertThat(((PermissionRequest) result).operatorUserId()).isEqualTo(99L);
    }

    @Test
    void supportsParameter_customRequestTypeWithLongConstructor_returnsTrue()
            throws NoSuchMethodException {
        MethodParameter param = MethodParameter.forExecutable(
                SampleController.class.getMethod("handleCustom", CustomRequest.class), 0);

        assertThat(resolver.supportsParameter(param)).isTrue();
    }

    @Test
    void supportsParameter_requestTypeWithoutSupportedConstructor_returnsFalse()
            throws NoSuchMethodException {
        MethodParameter param = MethodParameter.forExecutable(
                SampleController.class.getMethod("handleUnsupported", UnsupportedRequest.class), 0);

        assertThat(resolver.supportsParameter(param)).isFalse();
    }

    @Test
    void supportsParameter_factoryRejectsType_returnsFalse() throws NoSuchMethodException {
        PermissionRequestFactory rejectingFactory = new PermissionRequestFactory() {
            @Override
            public PermissionRequest create(Long userId, MethodParameter parameter) {
                throw new AssertionError("must not be called");
            }

            @Override
            public boolean supports(Class<?> parameterType) {
                return false;
            }
        };
        PermissionArgumentResolver rejectingResolver =
                new PermissionArgumentResolver("X-User-Id", rejectingFactory);
        MethodParameter param = MethodParameter.forExecutable(
                SampleController.class.getMethod("handle", PermissionRequest.class), 0);

        assertThat(rejectingResolver.supportsParameter(param)).isFalse();
    }

    @Test
    void resolveArgument_customRequestFactory_createsCustomRequest() throws NoSuchMethodException {
        PermissionArgumentResolver customResolver = new PermissionArgumentResolver(
                "X-User-Id", (userId, parameter) -> new CustomRequest(userId));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Id", "123");
        NativeWebRequest webRequest = new ServletWebRequest(request);
        MethodParameter param = MethodParameter.forExecutable(
                SampleController.class.getMethod("handleCustom", CustomRequest.class), 0);

        Object result = customResolver.resolveArgument(param, null, webRequest, null);

        assertThat(result).isInstanceOf(CustomRequest.class);
        assertThat(((PermissionRequest) result).operatorUserId()).isEqualTo(123L);
    }

    @Test
    void constructor_nullHeader_throwsException() {
        assertThatThrownBy(() -> new PermissionArgumentResolver(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("userIdHeader");
    }

    @Test
    void constructor_nullFactory_throwsException() {
        assertThatThrownBy(() -> new PermissionArgumentResolver("X-User-Id", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("requestFactory");
    }

    // Helper class for testing supportsParameter
    static class SampleController {
        public void handle(@CurrentPermission PermissionRequest request) {}
        public void handleCustom(@CurrentPermission CustomRequest request) {}
        public void handleUnsupported(@CurrentPermission UnsupportedRequest request) {}
        public void other(String param) {}
    }

    public static class CustomRequest implements PermissionRequest {
        private final Long operatorUserId;

        public CustomRequest(Long operatorUserId) {
            this.operatorUserId = operatorUserId;
        }

        @Override
        public Long operatorUserId() {
            return operatorUserId;
        }
    }

    public static class UnsupportedRequest implements PermissionRequest {
        @Override
        public Long operatorUserId() {
            return 1L;
        }
    }
}
