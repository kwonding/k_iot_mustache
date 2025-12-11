package org.example.demo_ssr_v0.board;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    // 게시글 수정 폼 페이지 요청 (화면요청)
    // http://localhost:8080/board/1/update
    @GetMapping("/board/{id}/update")
    public String updateForm(@PathVariable Long id, Model model) {
        // Model model 말고 HttpServletRequest request 가능

        Board board = repository.findById(id);
        if (board == null) {
            throw new RuntimeException("수정할 게시글을 찾을 수 없습니다.");
        }

        model.addAttribute("board", board);
//        request.setAttribute("board", board);

        return "board/update-form";
    }

    // 게시글 수정 요청 (기능요청)
    // http://localhost:8080/board/1/update
    @PostMapping("/board/{id}/update")
    public String updateProc(
            @PathVariable Long id,
            BoardRequest.UpdateDto updateDto
    ) {
        try {
            repository.updateById(id, updateDto);
            // 더티 체킹 활용
        } catch (Exception e) {
            throw new RuntimeException("게시글 수정 실패");
        }

        return "redirect:/board/list";
    }

    @GetMapping({"/board/list", "/"})
    public String boardList(Model model) {
        List<Board> boardList = repository.findAll();

        model.addAttribute("boardList", boardList);

        return "board/list";
    }

    // 게시글 저장 (화면요청)
    // http://localhost:8080/board/save
    @GetMapping("/board/save")
    public String saveForm() {
        return "board/save-form";
    }

    // 게시글 저장 요청 (기능요청)
    // http://localhost:8080/board/save
    @PostMapping("/board/save")
    public String saveProc(BoardRequest.SaveDto saveDto) {
        // HTTP 요청 : username=값&title=값&Content=값
        // 스프링이 처리 : new SaveDto 객체 생성 후 setter 메서드 호출해서 값을 쏙 넣어줌

        Board board = saveDto.toEntity();
        repository.save(board);

        return "redirect:/";
    }

    // 상세보기 화면
    // http://localhost:8080/board/1
    @GetMapping("/board/{id}")
    public String getBoardById(@PathVariable Long id, Model model) {

        Board board = repository.findById(id);
        if (board == null) {
            // 404 Not Found
            throw new RuntimeException("게시글을 찾을 수 없습니다 :" + id);
        }

        model.addAttribute("board", board);

        return "/board/detail";
    }

    // 삭제 @DeleteMapping 이지만, form 태그 get, post만 가능 (쓰려면 fetch 함수 활용해야함)
    @PostMapping("/board/{id}/delete")
    public String deleteForm(@PathVariable Long id) {

        repository.deleteById(id);

        return "redirect:/";
    }

}
