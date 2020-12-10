package uk.co.odinconsultants.java.lang;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.lang.AutoCloseable;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class ClosingTimeTest {

    public static final String IN_RESOURCE = "in resource";
    public static final String IN_CLOSE = "in close";

    class MyResource implements AutoCloseable {
        @Override
        public void close() throws Exception {
            System.out.println("about to blow up on close");
            throw new Exception(IN_CLOSE);
        }
        public String toString() {
            return "MyReource";
        }
    }

    @Test
    public void testErrorsInClose() throws Exception {
        try {
            try (MyResource resource = new MyResource()) {
                System.out.println("within resource " + resource + ". About to blow up");
                throw new Exception(IN_RESOURCE);
            }
        } catch (Exception e) {
            assertThat(e.getSuppressed().length, is(1));
            assertThat(e.getSuppressed()[0].getMessage(), is(IN_CLOSE));
            assertThat(e.getMessage(), is(IN_RESOURCE));
        }
    }


}
