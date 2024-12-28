package com.fupto.back.admin.board.service;

import com.fupto.back.admin.board.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


public interface BoardService {

    // 전체 조회
    List<BoardListDto> getList();
    List<Long> findBoardsWithComments(List<Long> boardIds);

    // id 조회
    BoardDetailDto getBoardById(Long id);
    BoardDefaultDto getSearch(BoardSearchDto boardSearchDto);

    // 등록
    BoardListDto createPost(BoardListDto boardListDto, MultipartFile file) throws IOException;

    // 수정
    BoardListDto show(Long id);
    BoardListDto update(Long id, BoardUpdateDto boardUpdateDto, MultipartFile file) throws IOException;

    // 삭제
    SuccessResponseDto deletePost(Long id) throws Exception;
    void deleteSelected(List<Long> ids);

    // 엑티브 수정
    BoardListDto updateActive(Long id, Boolean active);


}
