package com.management.school.core;

import com.management.school.model.Admin;
import org.springframework.stereotype.Component;

@Component
public class AdminSession {

	private Admin currentAdmin;

	public Admin getCurrentAdmin() {
		return currentAdmin;
	}

	public void setCurrentAdmin(Admin currentAdmin) {
		this.currentAdmin = currentAdmin;
	}

	public void logout() {
		this.currentAdmin = null;
	}

	public boolean isLoggedIn() {
		return currentAdmin != null;
	}
}