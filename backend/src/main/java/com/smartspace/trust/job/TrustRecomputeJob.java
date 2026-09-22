package com.smartspace.trust.job;

import com.smartspace.trust.entity.TrustScore;
import com.smartspace.trust.repository.TrustScoreRepository;
import com.smartspace.trust.service.TrustScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrustRecomputeJob {

    private final TrustScoreRepository trustScoreRepository;
    private final TrustScoreService trustScoreService;

    // Run every night at 2:00 AM
    @Scheduled(cron = "0 0 2 * * ?")
    public void recomputeAllScores() {
        log.info("Starting nightly TrustRecomputeJob...");
        List<TrustScore> allScores = trustScoreRepository.findAll();
        
        int count = 0;
        for (TrustScore score : allScores) {
            try {
                trustScoreService.recomputeScore(score.getSubjectType(), score.getSubjectId());
                count++;
            } catch (Exception e) {
                log.error("Failed to recompute trust score for {} {}", score.getSubjectType(), score.getSubjectId(), e);
            }
        }
        log.info("Finished TrustRecomputeJob. Recomputed {} scores.", count);
    }
}
