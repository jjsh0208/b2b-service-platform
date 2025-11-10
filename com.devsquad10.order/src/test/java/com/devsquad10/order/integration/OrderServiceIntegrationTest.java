package com.devsquad10.order.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.devsquad10.order.application.dto.OrderReqDto;
import com.devsquad10.order.application.dto.OrderResDto;
import com.devsquad10.order.application.dto.OrderUpdateReqDto;
import com.devsquad10.order.application.dto.PageOrderResponseDto;
import com.devsquad10.order.application.exception.OrderNotFoundException;
import com.devsquad10.order.application.service.OrderService;
import com.devsquad10.order.domain.enums.OrderStatus;
import com.devsquad10.order.domain.model.Order;
import com.devsquad10.order.domain.repository.OrderRepository;

import jakarta.transaction.Transactional;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
public class OrderServiceIntegrationTest {

	@Autowired
	private OrderService orderService;

	@Autowired
	private OrderRepository orderRepository;

	private Order testOrder;

	private UUID testOrderId;
	private UUID testSupplierId;
	private UUID testRecipientsId;
	private UUID testProductId;
	private UUID testShippingId;

	@BeforeEach
	void setUp() {

		testRecipientsId = UUID.fromString("2f46cca6-4066-49ba-8ef1-c143b58cb8cb");
		testProductId = UUID.fromString("032254d1-960e-4115-8b1d-73e9b802c20a");
		testShippingId = UUID.randomUUID();

		testOrder = Order.builder()
			.recipientsId(testRecipientsId)
			.productId(testProductId)
			.quantity(10)
			.requestDetails("Order Details")
			.shippingId(testShippingId)
			.deadLine(new Date())
			.status(OrderStatus.ORDER_RECEIVED)
			.createdBy(UUID.randomUUID())
			.build();

		orderRepository.save(testOrder);

		testOrderId = testOrder.getId();
	}

	@Test
	@DisplayName("Order 접수 - Success")
	void testCreateOrderSuccess() {
		// Given
		UUID userId = UUID.randomUUID();
		OrderReqDto orderReqDto = new OrderReqDto(testRecipientsId, testProductId, 5, "부재 시 문앞", new Date());

		OrderResDto result = orderService.createOrder(orderReqDto, userId);

		assertNotNull(result);
		assertEquals(5, result.getQuantity());
		assertEquals("부재 시 문앞", result.getRequestDetails());
	}

	@Test
	@DisplayName("Order 단일 조회 - Success")
	void testGetOrderByIdSuccess() {
		//OrderNotFoundException
		// When
		OrderResDto result = orderService.getOrderById(testOrderId);

		// Then
		assertNotNull(result);
		assertEquals(testOrder.getProductName(), result.getProductName());
		assertEquals(testOrder.getQuantity(), result.getQuantity());

	}

	@Test
	@DisplayName("Order 단일 조회 - Fail - OrderNotFound")
	void testGetOrderByIdFail() {
		//OrderNotFoundException
		// Given
		UUID failOrderId = UUID.randomUUID();

		// When & Then
		OrderNotFoundException exception = assertThrows(OrderNotFoundException.class, () -> {
			orderService.getOrderById(failOrderId);
		});

		// Then
		assertEquals("Order Not Found By Id : " + failOrderId, exception.getMessage());

	}

	@Test
	@DisplayName("Order 검색 - Success")
	void testSearchOrdersSuccess() {
		// Given
		String q = null;
		String category = null;
		int page = 0;
		int size = 10;
		String sort = "createdAt";
		String order = "desc";

		for (int i = 0; i < 10; i++) {
			Order testProduct = Order.builder()
				.recipientsId(testRecipientsId)
				.productId(testProductId)
				.quantity(10)
				.requestDetails("Order Details" + i)
				.deadLine(new Date())
				.status(OrderStatus.ORDER_RECEIVED)
				.createdBy(UUID.randomUUID())
				.build();

			orderRepository.save(testProduct);
		}

		// When
		PageOrderResponseDto result1 = orderService.searchOrders(q, category, page, size, sort, order);
		PageOrderResponseDto result2 = orderService.searchOrders(q, category, page, size, sort, order);

		// Then
		// 첫 번째 결과는 null이 아니어야 한다.
		assertNotNull(result1);

		// 두 번째 호출에서는 캐시된 결과이므로, 첫 번째 결과와 동일한지 비교
		assertNotNull(result2);
		// 결과의 내용이 동일한지 순서대로 비교
		assertEquals(result1.getContent().size(), result2.getContent().size());
		for (int i = 0; i < result1.getContent().size(); i++) {
			OrderResDto company1 = result1.getContent().get(i);
			OrderResDto company2 = result2.getContent().get(i);
			assertEquals(company1.getId(), company2.getId());
			assertEquals(company1.getRequestDetails(), company2.getRequestDetails());
			assertEquals(company1.getQuantity(), company2.getQuantity());
		}

		// 페이징 처리 테스트
		assertEquals(10, result1.getContent().size());
		assertEquals("Order Details9", result1.getContent().get(0).getRequestDetails());
	}

