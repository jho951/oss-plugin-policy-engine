package com.pluginpolicyengine.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pluginpolicyengine.core.store.InMemoryPolicyStore;

import org.junit.jupiter.api.Test;

class PolicyEngineTest {
	private final InMemoryPolicyStore store = new InMemoryPolicyStore();
	private final PolicyEngine engine = new PolicyEngine(store);

	@Test
	void rejectsNullStore() {
		assertThatThrownBy(() -> new PolicyEngine(null))
			.isInstanceOf(NullPointerException.class)
			.hasMessage("store");
	}

	@Test
	void returnsNotFoundDecisionForMissingPolicy() {
		PolicyDecision decision = engine.evaluate("missing.feature", context("user-1"));

		assertThat(decision.allowed()).isFalse();
		assertThat(decision.variant()).isEqualTo("off");
		assertThat(decision.reason()).isEqualTo("POLICY_NOT_FOUND");
		assertThat(decision.meta()).containsEntry("key", "missing.feature");
	}

	@Test
	void disabledPolicyIsAlwaysOff() {
		store.put(PolicyDefinition.builder("checkout.newFlow")
			.enabled(false)
			.rolloutPercent(100)
			.build());

		PolicyDecision decision = engine.evaluate("checkout.newFlow", context("user-1"));

		assertThat(decision.allowed()).isFalse();
		assertThat(decision.variant()).isEqualTo("off");
		assertThat(decision.reason()).isEqualTo("POLICY_DISABLED");
	}

	@Test
	void denyRulesWinOverExplicitAllowRules() {
		store.put(PolicyDefinition.builder("admin.panel")
			.targeting(Targeting.builder()
				.allowUser("user-1")
				.denyUser("user-1")
				.build())
			.build());

		PolicyDecision decision = engine.evaluate("admin.panel", context("user-1"));

		assertThat(decision.allowed()).isFalse();
		assertThat(decision.reason()).isEqualTo("TARGET_DENY");
	}

	@Test
	void explicitAllowBypassesRolloutPercentage() {
		store.put(PolicyDefinition.builder("beta.banner")
			.rolloutPercent(0)
			.defaultVariant("enabled")
			.targeting(Targeting.builder()
				.allowUser("user-1")
				.build())
			.build());

		PolicyDecision decision = engine.evaluate("beta.banner", context("user-1"));

		assertThat(decision.allowed()).isTrue();
		assertThat(decision.variant()).isEqualTo("enabled");
		assertThat(decision.reason()).isEqualTo("TARGET_ALLOW");
	}

	@Test
	void missesWhenAllowListEligibilityDoesNotMatch() {
		store.put(PolicyDefinition.builder("beta.checkout")
			.targeting(Targeting.builder()
				.allowGroup("beta")
				.build())
			.build());

		PolicyDecision decision = engine.evaluate("beta.checkout", PolicyContext.builder()
			.userId("user-1")
			.group("general")
			.build());

		assertThat(decision.allowed()).isFalse();
		assertThat(decision.reason()).isEqualTo("TARGET_MISS");
	}

	@Test
	void requiresAllAttributeEligibilityRules() {
		store.put(PolicyDefinition.builder("pricing.experiment")
			.targeting(Targeting.builder()
				.requireAttrIn("region", java.util.Collections.singleton("KR"))
				.requireAttrIn("plan", java.util.Collections.singleton("PRO"))
				.build())
			.build());

		PolicyDecision missingPlan = engine.evaluate("pricing.experiment", PolicyContext.builder()
			.userId("user-1")
			.attr("region", "KR")
			.build());
		PolicyDecision matched = engine.evaluate("pricing.experiment", PolicyContext.builder()
			.userId("user-1")
			.attr("region", "KR")
			.attr("plan", "PRO")
			.build());

		assertThat(missingPlan.allowed()).isFalse();
		assertThat(missingPlan.reason()).isEqualTo("TARGET_MISS");
		assertThat(matched.allowed()).isTrue();
		assertThat(matched.reason()).isEqualTo("ROLLOUT_IN");
	}

	@Test
	void zeroRolloutBlocksStableUsers() {
		store.put(PolicyDefinition.builder("search.ranking")
			.rolloutPercent(0)
			.build());

		PolicyDecision decision = engine.evaluate("search.ranking", context("user-1"));

		assertThat(decision.allowed()).isFalse();
		assertThat(decision.reason()).isEqualTo("ROLLOUT_OUT");
	}

	@Test
	void rolloutRequiresStableUserOrAnonymousId() {
		store.put(PolicyDefinition.builder("search.ranking")
			.rolloutPercent(50)
			.build());

		PolicyDecision decision = engine.evaluate("search.ranking", PolicyContext.builder().build());

		assertThat(decision.allowed()).isFalse();
		assertThat(decision.reason()).isEqualTo("ROLLOUT_OUT");
	}

	@Test
	void anonymousIdCanBeUsedAsStableRolloutBasis() {
		store.put(PolicyDefinition.builder("anonymous.experiment")
			.rolloutPercent(100)
			.variant("control", 1)
			.variant("treatment", 1)
			.build());

		PolicyContext anonymous = PolicyContext.builder()
			.attr("anonId", "anon-1")
			.build();

		PolicyDecision first = engine.evaluate("anonymous.experiment", anonymous);
		PolicyDecision second = engine.evaluate("anonymous.experiment", anonymous);

		assertThat(first.allowed()).isTrue();
		assertThat(first.variant()).isIn("control", "treatment");
		assertThat(second.variant()).isEqualTo(first.variant());
	}

	@Test
	void fallsBackToDefaultVariantWhenVariantWeightsAreZero() {
		store.put(PolicyDefinition.builder("zero.weight")
			.defaultVariant("fallback")
			.variant("control", 0)
			.variant("treatment", -1)
			.build());

		PolicyDecision decision = engine.evaluate("zero.weight", context("user-1"));

		assertThat(decision.allowed()).isTrue();
		assertThat(decision.variant()).isEqualTo("fallback");
	}

	@Test
	void weightedVariantSelectionIsDeterministicForSameUser() {
		store.put(PolicyDefinition.builder("copy.experiment")
			.variant("control", 1)
			.variant("treatment", 1)
			.build());

		PolicyDecision first = engine.evaluate("copy.experiment", context("user-42"));
		PolicyDecision second = engine.evaluate("copy.experiment", context("user-42"));

		assertThat(first.allowed()).isTrue();
		assertThat(first.reason()).isEqualTo("ROLLOUT_IN");
		assertThat(first.variant()).isIn("control", "treatment");
		assertThat(second.variant()).isEqualTo(first.variant());
	}

	@Test
	void variantReturnsFallbackWhenPolicyIsNotAllowed() {
		store.put(PolicyDefinition.builder("disabled.variant")
			.enabled(false)
			.build());

		assertThat(engine.variant("disabled.variant", context("user-1"), "fallback")).isEqualTo("fallback");
		assertThat(engine.variant("disabled.variant", context("user-1"), null)).isEqualTo("off");
	}

	private static PolicyContext context(String userId) {
		return PolicyContext.builder().userId(userId).build();
	}
}
