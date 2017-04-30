package com.encens.khipus.test.mock;

import com.encens.khipus.util.URLCipher;

/**
 * Class that must be used in test
 *
 * @author
 * @version 1.0
 */
public class MockURLCipher extends URLCipher {

    public static final MockURLCipher i = new MockURLCipher();

    private MockURLCipher() {
        super.init();
    }
}
