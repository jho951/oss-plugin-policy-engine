package com.pluginpolicyengine.api;

import com.pluginpolicyengine.core.FlagDecision;
import com.pluginpolicyengine.core.FlagContext;

/** 애플리케이션 코드에서 사용하는 기능 플래그 평가 진입점입니다.*/
public interface FeatureFlagClient {
	/**
	 * 주어진 컨텍스트에서 기능 플래그를 평가하고 상세 결정을 반환합니다.
	 *
	 * @param key 기능 플래그 키
	 * @param ctx 요청/사용자 컨텍스트
	 * @return 상세 평가 결과
	 */
	FlagDecision evaluate(String key, FlagContext ctx);

	/**
	 * 주어진 컨텍스트에서 기능 플래그 활성 여부를 평가합니다.
	 *
	 * @param key 기능 플래그 키
	 * @param ctx 요청/사용자 컨텍스트
	 * @return 플래그가 활성화되면 {@code true}
	 */
	default boolean isEnabled(String key, FlagContext ctx) {
		return evaluate(key, ctx).enabled();
	}

	/**
	 * 주어진 플래그를 평가하여 선택된 variant를 반환합니다.
	 *
	 * @param key 기능 플래그 키
	 * @param ctx 요청/사용자 컨텍스트
	 * @param fallbackVariant 플래그 비활성 시 사용할 기본 variant
	 * @return 선택된 variant 또는 fallback variant
	 */
	default String variant(String key, FlagContext ctx, String fallbackVariant) {
		FlagDecision decision = evaluate(key, ctx);
		return decision.enabled() ? decision.variant() : (fallbackVariant != null ? fallbackVariant : "off");
	}
}
