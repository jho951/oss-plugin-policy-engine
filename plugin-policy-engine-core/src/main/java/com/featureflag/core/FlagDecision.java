package com.pluginpolicyengine.core;

import java.util.Collections;
import java.util.Map;

/**
 * 기능 플래그 평가 결과를 담는 불변 객체입니다.
 */
public final class FlagDecision {
	private final boolean enabled;
	private final String variant;
	private final String reason;
	private final Map<String, Object> meta;

	/**
	 * 평가 결과 객체를 생성합니다.
	 *
	 * @param enabled 최종 활성 상태
	 * @param variant 선택된 variant 이름
	 * @param reason 기계 판독 가능한 사유 코드
	 * @param meta 진단용 추가 메타데이터(선택)
	 */
	public FlagDecision(boolean enabled, String variant, String reason, Map<String, Object> meta) {
		this.enabled = enabled;
		this.variant = variant;
		this.reason = reason;
		this.meta = meta == null ? Map.of() : Collections.unmodifiableMap(meta);
	}

	/**
	 * @return 플래그 활성 여부
	 */
	public boolean enabled() { return enabled; }

	/**
	 * @return 선택된 variant 이름
	 */
	public String variant() { return variant; }

	/**
	 * @return 이번 평가의 사유 코드
	 */
	public String reason() { return reason; }

	/**
	 * @return 평가 결과에 연결된 불변 메타데이터
	 */
	public Map<String, Object> meta() { return meta; }
}
