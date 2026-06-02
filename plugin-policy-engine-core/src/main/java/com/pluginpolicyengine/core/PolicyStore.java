package com.pluginpolicyengine.core;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/** 정책 정의를 조회하기 위한 저장소 추상화입니다. */
public interface PolicyStore {
	/**
	 * 키로 정책을 조회합니다.
	 * @param key 정책 키
	 * @return 정책이 존재하면 해당 정의
	 */
	Optional<PolicyDefinition> find(String key);

	/**
	 * 모든 정책 정의를 반환합니다.
	 * @return 정책 키와 정의의 맵
	 */
	default Map<String, PolicyDefinition> findAll() {
		return Collections.emptyMap();
	}
}
