package com.pluginpolicyengine.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 정책 평가 결과를 담는 불변 객체입니다.
 */
public final class PolicyDecision {
	private final boolean allowed;
	private final String variant;
	private final String reason;
	private final Map<String, Object> meta;

	/**
	 * 평가 결과 객체를 생성합니다.
	 *
	 * @param allowed 최종 허용 여부
	 * @param variant 선택된 variant 이름
	 * @param reason 기계 판독 가능한 사유 코드
	 * @param meta 진단용 추가 메타데이터(선택)
	 */
	public PolicyDecision(boolean allowed, String variant, String reason, Map<String, Object> meta) {
		this.allowed = allowed;
		this.variant = variant;
		this.reason = reason;
		this.meta = meta == null ? Collections.<String, Object>emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<>(meta));
	}

	/**
	 * @return 정책 허용 여부
	 */
	public boolean allowed() { return allowed; }

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
