package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * transaction - linked with parameters, shutdown considers the connection pool
 * */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {

  private final DataSource dataSource;
  private final MemberRepositoryV2 memberRepository;

  public void accountTransfer(String fromId, String toId, int money) throws SQLException {

    Connection con = dataSource.getConnection();
    try {
      //start transaction
      con.setAutoCommit(false);

      //business logic
      bizLogic(con, fromId, toId, money);

      //commit on success
      con.commit();
    } catch (Exception e) {
      System.out.println("rollback");
      con.rollback();
      throw new IllegalStateException(e);
    } finally {
      release(con);
    }

  }

  private void bizLogic(Connection con, String fromId, String toId, int money) throws SQLException {
    Member fromMember = memberRepository.findById(con, fromId);
    Member toMember = memberRepository.findById(con, toId);

    memberRepository.update(con, fromId, fromMember.getMoney() - money);
    validation(toMember);
    memberRepository.update(con, toId, toMember.getMoney() + money);
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
