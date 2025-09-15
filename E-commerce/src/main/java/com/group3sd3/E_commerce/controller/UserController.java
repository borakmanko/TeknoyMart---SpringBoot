package com.group3sd3.E_commerce.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

import com.group3sd3.E_commerce.model.Cart;
import com.group3sd3.E_commerce.model.Category;
import com.group3sd3.E_commerce.model.OrderReq;
import com.group3sd3.E_commerce.model.Product;
import com.group3sd3.E_commerce.model.ProductOrder;
import com.group3sd3.E_commerce.model.User;
import com.group3sd3.E_commerce.model.Wishlist;
import com.group3sd3.E_commerce.model.StripeReq;
import com.group3sd3.E_commerce.service.CartService;
import com.group3sd3.E_commerce.service.CategoryService;
import com.group3sd3.E_commerce.service.OrderService;
import com.group3sd3.E_commerce.service.ProductService;
import com.group3sd3.E_commerce.service.UserService;
import com.group3sd3.E_commerce.service.WishlistService;
import com.group3sd3.E_commerce.util.OrderStatus;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private CartService cartService;

	@Autowired
	private ProductService productService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private WishlistService wishlistService;

	@GetMapping("/")
	public String home() {
		return "user/home";
	}

	@ModelAttribute
	public void getUserDetails(Principal p, Model m) {
		if (p != null) {
			String email = p.getName();
			User userDtls = userService.getUserByEmail(email);
			m.addAttribute("user", userDtls);
			Integer countCart = cartService.getCountCart(userDtls.getId());
			m.addAttribute("countCart", countCart);
			Integer filteredCart = cartService.getCountUnpurchasedCart(userDtls.getId());
			m.addAttribute("filteredCart", filteredCart);
		}

		List<Category> allActiveCategory = categoryService.getAllActiveCategory();
		m.addAttribute("categorys", allActiveCategory);
	}

	@GetMapping("/addCart")
	public String addToCart(@RequestParam String productItemId, @RequestParam Integer uid, HttpSession session) {
		Cart saveCart = cartService.saveCart(productItemId, uid);

		if (ObjectUtils.isEmpty(saveCart)) {
			session.setAttribute("errorMsg", "Something wrong on server");
		} else {
			session.setAttribute("succMsg", "Added to cart successfully");
		}
		return "redirect:/product/" + productItemId;
	}

	@GetMapping("/cart")
	public String loadCartPage(Principal p, Model m) {
		User user = getLoggedInUserDetails(p);
		List<Cart> carts = cartService.getCartsByUser(user.getId());
		m.addAttribute("carts", carts);
		if (carts.size() > 0) {
			Double totalPrice = carts.get(carts.size() - 1).getTotalOrderPrice();
			Double serviceFee = BigDecimal.valueOf(totalPrice * 0.01).setScale(2, RoundingMode.HALF_UP).doubleValue();
			Double totalOrderPrice = BigDecimal.valueOf(totalPrice + serviceFee).setScale(2, RoundingMode.HALF_UP)
					.doubleValue();
			m.addAttribute("totalPrice", totalPrice);
			m.addAttribute("serviceFee", serviceFee);
			m.addAttribute("totalOrderPrice", totalOrderPrice);
		}
		return "/user/cart";
	}

	@GetMapping("/cartQuantityUpdate")
	public String updateCartQuantity(@RequestParam String sy, @RequestParam Integer cid) {
		cartService.updateQuantity(sy, cid);
		return "redirect:/user/cart";
	}

	private User getLoggedInUserDetails(Principal p) {
		String email = p.getName();
		User userDtls = userService.getUserByEmail(email);
		return userDtls;
	}

	@Value("${stripe.api.publicKey}")
	private String publicKey;

	@GetMapping("/payment-card")
	public String loadPaymentPage(Model m, Principal p) {
		User user = getLoggedInUserDetails(p);
		List<Cart> carts = cartService.getCartsByUser(user.getId());

		double totalOrderPrice = 0.0;
		double serviceFee = 0.0;
		for (Cart cart : carts) {
			cart.setTotalPrice(cart.getProduct().getDiscountPrice() * cart.getQuantity());
			serviceFee = cart.getTotalPrice() * 0.01;
			totalOrderPrice += cart.getTotalPrice() + serviceFee;
		}

		StripeReq stripeReq = new StripeReq();
		stripeReq.setUserId(user.getId());
		stripeReq.setEmail(user.getEmail());
		stripeReq.setTotalOrderPrice(totalOrderPrice);

		m.addAttribute("stripeReq", stripeReq);
		m.addAttribute("publicKey", publicKey);
		return "/user/stripe/stripe_payment";
	}

	@PostMapping("/payment-card")
	public String processPayment(@Valid @ModelAttribute("stripeReq") StripeReq stripeReq, BindingResult result, Model m)
			throws Exception {
		if (result.hasErrors()) {
			return "/user/stripe/stripe_payment";
		}
		m.addAttribute("publicKey", publicKey);
		m.addAttribute("email", stripeReq.getEmail());
		m.addAttribute("totalOrderPrice", stripeReq.getTotalOrderPrice());
		return "/user/stripe/stripe_checkout";
	}

	@GetMapping("/order")
	public String orderPage(Principal p, Model m) {
		User user = getLoggedInUserDetails(p);
		List<Cart> carts = cartService.getCartsByUser(user.getId());
		m.addAttribute("carts", carts);
		if (carts.size() > 0) {
			Double orderPrice = carts.get(carts.size() - 1).getTotalOrderPrice();
			Double serviceFee = orderPrice * 0.01;
			Double totalOrderPrice = orderPrice + serviceFee;
			m.addAttribute("orderPrice", orderPrice);
			m.addAttribute("serviceFee", serviceFee);
			m.addAttribute("totalOrderPrice", totalOrderPrice);
		}
		return "/user/billing";
	}

	@PostMapping("/save-order")
	public String saveOrder(@ModelAttribute OrderReq request, Principal p) {
		User user = getLoggedInUserDetails(p);
		orderService.saveOrder(user.getId(), request);

		return "redirect:/user/success";
	}

	@GetMapping("/success")
	public String loadSuccess(Principal p) {
		User user = getLoggedInUserDetails(p);
		cartService.markItemsAsPurchased(user.getId());
		return "/user/success";
	}

	@GetMapping("/orders")
	public String myOrder(Model m, Principal p) {
		User loginUser = getLoggedInUserDetails(p);
		List<ProductOrder> orders = orderService.getOrdersByUser(loginUser.getId());
		m.addAttribute("orders", orders);
		return "/user/orders";
	}

	@GetMapping("/update-status")
	public String updateOrderStatus(@RequestParam Integer id, @RequestParam Integer st, HttpSession session) {

		OrderStatus[] values = OrderStatus.values();
		String status = null;

		for (OrderStatus orderSt : values) {
			if (orderSt.getId().equals(st)) {
				status = orderSt.getName();
			}
		}

		Boolean updateOrder = orderService.updateOrderStatus(id, status);

		if (updateOrder) {
			session.setAttribute("succMsg", "Status updated successfully");
		} else {
			session.setAttribute("errorMsg", "Something wrong on server");
		}
		return "redirect:/user/orders";
	}

	@PostMapping("/update-profile")
	public String updateProfile(@ModelAttribute User user, @RequestParam MultipartFile img, HttpSession session) {
		User updateUserProfile = userService.updateUserProfile(user, img);
		if (ObjectUtils.isEmpty(updateUserProfile)) {
			session.setAttribute("errorMsg", "Something wrong on server");
		} else {
			session.setAttribute("succMsg", "Profile updated successfully");
		}
		return "redirect:/user/";
	}

	@PostMapping("/change-password")
	public String changePassword(@RequestParam String newPassword, @RequestParam String currentPassword, Principal p,
			HttpSession session) {
		User loggedInUserDetails = getLoggedInUserDetails(p);

		boolean matches = passwordEncoder.matches(currentPassword, loggedInUserDetails.getPassword());

		if (matches) {
			String encodePassword = passwordEncoder.encode(newPassword);
			loggedInUserDetails.setPassword(encodePassword);
			User updateUser = userService.updateUser(loggedInUserDetails);
			if (ObjectUtils.isEmpty(updateUser)) {
				session.setAttribute("errorMsg", "Something wrong on server");
			} else {
				session.setAttribute("succMsg", "Password changed successfully");
			}
		} else {
			session.setAttribute("errorMsg", "Current password not matched");
		}

		return "redirect:/user/";
	}

	@GetMapping("/wishlist/add/{id}")
	public String addToWishlist(@PathVariable Integer id, Principal p, HttpSession session) {
		User user = getLoggedInUserDetails(p);
		Product product = productService.getProductById(id);

		try {
			wishlistService.saveWishlist(user, product);
			session.setAttribute("succMsg", "Product added to wishlist");
		} catch (Exception e) {
			session.setAttribute("errorMsg", "Something wrong on server");
		}

		return "redirect:/products";
	}

	@GetMapping("/wishlist/remove/{id}")
	public String removeFromWishlist(@PathVariable Integer id, Principal p, HttpSession session) {
		User user = getLoggedInUserDetails(p);
		Product product = productService.getProductById(id);

		if (user == null || product == null) {
			session.setAttribute("errorMsg", "User or Product not found.");
			return "redirect:/user/wishlist";
		}

		try {
			wishlistService.deleteWishlist(user, product);
			session.setAttribute("succMsg", "Product removed from wishlist");
		} catch (Exception e) {
			session.setAttribute("errorMsg", "Something wrong on server");
		}

		return "redirect:/user/wishlist";
	}

	@GetMapping("/wishlist")
	public String wishlistPage(Principal p, Model m) {
		User user = getLoggedInUserDetails(p);
		List<Wishlist> wishlists = wishlistService.getWishlistByUser(user);
		m.addAttribute("wishlists", wishlists);
		return "/user/wishlist";
	}

	@GetMapping("/addFromWishlist")
	public String addFromWishlist(Principal p, Model m, @RequestParam String productItemId, @RequestParam Integer uid,
			HttpSession session) {
		Cart saveCart = cartService.saveCart(productItemId, uid);

		if (ObjectUtils.isEmpty(saveCart)) {
			session.setAttribute("errorMsg", "Something wrong on server");
		} else {
			session.setAttribute("succMsg", "Added to cart successfully");
		}

		User user = getLoggedInUserDetails(p);
		Product product = productService.getProductbyItemId(productItemId);
		wishlistService.deleteWishlist(user, product);

		return "redirect:/user/cart";
	}

	@GetMapping("/wishlist/removeFromCart/{id}")
	public String removeFromCart(@PathVariable Integer id, Principal p, HttpSession session) {
		User user = getLoggedInUserDetails(p);
		Product product = productService.getProductById(id);

		if (user == null || product == null) {
			session.setAttribute("errorMsg", "User or Product not found.");
			return "redirect:/user/wishlist";
		}

		try {
			wishlistService.deleteWishlist(user, product);
			session.setAttribute("succMsg", "Product removed from wishlist");
		} catch (Exception e) {
			session.setAttribute("errorMsg", "Something wrong on server");
		}

		return "redirect:/products";
	}
}
