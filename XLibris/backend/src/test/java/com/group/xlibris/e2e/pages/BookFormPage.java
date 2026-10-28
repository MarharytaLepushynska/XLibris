package com.group.xlibris.e2e.pages;

import com.microsoft.playwright.Page;

public class BookFormPage {

    private final Page page;

    public BookFormPage(Page page) {
        this.page = page;
    }

    public void fillForm(String title, String description) {
        page.locator("#title").fill(title);
        page.locator("#description").fill(description);
        page.locator("#genreId")
                .selectOption(new String[]{page.locator("#genreId option").nth(1)
                        .getAttribute("value")});
    }

    public void submit() {
        page.locator(".save-button").click();
    }
}