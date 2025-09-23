package org.example.expert.domain.todo.dto.response;

import lombok.Getter;

@Getter
public class TodoSearchAllResponse {
    private final String title;
    private final long numOfManager;
    private final long numOfComments;

    public  TodoSearchAllResponse(String title, long numOfManager, long numOfComments) {
        this.title = title;
        this.numOfManager = numOfManager;
        this.numOfComments = numOfComments;
    }
}
