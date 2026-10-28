package com.group.xlibris.e2e.pages;

import com.microsoft.playwright.Page;

public class LoginPage {

    private final Page page;

    public LoginPage(Page page) {
        this.page = page;
    }

    public void open() {
        page.navigate("http://localhost:8080/login");
    }

    public void login(String email, String password) {
        page.locator("#username").fill(email);
        page.locator("#password").fill(password);
        page.locator("button[type='submit']").click();
    }
}