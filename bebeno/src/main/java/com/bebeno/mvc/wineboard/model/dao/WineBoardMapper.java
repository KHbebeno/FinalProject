package com.bebeno.mvc.wineboard.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.bebeno.mvc.wineboard.model.vo.WineBoard;

@Mapper
public interface WineBoardMapper {

	// 게시글 목록조회 
	List<WineBoard> wineBoardList();

	// 게시글 수정
	int updateWineBoard(WineBoard wineboard);

	// 게시글 작성
	int insertWineBoard(WineBoard wineboard);

	// 게시물 상세조회 
	WineBoard selectWineBoardByNo(@Param("wineBno") Integer wineBno);

	// 게시글 삭제
	int deleteWineBoard();
	
	
	
	
}
