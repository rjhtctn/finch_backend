package com.rjhtctn.finch_backend.service;

import com.rjhtctn.finch_backend.model.*;
import com.rjhtctn.finch_backend.repository.*;
import com.rjhtctn.finch_backend.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final FinchRepository finchRepository;
    private final UserService userService;

    @Transactional
    public void toggleBookmark(UUID finchId, String username) {
        User user = userService.findUserByUsernameOrEmail(username);
        Finch finch = finchRepository.findById(finchId)
                .orElseThrow(() -> new ResourceNotFoundException("Finch not found"));

        bookmarkRepository.findByUserAndFinch(user, finch)
                .ifPresentOrElse(
                        bookmarkRepository::delete,
                        () -> bookmarkRepository.save(new Bookmark(user, finch))
                );
    }

    public List<Bookmark> getUserBookmarks(String username) {
        User user = userService.findUserByUsernameOrEmail(username);
        return bookmarkRepository.findAllByUser(user);
    }

    public long getBookmarkCount(Finch finch) {
        return bookmarkRepository.countByFinch(finch);
    }

    public boolean isBookmarkedByUser(User user, Finch finch) {
        return bookmarkRepository.findByUserAndFinch(user, finch).isPresent();
    }
}