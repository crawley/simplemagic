package com.j256.simplemagic.entries;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.j256.simplemagic.ContentInfoUtil.ErrorCallBack;

public class MagicEntryParserTest {

	@Test
	public void testCoverage() {
		// no previous line
		assertNull(MagicEntryParser.parseLine(null, "!:stuff", null));
		// no non-whitespace
		assertNull(MagicEntryParser.parseLine(null, "            ", null));
		// 0 level
		assertNull(MagicEntryParser.parseLine(null, ">0   ", null));

		// no whitespace
		assertNull(MagicEntryParser.parseLine(null, "100", null));
		// no whitespace, with error
		LocalErrorCallBack error = new LocalErrorCallBack();
		assertNull(MagicEntryParser.parseLine(null, "100", error));
		assertNotNull(error.details);

		// no pattern
		assertNull(MagicEntryParser.parseLine(null, ">1   ", null));
		assertNull(MagicEntryParser.parseLine(null, ">1   ", error));
		assertNotNull(error.details);

		// no type
		assertNull(MagicEntryParser.parseLine(null, ">1   wow", null));
		assertNull(MagicEntryParser.parseLine(null, ">1   wow", error));
		assertNotNull(error.details);

		// no value
		assertNull(MagicEntryParser.parseLine(null, ">1   wow     ", null));
		assertNull(MagicEntryParser.parseLine(null, ">1   wow     ", error));
		assertNotNull(error.details);
	}

	@Test
	public void testBadLevel() {
		// no level number
		assertNull(MagicEntryParser.parseLine(null, ">   string   SONG   Format", null));
		// no level number with error
		LocalErrorCallBack error = new LocalErrorCallBack();
		assertNull(MagicEntryParser.parseLine(null, ">   string   SONG   Format", error));
		assertNotNull(error.details);
		// level not a number
		assertNull(MagicEntryParser.parseLine(null, ">a   string   SONG   Format", null));
		// level not a number with error
		error = new LocalErrorCallBack();
		assertNull(MagicEntryParser.parseLine(null, ">b   string   SONG   Format", error));
		assertNotNull(error.details);
	}

	@Test
	public void testTypeString() {
		// & part not a number
		assertNull(MagicEntryParser.parseLine(null, ">1   short&a    Format", null));
		// & part not a number with error
		LocalErrorCallBack error = new LocalErrorCallBack();
		assertNull(MagicEntryParser.parseLine(null, ">1   short&a    Format", error));
		assertNotNull(error.details);

		// no type string
		assertNull(MagicEntryParser.parseLine(null, ">1   &0    Format", null));
		// no type string with error
		error = new LocalErrorCallBack();
		assertNull(MagicEntryParser.parseLine(null, ">1   &0    Format", error));
		assertNotNull(error.details);

		// unknown matcher
		assertNull(MagicEntryParser.parseLine(null, ">1   unknowntype    Format", null));
		// unknown matcher with error
		error = new LocalErrorCallBack();
		assertNull(MagicEntryParser.parseLine(null, ">1   unknowntype    Format", error));
		assertNotNull(error.details);
	}

	@Test
	public void testValue() {
		// value not a number
		assertNull(MagicEntryParser.parseLine(null, ">0    byte     =z     format", null));
		// value not a number, with error
		LocalErrorCallBack error = new LocalErrorCallBack();
		assertNull(MagicEntryParser.parseLine(null, ">0    byte     =z     format", error));
		assertNotNull(error.details);
	}

	@Test
	public void testSpecial() {
		MagicEntry prev = MagicEntryParser.parseLine(null, "0   string   SONG   Format", null);
		assertNotNull(prev);

		// no whitespace
		assertNull(MagicEntryParser.parseLine(prev, "!:", null));
		// no whitespace, no error
		LocalErrorCallBack error = new LocalErrorCallBack();
		assertNull(MagicEntryParser.parseLine(prev, "!:", error));
		assertNotNull(error.details);

		// no value after whitespace
		assertNull(MagicEntryParser.parseLine(prev, "!:    ", null));
		// no value after whitespace, with error
		error = new LocalErrorCallBack();
		assertNull(MagicEntryParser.parseLine(prev, "!:    ", error));
		assertNotNull(error.details);
	}
	
