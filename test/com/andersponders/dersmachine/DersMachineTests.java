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
	
	private static final String program = "p/3 : get_structure f/1,1\n"
			+ "unify_variable 4\n"
			+ "get_structure h/2,2\n"
			+ "unify_variable 5\n"
			+ "unify_variable 6\n"
			+ "get_value 5,3\n"
			+ "get_structure f/1,6\n"
			+ "unify_variable 7\n"
			+ "get_structure a/0,7\n"
			+ "proceed\n";
	
	private static final String query_program = "put_variable 4,1\n"
			+ "put_structure h/2,2\n"
			+ "set_value 4\n"
			+ "set_variable 5\n"
			+ "put_structure f/1,3\n"
			+ "set_value 5\n"
			+ "call p/3\n"
			+ "p/3 : get_structure f/1,1\n"
			+ "unify_variable 4\n"
			+ "get_structure h/2,2\n"
			+ "unify_variable 5\n"
			+ "unify_variable 6\n"
			+ "get_value 5,3\n"
			+ "get_structure f/1,6\n"
			+ "unify_variable 7\n"
			+ "get_structure a/0,7\n";
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
	public void test_query()
	{
		DersMachine machine = new DersMachine();
		machine.load(query);
		machine.pp_heap();
	}
	
	@Test
	public void test_program()
	{
		DersMachine machine = new DersMachine();
		machine.load(query_program);
		machine.pp_heap();
	}

}
