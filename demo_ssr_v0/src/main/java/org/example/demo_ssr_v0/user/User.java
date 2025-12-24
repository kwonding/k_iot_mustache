package org.example.demo_ssr_v0.user;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor // 기본 생성자
@Data
@Table(name = "user_tb")
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB 전략 따르겠다
    private Long id;

    @Column(unique = true)
    private String username;
    private String password;
    private String email;

    @CreationTimestamp
    private Timestamp createdAt;

    private String profileImage; // 추가

    /**
     * User(1) : UserRole(N)
     * User 가 UserRole 리스트를 관리합니다.(단방향)
     * 실제 DB 의 'user_role_tb' 테이블에 user_id 라는 fk 컬럼 생김
     *
     * CascadeType.ALL
     * - 운명 공동체,
     * User를 저장하면 Role도 자동 저장됨, User를 삭제하면 가지고 있던 Role도 함께 삭제됨
     * ex) 홍길동 - 관리자, 일반 사용자 // 홍길동 삭제 시 2가지 행도 자동 삭제 됨
     *
     * orphanRemoval = true
     * 리스트와 DB의 동기화
     * Java의 roles 리스트에서 요소(Role)를 .remove() 하거나 .clear() 하면
     * DB에서도 해당 데이터 (DELETE)가 실제로 처리됨
     */
    // 나중에 다른 개발자가 findById(쿼리메서드 호출시) 신경 쓸 필요없이 전부 role 까지 반환해줌
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private List<UserRole> roles = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'LOCAL'") // 문자열이므로 작은 따옴표 필수!
    private OAuthProvider provider;

    @Builder
    public User(Long id,
                String username,
                String password,
                String email,
                Timestamp createdAt,
                String profileImage,
                OAuthProvider provider
    ) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.createdAt = createdAt;
        this.profileImage = profileImage; // 추가
        this.provider = provider;

        // 방어적 코드 작성 (빌더의 경우 null이 들어올 수도 있으니까)
        if (provider == null) { // provider가 null일 경우
            this.provider = OAuthProvider.LOCAL; // 기본값으로 넣어줌
        } else {
            this.provider = provider; // 들어온 값이 있다면 그대로 저장
        }
    }

    // 회원정보 수정 비즈니스 로직 추가
    // DTO 설계
    public void update(UserRequest.UpdateDTO updateDTO) {
        // 유효성 검사
        updateDTO.validate();

        this.password = updateDTO.getPassword();
        this.email = updateDTO.getEmail();
        this.profileImage = updateDTO.getProfileImageFilename();

        // 더티 체킹 (변경 감지)
        // 트랜잭션이 끝나면 자동으로 update 쿼리 진행
    }

    // 회원 정보 소유자 확인 로직
    public boolean isOwner(Long userId) {
        return this.id.equals(userId);
    }

    // 새로운 역할을 추가하는 기능
    public void addRole(Role role) {
        this.roles.add(UserRole.builder().role(role).build());
    }

    // 해당 역할을 가지고 있는지 확인하는 기능
    public boolean hasRole(Role role) {
        // roles (리스트)에 컬렉션이 없거나 비어 있으면 역할이 없는 것
        if (this.roles == null || this.roles.isEmpty()) {
            return false;
        }
        // 즉시 로딩이라서 바로 사용해도 LAZY Exception 초기화예외 안터짐
        // any(어떤 것이든), Match(일치하다)
        // 즉, 리스트 안에 있는 것들 중 하나라도 조건이 맞는게 있다면 true를 반환
        return this.roles.stream() // Collection 이니까
                .anyMatch(r -> r.getRole() == role);
    }

    // 관리자인지 여부를 반환
    public boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }

    // 템플릿에서 {{#isAdmin}}...{{/isAdmin}} 형태로 사용하는 편의 메서드 설계
    public boolean getIsAdmin() {
        return isAdmin();
    }

    // 화면에 표시할 역할 문자열 제공
    // - ADMIN 이면 "ADMIN"
    public String getRoleDisplay() {
        return isAdmin() ? "ADMIN" : "USER";
    }

    // 분기 처리(머스태치 화면에서는 서버에 저장된 이미지든, URL 이미지이든 그냥
    // getProfilePath 라는 변수를 호출하면 알아서 세팅되게 만들겠다.
    public String getProfilePath() {
        if (this.profileImage == null) {
            return null;
        }
        // https 로 시작하면 소셜 이미지 URL 그대로 리턴
        // 아니면 (로컬 이미지) 폴더 경로 붙여서 리턴
        if (this.profileImage.startsWith("http")) {
            return this.profileImage;
        }
        return "/images/" + this.profileImage;
    }

    // return 이 true, false를 반환하는 연산자는 ==
    // 상수가 열거형이라서 이렇게 해야함
    public boolean isLocal() {
        return this.provider == OAuthProvider.LOCAL;
    }
}
