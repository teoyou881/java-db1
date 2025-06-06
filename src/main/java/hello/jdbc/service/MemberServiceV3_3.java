package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import java.sql.SQLException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

/*
 * transaction - transactional AOP
 * */
@Slf4j
public class MemberServiceV3_3 {

  private final MemberRepositoryV3 memberRepository;

  public MemberServiceV3_3(MemberRepositoryV3 memberRepository) {this.memberRepository = memberRepository;}

  @Transactional
  public void accountTransfer(String fromId, String toId, int money) throws SQLException {
    bizLogic(fromId, toId, money);
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
