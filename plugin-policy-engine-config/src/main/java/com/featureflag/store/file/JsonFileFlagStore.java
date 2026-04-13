package com.pluginpolicyengine.store.file;

import com.pluginpolicyengine.core.FlagStore;
import com.pluginpolicyengine.core.FlagDefinition;

import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/** JSON 파일에서 플래그 정의를 읽는 파일 기반 {@link FlagStore} 구현체입니다. */
public final class JsonFileFlagStore implements FlagStore {

	private final Path filePath;
	private final long ttlMs;
	private final JsonFlagSerde serde;

	private final AtomicReference<Cache> cacheRef = new AtomicReference<>(Cache.empty());

	/**
	 * JSON 파일 기반 저장소를 생성합니다.
	 *
	 * @param filePath JSON 파일 경로
	 * @param ttl 캐시 TTL(0이면 TTL 캐시 비활성)
	 */
	public JsonFileFlagStore(String filePath, Duration ttl) {
		if (filePath == null || filePath.isBlank()) { throw new IllegalArgumentException("filePath is blank");}
		this.filePath = Paths.get(filePath);
		this.ttlMs = ttl == null ? 0L : Math.max(0L, ttl.toMillis());
		this.serde = new JsonFlagSerde();
	}

	/**
	 * 현재 캐시/스냅샷에서 키로 플래그를 조회합니다.
	 *
	 * @param key 기능 플래그 키
	 * @return 플래그가 존재하면 해당 정의
	 */
	@Override
	public Optional<FlagDefinition> find(String key) {
		if (key == null || key.isBlank()) return Optional.empty();
		Cache c = loadIfNeeded();
		return Optional.ofNullable(c.flags.get(key));
	}

	/**
	 * 로드된 모든 플래그를 불변 맵으로 반환합니다.
	 *
	 * @return 키와 정의의 맵
	 */
	@Override
	public Map<String, FlagDefinition> findAll() {
		return Collections.unmodifiableMap(loadIfNeeded().flags);
	}

	private Cache loadIfNeeded() {
		long now = System.currentTimeMillis();
		Cache cur = cacheRef.get();

		// TTL이 0이면 매번 읽기(원하면 TTL>0 권장)
		if (ttlMs > 0 && (now - cur.loadedAtMs) < ttlMs && cur.loadedAtMs > 0) {
			return cur;
		}

		// 파일 mtime 체크로 “TTL 지나도 파일 안 바뀌면 재파싱 생략” (가벼운 최적화)
		long mtime = lastModifiedMillis(filePath);
		if (cur.loadedAtMs > 0 && cur.fileMtimeMs == mtime && ttlMs > 0) {
			cacheRef.set(new Cache(cur.flags, now, mtime));
			return cacheRef.get();
		}

		// reload
		Map<String, FlagDefinition> parsed = readAndParse(filePath);
		Cache next = new Cache(parsed, now, mtime);
		cacheRef.set(next);
		return next;
	}

	private Map<String, FlagDefinition> readAndParse(Path path) {
		if (!Files.exists(path)) {
			return new HashMap<>();
		}
		try {
			String json = Files.readString(path);
			return serde.parseToMap(json);
		} catch (IOException e) {
			// 운영에서는 로깅 후 “이전 캐시 유지” 같은 전략도 가능
			return new HashMap<>();
		}
	}

	private static long lastModifiedMillis(Path path) {
		try {
			return Files.exists(path) ? Files.getLastModifiedTime(path).toMillis() : 0L;
		} catch (IOException e) {
			return 0L;
		}
	}

	private static final class Cache {
		final Map<String, FlagDefinition> flags;
		final long loadedAtMs;
		final long fileMtimeMs;

		Cache(Map<String, FlagDefinition> flags, long loadedAtMs, long fileMtimeMs) {
			this.flags = flags == null ? new HashMap<>() : new HashMap<>(flags);
			this.loadedAtMs = loadedAtMs;
			this.fileMtimeMs = fileMtimeMs;
		}

		static Cache empty() {
			return new Cache(new HashMap<>(), 0L, 0L);
		}
	}
}
