package org.example.expert.domain.user.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application.yml")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class UserBulkInsertTest {

    // 과제 13번 요구사항: 테스트 코드로 500만 건 유저 데이터를 생성한다.
    private static final int TOTAL_COUNT = 5_000_000;
    // 너무 자주 commit 하면 느리고, 너무 크게 잡으면 메모리 사용량이 커져서 적당한 배치 크기로 타협했다.
    private static final int BATCH_SIZE = 20_000;
    private static final String DEFAULT_PASSWORD = "encoded-password";
    // 조회 성능 비교용으로 항상 같은 조건을 재현할 수 있게 샘플 닉네임을 고정했다.
    private static final String SAMPLE_NICKNAME = nicknameOf(2_500_000);
    // email unique 충돌을 피하기 위해 실행 시점마다 다른 prefix를 붙인다.
    private static final String RUN_PREFIX =
            "bulk" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

    @Autowired
    private DataSource dataSource;

    @Test
    @Disabled("Run manually on a local database for large-volume insert benchmarking.")
    void bulkInsertUsers() throws Exception {
        String sql = "insert into users (email, password, nickname, user_role) values (?, ?, ?, ?)";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // JDBC batch insert 성능을 위해 auto-commit을 끄고, 일정 단위마다 직접 commit 한다.
            con.setAutoCommit(false);

            for (int i = 1; i <= TOTAL_COUNT; i++) {
                // email은 unique 제약이 있으므로 실행 prefix + 순번 조합으로 재실행 가능하게 만든다.
                ps.setString(1, RUN_PREFIX + "_user" + i + "@test.com");
                ps.setString(2, DEFAULT_PASSWORD);
                // nickname은 단순히 nick_1, nick_2 ... 대신 해시 비슷하게 섞어서 만든다.
                // 과제 요구사항의 "랜덤" 취지와, 동일 nickname이 들어갈 가능성을 낮추기 위한 처리다.
                ps.setString(3, nicknameOf(i));
                ps.setString(4, "USER");
                ps.addBatch();

                if (i % BATCH_SIZE == 0) {
                    // 네트워크 왕복과 SQL 실행 횟수를 줄이기 위해 batch 단위로 한 번에 insert 한다.
                    ps.executeBatch();
                    con.commit();
                    ps.clearBatch();

                    // 장시간 실행 작업이라 중간 진행률을 남겨야 현재 어디까지 들어갔는지 확인하기 쉽다.
                    if (i % 200_000 == 0) {
                        System.out.println("Inserted rows: " + i + ", prefix=" + RUN_PREFIX);
                    }
                }
            }

            // 마지막 배치 잔여 건 처리
            ps.executeBatch();
            con.commit();
            System.out.println("Bulk insert completed. prefix=" + RUN_PREFIX);
        }
    }

    @Test
//    @Disabled("Run manually after bulk insert to compare nickname lookup latency.")
    void measureSearchByNickname() throws Exception {
        String sql = "select id, email, nickname from users where nickname = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, SAMPLE_NICKNAME);

            // 실제 조회 시간을 보기 위해 쿼리 실행 직전/직후를 측정한다.
            long start = System.nanoTime();
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("Sample user not found: " + SAMPLE_NICKNAME);
                }

                System.out.println("Found user id: " + rs.getLong("id"));
                System.out.println("Found nickname: " + rs.getString("nickname"));
            }
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;

            System.out.println("Nickname lookup time(ms): " + elapsedMs);
        }
    }

    @Test
//    @Disabled("Run manually when you want to inspect the query plan.")
    void explainSearchByNickname() throws Exception {
        String sql = "EXPLAIN select id, email, nickname from users where nickname = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, SAMPLE_NICKNAME);

            try (ResultSet rs = ps.executeQuery()) {
                // EXPLAIN 결과를 출력해서 단순 체감 속도뿐 아니라
                // 실제로 풀스캔(ALL)인지 인덱스 탐색(ref/range 등)인지 같이 확인한다.
                while (rs.next()) {
                    System.out.println(
                            "type=" + rs.getString("type")
                                    + ", key=" + rs.getString("key")
                                    + ", rows=" + rs.getString("rows")
                                    + ", extra=" + rs.getString("Extra")
                    );
                }
            }
        }
    }

    @Test
    @Disabled("Run manually only if the nickname index does not exist yet.")
    void createNicknameIndex() throws Exception {
        try (Connection con = dataSource.getConnection();
             Statement stmt = con.createStatement()) {
            // nickname 정확 일치 검색 속도 개선용 인덱스
            stmt.execute("create index idx_users_nickname on users (nickname)");
        }
    }

    private static String nicknameOf(int value) {
        // Knuth multiplicative hashing 계열 방식으로 값을 한 번 섞은 뒤 36진수 문자열로 바꾼다.
        // 완전한 난수는 아니지만, 대량 데이터 테스트에서 규칙성이 덜 드러나고 중복 가능성이 낮다.
        long mixed = Integer.toUnsignedLong(value) * 2654435761L;
        return "nick_" + Long.toUnsignedString(mixed, 36);
    }
}
