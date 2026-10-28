package io.github.steventsai.permission.mybatis;

import io.github.steventsai.permission.PermissionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PermissionRequestFactoryTest {

    private final PermissionRequestFactory factory = PermissionRequestFactory.defaultFactory();

    @Test
    void supports_defaultRequestType() {
        assertThat(factory.supports(PermissionRequest.class)).isTrue();
        assertThat(factory.supports(DefaultPermissionRequest.class)).isTrue();
    }

    @Test
    void supports_concreteTypesWithLongConstructor() {
        assertThat(factory.supports(LongConstructorRequest.class)).isTrue();
        assertThat(factory.supports(PrimitiveLongConstructorRequest.class)).isTrue();
    }

    @Test
    void supports_unsupportedTypes() {
        assertThat(factory.supports(Object.class)).isFalse();
        assertThat(factory.supports(String.class)).isFalse();
        assertThat(factory.supports(NoConstructorRequest.class)).isFalse();
        assertThat(factory.supports(null)).isFalse();
    }

    @Test
    void create_withNullParameter_returnsDefaultRequest() {
        PermissionRequest request = factory.create(7L, null);

        assertThat(request).isInstanceOf(DefaultPermissionRequest.class);
        assertThat(request.operatorUserId()).isEqualTo(7L);
    }

    @Test
    void create_withInterfaceParameter_returnsDefaultRequest() throws NoSuchMethodException {
        PermissionRequest request = factory.create(7L, parameter("handleInterface", PermissionRequest.class));

        assertThat(request).isInstanceOf(DefaultPermissionRequest.class);
        assertThat(request.operatorUserId()).isEqualTo(7L);
    }

    @Test
    void create_withConcreteType_usesLongConstructor() throws NoSuchMethodException {
        PermissionRequest request = factory.create(
                7L, parameter("handleLongConstructor", LongConstructorRequest.class));

        assertThat(request).isInstanceOf(LongConstructorRequest.class);
        assertThat(request.operatorUserId()).isEqualTo(7L);
    }

    @Test
    void create_withPrimitiveLongConstructor_usesLongConstructor() throws NoSuchMethodException {
        PermissionRequest request = factory.create(
                7L, parameter("handlePrimitiveLongConstructor", PrimitiveLongConstructorRequest.class));

        assertThat(request).isInstanceOf(PrimitiveLongConstructorRequest.class);
        assertThat(request.operatorUserId()).isEqualTo(7L);
    }

    @Test
    void create_withUnsupportedType_throwsException() throws NoSuchMethodException {
        MethodParameter parameter = parameter("handleNoConstructor", NoConstructorRequest.class);

        assertThatThrownBy(() -> factory.create(7L, parameter))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported @CurrentPermission parameter type");
    }

    @Test
    void create_whenConstructorFails_throwsException() throws NoSuchMethodException {
        MethodParameter parameter = parameter("handleThrowing", ThrowingConstructorRequest.class);

        assertThatThrownBy(() -> factory.create(7L, parameter))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Failed to instantiate")
                .hasRootCauseInstanceOf(IllegalStateException.class);
    }

    private static MethodParameter parameter(String methodName, Class<?> parameterType)
            throws NoSuchMethodException {
        return MethodParameter.forExecutable(
                Controller.class.getMethod(methodName, parameterType), 0);
    }

    static class Controller {
        public void handleInterface(PermissionRequest request) {
        }

        public void handleLongConstructor(LongConstructorRequest request) {
        }

        public void handlePrimitiveLongConstructor(PrimitiveLongConstructorRequest request) {
        }

        public void handleNoConstructor(NoConstructorRequest request) {
        }

        public void handleThrowing(ThrowingConstructorRequest request) {
        }
    }

    public static final class LongConstructorRequest implements PermissionRequest {
        private final Long operatorUserId;

        public LongConstructorRequest(Long operatorUserId) {
            this.operatorUserId = operatorUserId;
        }

        @Override
        public Long operatorUserId() {
            return operatorUserId;
        }
    }

    public static final class PrimitiveLongConstructorRequest implements PermissionRequest {
        private final Long operatorUserId;

        public PrimitiveLongConstructorRequest(long operatorUserId) {
            this.operatorUserId = operatorUserId;
        }

        @Override
        public Long operatorUserId() {
            return operatorUserId;
        }
    }

    public static final class NoConstructorRequest implements PermissionRequest {
        @Override
        public Long operatorUserId() {
            return 1L;
        }
    }

    public static final class ThrowingConstructorRequest implements PermissionRequest {
        public ThrowingConstructorRequest(Long operatorUserId) {
            throw new IllegalStateException("boom");
        }

        @Override
        public Long operatorUserId() {
            return null;
        }
    }
}
