package io.github.steventsai.permission.mybatis;

import io.github.steventsai.permission.DataPermissionContext;
import io.github.steventsai.permission.PermissionRequest;
import io.github.steventsai.permission.PermissionResolver;
import io.github.steventsai.permission.StandardDataScope;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionWebAutoConfigurationTest {

    private final WebApplicationContextRunner webContextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    MybatisPermissionAutoConfiguration.class,
                    PermissionWebAutoConfiguration.class))
            .withBean(PermissionResolver.class, TestPermissionResolver::new);

    @Test
    void headerResolverIsDisabledByDefault() {
        webContextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(PermissionArgumentResolver.class);
            assertThat(context).doesNotHaveBean(WebMvcConfigurer.class);
        });
    }

    @Test
    void createsResolverAndMvcConfigurerWhenEnabled() {
        webContextRunner
                .withPropertyValues("spring.data.permission.header-resolver-enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(PermissionArgumentResolver.class);
                    assertThat(context).hasSingleBean(WebMvcConfigurer.class);

                    PermissionArgumentResolver resolver = context.getBean(PermissionArgumentResolver.class);
                    List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();
                    context.getBean(WebMvcConfigurer.class).addArgumentResolvers(resolvers);

                    assertThat(resolvers).containsExactly(resolver);
                });
    }

    @Test
    void backsOffWhenCustomResolverProvided() {
        webContextRunner
                .withPropertyValues("spring.data.permission.header-resolver-enabled=true")
                .withBean("customPermissionArgumentResolver", PermissionArgumentResolver.class,
                        () -> new PermissionArgumentResolver("X-Custom-User"))
                .run(context -> {
                    assertThat(context).hasSingleBean(PermissionArgumentResolver.class);

                    PermissionArgumentResolver custom =
                            context.getBean("customPermissionArgumentResolver", PermissionArgumentResolver.class);
                    List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();
                    context.getBean(WebMvcConfigurer.class).addArgumentResolvers(resolvers);

                    assertThat(resolvers).containsExactly(custom);
                });
    }

    @Test
    void doesNotCreateResolverInNonWebApplication() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        MybatisPermissionAutoConfiguration.class,
                        PermissionWebAutoConfiguration.class))
                .withPropertyValues("spring.data.permission.header-resolver-enabled=true")
                .withBean(PermissionResolver.class, TestPermissionResolver::new)
                .run(context -> {
                    assertThat(context).doesNotHaveBean(PermissionArgumentResolver.class);
                    assertThat(context).doesNotHaveBean(WebMvcConfigurer.class);
                });
    }

    private static final class TestPermissionResolver implements PermissionResolver {
        @Override
        public DataPermissionContext resolve(PermissionRequest request) {
            return DataPermissionContext.builder()
                    .operatorUserId(request.operatorUserId())
                    .scope(StandardDataScope.ALL)
                    .build();
        }
    }
}
