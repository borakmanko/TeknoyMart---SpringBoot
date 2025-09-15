package com.group3sd3.E_commerce.service;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Service
public class CommnServiceImpl implements CommonService {

	@Override
	public void removeSessionMessage() {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (attributes != null) {
			HttpServletRequest request = attributes.getRequest();
			HttpSession session = request.getSession();
			if (session != null) {
				session.removeAttribute("succMsg");
				session.removeAttribute("errorMsg");
			}
		}
	}
}