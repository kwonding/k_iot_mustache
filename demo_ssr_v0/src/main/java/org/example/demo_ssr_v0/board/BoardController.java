package org.example.demo_ssr_v0.board;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.demo_ssr_v0._core.errors.exception.*;
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
    private final BoardPersistRepository repository;

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
        if (sessionUser == null) {
            System.out.println("로그인 안한 사용자의 요청이 들어옴");
            return "redirect:/login";
        }

        // 2. 인가 검사 (O)
        Board board = repository.findById(id);
        if (board == null) {
            throw new Exception500("게시글이 삭제 되었습니다.");
        }

        if (!board.isOwner(sessionUser.getId())) {
            throw new Exception403("해당 게시글의 수정 권한이 없습니다.");
        }

//        if (board.getUser().getId().equals(sessionUser.getId()) == false) {
//            System.out.println("권한 없음");
//            throw new RuntimeException("수정 권한 없습니다");
//        }

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
        // 1. 인증 처리
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            throw new Exception401("로그인 후 사용할 수 있습니다.");
        }

        // 2. 권한 체크
        // 조회
        Board board = repository.findById(id);

        if (!board.isOwner(sessionUser.getId())) {
            throw new Exception403("해당 게시글의 수정 권한이 없습니다.");
        }

        try {
            repository.updateById(id, updateDto);
            // 더티 체킹 활용
        } catch (Exception e) {
            throw new RuntimeException("게시글 수정 실패");
        }

        return "redirect:/board/list";
    }

    /**
     * 게시글 목록 화면 요청 - 인증 X, 인가 X
     * @param model
     * @return
     */
    @GetMapping({"/board/list", "/"})
    public String boardList(Model model) {
        List<Board> boardList = repository.findAll();

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
        if (sessionUser == null) {
            throw new Exception401("로그인 후 사용 가능합니다.");
        }

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

        // 1. 인증 처리
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            throw new Exception401("로그인 후 사용 가능합니다.");
        }

        Board board = saveDto.toEntity(sessionUser);
        repository.save(board);

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
    public String getBoardById(@PathVariable Long id, Model model) {

        Board board = repository.findById(id);
        if (board == null) {
            // 404 Not Found
            throw new Exception404("게시글을 찾을 수 없습니다.");
        }

        model.addAttribute("board", board);

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
        if (sessionUser == null) {
            throw new Exception401("로그인 후 사용 가능합니다.");
        }

        // 2. 인가 처리 || 관리자 권한
        // 조회
        Board board = repository.findById(id);
        if (!board.isOwner(sessionUser.getId())) {
            throw new Exception403("삭제 권한이 없습니다.");
        }

        repository.deleteById(id);

        return "redirect:/";
    }

}
