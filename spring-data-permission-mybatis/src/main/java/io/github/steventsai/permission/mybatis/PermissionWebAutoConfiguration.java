package io.github.steventsai.permission.mybatis;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Opt-in Spring MVC integration for {@link CurrentPermission}.
 * <p>
 * The header resolver is disabled by default because the default header is not
 * an authentication mechanism. Enable it only for demos/tests or behind a
 * trusted gateway, or replace it with a resolver backed by real authentication.
 */
@AutoConfiguration(after = MybatisPermissionAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(WebMvcConfigurer.class)
@ConditionalOnProperty(
        prefix = "spring.data.permission",
        name = "header-resolver-enabled",
        havingValue = "true")
@ConditionalOnBean({PermissionProperties.class, PermissionRequestFactory.class})
public class PermissionWebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PermissionArgumentResolver permissionArgumentResolver(
            PermissionProperties properties,
            PermissionRequestFactory requestFactory) {
        return new PermissionArgumentResolver(properties.getUserIdHeader(), requestFactory);
    }

    @Bean
    @ConditionalOnMissingBean(name = "permissionWebMvcConfigurer")
    public WebMvcConfigurer permissionWebMvcConfigurer(PermissionArgumentResolver argumentResolver) {
        return new WebMvcConfigurer() {
            @Override
            public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
                resolvers.add(argumentResolver);
            }
        };
    }
}
