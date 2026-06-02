package com.pluginpolicyengine.core;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** 컨텍스트를 기준으로 정책을 평가하는 핵심 서비스입니다. */
public final class PolicyEngine {
	private final PolicyStore store;

	/**
	 * {@link PolicyStore}를 사용해 서비스를 생성합니다.
	 *
	 * @param store 정책 저장소
	 */
	public PolicyEngine(PolicyStore store) { this.store = Objects.requireNonNull(store, "store"); }

	/**
	 * 정책을 평가하고 상세 의사결정 정보를 반환합니다.
	 *
	 * @param key 정책 키
	 * @param ctx 요청 컨텍스트
	 * @return 평가 결과
	 */
	public PolicyDecision evaluate(String key, PolicyContext ctx) {
		Optional<PolicyDefinition> defOpt = store.find(key);
		if (!defOpt.isPresent()) {
			return new PolicyDecision(false, "off", "POLICY_NOT_FOUND", meta("key", key));
		}
		PolicyDefinition def = defOpt.get();

		if (!def.enabled()) {
			return new PolicyDecision(false, "off", "POLICY_DISABLED", meta("key", key));
		}

		Targeting t = def.targeting();

		if (t.isExplicitlyDenied(ctx)) {
			return new PolicyDecision(false, "off", "TARGET_DENY", meta("key", key));
		}

		if (t.isExplicitlyAllowed(ctx)) {
			String v = pickVariant(def, key, ctx);
			return new PolicyDecision(true, v, "TARGET_ALLOW", meta("key", key, "variant", v));
		}

		if (t.hasEligibilityRules() && !t.matchesEligibility(ctx)) {
			return new PolicyDecision(false, "off", "TARGET_MISS", meta("key", key));
		}

		if (!passesRollout(def.rolloutPercent(), key, ctx)) {
			return new PolicyDecision(false, "off", "ROLLOUT_OUT", meta("key", key, "rollout", def.rolloutPercent()));
		}

		String v = pickVariant(def, key, ctx);
		return new PolicyDecision(true, v, "ROLLOUT_IN", meta("key", key, "variant", v, "rollout", def.rolloutPercent()));
	}

	/**
	 * 허용/차단 상태만 필요할 때 사용하는 편의 메서드입니다.
	 *
	 * @param key 정책 키
	 * @param ctx 요청 컨텍스트
	 * @return 허용되면 {@code true}
	 */
	public boolean isAllowed(String key, PolicyContext ctx) {
		return evaluate(key, ctx).allowed();
	}

	/**
	 * 선택된 variant 또는 fallback을 반환하는 편의 메서드입니다.
	 *
	 * @param key 정책 키
	 * @param ctx 요청 컨텍스트
	 * @param fallback 비활성일 때 사용할 fallback variant
	 * @return 선택된 variant 또는 fallback
	 */
	public String variant(String key, PolicyContext ctx, String fallback) {
		PolicyDecision d = evaluate(key, ctx);
		return d.allowed() ? d.variant() : (fallback != null ? fallback : "off");
	}

	private boolean passesRollout(int percent, String key, PolicyContext ctx) {
		if (percent >= 100) return true;
		if (percent <= 0) return false;

		String basis = basisId(ctx);
		// basis가 없으면 “항상 OFF”로 두는게 운영상 안전함(원하면 랜덤/쿠키로 확장)
		if (basis == null) return false;

		int bucket = (int)(hashToPositiveLong(key + ":" + basis) % 100);
		return bucket < percent;
	}

	private String pickVariant(PolicyDefinition def, String key, PolicyContext ctx) {
		if (def.variants().isEmpty()) return def.defaultVariant();

		String basis = basisId(ctx);
		if (basis == null) return def.defaultVariant();

		int total = 0;
		for (PolicyDefinition.VariantAllocation v : def.variants()) total += v.weight();
		if (total <= 0) return def.defaultVariant();

		long r = hashToPositiveLong("variant:" + key + ":" + basis) % total;
		int acc = 0;
		for (PolicyDefinition.VariantAllocation v : def.variants()) {
			acc += v.weight();
			if (r < acc) return v.name();
		}
		return def.defaultVariant();
	}

	private String basisId(PolicyContext ctx) {
		if (hasText(ctx.userId())) return ctx.userId();
		String anon = ctx.attrs().get("anonId");
		if (hasText(anon)) return anon;
		return null;
	}

	private static boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private static Map<String, Object> meta(Object... pairs) {
		Map<String, Object> values = new LinkedHashMap<>();
		for (int i = 0; i < pairs.length; i += 2) {
			values.put((String) pairs[i], pairs[i + 1]);
		}
		return values;
	}

	private static long hashToPositiveLong(String s) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] h = md.digest(s.getBytes(StandardCharsets.UTF_8));
			long v = 0L;
			for (int i = 0; i < 8; i++) v = (v << 8) | (h[i] & 0xffL);
			return v & Long.MAX_VALUE;
		} catch (Exception e) {
			// 최후 fallback (운영에선 절대 권장 X)
			return Math.abs((long)s.hashCode());
		}
	}
}
