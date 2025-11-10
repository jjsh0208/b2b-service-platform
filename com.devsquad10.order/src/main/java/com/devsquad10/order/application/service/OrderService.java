package com.devsquad10.order.application.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsquad10.order.application.dto.OrderFeignClientDto;
import com.devsquad10.order.application.dto.OrderReqDto;
import com.devsquad10.order.application.dto.OrderResDto;
import com.devsquad10.order.application.dto.OrderUpdateReqDto;
import com.devsquad10.order.application.dto.PageOrderResponseDto;
import com.devsquad10.order.application.dto.message.ShippingUpdateRequest;
import com.devsquad10.order.application.dto.message.StockDecrementMessage;
import com.devsquad10.order.application.dto.message.StockReversalMessage;
import com.devsquad10.order.application.exception.OrderNotFoundException;
import com.devsquad10.order.application.messaging.OrderMessageService;
import com.devsquad10.order.domain.enums.OrderStatus;
import com.devsquad10.order.domain.model.Order;
import com.devsquad10.order.domain.repository.OrderQuerydslRepository;
import com.devsquad10.order.domain.repository.OrderRepository;
import com.devsquad10.order.infrastructure.client.CompanyClient;
import com.devsquad10.order.infrastructure.client.ShippingClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

	private final OrderRepository orderRepository;
	private final OrderQuerydslRepository orderQuerydslRepository;
	private final OrderMessageService orderMessageService;
	private final CompanyClient companyClient;
	private final ShippingClient shippingClient;

	/**
	 * 주문 접수 기능
	 *
	 * 주어진 주문 접수 요청 DTO( OrderReqDto )를 기반으로 새 주문를 접수한다.
	 * 1. 주문을 생성해 DB에 저장한다.
	 * 2. RabbitMQ를 통해 Product에 주문의 상품 ID 에 해당하는 재품의 재고 감소를 요청한다.
	 * 3. 저장된 주문를 DTO( OrderResDto ) 형식으로 변환하여 반환한다.
	 *
	 *  주문를 등록할 때 생성자는는 사용자가 로그인 후 전달한 유저 ID를 사용하여 저장한다.
	 *
	 * @param orderReqDto
	 * @param userId
	 * @return
	 */
	@CachePut(cacheNames = "orderCache", key = "#result.id")
	public OrderResDto createOrder(OrderReqDto orderReqDto, UUID userId) {
		Order order = Order.builder()
			.recipientsId(orderReqDto.getRecipientsId())
			.productId(orderReqDto.getProductId())
			.quantity(orderReqDto.getQuantity())
			.requestDetails(orderReqDto.getRequestDetails())
			.deadLine(orderReqDto.getDeadLine())
			.status(OrderStatus.ORDER_RECEIVED)
			.createdBy(userId)
			.build();

		// DB에 저장
		orderRepository.save(order);

		log.info("재고 감소 이벤트 요청: 주문 ID = {}, 제품 ID = {}, 수량 = {}",
			order.getId(), order.getProductId(), order.getQuantity());
		orderMessageService.sendStockDecrementMessage(order.toStockDecrementMessage());

		return order.toResponseDto();
	}

	/**
	 * 주문 조회 기능
	 *
	 * 특정 ID를 기반으로 주문을 조회한다. ( 캐싱 적용 )
	 * 업체 조회 시 캐시에서 먼저 데이터를 확인하고, 캐시가 없으면 DB에서 조회하여 반환한다.
	 *
	 * 이 과정에서, `@Cacheable`을 사용하여 주문 정보를 캐시하며, 해당 ID로 조회된 주문이 없다면 `OrderNotFoundException`을 발생시킨다.
	 * 캐시가 존재할 경우, 캐시된 업체 데이터를 빠르게 반환한다.
	 *
	 * @param id 조회할 주문의 ID
	 * @return 조회된 주문 정보를 담은 OrderResDto 객체
	 * @throws OrderNotFoundException 해당 ID의 주문이 존재하지 않을 경우 예외 발생
	 */
	@CachePut(cacheNames = "orderCache", key = "#id")
	@Transactional(readOnly = true)
	public OrderResDto getOrderById(UUID id) {
		return orderRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new OrderNotFoundException("Order Not Found By Id : " + id))
			.toResponseDto();
	}

	/**
	 * 주문 검색 기능
	 *
	 * 주어진 조건을 기반으로 주문 목록을 검색한다. ( 캐싱 적용 )
	 * 검색 조건에 따라 주문를 조회하고, 결과를 페이징 처리하여 반환한다.
	 *
	 * 이 과정에서 `@Cacheable`을 사용하여 검색 조건에 맞는 주문 목록을 캐시한다. 캐시가 존재하면 빠르게 반환하며,
	 * 캐시가 없을 경우 DB에서 검색하여 결과를 반환한다.
	 *
	 * @param q 검색어
	 * @param category 카테고리 ( 공급업체 ID , 수령업체 ID, 상품 ID, 배송 ID )
	 * @param page 페이지 번호
	 * @param size 페이지 크기
	 * @param sort 정렬 기준
	 * @param order 정렬 순서 (ASC/DESC)
	 * @return 검색 결과를 포함한 PageOrderResponseDto 객체
	 */
	@Cacheable(cacheNames = "orderSearchCache", key = "#q + '-' + #category + '-' + #page + '-' + #size")
	@Transactional(readOnly = true)
	public PageOrderResponseDto searchOrders(String q, String category, int page, int size, String sort, String order) {

		Page<Order> orderPages = orderQuerydslRepository.findAll(q, category, page, size, sort, order);

		Page<OrderResDto> orderResDtoPages = orderPages.map(Order::toResponseDto);

		return PageOrderResponseDto.toResponse(orderResDtoPages);

	}

	/**
	 * 주문 정보 업데이트 기능
	 *
	 * 주어진 주문 업데이트 요청 DTO(OrderUpdateReqDto)를 기반으로 주문 정보를 업데이트한다.
	 * 1. 기존 주문 정보 조회: 주어진 주문 ID로 주문을 조회하고, 주문이 존재하지 않으면 예외를 발생시킨다.
	 * 2. 배송지 변경 여부 확인: 요청된 배송지 정보가 기존 배송지와 다르면, 새로운 배송지 주소를 확인한다.
	 * 3. 수량 변경 여부 확인: 주문의 수량이 변경되었을 경우, 기존 재고와 비교하여 재고를 감소시키거나 회복시킨다.
	 * 4. 기타 정보 업데이트: 요청 사항(requestDetails)과 납기 기한(deadLine)을 업데이트한다.
	 * 5. 배송 정보 업데이트: 변경된 배송지 정보를 바탕으로 배송 정보를 업데이트하는 메시지를 전송한다.
	 * 6. 주문 정보 저장: 변경된 주문 정보를 데이터베이스에 저장한다.
	 *
	 * 이 과정에서 `@CachePut`을 사용하여 주문 캐시를 갱신하며,
	 * `orderSearchCache` 캐시를 비우기 위해 `@Caching`을 사용한다.
	 *
	 * @param id 주문 정보 업데이트 대상 주문의 ID
	 * @param orderUpdateReqDto 주문 정보를 업데이트할 요청 DTO
	 * @param userId 주문을 수정한 사용자의 ID
	 * @return 업데이트된 주문 정보를 담은 OrderResDto 객체
	 * @throws OrderNotFoundException 주문이 존재하지 않을 경우 예외 발생
	 * @throws IllegalArgumentException 유효하지 않은 배송지 정보가 제공될 경우 예외 발생
	 */
	@CachePut(cacheNames = "orderCache", key = "#id")
	@Caching(evict = {
		@CacheEvict(cacheNames = "orderSearchCache", allEntries = true)
	})
	public OrderResDto updateOrder(UUID id, OrderUpdateReqDto orderUpdateReqDto, UUID userId) {

		Order order = orderRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new OrderNotFoundException("Order Not Found By Id : " + id));

		String newRecipientsAddress = companyClient.findRecipientAddressByCompanyId(order.getRecipientsId());

		//1. 배송지 변경 여부 확인
		if (!order.getRecipientsId().equals(orderUpdateReqDto.getRecipientsId())) {
			newRecipientsAddress =
				companyClient.findRecipientAddressByCompanyId(orderUpdateReqDto.getRecipientsId());
			if (newRecipientsAddress == null) {
				throw new IllegalArgumentException(
					"Invalid recipient address for recipientsId : " + orderUpdateReqDto.getRecipientsId());
			}

			log.info("배송지 변경 감지: 기존 recipientsId={}, 새로운 recipientsId={}",
				order.getRecipientsId(), orderUpdateReqDto.getRecipientsId());

			order = order.toBuilder()
				.recipientsId(orderUpdateReqDto.getRecipientsId())
				.build();
		}

		//2. 수량도 변경되면 원래 재고 와 비교하여 감소되면 감소 줄어들면 재고 회복 메시지 전달
		int originalQuantity = order.getQuantity();
		int updatedQuantity = orderUpdateReqDto.getQuantity();

		if (originalQuantity != updatedQuantity) {
			// 수량이 증가하면 재고 감소 요청
			if (updatedQuantity > originalQuantity) {
				int quantityToDecrease = updatedQuantity - originalQuantity;
				StockDecrementMessage stockDecrementMessage = StockDecrementMessage.builder()
					.orderId(order.getId())
					.productId(order.getProductId())
					.quantity(quantityToDecrease) // 감소할 수량
					.build();
				log.info("재고 감소 이벤트 요청: 주문 ID={}, 제품 ID={}, 감소 수량={}",
					order.getId(), order.getProductId(), quantityToDecrease);

				orderMessageService.sendStockDecrementMessage(stockDecrementMessage);// 재고 감소 메시지 전송
			}
			// 수량이 감소하면 재고 회복 요청
			else if (updatedQuantity < originalQuantity) {
				int quantityToRecover = originalQuantity - updatedQuantity;
				StockReversalMessage stockReversalMessage = new StockReversalMessage(order.getProductId(),
					quantityToRecover);

				log.info("재고 회복 이벤트 요청: 주문 ID={}, 제품 ID={}, 회복 수량={}",
					order.getId(), order.getProductId(), quantityToRecover);

				orderMessageService.sendStockReversalMessage(stockReversalMessage);
			}

			// 주문 수량 업데이트
			order = order.toBuilder()
				.quantity(updatedQuantity)
				.build();
		}

		// 4.  기타 정보 업데이트
		order = order.toBuilder()
			.requestDetails(orderUpdateReqDto.getRequestDetails())  // 요청 사항 업데이트
			.deadLine(orderUpdateReqDto.getDeadLine())              // 납품 기한일자 업데이트
			.updatedBy(userId)
			.build();

		ShippingUpdateRequest shippingUpdateRequest = ShippingUpdateRequest.builder()
			.orderId(order.getId())
			.recipientsId(order.getRecipientsId())
			.address(newRecipientsAddress)
			.requestDetails(orderUpdateReqDto.getRequestDetails())
			.deadLine(orderUpdateReqDto.getDeadLine())
			.build();

		log.info("배송 업데이트 이벤트 요청: 주문 ID={}, recipientsId={}, 새로운 주소={}",
			order.getId(), order.getRecipientsId(), newRecipientsAddress);

		orderMessageService.sendShippingUpdateMessage(shippingUpdateRequest);

		// 5. 업데이트된 주문 정보 저장
		orderRepository.save(order);

		log.info("주문 정보 업데이트 완료: 주문 ID={}", order.getId());

		// 6. 업데이트된 주문 정보 반환
		return order.toResponseDto();
	}

	/**
	 * 주문 삭제 기능
	 *
	 * 주어진 주문 ID를 기반으로 주문을 삭제한다.
	 * 1. 주문 존재 여부 확인: 주어진 주문 ID로 주문을 조회하고, 주문이 존재하지 않으면 예외를 발생시킨다.
	 * 2. 재고 회복 요청: 삭제된 주문의 제품과 수량을 바탕으로 재고 회복 메시지를 전송한다.
	 * 3. 배송 정보 삭제: 주문에 대한 배송 정보를 삭제한다.
	 * 4. 주문 삭제: 주문을 삭제 상태로 업데이트하고, 삭제자 정보와 삭제 시간을 기록한다.
	 * 5. 캐시 갱신: 주문 삭제 후, `orderCache` 및 `orderSearchCache` 캐시를 갱신한다.
	 *
	 * @param id 삭제할 주문의 ID
	 * @param userId 주문을 삭제한 사용자의 ID
	 * @throws OrderNotFoundException 주문이 존재하지 않을 경우 예외 발생
	 */
	@Caching(evict = {
		@CacheEvict(cacheNames = "orderCache", key = "#id"),
		@CacheEvict(cacheNames = "orderSearchCache", key = "#id")
	})
	public void deleteOrder(UUID id, UUID userId) {
		Order order = orderRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new OrderNotFoundException("Order Not Found By Id : " + id));

		log.info("주문 삭제 요청: 주문 ID={}, 사용자 ID={}", id, userId);

		StockReversalMessage stockReversalMessage = new StockReversalMessage(order.getProductId(),
			order.getQuantity());

		log.info("재고 회복 이벤트 요청: 주문 ID={}, 제품 ID={}, 회복 수량={}",
			order.getId(), order.getProductId(), order.getQuantity());
		orderMessageService.sendStockReversalMessage(stockReversalMessage);

		log.info("배송 정보 삭제 요청: 주문 ID={}", id);
		shippingClient.deleteShippingForOrder(id);

		orderRepository.save(order.toBuilder()
			.deletedAt(LocalDateTime.now())
			.deletedBy(userId)
			.build());

		log.info("주문 삭제 완료: 주문 ID={}, 삭제한 사용자 ID={}", id, userId);
	}

	/**
	 * 주문 상태를 'SHIPPED'로 업데이트
	 *
	 * 주어진 배송 ID를 기반으로 해당 주문의 상태를 'SHIPPED'로 업데이트한다.
	 * 1. 배송 ID로 주문을 조회하고, 주문이 존재하지 않으면 예외를 발생시킨다.
	 * 2. 주문 상태를 'SHIPPED'로 변경하고, 변경된 주문을 저장한다.
	 *
	 * @param shippingId 배송 ID
	 * @throws OrderNotFoundException 주문이 존재하지 않을 경우 예외 발생
	 */
	public void updateOrderStatusToShipped(UUID shippingId) {
		Order order = orderRepository.findByShippingIdAndDeletedAtIsNull(shippingId)
			.orElseThrow(() -> new OrderNotFoundException(
				"No order found with shippingId: " + shippingId + " and deletedAt is null"));

		orderRepository.save(order.toBuilder()
			.status(OrderStatus.SHIPPED)
			.build());
	}

	/**
	 * 주문의 제품 세부 정보 조회
	 *
	 * 주어진 주문 ID를 기반으로 해당 주문의 제품 이름과 수량 정보를 반환한다.
	 * 1. 주문 ID로 주문을 조회하고, 주문이 존재하지 않으면 예외를 발생시킨다.
	 * 2. 주문의 제품 이름과 수량을 바탕으로 `OrderFeignClientDto`를 생성하여 반환한다.
	 *
	 * @param id 조회할 주문의 ID
	 * @return 주문의 제품 이름과 수량을 담은 `OrderFeignClientDto` 객체
	 * @throws OrderNotFoundException 주문이 존재하지 않을 경우 예외 발생
	 */
	public OrderFeignClientDto getOrderProductDetails(UUID id) {
		Order order = orderRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new OrderNotFoundException("Order Not Found By Id : " + id));

		return OrderFeignClientDto.builder()
			.productName(order.getProductName() != null ? order.getProductName() : null)
			.quantity(order.getQuantity() != null ? order.getQuantity() : null)
			.build();
	}
}
