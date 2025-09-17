package org.example.expert.queryDsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration      //스프링 설정 클래스 | 클래스 안에 정의된 @Bean을 보고 스프링이 직접 객체(@Bean)생성해서 IoC 컨테이너에 등록해줌
public class QueryDslConfig {
    @PersistenceContext //영속성 컨텍스트를 스프링이 주입
    private EntityManager entityManager;

    @Bean   //메서드의 반환 객체를 Bean으로 등록
    public JPAQueryFactory jpaQueryFactory() {

        return new JPAQueryFactory(entityManager);
    }
}
/*
QueryDSL은 JPQL 기반 쿼리를 직접 작성하고 실행하는 도구
JPA Repository를 쓰는 CRUD 메서드는 EntityManager를 내부에서 이미 사용 중
QueryDSL은 직접 쿼리를 만들기 때문에 EntityManager가 필요함
 */
/*
EntityManager와 영속성 컨택스트의 관계
EntityManager는 영속성 컨텍스트를 관리하고 영속성 컨텍스트는 엔티티 상태를 기억하고 DB와 안전하게 동기하는 공간
EntityManager = 관리자 | 영속성 컨텍스트 = 관리 대상 공간
 */