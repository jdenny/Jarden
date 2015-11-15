package mallon.generics;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class Lab1Test {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		Stack<String> stackS = new Stack<>();
		assertEquals(0, stackS.size());
		assertNull(stackS.top());
		stackS.push("john");
		assertEquals(1, stackS.size());
		stackS.push("julie");
		assertEquals(2, stackS.size());
		assertEquals("julie", stackS.top());
		assertEquals(2, stackS.size());
		stackS.pop();
		assertEquals(1, stackS.size());
		assertEquals("john", stackS.top(), "john");
		assertEquals(1, stackS.size());
		stackS.pop();
		assertEquals(0, stackS.size());
	}

}
