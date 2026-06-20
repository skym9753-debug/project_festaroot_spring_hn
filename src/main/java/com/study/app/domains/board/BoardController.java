package com.study.app.domains.board;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.study.app.domains.achievement.dto.AchievementResultDTO;
import com.study.app.domains.board.dto.CommunityPostDTO;
import com.study.app.domains.board.dto.PostAttachmentDTO;
import com.study.app.domains.board.dto.PostCommentDTO;
import com.study.app.domains.board.dto.PostReportDTO;
import com.study.app.domains.board.service.BoardService;
import com.study.app.domains.board.service.CommentActionService;
import com.study.app.domains.board.service.PostCommentService;
import com.study.app.domains.board.service.PostLikeService;
import com.study.app.domains.board.service.PostReportService;
import com.study.app.utils.JWTUtil;

@RestController
@RequestMapping("/board")
public class BoardController {

	@Autowired
	private JWTUtil jwt;

	@Autowired
	private BoardService boardService;

	@Autowired
	private PostCommentService commentService;

	@Autowired
	private PostLikeService likeService;

	@Autowired
	private PostReportService reportService;

	@Autowired
	private CommentActionService commentActionService;

	@PostMapping("/post")
	public ResponseEntity<Map<String,Object>> addPost(
			@RequestPart("post") CommunityPostDTO dto, 
			@RequestPart(value = "files", required = false) List<MultipartFile> files,
			@RequestHeader("Authorization") String authHeader){

		String token = authHeader.replace("Bearer ", "");
		String member_id = jwt.getSubject(token);

		dto.setMember_id(member_id);
		Map<String,Object> result = new HashMap<>();
		List<AchievementResultDTO> achievements = boardService.addPost(dto, files);
		result.put("achievements", achievements);

		return ResponseEntity.ok(result);
	}

	@GetMapping("/posts")
	public ResponseEntity<Map<String, Object>> getPostList(
			@RequestParam(defaultValue = "1") Long cpage,
			@RequestParam(defaultValue = "all") String category,
			@RequestParam(defaultValue = "latest") String sortBy,
			@RequestParam(defaultValue = "title") String searchType,
			@RequestParam(defaultValue = "") String keyword) {

		Map<String, Object> params = new HashMap<>();
		params.put("category", category);
		params.put("sortBy", sortBy);
		params.put("searchType", searchType);
		params.put("keyword", keyword);
		params.put("cpage", cpage);

		int totalPostCount = boardService.totalPostCount(params);

		Long startNum = cpage*10-9;
		Long endNum = cpage*10;

		params.put("startNum", startNum);
		params.put("endNum", endNum);

		List<CommunityPostDTO> list = boardService.getPosts(params);

		Map<String, Object> resp = new HashMap<>();

		resp.put("list", list);
		resp.put("totalPostCount", totalPostCount);

		return ResponseEntity.ok(resp);	
	}

	@GetMapping("/post/{id}")
	public ResponseEntity<Map<String, Object>> getPostDetail(@PathVariable Long id) {
		CommunityPostDTO dto = boardService.getPostDetail(id);
		List<PostAttachmentDTO> list = boardService.getPostAttachList(id);

		Map<String, Object> resp = new HashMap<>();
		resp.put("dto", dto);
		resp.put("list", list);

		return ResponseEntity.ok(resp);
	}