	@Test
	public void testSimpleCorrect() {
		MagicEntry ent = MagicEntryParser.parseLine(null, "0   string   SONG   Format", null);
		assertEquals("level 0,name 'Format',test 'SONG',format 'Format'", ent.toString());
	}
	
	@Test
	public void testBackspace() {
		MagicEntry ent = MagicEntryParser.parseLine(
				null, ">6	string		Exif		\b, EXIF standard", null);
		assertEquals("level 1,name ',',test 'Exif',format ', EXIF standard'," +
				"addOffset false,offset 6,offsetInfo null,matcher StringType," +
				"andValue null,unsignedType false,formatSpacePrefix false,clearFormat false", 
				ent.toString2());
	}
	
	@Test
	public void testRegression() {
		MagicEntry ent = MagicEntryParser.parseLine(
				null, "0	string		\\<?xml\\ version=\"", null);
		assertEquals("level 0,name 'unknown',test '<?xml version=\"'", 
				ent.toString());
	}
	
	@Test
	public void testSplitLine() {
		String[] parts = MagicEntryParser.splitLine("1 2 3 4", 4);
		assertEquals(4, parts.length);
		assertEquals("1", parts[0]);
		assertEquals("2", parts[1]);
		assertEquals("3", parts[2]);
		assertEquals("4", parts[3]);
		parts = MagicEntryParser.splitLine("1\\n 2\\t 3\\b 4\\\\", 4);
		assertEquals(4, parts.length);
		assertEquals("1\n", parts[0]);
		assertEquals("2\t", parts[1]);
		assertEquals("3\b", parts[2]);
		assertEquals("4\\", parts[3]);		
		parts = MagicEntryParser.splitLine("1\\007 2\\377 3\\xff 4\\x00", 4);
		assertEquals(4, parts.length);
		assertEquals("1\007", parts[0]);
		assertEquals("2\377", parts[1]);
		assertEquals("3\377", parts[2]);
		assertEquals("4\000", parts[3]);	
		parts = MagicEntryParser.splitLine("1\\ 2\\t3", 4);
		assertEquals(1, parts.length);
		assertEquals("1 2\t3", parts[0]);
		parts = MagicEntryParser.splitLine("1 2 3 4 5 6", 4);
		assertEquals(4, parts.length);
		assertEquals("1", parts[0]);
		assertEquals("2", parts[1]);
		assertEquals("3", parts[2]);
		assertEquals("4 5 6", parts[3]);
		parts = MagicEntryParser.splitLine(" 1  2  3  4  5  6 ", 4);
		assertEquals(4, parts.length);
		assertEquals("1", parts[0]);
		assertEquals("2", parts[1]);
		assertEquals("3", parts[2]);
		assertEquals("4  5  6 ", parts[3]);
		parts = MagicEntryParser.splitLine(">6	string		Exif		\b, EXIF standard", 4);
		assertEquals(4, parts.length);
		assertEquals(">6", parts[0]);
		assertEquals("string", parts[1]);
		assertEquals("Exif", parts[2]);
		assertEquals("\b, EXIF standard", parts[3]);
		parts = MagicEntryParser.splitLine(">6	string		Exif		\\b, EXIF standard", 4);
		assertEquals(4, parts.length);
		assertEquals(">6", parts[0]);
		assertEquals("string", parts[1]);
		assertEquals("Exif", parts[2]);
		assertEquals("\b, EXIF standard", parts[3]);
		parts = MagicEntryParser.splitLine("0	string		\\<?xml\\ version=\"", 4);
		assertEquals(3, parts.length);
		assertEquals("0", parts[0]);
		assertEquals("string", parts[1]);
		assertEquals("<?xml version=\"", parts[2]);
	}

	private static class LocalErrorCallBack implements ErrorCallBack {
		@SuppressWarnings("unused")
		String line;
		String details;
		@SuppressWarnings("unused")
		Exception e;

		@Override
		public void error(String line, String details, Exception e) {
			this.line = line;
			this.details = details;
			this.e = e;
		}
	}
}
