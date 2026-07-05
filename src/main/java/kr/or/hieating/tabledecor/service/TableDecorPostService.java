package kr.or.hieating.tabledecor.service;

import java.util.List;
import kr.or.hieating.tabledecor.dto.TableDecorLikeToggleResponseDto;
import kr.or.hieating.tabledecor.dto.TableDecorPostListItemDto;
import kr.or.hieating.tabledecor.dto.TableDecorPostPageResponseDto;
import kr.or.hieating.tabledecor.dto.TableDecorPostSearchCondition;
import kr.or.hieating.tabledecor.mapper.TableDecorPostMapper;
import kr.or.hieating.utils.ImageUrlResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TableDecorPostService {

  private final TableDecorPostMapper tableDecorPostMapper;
  private final ImageUrlResolver imageUrlResolver;

  public TableDecorPostPageResponseDto findPosts(TableDecorPostSearchCondition condition) {
    int totalCount = tableDecorPostMapper.countPosts(condition);
    int totalPages = Math.max((int) Math.ceil((double) totalCount / condition.getSize()), 1);
    List<TableDecorPostListItemDto> posts = tableDecorPostMapper.findPosts(condition);

    posts.forEach(post -> post.setImageSrc(imageUrlResolver.resolve(post.getImageSrc())));

    return new TableDecorPostPageResponseDto(
        posts, condition.getPage(), condition.getSize(), totalCount, totalPages);
  }

  public List<TableDecorPostListItemDto> findTopLikedPosts(Long currentUserId, int limit) {
    int safeLimit = Math.max(1, limit);
    List<TableDecorPostListItemDto> posts =
        tableDecorPostMapper.findTopLikedPosts(currentUserId, safeLimit);

    posts.forEach(post -> post.setImageSrc(imageUrlResolver.resolve(post.getImageSrc())));
    return posts;
  }

  @Transactional
  public TableDecorLikeToggleResponseDto toggleLike(Long userId, Long postId) {
    boolean alreadyLiked = tableDecorPostMapper.countLike(userId, postId) > 0;

    if (alreadyLiked) {
      tableDecorPostMapper.deleteLike(userId, postId);
      tableDecorPostMapper.decreasePostLikeCount(postId);
      return new TableDecorLikeToggleResponseDto(
          postId, false, tableDecorPostMapper.findPostLikeCount(postId));
    }

    tableDecorPostMapper.insertLike(userId, postId);
    tableDecorPostMapper.increasePostLikeCount(postId);
    return new TableDecorLikeToggleResponseDto(
        postId, true, tableDecorPostMapper.findPostLikeCount(postId));
  }
}
