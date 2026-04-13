package com.pluginpolicyengine.core;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/**
 * 컨텍스트를 기준으로 기능 플래그를 평가하는 핵심 서비스입니다.
 */
public final class FeatureFlagService {
	private final FlagStore store;

	/**
	 * {@link FlagStore}를 사용해 서비스를 생성합니다.
	 *
	 * @param store 플래그 저장소
	 */
	public FeatureFlagService(FlagStore store) { this.store = Objects.requireNonNull(store, "store"); }

	/**
	 * 플래그를 평가하고 상세 의사결정 정보를 반환합니다.
	 *
	 * @param key 기능 플래그 키
	 * @param ctx 요청 컨텍스트
	 * @return 평가 결과
	 */
	public FlagDecision evaluate(String key, FlagContext ctx) {
		var defOpt = store.find(key);
		if (defOpt.isEmpty()) {
			return new FlagDecision(false, "off", "FLAG_NOT_FOUND", Map.of("key", key));
		}
		FlagDefinition def = defOpt.get();

		if (!def.enabled()) {
			return new FlagDecision(false, "off", "FLAG_DISABLED", Map.of("key", key));
		}

		Targeting t = def.targeting();

		if (t.isExplicitlyDenied(ctx)) {
			return new FlagDecision(false, "off", "TARGET_DENY", Map.of("key", key));
		}

		if (t.isExplicitlyAllowed(ctx)) {
			String v = pickVariant(def, key, ctx);
			return new FlagDecision(true, v, "TARGET_ALLOW", Map.of("key", key, "variant", v));
		}

		if (t.hasEligibilityRules() && !t.matchesEligibility(ctx)) {
			return new FlagDecision(false, "off", "TARGET_MISS", Map.of("key", key));
		}

		if (!passesRollout(def.rolloutPercent(), key, ctx)) {
			return new FlagDecision(false, "off", "ROLLOUT_OUT", Map.of("key", key, "rollout", def.rolloutPercent()));
		}

		String v = pickVariant(def, key, ctx);
		return new FlagDecision(true, v, "ROLLOUT_IN", Map.of("key", key, "variant", v, "rollout", def.rolloutPercent()));
	}

	/**
	 * 활성/비활성 상태만 필요할 때 사용하는 편의 메서드입니다.
	 *
	 * @param key 기능 플래그 키
	 * @param ctx 요청 컨텍스트
	 * @return 활성화되면 {@code true}
	 */
	public boolean isEnabled(String key, FlagContext ctx) {
		return evaluate(key, ctx).enabled();
	}

	/**
	 * 선택된 variant 또는 fallback을 반환하는 편의 메서드입니다.
	 *
	 * @param key 기능 플래그 키
	 * @param ctx 요청 컨텍스트
	 * @param fallback 비활성일 때 사용할 fallback variant
	 * @return 선택된 variant 또는 fallback
	 */
	public String variant(String key, FlagContext ctx, String fallback) {
		FlagDecision d = evaluate(key, ctx);
		return d.enabled() ? d.variant() : (fallback != null ? fallback : "off");
	}

	private boolean passesRollout(int percent, String key, FlagContext ctx) {
		if (percent >= 100) return true;
		if (percent <= 0) return false;

		String basis = basisId(ctx);
		// basis가 없으면 “항상 OFF”로 두는게 운영상 안전함(원하면 랜덤/쿠키로 확장)
		if (basis == null) return false;

		int bucket = (int)(hashToPositiveLong(key + ":" + basis) % 100);
		return bucket < percent;
	}

	private String pickVariant(FlagDefinition def, String key, FlagContext ctx) {
		if (def.variants().isEmpty()) return def.defaultVariant();

		String basis = basisId(ctx);
		if (basis == null) return def.defaultVariant();

		int total = 0;
		for (var v : def.variants()) total += v.weight();
		if (total <= 0) return def.defaultVariant();

		long r = hashToPositiveLong("variant:" + key + ":" + basis) % total;
		int acc = 0;
		for (var v : def.variants()) {
			acc += v.weight();
			if (r < acc) return v.name();
		}
		return def.defaultVariant();
	}

	private String basisId(FlagContext ctx) {
		if (ctx.userId() != null && !ctx.userId().isBlank()) return ctx.userId();
		String anon = ctx.attrs().get("anonId");
		if (anon != null && !anon.isBlank()) return anon;
		return null;
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
