package io.github.steventsai.permission.mybatis;

import io.github.steventsai.permission.DataPermissionHelper;
import io.github.steventsai.permission.DataPermissionContext;
import io.github.steventsai.permission.PermissionRequest;
import io.github.steventsai.permission.PermissionResolver;
import io.github.steventsai.permission.StandardDataScope;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MybatisPermissionAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MybatisPermissionAutoConfiguration.class));

    @Test
    void createsDefaultBeansWhenResolverPresent() {
        contextRunner
                .withBean(PermissionResolver.class, TestPermissionResolver::new)
                .run(context -> {
                    assertThat(context).hasSingleBean(PermissionSqlParamAssembler.class);
                    assertThat(context).hasSingleBean(PermissionArgumentResolver.class);
                    assertThat(context).hasSingleBean(DataPermissionHelper.class);
                    assertThat(context).hasSingleBean(WebMvcConfigurer.class);
                });
    }

    @Test
    void doesNotCreateDataPermissionHelperWhenResolverMissing() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(DataPermissionHelper.class));
    }

    @Test
    void backsOffWhenFeatureDisabled() {
        contextRunner
                .withPropertyValues("spring.data.permission.enabled=false")
                .withBean(PermissionResolver.class, TestPermissionResolver::new)
                .run(context -> {
                    assertThat(context).doesNotHaveBean(PermissionSqlParamAssembler.class);
                    assertThat(context).doesNotHaveBean(PermissionArgumentResolver.class);
                    assertThat(context).doesNotHaveBean(DataPermissionHelper.class);
                    assertThat(context).doesNotHaveBean(WebMvcConfigurer.class);
                });
    }

    @Test
    void usesConfiguredColumnNamesAndHeader() {
        contextRunner
                .withPropertyValues(
                        "spring.data.permission.user-id-header=X-Auth-User",
                        "spring.data.permission.org-id-column=t.org_id",
                        "spring.data.permission.owner-user-id-column=t.owner_user_id")
                .withBean(PermissionResolver.class, TestPermissionResolver::new)
                .run(context -> {
                    PermissionSqlParamAssembler assembler = context.getBean(PermissionSqlParamAssembler.class);
                    PermissionArgumentResolver resolver = context.getBean(PermissionArgumentResolver.class);

                    assertThat(assembler.assemble(DataPermissionContext.builder()
                            .operatorUserId(1L)
                            .scope(StandardDataScope.ALL)
                            .build()))
                            .containsEntry("_orgIdColumn", "t.org_id")
                            .containsEntry("_ownerUserIdColumn", "t.owner_user_id");

                    List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();
                    context.getBean(WebMvcConfigurer.class).addArgumentResolvers(resolvers);
                    assertThat(resolvers).contains(resolver);
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
