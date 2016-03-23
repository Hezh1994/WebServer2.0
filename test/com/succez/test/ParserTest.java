package com.succez.test;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.succez.util.Parser;
import com.succez.web_server.Request;

public class ParserTest {

	@Test
	public void test() throws IOException {
		assertEquals(new Request("GET", "/d/test.txt"),
				Parser.parse("GET /d/test.txt HTTP1.1"));

		assertEquals(new Request("POST", "/d/document"),
				Parser.parse("POST /d/document HTTP1.1"));

		assertEquals(new Request("GET", "/d"), Parser.parse("GET /d HTTP1.1"));
	}
}
