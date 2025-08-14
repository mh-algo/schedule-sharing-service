package com.minhyung.schedule.security.principal;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 인증 컨텍스트에 담기는 사용자 주체(Principal).
 * <p>
 * 이 타입을 JSON으로 직렬화할 때 {@code id}는 제외됩니다.
 * JWT 생성 시에는 {@code id} 값을 JSON에 넣지 않고
 * {@code sub(Subject)} 클레임에만 매핑합니다.
 *
 * <h2>주의</h2>
 * <ul>
 *   <li>{@link JsonIgnore}는 모든 JSON 직렬화(응답/로그/캐시 등)에 적용됩니다.</li>
 *   <li>권한/클레임 정책이 바뀌면 JWT 생성부를 함께 업데이트하세요.</li>
 * </ul>
 *
 * @param id       내부 식별자(서브젝트). JSON 직렬화 시 제외되며, JWT의 {@code sub}로 매핑됩니다.
 * @param verified 예: 이메일 검증 여부 등 “검증 완료” 상태 플래그
 */
public record UserPrincipal(
        @JsonIgnore Long id,    // 토큰 생성할 때 json 변환 시 제외(id 값은 sub에 넣기 때문)
        boolean verified
) {
}
