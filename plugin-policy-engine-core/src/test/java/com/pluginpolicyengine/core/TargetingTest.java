package com.pluginpolicyengine.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class TargetingTest {
	@Test
	void allowAllHasNoEligibilityRulesAndMatches() {
		Targeting targeting = Targeting.allowAll();

		assertThat(targeting.hasEligibilityRules()).isFalse();
		assertThat(targeting.matchesEligibility(PolicyContext.builder().build())).isTrue();
		assertThat(targeting.isExplicitlyAllowed(PolicyContext.builder().userId("user-1").build())).isFalse();
		assertThat(targeting.isExplicitlyDenied(PolicyContext.builder().userId("user-1").build())).isFalse();
	}

	@Test
	void matchesAllowAndDenyGroups() {
		Targeting targeting = Targeting.builder()
			.allowGroup("beta")
			.denyGroup("blocked")
			.build();

		assertThat(targeting.isExplicitlyAllowed(PolicyContext.builder().group("beta").build())).isTrue();
		assertThat(targeting.isExplicitlyDenied(PolicyContext.builder().group("blocked").build())).isTrue();
		assertThat(targeting.matchesEligibility(PolicyContext.builder().group("general").build())).isFalse();
	}

	@Test
	void attributeEligibilityCanMatchWithoutAllowLists() {
		Targeting targeting = Targeting.builder()
			.requireAttrIn("region", java.util.Collections.singleton("KR"))
			.build();

		assertThat(targeting.hasEligibilityRules()).isTrue();
		assertThat(targeting.matchesEligibility(PolicyContext.builder().attr("region", "KR").build())).isTrue();
		assertThat(targeting.matchesEligibility(PolicyContext.builder().attr("region", "US").build())).isFalse();
	}

	@Test
	void builderIgnoresNullInputsAndCopiesAttributeValues() {
		Set<String> regions = new HashSet<>();
		regions.add("KR");

		Targeting targeting = Targeting.builder()
			.allowUser(null)
			.denyUser(null)
			.allowGroup(null)
			.denyGroup(null)
			.requireAttrIn(null, regions)
			.requireAttrIn("region", null)
			.requireAttrIn("region", regions)
			.build();

		regions.add("US");

		assertThat(targeting.matchesEligibility(PolicyContext.builder().attr("region", "KR").build())).isTrue();
		assertThat(targeting.matchesEligibility(PolicyContext.builder().attr("region", "US").build())).isFalse();
	}
}
