package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import java.sql.SQLException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/*
 * transaction - transaction template
 * */
@Slf4j
public class MemberServiceV3_2 {

  // private final DataSource dataSource;
  // private final PlatformTransactionManager transactionManager;
  private final TransactionTemplate txTemplate;
  private final MemberRepositoryV3 memberRepository;

  // To use TransactionTemplate, a transactionManager is required.
  // In the constructor, the transactionManager is injected and used to create the TransactionTemplate.
  public MemberServiceV3_2(PlatformTransactionManager transactionManager, MemberRepositoryV3 memberRepository) {
    this.txTemplate = new TransactionTemplate(transactionManager);
    this.memberRepository = memberRepository;
  }

  public void accountTransfer(String fromId, String toId, int money) throws SQLException {
    // If the business logic executes successfully, the transaction is committed.
    // If a runtime (unchecked) exception occurs, the transaction is rolled back.
    // If a checked exception occurs, the transaction is committed. (default behavior)

    /*
    * 템플릿: txTemplate.executeWithoutResult(callback)
     ├── try {
     │     트랜잭션 시작
     │     콜백 실행
     │     커밋
     │   } catch {
     │     롤백
     │   }

      콜백: () -> {
          bizLogic(...)
      }
    * */
    txTemplate.executeWithoutResult((status) -> {
      // business logic
      try {
        bizLogic(fromId, toId, money);
      } catch (SQLException e) {
        // In lambda expressions, you generally **cannot throw checked exceptions**.
        // That's because the **functional interface method** used by the lambda **does not declare a `throws` clause**.
        throw new RuntimeException(e);
      }
    });
  }

  private void bizLogic(String fromId, String toId, int money) throws SQLException {
    Member fromMember = memberRepository.findById(fromId);
    Member toMember = memberRepository.findById(toId);

    memberRepository.update(fromId, fromMember.getMoney() - money);
    validation(toMember);
    memberRepository.update(toId, toMember.getMoney() + money);
  }

  private void validation(Member toMember) {
    if (toMember.getMemberId()
                .equals("ex")) {
      throw new IllegalStateException("Exception occurred during transfer");
    }
  }
}
