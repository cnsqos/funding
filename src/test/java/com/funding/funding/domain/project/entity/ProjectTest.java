package com.funding.funding.domain.project.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import com.funding.funding.domain.project.exception.InvalidProjectStatusTransitionException;
import com.funding.funding.domain.project.status.ProjectStatus;

class ProjectTest {

    // ✅ helper: 규칙대로 FUNDING 상태까지 만든다
    private Project createFundingProject() {
        Project project = new Project();                 // DRAFT
        project.requestReview();                         // DRAFT -> REVIEW_REQUESTED
        project.changeStatus(ProjectStatus.APPROVED);     // REVIEW_REQUESTED -> APPROVED
        project.changeStatus(ProjectStatus.FUNDING);      // APPROVED -> FUNDING
        return project;
    }

    // ✅ helper: 규칙대로 REJECTED 상태까지 만든다
    private Project createRejectedProject() { // 왜 필요한가 REJECTED -> 재심사 테스트를 깔끔하게 분리
        Project project = new Project();                 // DRAFT
        project.requestReview();                         // DRAFT -> REVIEW_REQUESTED
        project.changeStatus(ProjectStatus.REJECTED);     // REVIEW_REQUESTED -> REJECTED
        return project;
    }

    @Test
    void DRAFT에서_심사요청하면_REVIEW_REQUESTED로_변경된다() { // DRAFT → REVIEW_REQUESTED 허용, 로그 내용(before=DRAFT, after=REVIEW_REQUESTED)
        Project project = new Project();

        project.requestReview();

        assertEquals(ProjectStatus.REVIEW_REQUESTED, project.getStatus());
    }

    @Test
    void REVIEW_REQUESTED에서_APPROVED로_변경된다() { // REVIEW_REQUESTED → APPROVED 허용, 로그 내용(before=REVIEW_REQUESTED, after=APPROVED)
        Project project = new Project();
        project.requestReview(); // DRAFT -> REVIEW_REQUESTED

        project.changeStatus(ProjectStatus.APPROVED);

        assertEquals(ProjectStatus.APPROVED, project.getStatus());
    }

    @Test
    void REVIEW_REQUESTED에서_REJECTED로_변경된다() { // REVIEW_REQUESTED → REJECTED 허용, 로그내용(REVIEW_REQUESTED → REJECTED 허용)
        Project project = new Project();
        project.requestReview(); // DRAFT -> REVIEW_REQUESTED

        project.changeStatus(ProjectStatus.REJECTED);

        assertEquals(ProjectStatus.REJECTED, project.getStatus());
    }

    // ✅ [에러 1] REJECTED에서_재심사요청하면_REVIEW_REQUESTED로_변경된다
    @Test
    void REJECTED에서_재심사요청하면_REVIEW_REQUESTED로_변경된다() { //REJECTED → REVIEW_REQUESTED 허용(재심사 규칙), 로그 내용(before=REJECTED, after=REVIEW_REQUESTED) 
        Project project = createRejectedProject(); // ... -> REJECTED

        project.requestReview(); // REJECTED -> REVIEW_REQUESTED

        assertEquals(ProjectStatus.REVIEW_REQUESTED, project.getStatus());
    }

    @Test
    void APPROVED에서_FUNDING으로_변경된다() { // APPROVED → FUNDING 허용, 로그 내용(before=APPROVED, after=FUNDING)
        Project project = new Project();
        project.requestReview(); // DRAFT -> REVIEW_REQUESTED
        project.changeStatus(ProjectStatus.APPROVED); // REVIEW_REQUESTED -> APPROVED

        project.changeStatus(ProjectStatus.FUNDING);

        assertEquals(ProjectStatus.FUNDING, project.getStatus());
    }

    // ✅ [에러 2] FUNDING에서_STOPPED로_변경된다
    @Test
    void FUNDING에서_STOPPED로_변경된다() { // FUNDING → STOPPED 허용, 로그 내용(before=FUNDING, after=STOPPED)
        Project project = createFundingProject(); // ... -> FUNDING

        project.changeStatus(ProjectStatus.STOPPED); // FUNDING -> STOPPED

        assertEquals(ProjectStatus.STOPPED, project.getStatus());
    }

    // ✅ [에러 3] STOPPED에서_FUNDING으로_재개된다
    @Test
    void STOPPED에서_FUNDING으로_재개된다() { // STOPPED → FUNDING 허용(재개 규칙), 로그 내용(before=STOPPED, after=FUNDING)
        Project project = createFundingProject(); // ... -> FUNDING
        project.changeStatus(ProjectStatus.STOPPED); // FUNDING -> STOPPED

        project.changeStatus(ProjectStatus.FUNDING); // STOPPED -> FUNDING

        assertEquals(ProjectStatus.FUNDING, project.getStatus());
    }

    // ✅ [에러 4] FUNDING에서_DELETE_REQUESTED로_변경된다
    @Test
    void FUNDING에서_DELETE_REQUESTED로_변경된다() { // FUNDING → DELETE_REQUESTED 허용, 로그 내용(before=FUNDING, after=DELETE_REQUESTED)
        Project project = createFundingProject(); // ... -> FUNDING

        project.changeStatus(ProjectStatus.DELETE_REQUESTED); // FUNDING -> DELETE_REQUESTED

        assertEquals(ProjectStatus.DELETE_REQUESTED, project.getStatus());
    }

    // ✅ [에러 5] DELETE_REQUESTED에서_DELETED로_변경된다
    @Test
    void DELETE_REQUESTED에서_DELETED로_변경된다() { // DELETE_REQUESTED → DELETED 허용, 로그 내용(before=DELETE_REQUESTED, after=DELETED)
        Project project = createFundingProject(); // ... -> FUNDING
        project.changeStatus(ProjectStatus.DELETE_REQUESTED); // FUNDING -> DELETE_REQUESTED

        project.changeStatus(ProjectStatus.DELETED); // DELETE_REQUESTED -> DELETED

        assertEquals(ProjectStatus.DELETED, project.getStatus());
    }

    // ✅ [에러 6] FUNDING에서_심사요청하면_예외가_발생한다
    @Test
    void FUNDING에서_심사요청하면_예외가_발생한다() { // FUNDING에서 requestReview()는 불허, 
        Project project = createFundingProject(); // ... -> FUNDING

        assertThrows(InvalidProjectStatusTransitionException.class, project::requestReview);
    }

    @Test
    void DRAFT에서_APPROVED로_바로가면_예외() { // DRAFT → APPROVED 직행 불허(실패 케이스 담당), 예외 발생, 상태 DRAFT 그대로, 로그 추가 없음
        Project project = new Project(); // DRAFT

        assertThrows(InvalidProjectStatusTransitionException.class,
                () -> project.changeStatus(ProjectStatus.APPROVED));
    }
}