package com.fupto.back.anonymous.board.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.fupto.back.anonymous.board.dto.*;
import com.fupto.back.anonymous.board.service.BoardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("boards")
public class BoardController {

    private BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping("/all")
    public List<BoardDto> findAll() {
        return boardService.findAll();
    }

    @GetMapping("list")
    public ResponseEntity<DefaultDto> userSearchBoard(
            @ModelAttribute SearchDto searchDto
    ) {
        return ResponseEntity.ok(boardService.userSearch(searchDto));
    }

    @GetMapping("{id}/detail")
    public ResponseEntity<DetailDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(boardService.getById(id));
    }

    @GetMapping("/check-comments/{boardId}")
    public ResponseEntity<Boolean> hasComments(@PathVariable Long boardId) {
        boolean hasComments = boardService.findBoardHasComments(boardId);
        return ResponseEntity.ok(hasComments);
    }


    @PatchMapping("{id}/inactive")
    public ResponseEntity<BoardDto> userInActive(
            @PathVariable Long id,
            @RequestParam Boolean active) throws Exception {
        return ResponseEntity.ok(boardService.userInActive(id, active));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/post")
    public ResponseEntity<BoardDto> userPost(
            @RequestParam("boardData") String boardDataJson,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        try{
            ObjectMapper  objectMapper = new ObjectMapper();
            BoardDto boardDto = objectMapper.readValue(boardDataJson, BoardDto.class);

            // 공지사항, FAQ 인 경우 ADMIN 권한 체크
            if (boardDto.getBoardCategoryId() == 1 || boardDto.getBoardCategoryId() == 3) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (!auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }

            BoardDto postBoard = boardService.userPost(boardDto, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(postBoard);
        }  catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("{id}/edit")
    public ResponseEntity<BoardDto> userEdit(@PathVariable Long id) {
        System.out.println(id);

        return ResponseEntity.ok(boardService.show(id));
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BoardDto> userEdit(
            @PathVariable Long id,
            @RequestPart("boardData") String boardDataJson,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            ObjectMapper  objectMapper = new ObjectMapper();
            UserUpdateDto userUpdateDto = objectMapper.readValue(boardDataJson, UserUpdateDto.class);

            BoardDto userUpdateBoard = boardService.update(id, userUpdateDto, file);

            return ResponseEntity.ok(userUpdateBoard);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

}
