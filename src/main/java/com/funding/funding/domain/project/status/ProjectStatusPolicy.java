package com.funding.funding.domain.project.status;

import java.util.EnumMap; // Map을 쓰는데, 키가 enum(ProjectStatus)일 때 가장 적합한 구현체가 EnumMap
import java.util.EnumSet; // Set을 쓰는데, 원소가 enum(ProjectStatus)일 때 가장 적합한 구현체가 EnumSet
import java.util.Map; 	  // Map 인터페이스 타입으로 선언해서 구현체 교체가 쉬움(유연성)
import java.util.Set;	  // Set 인터페이스 타입으로 선언해서 구현체 교체가 쉬움(유연성)

// policy가 하는 일 이 상태 전이가 허용되는가 판단
// True / False만 반환, 메시지 생성 X, 예외 안 던짐
public final class ProjectStatusPolicy {

    // from(현재 상태) -> 가능한 to(다음 상태) 목록
	// 규칙표라서 상속해서 바꿔치기 하면 규칙이 흔들릴 가능성이 있기 때문에 final로 막아서 규칙이 한 군데에서만 유지되게 함
	// Map<ProjectStatus, Set<ProjectStatus>> -> 현재 상태(from) - 가능한 다음 상태들(to의 집합) 구조를 표현한 것
	// new EnumMap<>(ProjectStatus.class) -> 키가 enum이니까 EnumMap으로 최적화
    private static final Map<ProjectStatus, Set<ProjectStatus>> ALLOWED = new EnumMap<>(ProjectStatus.class);

    static { // 클래스가 로딩될 때 딱 한 번 규칙표를 채워 넣는 블록
        ALLOWED.put(ProjectStatus.DRAFT, // DRAFT에서 갈 수 있는 상태는 REVIEW_REQUESTED만 허용
                EnumSet.of(ProjectStatus.REVIEW_REQUESTED)); // 허용 목적지 목록을 set으로 만들고 중복을 	방지

        ALLOWED.put(ProjectStatus.REVIEW_REQUESTED, 	// 심사 요청 상태 -> 관리자 승인/반려 둘 중 하나만 가능
                EnumSet.of(ProjectStatus.APPROVED, ProjectStatus.REJECTED));

        ALLOWED.put(ProjectStatus.REJECTED, 			// 반려된 프로젝트는 재심사 요청만 가능, APPROVED로 직행 불가
                EnumSet.of(ProjectStatus.REVIEW_REQUESTED));

        ALLOWED.put(ProjectStatus.APPROVED, 			// 승인 이후에는 시작일 도달 시 FUNDING으로만 이동
                EnumSet.of(ProjectStatus.FUNDING)); 	// 승인 상태에서 바로 SUCESS/FAILED 같은 건 불가

        ALLOWED.put(ProjectStatus.FUNDING,
                EnumSet.of(
                        ProjectStatus.SUCCESS, 			// 마감 + 목표달성 
                        ProjectStatus.FAILED,  			// 마감 + 목표미달
                        ProjectStatus.STOPPED, 			// 관리자 중단
                        ProjectStatus.DELETE_REQUESTED	// 사용자 삭제 요청
                ));

        ALLOWED.put(ProjectStatus.STOPPED,
                EnumSet.of(ProjectStatus.FUNDING));     // 중단 상태, 재개 승인 시 FUNDING으로 
 
        ALLOWED.put(ProjectStatus.DELETE_REQUESTED,     // 삭제 요청 상태에서 삭제 완료로만 이동 가능
                EnumSet.of(ProjectStatus.DELETED));     // Project는 "전이만" 수행한다는 팀 규칙에 맞음

        // SUCCESS/FAILED/DELETED 는 종결 상태라서 put 안 함 (=> 기본 불허)
    }

    private ProjectStatusPolicy() {} 					// 생성자 PRIVATE로 막아서 객체 생성 금지
    													// 이 클래스는 인스턴스가 필요 없음
    public static boolean isAllowed(ProjectStatus from, ProjectStatus to) { // 어디서 상태를 바꾸려 할 때든 이걸 호출해서 판단하게 만들려고
        if (from == null || to == null) return false;   // null이 들어오면 바로 불허, null이 전달돼도 예외 폭발보다 안전하게 차단
        return ALLOWED.getOrDefault(from, EnumSet.noneOf(ProjectStatus.class)).contains(to); // from에 대한 규칙이 없으면 전부 불허로 처리
    }																						 // 그 집합 안에 to가 있으면 True, 없으면 Flase				
}																							 // 정해진 경로로만 강제 이동