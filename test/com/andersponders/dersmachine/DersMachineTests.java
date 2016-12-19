package com.andersponders.dersmachine;

import static org.junit.Assert.*;

import org.junit.Test;

public class DersMachineTests {
	private static final String query = "put_structure h/2,3\n"
			+ "set_variable 2\n"
			+ "set_variable 5\n"
			+ "put_structure f/1,4\n"
			+ "set_value 5\n"
			+ "put_structure p/3,1\n"
			+ "set_value 2\n"
			+ "set_value 3\n"
			+ "set_value 4\n";
	@Test
	public void testInit() {
		DersMachine machine = new DersMachine();
		machine.put_structure("f/2", 3);
		machine.set_variable(0);
		machine.set_variable(2);
		machine.put_structure("g/3", 0);
		machine.pp_heap();
	}
	
	@Test
	public void test_run()
	{
		DersMachine machine = new DersMachine();
		machine.load(query);
		machine.pp_heap();
	}

}
