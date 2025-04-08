package hello.jdbc.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * transaction - transactional AOP
 * register automatically dataSource, transactionManager
 */
@Slf4j
// To apply Spring AOP, the Spring container is required.
// If this annotation is present, the Spring container is created during testing using Spring Boot.
// Then, within the test, you can use beans managed by the Spring container through @Autowired, etc.
@SpringBootTest
class MemberServiceV3_4Test {

  public static final String MEMBER_A = "memberA";
  public static final String MEMBER_B = "memberB";
  public static final String MEMBER_EX = "ex";

  @Autowired
  private MemberRepositoryV3 memberRepository;
  @Autowired
  private MemberServiceV3_3 memberService;

  @AfterEach
  void after() throws SQLException {
    memberRepository.delete(MEMBER_A);
    memberRepository.delete(MEMBER_B);
    memberRepository.delete(MEMBER_EX);
  }

  @Test
  void AopCheck() {
    log.info("memberService class={}", memberService.getClass());
    log.info("memberRepository class={}", memberRepository.getClass());
    Assertions.assertThat(AopUtils.isAopProxy(memberService))
              .isTrue();
    Assertions.assertThat(AopUtils.isAopProxy(memberRepository))
              .isFalse();
  }

  @Test
  @DisplayName("Successful transfer")
  void accountTransfer() throws SQLException {
    // given
    Member memberA = new Member(MEMBER_A, 10000);
    Member memberB = new Member(MEMBER_B, 10000);
    memberRepository.save(memberA);
    memberRepository.save(memberB);

    // when
    log.info("START TX");
    memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);
    log.info("END TX");

    // then
    Member findMemberA = memberRepository.findById(memberA.getMemberId());
    Member findMemberB = memberRepository.findById(memberB.getMemberId());
    assertThat(findMemberA.getMoney()).isEqualTo(8000);
    assertThat(findMemberB.getMoney()).isEqualTo(12000);
  }

  @Test
  @DisplayName("Exception occurs during transfer")
  void accountTransferEx() throws SQLException {
    // given
    Member memberA = new Member(MEMBER_A, 10000);
    Member memberEx = new Member(MEMBER_EX, 10000);
    memberRepository.save(memberA);
    memberRepository.save(memberEx);

    // when
    assertThatThrownBy(
        () -> memberService.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), 2000)).isInstanceOf(
        IllegalStateException.class);

    // then
    Member findMemberA = memberRepository.findById(memberA.getMemberId());
    Member findMemberEx = memberRepository.findById(memberEx.getMemberId());

    //    memberA's balance should be rolled back
    assertThat(findMemberA.getMoney()).isEqualTo(10000);
    assertThat(findMemberEx.getMoney()).isEqualTo(10000);
  }

  // By creating an inner configuration class inside the test and adding this annotation,
  // you can register additional Spring beans needed for the test,
  // in addition to the beans automatically provided by Spring Boot.
  @TestConfiguration
  static class TestConfig {

    /*
     * Spring Boot automatically registers a DataSource and a TransactionManager
     * as long as you provide the necessary database settings in application.properties or application.yml.
     * */
    // @Bean
    // DataSource dataSource() {return new DriverManagerDataSource(URL, USERNAME, PASSWORD);}
    //
    // @Bean
    // PlatformTransactionManager transactionManager() {return new DataSourceTransactionManager(dataSource());}
    private final DataSource dataSource;

    TestConfig(DataSource dataSource) {this.dataSource = dataSource;}

    @Bean
    MemberRepositoryV3 memberRepositoryV3() {return new MemberRepositoryV3(dataSource);}

    @Bean
    MemberServiceV3_3 memberServiceV3_3() {return new MemberServiceV3_3(memberRepositoryV3());}
  }


}