	@Test
	@DisplayName("Order 업데이트 - Success")
	void testUpdateOrderSuccess() {
		// Given
		UUID userId = UUID.randomUUID();

		OrderUpdateReqDto orderUpdateReqDto = new OrderUpdateReqDto(testRecipientsId, 100, "update Test",
			new Date());

		// When
		OrderResDto result = orderService.updateOrder(testOrderId, orderUpdateReqDto, userId);

		// Then
		assertNotNull(result);
		assertEquals("update Test", result.getRequestDetails());
		assertEquals(100, result.getQuantity());

	}

	@Test
	@DisplayName("Order 업데이트 - Fail - OrderNotFound")
	void testUpdateOrderFailOrderNotFound() {
		// Given
		UUID userId = UUID.randomUUID();
		UUID failRecipientsId = UUID.randomUUID();

		OrderUpdateReqDto orderUpdateReqDto = new OrderUpdateReqDto(testRecipientsId, 100, "update Test",
			new Date());

		// When & Then
		OrderNotFoundException exception = assertThrows(OrderNotFoundException.class, () -> {
			orderService.updateOrder(failRecipientsId, orderUpdateReqDto, userId);
		});

		assertEquals("Order Not Found By Id : " + failRecipientsId, exception.getMessage());

	}

	@Test
	@DisplayName("Order 업데이트 - Fail - IllegalArgumentException")
	void testUpdateOrderFailIllegalArgumentException() {
		// Given
		UUID userId = UUID.randomUUID();
		UUID failRecipientsId = UUID.randomUUID();

		OrderUpdateReqDto orderUpdateReqDto = new OrderUpdateReqDto(failRecipientsId, 100, "update Test",
			new Date());

		// When & Then
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			orderService.updateOrder(testOrderId, orderUpdateReqDto, userId);
		});

		assertEquals("Invalid recipient address for recipientsId : " + failRecipientsId, exception.getMessage());

	}

	@Test
	@DisplayName("Order 논리적 삭제 - Success")
	void testDeleteOrderSuccess() {
		// Given
		UUID userId = UUID.randomUUID();

		// When
		orderService.deleteOrder(testOrderId, userId);

		// Then
		Optional<Order> order = orderRepository.findByIdAndDeletedAtIsNull(testOrderId);
		assertTrue(order.isEmpty());

	}

	@Test
	@DisplayName("Order 논리적 삭제 - Fail - OrderNotFound")
	void testDeleteOrderFailOrderNotFound() {
		// Given
		UUID userId = UUID.randomUUID();
		UUID failOrderId = UUID.randomUUID();

		// When & Then
		OrderNotFoundException exception = assertThrows(OrderNotFoundException.class, () -> {
			orderService.deleteOrder(failOrderId, userId);
		});

		assertEquals("Order Not Found By Id : " + failOrderId, exception.getMessage());

	}

	@Test
	@DisplayName("Order 논리적 삭제 - Success")
	void testUpdateOrderStatusToShippedSuccess() {

		// When
		orderService.updateOrderStatusToShipped(testShippingId);

		// Then
		Optional<Order> order = orderRepository.findByIdAndDeletedAtIsNull(testOrderId);

		assertNotNull(order);
		assertEquals(OrderStatus.SHIPPED, order.get().getStatus());

	}

	@Test
	@DisplayName("Order 논리적 삭제 - Fail - OrderNotFound")
	void testUpdateOrderStatusToShippedFailOrderNotFound() {
		// Given
		UUID failShippingId = UUID.randomUUID();

		// When & Then
		OrderNotFoundException exception = assertThrows(OrderNotFoundException.class, () -> {
			orderService.updateOrderStatusToShipped(failShippingId);
		});

		assertEquals("No order found with shippingId: " + failShippingId + " and deletedAt is null",
			exception.getMessage());

	}
}
