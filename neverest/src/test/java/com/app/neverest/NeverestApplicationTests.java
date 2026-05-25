package com.app.neverest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class NeverestApplicationTests {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void contextLoads() {
	}

	@Test
	void databaseConnectionIsHealthy() {
		Integer value = jdbcTemplate.queryForObject("select 1", Integer.class);
		assertThat(value).isEqualTo(1);
	}

	@Test
	void seededUsersAreAvailable() {
		Integer users = jdbcTemplate.queryForObject("select count(*) from nev_users", Integer.class);
		assertThat(users).isNotNull();
		assertThat(users).isGreaterThanOrEqualTo(2);
	}

}
