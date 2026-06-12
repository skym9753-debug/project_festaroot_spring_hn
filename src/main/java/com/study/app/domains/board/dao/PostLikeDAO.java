package com.study.app.domains.board.dao;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.study.app.domains.board.dto.PostLikeDTO;

@Repository
public class PostLikeDAO {

    @Autowired
    private SqlSessionTemplate mybatis;

    public int countLike(PostLikeDTO dto) {
        return mybatis.selectOne("PostLike.countLike", dto);
    }

    public int insertLike(PostLikeDTO dto) {
        return mybatis.insert("PostLike.insertLike", dto);
    }

    public int deleteLike(PostLikeDTO dto) {
        return mybatis.delete("PostLike.deleteLike", dto);
    }

    public int increaseLikeCount(Long post_id) {
        return mybatis.update("PostLike.increaseLikeCount", post_id);
    }

    public int decreaseLikeCount(Long post_id) {
        return mybatis.update("PostLike.decreaseLikeCount", post_id);
    }

    public int getLikeCount(Long post_id) {
        return mybatis.selectOne("PostLike.getLikeCount", post_id);
    }
    
}
