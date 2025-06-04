package ac.su.kdt.secondhandmarketplace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing; // JPA Auditing 기능을 활성화 (createAt, updateAt 자동화에 필요할 수 있음)

@SpringBootApplication
@EnableJpaAuditing // JPA Auditing을 활성화하여 엔티티의 생성 및 수정 시간을 자동으로 관리할 수 있도록 함. 더 유연한 감사 기능을 위해 사용할 수 있음

public class SecondhandmarketplaceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecondhandmarketplaceApplication.class, args);
    }

}
