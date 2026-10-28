package io.github.steventsai.permission.mybatis;

import io.github.steventsai.permission.DataPermissionContext;
import io.github.steventsai.permission.DataPermissionHelper;
import io.github.steventsai.permission.PermissionRequest;
import io.github.steventsai.permission.PermissionResolver;
import io.github.steventsai.permission.StandardDataScope;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MybatisPermissionAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MybatisPermissionAutoConfiguration.class));

    @Test
    void createsCoreBeansWhenResolverPresent() {
        contextRunner
                .withBean(PermissionResolver.class, TestPermissionResolver::new)
                .run(context -> {
                    assertThat(context).hasSingleBean(PermissionSqlParamAssembler.class);
                    assertThat(context).hasSingleBean(PermissionRequestFactory.class);
                    assertThat(context).hasSingleBean(DataPermissionHelper.class);
                    // Web integration is opt-in and lives in PermissionWebAutoConfiguration.
                    assertThat(context).doesNotHaveBean(PermissionArgumentResolver.class);
                    assertThat(context).doesNotHaveBean(WebMvcConfigurer.class);
                });
    }

    @Test
    void doesNotCreateDataPermissionHelperWhenResolverMissing() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(DataPermissionHelper.class);
            assertThat(context).hasSingleBean(PermissionSqlParamAssembler.class);
            assertThat(context).hasSingleBean(PermissionRequestFactory.class);
        });
    }

    @Test
    void doesNotCreateDataPermissionHelperWhenMultipleResolversPresent() {
        contextRunner
                .withBean("resolverOne", PermissionResolver.class, TestPermissionResolver::new)
                .withBean("resolverTwo", PermissionResolver.class, TestPermissionResolver::new)
                .run(context -> assertThat(context).doesNotHaveBean(DataPermissionHelper.class));
    }

    @Test
    void backsOffWhenFeatureDisabled() {
        contextRunner
                .withPropertyValues("spring.data.permission.enabled=false")
                .withBean(PermissionResolver.class, TestPermissionResolver::new)
                .run(context -> {
                    assertThat(context).doesNotHaveBean(PermissionSqlParamAssembler.class);
                    assertThat(context).doesNotHaveBean(PermissionRequestFactory.class);
                    assertThat(context).doesNotHaveBean(DataPermissionHelper.class);
                    assertThat(context).doesNotHaveBean(PermissionArgumentResolver.class);
                    assertThat(context).doesNotHaveBean(WebMvcConfigurer.class);
                });
    }

    @Test
    void usesConfiguredColumnNamesAndMaxOrgIds() {
        contextRunner
                .withPropertyValues(
                        "spring.data.permission.user-id-header=X-Auth-User",
                        "spring.data.permission.org-id-column=t.org_id",
                        "spring.data.permission.owner-user-id-column=t.owner_user_id",
                        "spring.data.permission.max-org-ids=42")
                .withBean(PermissionResolver.class, TestPermissionResolver::new)
                .run(context -> {
                    PermissionSqlParamAssembler assembler = context.getBean(PermissionSqlParamAssembler.class);
                    PermissionRequestFactory requestFactory = context.getBean(PermissionRequestFactory.class);

                    Map<String, Object> params = assembler.assemble(DataPermissionContext.builder()
                            .operatorUserId(1L)
                            .scope(StandardDataScope.ALL)
                            .build());

                    assertThat(params)
                            .containsEntry("_orgIdColumn", "t.org_id")
                            .containsEntry("_ownerUserIdColumn", "t.owner_user_id");

                    assertThat(requestFactory.create(7L, null))
                            .isInstanceOf(DefaultPermissionRequest.class)
                            .extracting(PermissionRequest::operatorUserId)
                            .isEqualTo(7L);
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