	@PutMapping("/post/{id}")
	public ResponseEntity<Void> updatePost(
			@PathVariable Long id,
			@RequestPart("post") CommunityPostDTO dto,
			@RequestPart(value = "files", required = false) List<MultipartFile> files){
		dto.setPost_id(id);
		System.out.println(id);
		boardService.updatePost(dto, files);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/post/{id}")
	public ResponseEntity<Void> deletePost(
			@PathVariable Long id) {
		System.out.println("게시글 삭제");
		
		boardService.deletePost(id);
		return ResponseEntity.ok().build();
	}

	// 댓글 / 대댓글 작성
	@PostMapping("/posts/{post_id}/comments")
	public ResponseEntity< Map<String,Object>> addComment(
			@PathVariable Long post_id,
			@RequestBody PostCommentDTO dto,
			@RequestHeader("Authorization") String authorization
			) {
		String token = authorization.replace("Bearer ", "");
		String member_id = jwt.getSubject(token);

		dto.setPost_id(post_id);
		dto.setMember_id(member_id);

		Map<String,Object> result = new HashMap<>(); 
		List<AchievementResultDTO> achievements = commentService.addComment(dto);
		result.put("achievements", achievements);

		return ResponseEntity.ok(result);
		//        if (result > 0) {
		//            return ResponseEntity.ok().build();
		//        }
		//
		//        return ResponseEntity.badRequest().build();
	}

	// 댓글 목록 조회
	@GetMapping("/posts/{post_id}/comments")
	public ResponseEntity<List<PostCommentDTO>> getComments(
			@PathVariable Long post_id
			) {
		List<PostCommentDTO> comments = commentService.getComments(post_id);

		return ResponseEntity.ok(comments);
	}

	// 댓글 수정
	@PutMapping("/comments/{comment_id}")
	public ResponseEntity<Void> updateComment(
			@PathVariable Long comment_id,
			@RequestBody PostCommentDTO dto,
			@RequestHeader("Authorization") String authorization
			) {
		String token = authorization.replace("Bearer ", "");
		String member_id = jwt.getSubject(token);

		dto.setComment_id(comment_id);
		dto.setMember_id(member_id);

		int result = commentService.updateComment(dto);

		if (result > 0) {
			return ResponseEntity.ok().build();
		}

		return ResponseEntity.badRequest().build();
	}

	// 댓글 삭제
	@DeleteMapping("/comments/{comment_id}")
	public ResponseEntity<Void> deleteComment(
			@PathVariable Long comment_id,
			@RequestHeader("Authorization") String authorization
			) {
		String token = authorization.replace("Bearer ", "");
		String member_id = jwt.getSubject(token);

		int result = commentService.deleteComment(comment_id, member_id);

		if (result > 0) {
			return ResponseEntity.ok().build();
		}

		return ResponseEntity.badRequest().build();
	}

	// 좋아요 토글
	@PostMapping("/posts/{post_id}/like")
	public ResponseEntity<Map<String, Object>> toggleLike(
			@PathVariable Long post_id,
			@RequestHeader("Authorization") String authorization
			) {
		String token = authorization.replace("Bearer ", "");
		String member_id = jwt.getSubject(token);

		Map<String, Object> result =
				likeService.toggleLike(post_id, member_id);

		return ResponseEntity.ok(result);
	}

	// 좋아요 상태 조회
	@GetMapping("/posts/{post_id}/like")
	public ResponseEntity<Map<String, Object>> getLikeStatus(
			@PathVariable Long post_id,
			@RequestHeader("Authorization") String authorization
			) {
		String token = authorization.replace("Bearer ", "");
		String member_id = jwt.getSubject(token);

		Map<String, Object> result =
				likeService.getLikeStatus(post_id, member_id);

		return ResponseEntity.ok(result);
	}

	// 게시글 신고
	@PostMapping("/posts/{post_id}/report")
	public ResponseEntity<String> reportPost(
			@PathVariable Long post_id,
			@RequestBody PostReportDTO dto,
			@RequestHeader("Authorization") String authorization
			) {
		String token = authorization.replace("Bearer ", "");
		String member_id = jwt.getSubject(token);

		dto.setPost_id(post_id);
		dto.setMember_id(member_id);



		boolean result = reportService.addReport(dto);

		if (result) {
			return ResponseEntity.ok("success");
		}

		return ResponseEntity.badRequest().body("already_reported");
	}

	// 댓글 / 대댓글 좋아요 토글
	@PostMapping("/comments/{comment_id}/like")
	public ResponseEntity<Map<String, Object>> toggleCommentLike(
			@PathVariable Long comment_id,
			@RequestHeader("Authorization") String authorization
			) {
		String token = authorization.replace("Bearer ", "");
		String member_id = jwt.getSubject(token);

		Map<String, Object> result =
				commentActionService.toggleCommentLike(comment_id, member_id);

		return ResponseEntity.ok(result);
	}

	// 댓글 / 대댓글 신고
	@PostMapping("/comments/{comment_id}/report")
	public ResponseEntity<String> reportComment(
			@PathVariable Long comment_id,
			@RequestBody Map<String, String> body,
			@RequestHeader("Authorization") String authorization
			) {
		String token = authorization.replace("Bearer ", "");
		String member_id = jwt.getSubject(token);

		String reason = body.get("reason");

		boolean result =
				commentActionService.reportComment(
						comment_id,
						member_id,
						reason
						);

		if (result) {
			return ResponseEntity.ok("success");
		}

		return ResponseEntity.badRequest().body("already_reported");
	}

	@GetMapping("/mypost/{id}")
	public ResponseEntity<List<CommunityPostDTO>> getMyPostList(@PathVariable String id,
			@RequestHeader("Authorization") String authorization){

		String token = authorization.replace("Bearer ", "");
		String member_id = jwt.getSubject(token);
		
		if(!id.equals(member_id)) {
			return ResponseEntity.badRequest().build();
		}
		
		List<CommunityPostDTO> list =  boardService.getMypostList(id);
		
		return ResponseEntity.ok(list);
	}
	
	// 메인화면 용 실시간 인기 게시글 Top 5 조회
	@GetMapping("/posts/popular")
	public ResponseEntity<List<CommunityPostDTO>> getPopularPosts() {
		Map<String, Object> params = new HashMap<>();
		params.put("category", "all");
		params.put("sortBy", "popular");
		params.put("excludeNotice", "Y"); // 공지사항(notice) 카테고리를 제외하기 위한 플래그 추가
		params.put("startNum", 1L); // 1L (Long 타입 통일)
	    params.put("endNum", 5L);   // 5L (Long 타입 통일)      

		List<CommunityPostDTO> popularList = boardService.getPosts(params);
		return ResponseEntity.ok(popularList);
	}

}
