package projectcollab;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.seed-data.enabled=false")
class ProjectCollabApplicationTests {

    @Test
    void contextLoads() {
    }
}
