package examples;

import com.intuit.karate.junit5.Karate;

class KarateDemoTest {

    @Karate.Test
    Karate testAll() {
        return Karate.run().relativeTo(getClass());
    }
}
