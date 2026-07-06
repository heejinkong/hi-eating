package kr.or.hieating.ai.mapper;

import kr.or.hieating.ai.dto.TargetSelectionJobDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TargetSelectionJobMapper {

  int insertPendingJob(
      @Param("hotDealId") long hotDealId, @Param("maxRetryCount") int maxRetryCount);

  TargetSelectionJobDto findNextRunnableJob();

  TargetSelectionJobDto findJobByHotDealId(@Param("hotDealId") long hotDealId);

  int claimJob(@Param("jobId") long jobId);

  int markCompleted(
      @Param("jobId") long jobId,
      @Param("candidateCount") int candidateCount,
      @Param("selectedCount") int selectedCount,
      @Param("insertedCount") int insertedCount);

  int markFailed(@Param("jobId") long jobId, @Param("failureReason") String failureReason);

  int recoverInterruptedJobs();
}
