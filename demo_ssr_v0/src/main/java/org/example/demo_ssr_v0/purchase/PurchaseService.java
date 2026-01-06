package org.example.demo_ssr_v0.purchase;

import lombok.RequiredArgsConstructor;
import org.example.demo_ssr_v0._core.errors.exception.Exception400;
import org.example.demo_ssr_v0._core.errors.exception.Exception404;
import org.example.demo_ssr_v0.board.Board;
import org.example.demo_ssr_v0.board.BoardRepository;
import org.example.demo_ssr_v0.user.User;
import org.example.demo_ssr_v0.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    // 유료 게시글 구매 기능
    private static final Integer PREMIUM_BOARD_PRICE = 500;

    @Transactional
    public void 구매하기(Long userId, Long boardId) {
        // 홍길동이 1번 게시글을 구매한다

        // 1. 게시글 조회 (유료/무료 게시글 확인)
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));

        // 2. 유료 게시글인지 확인
        if (board.getPremium() == null || board.getPremium() == false) {
            throw new Exception400("유료 게시글이 아닙니다");
        }

        // 3. 작성자가 자신의 게시글을 구매하려는 경우 방지
        if (board.isOwner(userId)) {
            throw new Exception400("자신이 작성한 게시글은 구매할 수 없습니다.");
        }

        // 4. 일반 사용자가 이미 구매한 게시글인지 여부 확인
        if (purchaseRepository.existsByUserIdAndBoardId(userId, boardId)) {
            throw new Exception400("이미 구매한 게시글 입니다.");
        }

        // 5. 사용자 정보 조회 (구매 요청자)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다."));

        // 6. 해당하는 사용자의 포인트 차감 처리(User 테이블)
        user.deductPoint(PREMIUM_BOARD_PRICE);

        // 7. 구매 내역 저장
        Purchase purchase = Purchase.builder()
                .user(user)
                .board(board)
                .price(PREMIUM_BOARD_PRICE)
                .build();

        // 엔티티로 저장해야 함
        purchaseRepository.save(purchase);

        // 8. 구매 요청자의 포인트 차감 갱신 (user 상태 갱신 - 더티체킹 쓰고있긴 하지만 한번 더)
        userRepository.save(user);

    }

    // 게시글 상세 보기 화면 들어 갈 때 내가 구매한 글 여부 확인
    public boolean 구매여부확인(Long userId, Long boardId) {
        // 비로그인 시 false로 던져주기 위함
        if (userId == null) {
            return false;
        }
        return purchaseRepository.existsByUserIdAndBoardId(userId, boardId);
    }

    // 유료 게시글 구매 내역 조회(세션 유저 기준)
    public List<PurchaseResponse.ListDTO> 구매내역조회(Long userId) {
        List<Purchase> purchaseList = purchaseRepository.findAllByUserIdWithBoard(userId);

        // 트랜잭션내에서 엔티티를 DTO로 변환
        return purchaseList.stream()
                .map(PurchaseResponse.ListDTO::new)
                .toList();
    }
}
