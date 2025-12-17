package org.example.demo_ssr_v0.board;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.demo_ssr_v0._core.errors.exception.*;
import org.example.demo_ssr_v0.reply.ReplyResponse;
import org.example.demo_ssr_v0.reply.ReplyService;
import org.example.demo_ssr_v0.user.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@RequiredArgsConstructor // 의존성 주입 방법3 (with final)
@Controller // IoC
public class BoardController {

//    @Autowired // 의존성 주입 방법 1
    private final BoardService boardService;
    private final ReplyService replyService; // 추가

    // 생성자 의존 주입 방법 2
//    public BoardController(BoardPersistRepository boardPersistRepository) {
//        this.boardPersistRepository = boardPersistRepository;
//    }

    /**
     * 게시글 수정 화면 요청 (권한 확인 - 인가 처리)
     * @param id
     * @param model
     * @param session
     * @return
     */
    // http://localhost:8080/board/1/update
    @GetMapping("/board/{id}/update")
    public String updateForm(@PathVariable Long id, Model model, HttpSession session) {
        // Model model 말고 HttpServletRequest request 가능

        // 1. 인증 검사 (O) - 로그인
        User sessionUser = (User) session.getAttribute("sessionUser"); // sessionUSer -> 상수로 빼면 좋음
        // LoginInterceptor 가 알아서 처리 해줌! 위의 코드는 밑에서 조회해야해서 살려둠

        // 2. 인가 검사 (O)
        BoardResponse.UpdateFormDTO board = boardService.게시글수정화면(id, sessionUser.getId());

        model.addAttribute("board", board);
//        request.setAttribute("board", board);

        return "board/update-form";
    }

    /**
     * 게시글 수정 기능 요청 - 권한 확인
     * @param id
     * @param updateDto
     * @param session
     * @return
     */
    // http://localhost:8080/board/1/update
    @PostMapping("/board/{id}/update")
    public String updateProc(
            @PathVariable Long id,
            BoardRequest.UpdateDto updateDto,
            HttpSession session
    ) {
        // 인증 처리
        User sessionUser = (User) session.getAttribute("sessionUser");
        // LoginInterceptor 가 알아서 처리 해줌!

        updateDto.validate();
        boardService.게시글수정(updateDto, id, sessionUser.getId());

        return "redirect:/board/list";
    }

    /**
     * 게시글 목록 화면 요청 - 인증 X, 인가 X
     * @param model
     * @return
     */
    @GetMapping({"/board/list", "/"})
    public String boardList(Model model) {
        List<BoardResponse.ListDTO> boardList = boardService.게시글목록조회();
        model.addAttribute("boardList", boardList);

        return "board/list";
    }

    /**
     * 게시글 작성 화면 요청 - 인증 O, 인가 X
     * @param session
     * @return
     */
    // http://localhost:8080/board/save
    @GetMapping("/board/save")
    public String saveForm(HttpSession session) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        // LoginInterceptor 가 알아서 처리 해줌!

        return "board/save-form";
    }

    /**
     * 게시글 작성 기능 요청 - 인증 O, 인가 X
     * @param saveDto
     * @param session
     * @return
     */
    // http://localhost:8080/board/save
    @PostMapping("/board/save")
    public String saveProc(BoardRequest.SaveDto saveDto, HttpSession session) {
        // HTTP 요청 : username=값&title=값&Content=값
        // 스프링이 처리 : new SaveDto 객체 생성 후 setter 메서드 호출해서 값을 쏙 넣어줌

        // 1. 인증 검사 - 인터셉터
        User sessionUser = (User) session.getAttribute("sessionUser");
        // LoginInterceptor 가 알아서 처리 해줌!

        // 2. 유효성검사 (형식), 논리적인 검사는 (서비스단)
        boardService.게시글작성(saveDto, sessionUser);

        return "redirect:/";
    }

    /**
     * 게시글 상세보기 화면 요청
     * @param id
     * @param model
     * @return
     */
    // http://localhost:8080/board/1
    @GetMapping("/board/{id}")
    public String detail(@PathVariable(name = "id") Long boardId, Model model, HttpSession session) {

        BoardResponse.DetailDTO board = boardService.게시글상세조회(boardId);

        // 세션에 로그인 사용자 정보 조회(없을 수도 있음)
        User sessionUser = (User) session.getAttribute("sessionUser");
        boolean isOwner = false;
        // 응답 DTO에 담겨있는 정보와 SessionUser 담겨 있는 정보를 확인하여 처리 가능
        if (sessionUser != null && board.getUserId() != null) {
            isOwner = board.getUserId().equals(sessionUser.getId()); // isOwner에 true/false 담김
        }

        // 댓글 목록 조회 (추가)
        // 로그인 안 한 상태에서 댓글 목록 요청시에 sessionUserId는 null 값임 - 방어적 코드 필요
        Long sessionUserId = sessionUser != null ? sessionUser.getId() : null;
        List<ReplyResponse.ListDTO> replyList = replyService.댓글목록조회(boardId, sessionUserId);

        model.addAttribute("isOwner", isOwner);
        model.addAttribute("board", board);
        model.addAttribute("replyList", replyList);

        return "/board/detail";
    }

    /**
     * 게시글 삭제 요청 기능 - 인증 O, 인가 O
     * @param id
     * @param session
     * @return
     */
    // 삭제 @DeleteMapping 이지만, form 태그 get, post만 가능 (쓰려면 fetch 함수 활용해야함)
    @PostMapping("/board/{id}/delete")
    public String deleteForm(@PathVariable Long id, HttpSession session) {
        // 1. 인증 처리
        User sessionUser = (User) session.getAttribute("sessionUser");
        // LoginInterceptor 가 알아서 처리 해줌!

        // 2. 인가 처리 || 관리자 권한
        // 조회
        boardService.게시글삭제(id, sessionUser.getId());

        return "redirect:/";
    }

}
