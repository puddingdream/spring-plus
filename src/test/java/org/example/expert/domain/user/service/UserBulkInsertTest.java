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

    private static final int TOTAL_COUNT = 5_000_000;
    private static final int BATCH_SIZE = 20_000;
    private static final String DEFAULT_PASSWORD = "encoded-password";
    private static final String SAMPLE_NICKNAME = nicknameOf(2_500_000);
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

            con.setAutoCommit(false);

            for (int i = 1; i <= TOTAL_COUNT; i++) {
                ps.setString(1, RUN_PREFIX + "_user" + i + "@test.com");
                ps.setString(2, DEFAULT_PASSWORD);
                ps.setString(3, nicknameOf(i));
                ps.setString(4, "USER");
                ps.addBatch();

                if (i % BATCH_SIZE == 0) {
                    ps.executeBatch();
                    con.commit();
                    ps.clearBatch();

                    if (i % 200_000 == 0) {
                        System.out.println("Inserted rows: " + i + ", prefix=" + RUN_PREFIX);
                    }
                }
            }

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
            stmt.execute("create index idx_users_nickname on users (nickname)");
        }
    }

    private static String nicknameOf(int value) {
        long mixed = Integer.toUnsignedLong(value) * 2654435761L;
        return "nick_" + Long.toUnsignedString(mixed, 36);
    }
}
