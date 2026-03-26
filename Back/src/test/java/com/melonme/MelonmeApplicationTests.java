package com.melonme;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("통합 테스트 환경(PostgreSQL)이 필요하므로 단위테스트 실행 시 제외")
class MelonmeApplicationTests {

    @Test
    void contextLoads() {
    }
}
