package org.d2rq.mapgen;

import java.util.List;

import org.d2rq.mapgen.Filter;
import org.d2rq.mapgen.FilterParser;
import org.d2rq.mapgen.Filter.IdentifierMatcher;
import org.d2rq.mapgen.FilterParser.ParseException;
import org.junit.Test;

import static org.junit.Assert.*;

public class FilterParserTest {

	@Test
	public void testEmpty() throws ParseException {
		assertEquals("", toString(new FilterParser("").parse()));
	}
	
	@Test
	public void testSimple() throws ParseException {
		assertEquals("'foo'", toString(new FilterParser("foo").parse()));
	}
	
	@Test
	public void testMultipleStrings() throws ParseException {
		assertEquals("'foo'.'bar'", toString(new FilterParser("foo.bar").parse()));
	}
	
	@Test
	public void testMultipleFilters() throws ParseException {
		assertEquals("'foo','bar'", toString(new FilterParser("foo,bar").parse()));
	}
	
	@Test
	public void testMultipleFiltersNewline() throws ParseException {
		assertEquals("'foo','bar'", toString(new FilterParser("foo\n\rbar").parse()));
	}
	
	@Test
	public void testRegex() throws ParseException {
		assertEquals("/foo/0", toString(new FilterParser("/foo/").parse()));
	}

	@Test
	public void testRegexWithFlag() throws ParseException {
		assertEquals("/foo/2", toString(new FilterParser("/foo/i").parse()));
	}

	@Test
	public void testMutlipleRegexes() throws ParseException {
		assertEquals("/foo/0./bar/0", toString(new FilterParser("/foo/./bar/").parse()));
	}

	@Test
	public void testMutlipleRegexFilters() throws ParseException {
		assertEquals("/foo/0,/bar/0", toString(new FilterParser("/foo/,/bar/").parse()));
	}

	@Test
	public void testDotInRegex() throws ParseException {
		assertEquals("/foo.bar/0", toString(new FilterParser("/foo.bar/").parse()));
	}

	@Test
	public void testEscapedDotInRegex() throws ParseException {
		assertEquals("/foo\\.bar/0", toString(new FilterParser("/foo\\.bar/").parse()));
	}

	@Test
	public void testCommaInRegex() throws ParseException {
		assertEquals("/foo,bar/0", toString(new FilterParser("/foo,bar/").parse()));
	}

	@Test
	public void testIncompleteRegex() {
		try {
			new FilterParser("/foo").parse();
			fail("Should have thrown ParseException because of unterminated regex");
		} catch (ParseException ex) {
			// expected
		}
	}

	@Test
	public void testIncompleteRegexNewline() {
		try {
			new FilterParser("/foo\nbar/").parse();
			fail("Should have thrown ParseException because of unterminated regex");
		} catch (ParseException ex) {
			// expected
		}
	}

	@Test
	public void testComplex() throws ParseException {
		assertEquals("/.*/0.'CHECKSUM','USER'.'PASSWORD'",
				toString(new FilterParser("/.*/.CHECKSUM,USER.PASSWORD").parse()));
	}
	
	@Test
	public void testParseAsSchemaFilter() throws ParseException {
		Filter result = new FilterParser("schema1,schema2").parseSchemaFilter();
		assertTrue(result.matchesSchema("schema1"));
		assertTrue(result.matchesSchema("schema2"));
		assertFalse(result.matchesSchema("schema3"));
		assertFalse(result.matchesSchema(null));
	}
	
	@Test
	public void testParseAsSchemaFilterWithRegex() throws ParseException {
		Filter result = new FilterParser("/schema[12]/i").parseSchemaFilter();
		assertTrue(result.matchesSchema("schema1"));
		assertTrue(result.matchesSchema("SCHEMA2"));
		assertFalse(result.matchesSchema("schema3"));
		assertFalse(result.matchesSchema(null));
	}

	@Test
	public void testParseAsSchemaFilterFail() {
		try {
			new FilterParser("schema.table").parseSchemaFilter();
			fail("Should have failed because schema.table is not in schema notation");
		} catch (ParseException ex) {
			// expected
		}
	}

	@Test
	public void testParseAsTableFilter() throws ParseException {
		Filter result = new FilterParser("schema.table1,schema.table2,table3").parseTableFilter(false);
		assertTrue(result.matchesTable("schema", "table1"));
		assertTrue(result.matchesTable("schema", "table2"));
		assertTrue(result.matchesTable(null, "table3"));
		assertFalse(result.matchesTable("schema", "table3"));
		assertFalse(result.matchesTable("schema", "table4"));
		assertFalse(result.matchesTable("schema2", "table1"));
		assertFalse(result.matchesTable(null, "table1"));
		assertFalse(result.matchesTable(null, "table4"));
	}
	
	@Test
	public void testTableFilterTooMany() {
		try {
			new FilterParser("a.b.c").parseTableFilter(true);
			fail("Should have failed because not in schema notation");
		} catch (ParseException ex) {
			// expected
		}
	}

	@Test
	public void testParseAsColumnFilter() throws ParseException {
		Filter result = new FilterParser("s.t1.c1,t2.c2,t2.c3").parseColumnFilter(false);
		assertTrue(result.matchesColumn("s", "t1", "c1"));
		assertTrue(result.matchesColumn(null, "t2", "c2"));
		assertTrue(result.matchesColumn(null, "t2", "c3"));
		assertFalse(result.matchesColumn(null, "t1", "c1"));
		assertFalse(result.matchesColumn("s", "t2", "c2"));
		assertFalse(result.matchesColumn(null, "t1", "c3"));
	}
	
	private String toString(List<List<IdentifierMatcher>> filters) {
		StringBuilder result = new StringBuilder();
		for (List<IdentifierMatcher> l: filters) {
			for (IdentifierMatcher m: l) {
				result.append(m.toString());
				result.append('.');
			}
			if (!l.isEmpty()) {
				result.deleteCharAt(result.length() - 1);
			}
			result.append(',');
		}
		if (!filters.isEmpty()) {
			result.deleteCharAt(result.length() - 1);
		}
		return result.toString();
	}
}
