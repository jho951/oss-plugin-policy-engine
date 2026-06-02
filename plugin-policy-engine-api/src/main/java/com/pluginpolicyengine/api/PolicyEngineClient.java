package com.pluginpolicyengine.api;

import com.pluginpolicyengine.core.PolicyDecision;
import com.pluginpolicyengine.core.PolicyContext;

/** 애플리케이션 코드에서 사용하는 정책 평가 진입점입니다. */
public interface PolicyEngineClient {
	/**
	 * 주어진 컨텍스트에서 정책을 평가하고 상세 결정을 반환합니다.
	 *
	 * @param key 정책 키
	 * @param ctx 요청/사용자 컨텍스트
	 * @return 상세 평가 결과
	 */
	PolicyDecision evaluate(String key, PolicyContext ctx);

	/**
	 * 주어진 컨텍스트에서 정책 허용 여부를 평가합니다.
	 *
	 * @param key 정책 키
	 * @param ctx 요청/사용자 컨텍스트
	 * @return 정책이 허용되면 {@code true}
	 */
	default boolean isAllowed(String key, PolicyContext ctx) {
		return evaluate(key, ctx).allowed();
	}

	/**
	 * 주어진 정책을 평가하여 선택된 variant를 반환합니다.
	 *
	 * @param key 정책 키
	 * @param ctx 요청/사용자 컨텍스트
	 * @param fallbackVariant 정책 비활성 시 사용할 기본 variant
	 * @return 선택된 variant 또는 fallback variant
	 */
	default String variant(String key, PolicyContext ctx, String fallbackVariant) {
		PolicyDecision decision = evaluate(key, ctx);
		return decision.allowed() ? decision.variant() : (fallbackVariant != null ? fallbackVariant : "off");
	}
}
