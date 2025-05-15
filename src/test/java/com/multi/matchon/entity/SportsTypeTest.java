package com.multi.matchon.entity;


import com.multi.matchon.common.domain.SportsType;
import com.multi.matchon.common.domain.SportsTypeName;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@Rollback(false)
public class SportsTypeTest {

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("sports_type test")
    void test(){
        SportsType sportsType = new SportsType();
        sportsType.setSportsTypeName(SportsTypeName.SOCCER);
        em.persist(sportsType);
        em.flush();
    }

}
