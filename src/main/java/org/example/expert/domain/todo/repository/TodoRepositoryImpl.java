package org.example.expert.domain.todo.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.comment.entity.QComment;
import org.example.expert.domain.manager.entity.QManager;
import org.example.expert.domain.todo.dto.response.TodoSearchAllResponse;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.enums.SearchType;
import org.example.expert.domain.user.entity.QUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TodoRepositoryImpl implements TodoRepositoryCustom{

    private final JPAQueryFactory queryFactory;
    private final QTodo todo = QTodo.todo;
    private final QUser user = QUser.user;
    private final QManager manager = QManager.manager;
    private final QComment comment = QComment.comment;

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {
        Todo result = queryFactory
                .selectFrom(todo)
                .leftJoin(todo.user, user)
                .fetchJoin()
                .where(todo.id.eq(todoId))
                .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public Page<TodoSearchAllResponse> searchAll(SearchType types, String keyword, Pageable pageable) {
        // 검색 조건 미리 생성
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        boolean hasNicknameSearch = false; //닉네임 검색 여부를 확인하는 플래그

        //조건을 순회하며 BooleanBuilder에 추가
            if(keyword != null && types != null ) {
                switch (types) {
                    case TITLE :
                        booleanBuilder.and(todo.title.containsIgnoreCase(keyword));
                        break;
                    case NICKNAME :
                        hasNicknameSearch = true;
                        booleanBuilder.and(user.nickname.containsIgnoreCase(keyword));
                        break;
                    case CREATED_AT:
                        addDateRangeCondition(booleanBuilder, keyword);
                        break;
                }
        }

        JPQLQuery<TodoSearchAllResponse> query = queryFactory
                .select(Projections.constructor(
                        TodoSearchAllResponse.class,
                        todo.title,
                        manager.id.countDistinct(),
                        comment.id.countDistinct()
                        ))
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(todo.comments, comment);

        //닉네임 검색 시에만 user 테이블 동적으로 조인
        if (hasNicknameSearch) {
            query.innerJoin(todo.user, user);
        }
        // 조건 적용
        query.where(booleanBuilder);

        //페이지네이션 쿼리 실행
        List<TodoSearchAllResponse> content = query
                .groupBy(todo.id)
                .orderBy(todo.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        //전체 갯수 쿼리 빌드
        JPQLQuery<Long> countQuery = queryFactory
                .select(todo.countDistinct())
                .from(todo);
        //닉네임 검색 시에만 user 테이블 동적으로 조인
        if(hasNicknameSearch) {
            countQuery.innerJoin(todo.user, user);
        }
        countQuery.where(booleanBuilder); //동일한 조건 재사
        long total = countQuery.fetchOne();
        return new PageImpl<>(content, pageable, total);
    }

    private void addDateRangeCondition(BooleanBuilder booleanBuilder, String keyword) {
        try {
            if (keyword.contains(",")) {
                String[] parts = keyword.split(",");
                LocalDateTime startDate = LocalDateTime.parse(parts[0].trim());
                LocalDateTime endDate = LocalDateTime.parse(parts[1].trim());
                booleanBuilder.or(todo.createdAt.between(startDate,endDate));
            }else {
                LocalDateTime date = LocalDateTime.parse(keyword.trim());
                LocalDateTime startDate = date.toLocalDate().atStartOfDay();
                LocalDateTime endDate = date.toLocalDate().atTime(23,59,59,999_999_999);
                booleanBuilder.or(todo.createdAt.between(startDate, endDate));
            }
        }catch (DateTimeParseException e) {
            throw new IllegalArgumentException("날짜 형식이 올바르지 않습니다.");
        }
    }
}