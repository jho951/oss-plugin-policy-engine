package com.pluginpolicyengine.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.jupiter.api.Test;

class PolicyModelTest {
	@Test
	void policyDefinitionRequiresKey() {
		assertThatThrownBy(() -> PolicyDefinition.builder(null).build())
			.isInstanceOf(NullPointerException.class)
			.hasMessage("key");
	}

	@Test
	void policyDefinitionAppliesDefaultsAndClampsNumericValues() {
		PolicyDefinition belowRange = PolicyDefinition.builder("below")
			.rolloutPercent(-10)
			.variant("negative", -1)
			.build();
		PolicyDefinition aboveRange = PolicyDefinition.builder("above")
			.rolloutPercent(150)
			.build();

		assertThat(belowRange.enabled()).isTrue();
		assertThat(belowRange.rolloutPercent()).isZero();
		assertThat(belowRange.defaultVariant()).isEqualTo("on");
		assertThat(belowRange.updatedAt()).isNotNull();
		assertThat(belowRange.variants()).hasSize(1);
		assertThat(belowRange.variants().get(0).weight()).isZero();
		assertThat(aboveRange.rolloutPercent()).isEqualTo(100);
	}

	@Test
	void variantAllocationRequiresName() {
		assertThatThrownBy(() -> new PolicyDefinition.VariantAllocation(null, 1))
			.isInstanceOf(NullPointerException.class)
			.hasMessage("name");
	}

	@Test
	void contextCopiesInputsAndExposesImmutableViews() {
		HashSet<String> groups = new HashSet<>(Arrays.asList("beta"));
		Map<String, String> attrs = new HashMap<>();
		attrs.put("region", "KR");

		PolicyContext context = PolicyContext.builder()
			.userId("user-1")
			.groups(groups)
			.attrs(attrs)
			.build();

		groups.add("staff");
		attrs.put("plan", "FREE");

		assertThat(context.groups()).containsExactly("beta");
		assertThat(context.attrs()).containsOnlyKeys("region");
		assertThatThrownBy(() -> context.groups().add("staff"))
			.isInstanceOf(UnsupportedOperationException.class);
		assertThatThrownBy(() -> context.attrs().put("plan", "PRO"))
			.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void definitionVariantsAreImmutable() {
		PolicyDefinition definition = PolicyDefinition.builder("copy")
			.variant("control", 1)
			.build();

		assertThatThrownBy(() -> definition.variants().add(new PolicyDefinition.VariantAllocation("treatment", 1)))
			.isInstanceOf(UnsupportedOperationException.class);
	}
}
