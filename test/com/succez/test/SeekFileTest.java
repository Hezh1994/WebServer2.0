package com.succez.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.succez.util.Seeker;

public class SeekFileTest {

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	@Test
	public void test() throws FileNotFoundException {
		assertEquals(new File("d:/document"), Seeker.getFile("/d/document"));
		assertEquals(new File("d:\\"), Seeker.getFile("/d"));
		assertEquals(new File("d:/图片/pic.jpg"), Seeker.getFile("/d/图片/pic.jpg"));

		expectedEx.expect(FileNotFoundException.class);
		expectedEx.expectMessage("文件不存在");
		Seeker.getFile("/d/dga");
	}
}
