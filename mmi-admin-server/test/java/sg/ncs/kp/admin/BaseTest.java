package sg.ncs.kp.admin;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

/**
 * @author Wang Shujin
 * @date 2022/8/21 17:11
 */
public class BaseTest {

    private AutoCloseable autoCloseable;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void release() throws Exception {
        autoCloseable.close();
    }

}
