package io.github.steventsai.permission.mybatis;

import io.github.steventsai.permission.DataPermissionHelper;
import io.github.steventsai.permission.PermissionResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Core auto-configuration for spring-data-permission.
 * <p>
 * This configuration deliberately has no Spring MVC dependency. Web support is
 * registered by {@link PermissionWebAutoConfiguration} only when the application
 * is a servlet web application, Spring MVC is present, and the header resolver
 * has been explicitly enabled.
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "spring.data.permission", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(PermissionProperties.class)
public class MybatisPermissionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PermissionSqlParamAssembler permissionSqlParamAssembler(PermissionProperties properties) {
        return new PermissionSqlParamAssembler(
                properties.getOrgIdColumn(),
                properties.getOwnerUserIdColumn(),
                properties.getMaxOrgIds());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnSingleCandidate(PermissionResolver.class)
    public DataPermissionHelper dataPermissionHelper(PermissionResolver permissionResolver,
                                                     PermissionSqlParamAssembler paramAssembler) {
        return new DefaultDataPermissionHelper(permissionResolver, paramAssembler);
    }

    @Bean
    @ConditionalOnMissingBean
    public PermissionRequestFactory permissionRequestFactory() {
        return PermissionRequestFactory.defaultFactory();
    }
}
