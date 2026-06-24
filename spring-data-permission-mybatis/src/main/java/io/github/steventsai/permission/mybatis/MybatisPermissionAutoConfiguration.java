package io.github.steventsai.permission.mybatis;

import io.github.steventsai.permission.DataPermissionHelper;
import io.github.steventsai.permission.PermissionResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Auto-configuration for spring-data-permission.
 * <p>
 * Activates when a {@link PermissionResolver} bean is present in the application context.
 * The user must provide exactly one {@link PermissionResolver} implementation.
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "spring.data.permission", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(PermissionProperties.class)
public class MybatisPermissionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PermissionSqlParamAssembler permissionSqlParamAssembler(PermissionProperties properties) {
        return new PermissionSqlParamAssembler(properties.getOrgIdColumn(), properties.getOwnerUserIdColumn());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(PermissionResolver.class)
    public DataPermissionHelper dataPermissionHelper(PermissionResolver permissionResolver,
                                                      PermissionSqlParamAssembler paramAssembler) {
        return new DefaultDataPermissionHelper(permissionResolver, paramAssembler);
    }

    @Bean
    @ConditionalOnMissingBean
    public PermissionArgumentResolver permissionArgumentResolver(PermissionProperties properties) {
        return new PermissionArgumentResolver(properties.getUserIdHeader());
    }

    @Bean
    public WebMvcConfigurer permissionWebMvcConfigurer(PermissionArgumentResolver argumentResolver) {
        return new WebMvcConfigurer() {
            @Override
            public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
                resolvers.add(argumentResolver);
            }
        };
    }
}
