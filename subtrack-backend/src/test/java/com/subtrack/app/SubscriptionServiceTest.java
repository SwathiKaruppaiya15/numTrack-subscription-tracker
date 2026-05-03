package com.subtrack.app;

import com.subtrack.app.dto.request.SubscriptionRequest;
import com.subtrack.app.dto.response.SubscriptionResponse;
import com.subtrack.app.entity.*;
import com.subtrack.app.exception.DuplicateResourceException;
import com.subtrack.app.mapper.SubscriptionMapper;
import com.subtrack.app.repository.SubscriptionRepository;
import com.subtrack.app.repository.UsageLogRepository;
import com.subtrack.app.repository.UserRepository;
import com.subtrack.app.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock SubscriptionRepository subscriptionRepository;
    @Mock UserRepository userRepository;
    @Mock UsageLogRepository usageLogRepository;
    @Mock SubscriptionMapper subscriptionMapper;

    @InjectMocks SubscriptionService subscriptionService;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .email("test@example.com")
                .fullName("Test User")
                .build();
    }

    @Test
    @DisplayName("Create subscription — happy path")
    void createSubscription_success() {
        SubscriptionRequest req = new SubscriptionRequest();
        req.setName("Netflix");
        req.setCategory("Entertainment");
        req.setAmount(new BigDecimal("499"));
        req.setBillingCycle(BillingCycle.MONTHLY);
        req.setStartDate(LocalDate.now().minusDays(5));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(subscriptionRepository.existsByUserIdAndName(userId, "Netflix")).thenReturn(false);
        when(subscriptionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(usageLogRepository.findLastUsedAt(any())).thenReturn(Optional.empty());
        when(usageLogRepository.countBySubscriptionId(any())).thenReturn(0L);
        when(subscriptionMapper.toResponse(any(), any(), anyLong()))
                .thenReturn(SubscriptionResponse.builder().name("Netflix").build());

        SubscriptionResponse response = subscriptionService.create(userId, req);
        assertThat(response.getName()).isEqualTo("Netflix");
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Create subscription — duplicate name throws DuplicateResourceException")
    void createSubscription_duplicate() {
        SubscriptionRequest req = new SubscriptionRequest();
        req.setName("Netflix");
        req.setCategory("Entertainment");
        req.setAmount(new BigDecimal("499"));
        req.setBillingCycle(BillingCycle.MONTHLY);
        req.setStartDate(LocalDate.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(subscriptionRepository.existsByUserIdAndName(userId, "Netflix")).thenReturn(true);

        assertThatThrownBy(() -> subscriptionService.create(userId, req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Netflix");
    }

    @Test
    @DisplayName("Cancel subscription — sets status to CANCELLED")
    void cancelSubscription() {
        UUID subId = UUID.randomUUID();
        Subscription sub = Subscription.builder()
                .id(subId).user(user).name("Spotify")
                .status(SubscriptionStatus.ACTIVE)
                .nextBillingDate(LocalDate.now().plusDays(10))
                .build();

        when(subscriptionRepository.findByIdAndUserId(subId, userId)).thenReturn(Optional.of(sub));
        when(subscriptionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(usageLogRepository.findLastUsedAt(any())).thenReturn(Optional.empty());
        when(usageLogRepository.countBySubscriptionId(any())).thenReturn(0L);
        when(subscriptionMapper.toResponse(any(), any(), anyLong()))
                .thenReturn(SubscriptionResponse.builder().status(SubscriptionStatus.CANCELLED).build());

        SubscriptionResponse response = subscriptionService.cancel(userId, subId);
        assertThat(response.getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
    }
}
