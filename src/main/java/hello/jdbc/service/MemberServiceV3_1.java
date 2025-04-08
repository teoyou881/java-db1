package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import java.sql.Connection;
import java.sql.SQLException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/*
 * transaction - transaction manager
 * */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_1 {

  // private final DataSource dataSource;
  private final PlatformTransactionManager transactionManager;
  private final MemberRepositoryV3 memberRepository;

  public void accountTransfer(String fromId, String toId, int money) throws SQLException {

    // start transaction
    TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

    try {
      // business logic
      bizLogic(fromId, toId, money);

      // commit on success
      transactionManager.commit(status);
    } catch (Exception e) {
      System.out.println("rollback");
      transactionManager.rollback(status);
      throw new IllegalStateException(e);
    }

  }

  private void bizLogic(String fromId, String toId, int money) throws SQLException {
    Member fromMember = memberRepository.findById(fromId);
    Member toMember = memberRepository.findById(toId);

    memberRepository.update(fromId, fromMember.getMoney() - money);
    validation(toMember);
    memberRepository.update(toId, toMember.getMoney() + money);
  }

  private static void release(Connection con) {
    if (con != null) {
      try {
        con.setAutoCommit(true);
        con.close();
      } catch (Exception e) {
        log.info("error", e);
      }
    }
  }

  private void validation(Member toMember) {
    if (toMember.getMemberId()
                .equals("ex")) {
      throw new IllegalStateException("Exception occurred during transfer");
    }
  }
}
