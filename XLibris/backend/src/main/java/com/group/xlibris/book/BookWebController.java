package com.group.xlibris.book;

import com.group.xlibris.author.AuthorService;
import com.group.xlibris.book.dto.BookResponse;
import com.group.xlibris.book.dto.EditBookForm;
import com.group.xlibris.genre.GenreService;
import com.group.xlibris.book.dto.BookRequest;
import com.group.xlibris.user.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class BookWebController {

    private final BookService bookService;
    private final AuthorService authorService;
    private final GenreService genreService;
    private final UserService userService;


    @GetMapping("/books")
    public String getAllBooks(
            Model model,
            Authentication authentication) {

        model.addAttribute("books", bookService.getAllBookCards());

        UUID currentUserId =
                userService.getUserByEmail(authentication.getName());

        Set<UUID> editableBookIds = bookService.getAllBooks()
                .stream()
                .filter(book -> currentUserId.equals(book.ownerId()))
                .map(BookResponse::id)
                .collect(Collectors.toSet());

        model.addAttribute("editableBookIds", editableBookIds);

        return "books/list";
    }



    @GetMapping("/books/{id}/edit")
    public String showEditForm(
            @PathVariable UUID id,
            Authentication authentication,
            Model model) {

        BookResponse book = bookService.getBookById(id);
        checkEditPermission(book, authentication);

        UUID currentUserId =
                userService.getUserByEmail(authentication.getName());

        if (!currentUserId.equals(book.ownerId())) {
            throw new AccessDeniedException(
                    "You cannot edit another user's book"
            );
        }

        EditBookForm form = new EditBookForm();

        form.setTitle(book.title());
        form.setDescription(book.description());
        form.setPhotoURL(book.photoURL());
        form.setAuthorId(book.authorId());
        form.setGenreId(book.genreId());

        model.addAttribute("form", form);
        model.addAttribute("bookId", id);
        model.addAttribute("authors", authorService.getAllAuthors());
        model.addAttribute("genres", genreService.getAllGenres());

        return "books/edit";
    }



    @PostMapping("/books/{id}/edit")
    public String updateBook(
            @PathVariable UUID id,
            @Valid @ModelAttribute("form") EditBookForm form,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        BookResponse book = bookService.getBookById(id);

        checkEditPermission(book, authentication);

        if (bindingResult.hasErrors()) {
            model.addAttribute("bookId", id);
            model.addAttribute("authors", authorService.getAllAuthors());
            model.addAttribute("genres", genreService.getAllGenres());

            return "books/edit";
        }

        BookRequest request = new BookRequest(
                id,
                form.getTitle(),
                form.getDescription(),
                form.getPhotoURL(),
                book.ownerId(),
                form.getAuthorId(),
                form.getGenreId()
        );

        UUID currentUserId = userService.getUserByEmail(authentication.getName());
        bookService.updateBook(id, request, currentUserId);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Book updated successfully!"
        );

        return "redirect:/books";
    }
    private void checkEditPermission(
            BookResponse book,
            Authentication authentication) {

        UUID currentUserId =
                userService.getUserByEmail(authentication.getName());

        if (!currentUserId.equals(book.ownerId())) {
            throw new AccessDeniedException(
                    "You can only edit your own books"
            );
        }
    }
}
