package com.minhyung.schedule.notification.scheduler;

import com.minhyung.schedule.notification.domain.QueueFailed;
import com.minhyung.schedule.notification.domain.NotificationRetryInfo;
import com.minhyung.schedule.notification.domain.QueueMessage;
import com.minhyung.schedule.notification.domain.RetryInfo;
import com.minhyung.schedule.notification.props.NotifyRetryProps;
import com.minhyung.schedule.notification.service.BackoffCalculator;
import com.minhyung.schedule.notification.service.NotificationStatusService;
import com.minhyung.schedule.notification.service.QueuePublisher;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class NotificationRetryScheduler {
    private final ThreadPoolTaskScheduler scheduler;
    private final NotificationStatusService notificationStatusService;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final NotifyRetryProps props;
    private final QueuePublisher publisher;
    private final BackoffCalculator backoffCalculator;

    public NotificationRetryScheduler(ThreadPoolTaskScheduler scheduler,
                                         NotificationStatusService notificationStatusService,
                                         NotifyRetryProps props, QueuePublisher publisher,
                                         BackoffCalculator backoffCalculator) {
        this.scheduler = scheduler;
        this.notificationStatusService = notificationStatusService;
        this.props = props;
        this.publisher = publisher;
        this.backoffCalculator = backoffCalculator;
    }

    @EventListener(ApplicationReadyEvent.class)
    protected void start() {
        if (running.compareAndSet(false, true)) {
            int n = Math.max(1, scheduler.getPoolSize());
            log.debug("ThreadPoolSize: {}", n);
            for (int i = 0; i < n; i++) {
                scheduler.submit(this::task);
            }
        }
    }

    @PreDestroy
    protected void stop() {
        running.set(false);
        scheduler.shutdown();    // 스레드 풀 종료
    }

    protected void task() {
        Thread currentThread = Thread.currentThread();
        while (running.get()) {
            try {
                int batchSize = props.batchSize();

                // 점유 만료된 PROGRESSING 상태를 RETRY_PENDING 상태로 변경
                notificationStatusService.changeProgressingExpiredToRetryPending(batchSize);

                // 최대 시도 횟수보다 작은 경우 READY, 클 경우 FAILED로 변경 후 재시도할 리스트 반환
                List<RetryInfo> sendingRetryInfoList = notificationStatusService.changeRetryPendingToReadyOrFailed(batchSize, props.maxAttempts());

                // 재시도할 알림이 존재하는 경우
                if (!sendingRetryInfoList.isEmpty()) {
                    Map<Long, RetryInfo> retryInfoMap = sendingRetryInfoList.stream()
                            .collect(Collectors.toMap(RetryInfo::notificationId, Function.identity()));

                    // notification 에서 수신자 id, payload 조회
                    List<Long> notificationIds = new ArrayList<>(retryInfoMap.keySet());
                    List<NotificationRetryInfo> notificationRetryInfoList = notificationStatusService.findRetryInfoByNotificationIds(notificationIds);

                    // sendingRetryInfo의 id와 NotificationRetryInfo의 notificationId가 일치할 경우 QueueMessage 생성
                    List<QueueMessage> messages = new ArrayList<>(notificationRetryInfoList.size());

                    for (NotificationRetryInfo notificationRetryInfo : notificationRetryInfoList) {
                        RetryInfo retryInfo = retryInfoMap.get(notificationRetryInfo.id());
                        if (retryInfo != null) {
                            messages.add(QueueMessage.of(notificationRetryInfo.id(), notificationRetryInfo.receiverId(),
                                    notificationRetryInfo.payload(), retryInfo.attempt()));
                        } else {
                            log.warn("id number mismatch (NotificationRetryInfo: {})", notificationRetryInfo.id());
                        }
                    }

                    // 큐에 메시지 삽입
                    List<QueueFailed> failedList = publisher.publishAll(messages);

                    // 큐에 삽입 실패한 경우, 나중에 재시도 할 수 있도록 sending 상태를 RETRY_PENDING로 변경
                    for (QueueFailed failed : failedList) {
                        // 지수백오프 계산
                        int attempt = failed.attempt();
                        long backoff = backoffCalculator.calculate(attempt);

                        String errorMessage = "Queue publish failed";
                        long sendingId = failed.sendingId();
                        boolean changedRetry = notificationStatusService.changeRetryPendingWhenQueuePublishFailed(sendingId, errorMessage, backoff, attempt);
                        if (!changedRetry) {
                            log.warn("failed to change RETRY_PENDING: {}", sendingId);
                        }
                    }
                } else {    // 재시도할 메시지가 존재하지 않는 경우
                    log.debug("Empty RetryInfoList");
                    Thread.sleep(props.delayMs());
                }
            } catch (InterruptedException e) {
                currentThread.interrupt();
                break;
            } catch (Exception e) {
                log.warn("Retry task error", e);
            }
        }
    }
}
