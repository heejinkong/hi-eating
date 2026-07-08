package kr.or.hieating.chat.mapper;

import java.util.List;
import java.util.Optional;
import kr.or.hieating.chat.dto.ChatMessageCreateCommand;
import kr.or.hieating.chat.dto.ChatMessageDto;
import kr.or.hieating.chat.dto.ChatRoomCreateCommand;
import kr.or.hieating.chat.dto.ChatRoomSummaryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ChatMapper {

  Optional<ChatRoomSummaryDto> findRoomByUserId(@Param("userId") long userId);

  Optional<ChatRoomSummaryDto> findRoomById(@Param("roomId") long roomId);

  List<ChatRoomSummaryDto> findRoomsByAdminId(@Param("adminId") long adminId);

  Optional<Long> findAssignableAdminId(@Param("excludeUserId") long excludeUserId);

  Optional<String> findAdminPresenceStatus(@Param("adminId") long adminId);

  List<ChatMessageDto> findRecentMessagesByRoomId(
      @Param("roomId") long roomId, @Param("limit") int limit);

  List<ChatMessageDto> findMessagesBeforeByRoomId(
      @Param("roomId") long roomId,
      @Param("beforeMessageId") long beforeMessageId,
      @Param("limit") int limit);

  Optional<ChatMessageDto> findMessageById(@Param("messageId") long messageId);

  int insertRoom(ChatRoomCreateCommand command);

  int insertMessage(ChatMessageCreateCommand command);

  int updateRoomForUserMessage(@Param("roomId") long roomId);

  int updateRoomForAdminMessage(@Param("roomId") long roomId, @Param("adminId") long adminId);

  int assignAdminToRoom(@Param("roomId") long roomId, @Param("adminId") long adminId);

  int markAdminConnected(@Param("adminId") long adminId);

  int markAdminDisconnected(@Param("adminId") long adminId);

  int updateAdminPresenceStatus(@Param("adminId") long adminId, @Param("status") String status);

  int recordAdminUserMessageReceived(@Param("adminId") long adminId);

  int markUserRead(@Param("roomId") long roomId);

  int markAdminRead(@Param("roomId") long roomId);
}
