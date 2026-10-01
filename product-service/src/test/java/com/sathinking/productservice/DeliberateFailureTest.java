package com.sathinking.productservice;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DeliberateFailureTest {

    @Test
    void thisTestIsDesignedToFail() {
        assertEquals(1, 2, "Deliberately failing to see Jenkins handle it");
    }
}
