package com.devsquad10.shipping.application.service.allocation;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsquad10.shipping.application.dto.MinimumCountAllocationResult;
import com.devsquad10.shipping.application.exception.shippingAgent.ShippingAgentAlreadyAllocatedException;
import com.devsquad10.shipping.application.exception.shippingAgent.ShippingAssignmentCountException;
import com.devsquad10.shipping.application.exception.shippingAgent.ShippingStatusIsNotAllocatedException;
import com.devsquad10.shipping.application.exception.shippingAgent.SlackMessageSendToDesHubManagerIdException;
import com.devsquad10.shipping.domain.enums.ShippingAgentType;
import com.devsquad10.shipping.domain.enums.ShippingStatus;
import com.devsquad10.shipping.domain.model.ShippingAgent;
import com.devsquad10.shipping.domain.repository.ShippingAgentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingAgentAllocation implements ShippingAgentAllocationMethod {

	private final ShippingAgentRepository shippingAgentRepository;

	@Override
	@Transactional
	public MinimumCountAllocationResult allocateCompanyAgent(UUID destinationHubId, ShippingStatus shippingStatus) {
		// 목적지 허브 도착전까지 담당자 할당 불가
		if (shippingStatus != ShippingStatus.HUB_ARV) {
			throw new ShippingStatusIsNotAllocatedException(shippingStatus + " 상태로 배송담당자 배정 불가합니다.");
		}
		return allocateAgent(destinationHubId, ShippingAgentType.COM_DVL);
	}

	@Override
	@Transactional
	public MinimumCountAllocationResult allocateHubAgent(UUID destinationHubId) {
		return allocateAgent(destinationHubId, ShippingAgentType.HUB_DVL);
	}

	// 공통 배정 로직
	private MinimumCountAllocationResult allocateAgent(UUID destinationHubId,ShippingAgentType agentType) {
		// 담당자 유형별 필터링
		List<ShippingAgent> possibleShippingAgents = getPossibleShippingAgents(destinationHubId, agentType);
		log.info("possibleShippingAgents.size() = {}", possibleShippingAgents.size());

		// 모든 담당자가 배송 불가능한 경우
		if (possibleShippingAgents.isEmpty()) {
			if(agentType == ShippingAgentType.COM_DVL) {
				// TODO: 도착허브 ID의 담당자에게 슬랙 메시지 전송
				throw new SlackMessageSendToDesHubManagerIdException(destinationHubId + " 허브의 가능한 " + agentType + " 담당자가 존재하지 않습니다.");
			} else {
				// TODO: 마스터에게 슬랙 메시지 전송
				throw new SlackMessageSendToDesHubManagerIdException("가능한 " + agentType + " 담당자가 존재하지 않습니다.");
			}
		}

		// 최소 배정 건수 담당자 선택
		ShippingAgent selectedAgent = possibleShippingAgents.stream()
			.min(Comparator.comparingInt(ShippingAgent::getAssignmentCount))
			.orElse(null);

		// 배정 횟수 증가 및 저장, 배송 진행 여부 수정 및 저장
		try {
			// 선택된 담당자 배타적 락 획득
			ShippingAgent lockedAgent = shippingAgentRepository.findByIdWithPessimisticLock(selectedAgent.getId());
			lockedAgent.increaseAssignmentCount();
			lockedAgent.updateIsTransit();
			shippingAgentRepository.save(lockedAgent);

			return MinimumCountAllocationResult.builder()
				.shippingManagerId(lockedAgent.getShippingManagerId())
				.shippingManagerSlackId(lockedAgent.getShippingManagerSlackId())
				.assignmentCount(lockedAgent.getAssignmentCount())
				.build();

		} catch (DataAccessException e) {
			throw new ShippingAssignmentCountException("배정 횟수 변경에 문제가 발생하였습니다.", e);
		}
	}

	private List<ShippingAgent> getPossibleShippingAgents(UUID destinationHubId, ShippingAgentType agentType) {
		List<ShippingAgent> shippingAgentList = shippingAgentRepository.findAllByDeletedAtIsNull();
		if (shippingAgentList == null) {
			throw new IllegalArgumentException("배정가능한 배송담당자가 존재하지 않습니다.");
		}

		if(agentType == ShippingAgentType.COM_DVL) {
			return shippingAgentList.stream()
				.filter(agent -> agent.getType() == agentType
					&& !agent.getIsTransit()
					&& agent.getHubId().equals(destinationHubId))
				.toList();
		} else {
			return shippingAgentList.stream()
				.filter(agent -> agent.getType() == agentType
					&& !agent.getIsTransit())
				.toList();
		}
	}
}
