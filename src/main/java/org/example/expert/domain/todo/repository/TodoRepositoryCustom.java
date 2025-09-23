package org.example.expert.domain.todo.repository;
import org.example.expert.domain.todo.dto.response.TodoSearchAllResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.enums.SearchType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TodoRepositoryCustom {
    Optional<Todo> findByIdWithUser(Long todoId);

    Page<TodoSearchAllResponse> searchAll(SearchType types, String keywords, Pageable pageable);
}