package com.group.xlibris.e2e.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;

public class BooksPage {

    private final Page page;

    public BooksPage(Page page) {
        this.page = page;
    }

    public void open() {
        page.navigate("http://localhost:8080/books");
    }

    public boolean isOpened() {
        return page.url().contains("/books") && page.locator(".book-grid").isVisible();
    }

    public boolean isAddButtonVisible() {
        return page.locator(".add-button").isVisible();
    }

    public void clickAddBook() {
        page.locator(".add-button").click();
    }

    public boolean hasBook(String title) {
        return page.locator(".book-card .book-title")
                       .getByText(title, new Locator.GetByTextOptions().setExact(true)).count() > 0;
    }

    public boolean hasSuccessMessage() {
        return page.locator(".success-message").isVisible();
    }
}