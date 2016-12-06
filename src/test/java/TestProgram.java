import org.junit.Test;

public class TestProgram {
	
	@Test
	public void runOnTestInput1() throws Exception {
		Program.main(new String[] {"src/test/resources/test1.html"});
	}
	
	@Test
	public void runOnTestInput2() throws Exception {
		Program.main(new String[] {"src/test/resources/test2.html"});
	}
	
	@Test
	public void runOnTestInput3() throws Exception {
		Program.main(new String[] {"src/test/resources/test3.html"});
	}
	
	@Test
	public void runOnTestInput4() throws Exception {
		Program.main(new String[] {"src/test/resources/test4.html"});
	}
	
}
