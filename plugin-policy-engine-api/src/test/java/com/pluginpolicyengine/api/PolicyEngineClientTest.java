package com.pluginpolicyengine.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import com.pluginpolicyengine.core.PolicyContext;
import com.pluginpolicyengine.core.PolicyDecision;

import org.junit.jupiter.api.Test;

class PolicyEngineClientTest {
	private final PolicyContext context = PolicyContext.builder().userId("user-1").build();

	@Test
	void defaultIsAllowedDelegatesToEvaluate() {
		PolicyEngineClient client = (key, ctx) -> new PolicyDecision(true, "on", "TEST", Collections.emptyMap());

		assertThat(client.isAllowed("feature.one", context)).isTrue();
	}

	@Test
	void defaultVariantReturnsDecisionVariantWhenAllowed() {
		PolicyEngineClient client = (key, ctx) -> new PolicyDecision(true, "treatment", "TEST", Collections.emptyMap());

		assertThat(client.variant("feature.one", context, "fallback")).isEqualTo("treatment");
	}

	@Test
	void defaultVariantReturnsFallbackWhenDenied() {
		PolicyEngineClient client = (key, ctx) -> new PolicyDecision(false, "off", "TEST", Collections.emptyMap());

		assertThat(client.variant("feature.one", context, "fallback")).isEqualTo("fallback");
		assertThat(client.variant("feature.one", context, null)).isEqualTo("off");
	}
}
