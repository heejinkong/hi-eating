package kr.or.hieating.tabledecor.mapper;

import java.util.List;
import kr.or.hieating.tabledecor.dto.TableDecorPostListItemDto;
import kr.or.hieating.tabledecor.dto.TableDecorPostSearchCondition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TableDecorPostMapper {

  List<TableDecorPostListItemDto> findPosts(TableDecorPostSearchCondition condition);

  int countPosts(TableDecorPostSearchCondition condition);

  List<TableDecorPostListItemDto> findTopLikedPosts(
      @Param("currentUserId") Long currentUserId, @Param("limit") int limit);

  int countLike(@Param("userId") Long userId, @Param("postId") Long postId);

  int insertLike(@Param("userId") Long userId, @Param("postId") Long postId);

  int deleteLike(@Param("userId") Long userId, @Param("postId") Long postId);

  int increasePostLikeCount(@Param("postId") Long postId);

  int decreasePostLikeCount(@Param("postId") Long postId);

  int findPostLikeCount(@Param("postId") Long postId);
}
