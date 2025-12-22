package org.example.demo_ssr_v0.user;

/**
 * 사용자 권한(역할)을 나타내는 enum 타입
 * - ADMIN : 관리자
 * - USER : 일반 사용자
 * - MANAGER ... 등
 *
 * 데이터의 범주화 할 때 사용함
 * 레벨 1 ~ 무한대 ...
 * 1 ~ 5 까지만 범주화 하고 싶을 때 사용
 */
public enum Role {
    ADMIN, USER
}
