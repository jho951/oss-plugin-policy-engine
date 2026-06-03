package com.pluginpolicyengine.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class PolicyDecisionTest {
	@Test
	void copiesMetaAndExposesImmutableView() {
		Map<String, Object> meta = new LinkedHashMap<>();
		meta.put("key", "feature.one");

		PolicyDecision decision = new PolicyDecision(true, "on", "TEST", meta);
		meta.put("rollout", 100);

		assertThat(decision.allowed()).isTrue();
		assertThat(decision.variant()).isEqualTo("on");
		assertThat(decision.reason()).isEqualTo("TEST");
		assertThat(decision.meta()).containsOnlyKeys("key");
		assertThatThrownBy(() -> decision.meta().put("rollout", 100))
			.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void nullMetaBecomesEmptyMap() {
		PolicyDecision decision = new PolicyDecision(false, "off", "TEST", null);

		assertThat(decision.meta()).isEmpty();
	}
}
