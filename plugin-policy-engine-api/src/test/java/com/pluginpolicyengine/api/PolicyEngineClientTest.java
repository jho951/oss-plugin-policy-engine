package com.pluginpolicyengine.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import com.pluginpolicyengine.core.PolicyContext;
import com.pluginpolicyengine.core.PolicyDecision;

import org.junit.jupiter.api.Test;

/**
 * {@link PolicyEngineClient} 인터페이스에 정의된 디폴트 메서드들의 동작을 검증하는 테스트 클래스입니다.
 * <p>
 * {@code isAllowed} 및 {@code variant} 등의 디폴트 메서드가 핵심 평가 로직({@code evaluate})과 올바르게 연동되는지 테스트합니다.
 * </p>
 */
class PolicyEngineClientTest {

    /**
     * 테스트에서 공통으로 사용할 정책 평가 컨텍스트 객체입니다.
     */
    private final PolicyContext context = PolicyContext.builder().userId("user-1").build();

    /**
     * {@link PolicyEngineClient#isAllowed(String, PolicyContext)} 디폴트 메서드가 내부적으로 평가 결과를 올바르게 위임하는지 테스트합니다.
     */
    @Test
    void defaultIsAllowedDelegatesToEvaluate() {
        PolicyEngineClient client = (key, ctx) -> new PolicyDecision(true, "on", "TEST", Collections.emptyMap());
        assertThat(client.isAllowed("feature.one", context)).isTrue();
    }

    /**
     * 정책이 허용({@code allowed = true})되었을 때,
     * {@link PolicyEngineClient#variant(String, PolicyContext, String)} 메서드가 결정된 베리언트 값을 정상적으로 반환하는지 테스트합니다.
     */
    @Test
    void defaultVariantReturnsDecisionVariantWhenAllowed() {
        PolicyEngineClient client = (key, ctx) -> new PolicyDecision(true, "treatment", "TEST", Collections.emptyMap());
        assertThat(client.variant("feature.one", context, "fallback")).isEqualTo("treatment");
    }

    /**
     * 정책이 거부({@code allowed = false})되었을 때,
     * {@link PolicyEngineClient#variant(String, PolicyContext, String)} 메서드가 지정된 폴백 값이나 거부된 결정을 올바르게 반환하는지 테스트합니다.
     */
    @Test
    void defaultVariantReturnsFallbackWhenDenied() {
        PolicyEngineClient client = (key, ctx) -> new PolicyDecision(false, "off", "TEST", Collections.emptyMap());
        assertThat(client.variant("feature.one", context, "fallback")).isEqualTo("fallback");
        assertThat(client.variant("feature.one", context, null)).isEqualTo("off");
    }
}
