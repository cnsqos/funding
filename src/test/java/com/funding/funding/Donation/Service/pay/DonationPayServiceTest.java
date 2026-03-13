package com.funding.funding.Donation.Service.pay;

import com.funding.funding.domain.donation.entity.Donation;
import com.funding.funding.domain.donation.repository.DonationRepository;
import com.funding.funding.domain.donation.service.pay.DonationPayService;
import com.funding.funding.domain.donation.status.DonationStatus;
import com.funding.funding.domain.project.entity.Project;
import com.funding.funding.domain.project.entity.ProjectStatus;
import com.funding.funding.domain.project.repository.ProjectRepository;
import com.funding.funding.domain.user.entity.User;
import com.funding.funding.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// DonationPayService 단위 테스트
class DonationPayServiceTest {

    private DonationRepository donationRepository;
    private UserRepository userRepository;
    private ProjectRepository projectRepository;
    private DonationPayService payService;

    @BeforeEach
    void setUp() {
        donationRepository  = mock(DonationRepository.class);
        userRepository      = mock(UserRepository.class);
        projectRepository   = mock(ProjectRepository.class);
        payService = new DonationPayService(donationRepository, userRepository, projectRepository);
    }

    // ────────────────────────────────────────
    // createDonation
    // ────────────────────────────────────────

    @Test
    void 후원_정상_생성() {
        // given
        User user = makeUser(1L);
        Project project = makeProject(1L, ProjectStatus.FUNDING);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(donationRepository.save(any())).thenAnswer(inv -> {
            Donation d = inv.getArgument(0);
            setField(d, "id", 100L);
            return d;
        });

        // when
        Donation result = payService.createDonation(1L, 1L, 5000L);

        // then
        assertNotNull(result);
        assertEquals(DonationStatus.PENDING, result.getStatus());
        assertNotNull(result.getCancelDeadline()); // 24시간 후 세팅 확인
        verify(donationRepository).save(any());
    }

    @Test
    void 금액이_1000원_미만이면_예외() {
        assertThrows(IllegalArgumentException.class,
                () -> payService.createDonation(1L, 1L, 500L));
    }

    @Test
    void 금액이_1000원_단위가_아니면_예외() {
        assertThrows(IllegalArgumentException.class,
                () -> payService.createDonation(1L, 1L, 1500L));
    }

    @Test
    void 존재하지_않는_유저면_예외() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> payService.createDonation(999L, 1L, 5000L));
    }

    @Test
    void 존재하지_않는_프로젝트면_예외() {
        User user = makeUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> payService.createDonation(1L, 999L, 5000L));
    }

    // ────────────────────────────────────────
    // markSuccess / markFailed
    // ────────────────────────────────────────

    @Test
    void PENDING상태_결제성공_처리() {
        Donation donation = makeDonation(1L, DonationStatus.PENDING);
        when(donationRepository.findById(1L)).thenReturn(Optional.of(donation));

        payService.markSuccess(1L);

        assertEquals(DonationStatus.SUCCESS, donation.getStatus());
    }

    @Test
    void PENDING상태_결제실패_처리() {
        Donation donation = makeDonation(1L, DonationStatus.PENDING);
        when(donationRepository.findById(1L)).thenReturn(Optional.of(donation));

        payService.markFailed(1L);

        assertEquals(DonationStatus.FAILED, donation.getStatus());
    }

    @Test
    void SUCCESS상태에서_다시_성공처리하면_예외() {
        Donation donation = makeDonation(1L, DonationStatus.SUCCESS);
        when(donationRepository.findById(1L)).thenReturn(Optional.of(donation));

        assertThrows(IllegalStateException.class, () -> payService.markSuccess(1L));
    }

    // ────────────────────────────────────────
    // 헬퍼
    // ────────────────────────────────────────

    private User makeUser(Long id) {
        // ✅ new User() → mock(User.class)
        //    User() 기본 생성자가 protected라 테스트에서 직접 호출 불가
        //    Mockito mock은 접근 제한 무시하고 객체 생성 가능
        User u = mock(User.class);
        when(u.getId()).thenReturn(id);
        return u;
    }

    private Project makeProject(Long id, ProjectStatus status) {
        Project p = new Project();
        setField(p, "id", id);
        setField(p, "status", status);
        return p;
    }

    private Donation makeDonation(Long id, DonationStatus status) {
        Donation d = new Donation();
        setField(d, "id", id);
        d.setStatus(status);
        setField(d, "amount", 5000L);
        Project p = new Project();
        setField(p, "id", 1L);
        setField(p, "currentAmount", 0L);
        setField(d, "project", p);

        return d;
    }

    private static void setField(Object t, String name, Object value) {
        try {
            Class<?> clazz = t.getClass();
            while (clazz != null) {
                try {
                    Field f = clazz.getDeclaredField(name);
                    f.setAccessible(true);
                    f.set(t, value);
                    return;
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }
            throw new NoSuchFieldException(name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}