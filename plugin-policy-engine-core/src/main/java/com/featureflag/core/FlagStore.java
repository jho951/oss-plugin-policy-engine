package com.pluginpolicyengine.core;

import java.util.*;

/**
 * 기능 플래그 정의를 조회하기 위한 저장소 추상화입니다.
 */
public interface FlagStore {
	/**
	 * 키로 플래그를 조회합니다.
	 *
	 * @param key 기능 플래그 키
	 * @return 플래그가 존재하면 해당 정의
	 */
	Optional<FlagDefinition> find(String key);

	/**
	 * 모든 플래그 정의를 반환합니다.
	 *
	 * @return 플래그 키와 정의의 맵
	 */
	default Map<String, FlagDefinition> findAll() { return Map.of(); }
}
