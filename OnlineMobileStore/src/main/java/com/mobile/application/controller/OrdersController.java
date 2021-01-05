package com.mobile.application.controller;

import java.util.List;
import java.util.Objects;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mobile.application.dto.OrdersDto;
import com.mobile.application.exception.OrderNotfoundException;
import com.mobile.application.exception.UserNotfoundException;
import com.mobile.application.model.Cart;
import com.mobile.application.model.Item;
import com.mobile.application.model.Orders;
import com.mobile.application.model.User;
import com.mobile.application.repository.CartRepository;
import com.mobile.application.repository.ItemRepository;
import com.mobile.application.repository.OrdersRepository;
import com.mobile.application.repository.UserRepository;

@Controller
@RequestMapping("/User")
@ResponseBody
public class OrdersController {

	@Autowired
	private OrdersRepository orderRepository;
	@Autowired
	private CartRepository cartRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ItemRepository itemRepository;
	@Autowired
	private ModelMapper modelMapper;
	@Value("${item_size}")
	private int size;

	/**
	 * Saves Users Orders from cart
	 * 
	 * @param models
	 * @param users
	 * @return
	 */

	// 1st method using Request Body
	@PostMapping(value = "/saveOrders/{id}")
	public Page<OrdersDto> saveOrders(@PathVariable Integer id,
			@RequestParam(value = "page", required = false) Integer pageNumber,
			@RequestParam(value = "size", required = false) Integer size,
			@RequestParam(value = "sortBy", required = false) String sort) {
		if (Objects.isNull(pageNumber))
			pageNumber = 0;
		if (Objects.isNull(size))
			size = 25;
		if (Objects.isNull(sort))
			sort = "cartid";

		Page<Orders> orders = null;
		User userList = userRepository.findById(id);
		String email = null;
		email = userList.getEmail();
		if (Objects.isNull(email)) {
			throw new UserNotfoundException("email id: " + email);
		}
		Pageable pageable = PageRequest.of(pageNumber, size, Sort.by(sort).descending());
		Page<Cart> cartList = cartRepository.findAllById(id, pageable);
		List<Cart> cart = cartList.getContent();
		for (var iterate : cart) {
			Orders newOrder = new Orders(id, email, iterate.getModel(), iterate.getItemname(), iterate.getQuantity(),
					iterate.getTotal(), "IN");
			orderRepository.save(newOrder);
			cartRepository.deleteById(iterate.getCartid());
			Item item = itemRepository.findById(iterate.getModel()).get();
			item.setQuantity_available(item.getQuantity_available() - iterate.getQuantity());
			itemRepository.save(item);
		}
		Pageable ordersPageable = PageRequest.of(pageNumber, size, Sort.by("orderid").descending());
		orders = orderRepository.findAllOrdersById(id, ordersPageable);
		if (Objects.isNull(orders)) {
			throw new OrderNotfoundException("No Items in your Cart to order");
		}
		return orders.map(allOrders -> {
			return modelMapper.map(allOrders,OrdersDto.class);
		});
	}

	/**
	 * Removes Users Specified Orders
	 * 
	 * @param orderid
	 * @param user
	 * @return
	 */
	@PostMapping("/removeOrder/{id}/{orderid}")
	public Page<OrdersDto> remove(@PathVariable Integer id, @PathVariable("orderid") int orderid,
			@RequestParam(value = "page", required = false) Integer pageNumber,
			@RequestParam(value = "size", required = false) Integer size,
			@RequestParam(value = "sortBy", required = false) String sort) {
		if (Objects.isNull(pageNumber))
			pageNumber = 0;
		if (Objects.isNull(size))
			size = 25;
		if (Objects.isNull(sort))
			sort = "orderid";

		Page<Orders> orders = null;
		Pageable pageable = PageRequest.of(pageNumber, size, Sort.by(sort).descending());
		orderRepository.deleteById(orderid);
		orders = orderRepository.findAllOrdersById(id, pageable);
		if (Objects.isNull(orders)) {
			throw new OrderNotfoundException("No Items in your Cart to order");
		}
		return orders.map(allOrders -> {
			return modelMapper.map(allOrders,OrdersDto.class);
		});
	}

	/**
	 * All yet to Complete Orders Every Order
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/getAllOrder/{id}")
	public Page<OrdersDto> getEveryOrder(@PathVariable Integer id,
			@RequestParam(value = "page", required = false) Integer pageNumber,
			@RequestParam(value = "size", required = false) Integer size,
			@RequestParam(value = "sortBy", required = false) String sort) {
		if (Objects.isNull(pageNumber))
			pageNumber = 0;
		if (Objects.isNull(size))
			size = 25;
		if (Objects.isNull(sort))
			sort = "orderid";

		Page<Orders> newOrders = null;
		if (Objects.isNull(id)) {
			throw new UserNotfoundException("User id: " + id);
		}
		Pageable pageable = PageRequest.of(pageNumber, size, Sort.by(sort).descending());
		newOrders = orderRepository.findAllOrdersById(id, pageable);
		if (Objects.isNull(newOrders)) {
			throw new OrderNotfoundException("No Items in your Cart to order");
		}
		return newOrders.map(allOrders -> {
			return modelMapper.map(allOrders,OrdersDto.class);
		});
	}

}
