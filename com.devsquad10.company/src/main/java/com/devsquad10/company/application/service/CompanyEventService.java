package com.devsquad10.company.application.service;

import java.text.SimpleDateFormat;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsquad10.company.application.dto.SoldOutMessageRequest;
import com.devsquad10.company.application.dto.message.StockSoldOutMessage;
import com.devsquad10.company.application.exception.CompanyNotFoundException;
import com.devsquad10.company.domain.model.Company;
import com.devsquad10.company.domain.repository.CompanyRepository;
import com.devsquad10.company.infrastructure.client.MessageClient;
import com.devsquad10.company.infrastructure.client.UserClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyEventService {

	private final CompanyRepository companyRepository;
	private final UserClient userClient;
	private final MessageClient messageClient;

	/**
	 * 재고 소진 메시지를 담당자에게 전송하는 메서드
	 *
	 * 해당 상품이 소진된 경우, 공급업체 담당자에게 Slack 메시지를 전송한다.
	 * 1. 공급업체 ID를 이용해 해당 업체 정보를 조회한다.
	 * 2. 업체에 해당하는 담당자의 Slack ID를 가져온다.
	 * 3. 소진 일자와 상품 이름을 포함하여 메시지를 생성한다.
	 * 4. 해당 메시지를 Slack을 통해 담당자에게 전송한다.
	 *
	 * @param stockSoldOutMessage 재고 소진 메시지 객체 (공급업체 ID, 상품명, 소진 일자 포함)
	 * @throws CompanyNotFoundException 공급업체 ID에 해당하는 회사를 찾을 수 없을 때 발생
	 * @throws IllegalArgumentException 공급업체 담당자의 Slack ID가 존재하지 않을 경우 발생
	 */
	public void stockSoldMessageSend(StockSoldOutMessage stockSoldOutMessage) {
		Company company = companyRepository.findByIdAndDeletedAtIsNull(stockSoldOutMessage.getSupplierId())
			.orElseThrow(
				() -> new CompanyNotFoundException("Company Not Found By Id : " + stockSoldOutMessage.getSupplierId()));

		String venderSlackId = userClient.getUserSlackId(company.getVenderId());

		if (venderSlackId == null)
			throw new IllegalArgumentException("슬랙 id가 존재하지않습니다.");

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

		String soldOutAt = formatter.format(stockSoldOutMessage.getSoldOutAt());

		SoldOutMessageRequest soldOutMessageRequest = SoldOutMessageRequest.builder()
			.venderSlackId(venderSlackId)
			.productName(stockSoldOutMessage.getProductName())
			.soldOutAt(soldOutAt)
			.build();

		messageClient.sendSoldOutMessage(soldOutMessageRequest);
	}
}
