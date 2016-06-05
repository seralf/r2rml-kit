package org.d2rq.download;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.d2rq.D2RQTestUtil;
import org.d2rq.HSQLDatabase;
import org.d2rq.algebra.DownloadRelation;
import org.d2rq.download.DownloadContentQuery;
import org.d2rq.lang.Mapping;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DownloadContentQueryTest {
	
	private HSQLDatabase db;
	private DownloadRelation downloadCLOB;
	private DownloadRelation downloadBLOB;
	private DownloadContentQuery q;
	
	@Before
	public void setUp() {
		db = new HSQLDatabase("test");
		db.executeSQL("CREATE TABLE People (ID INT NOT NULL PRIMARY KEY, PIC_CLOB CLOB NULL, PIC_BLOB BLOB NULL)");
		db.executeSQL("INSERT INTO People VALUES (1, 'Hello World!', NULL)");
		db.executeSQL("INSERT INTO People VALUES (2, NULL, HEXTORAW('404040'))");
		Mapping m = D2RQTestUtil.loadMapping("download/download-map.ttl");
		List<DownloadRelation> downloadRels = 
			new ArrayList<DownloadRelation>(m.compile().getDownloadRelations());
		if (downloadRels.get(0).getContentDownloadColumn().getColumn().getName().equals("PIC_CLOB")) {
			downloadCLOB = downloadRels.get(0);
			downloadBLOB = downloadRels.get(1);
		} else {
			downloadCLOB = downloadRels.get(1);
			downloadBLOB = downloadRels.get(0);
		}
	}

	@After
	public void tearDown() {
		db.close(true);
		if (q != null) q.close();
	}
	
	@Test
	public void testFixture() {
		assertNotNull(downloadCLOB);
		assertNotNull(downloadBLOB);
	}
	
	@Test
	public void testNullForNonDownloadURI() {
		q = new DownloadContentQuery(
				downloadCLOB, "http://not-in-the-mapping");
		assertFalse(q.hasContent());
		assertNull(q.getContentStream());
	}
	
	@Test
	public void testNullForNonExistingRecord() {
		// There is no People.ID=42 in the table
		q = new DownloadContentQuery(
				downloadCLOB, "http://example.org/downloads/clob/42");
		assertFalse(q.hasContent());
		assertNull(q.getContentStream());
	}
	
	@Test
	public void testReturnCLOBContentForExistingRecord() throws IOException {
		q = new DownloadContentQuery(
				downloadCLOB, "http://example.org/downloads/clob/1");
		assertTrue(q.hasContent());
		assertEquals("Hello World!", inputStreamToString(q.getContentStream()));
	}

	@Test
	public void testNULLContent() {
		q = new DownloadContentQuery(
				downloadCLOB, "http://example.org/downloads/clob/2");
		assertFalse(q.hasContent());
		assertNull(q.getContentStream());
	}
	
	@Test
	public void testReturnBLOBContentForExistingRecord() throws IOException {
		q = new DownloadContentQuery(downloadBLOB, "http://example.org/downloads/blob/2");
		assertTrue(q.hasContent());
		assertEquals("@@@", inputStreamToString(q.getContentStream()));
	}
	
	private String inputStreamToString(InputStream is) throws IOException {
		final char[] buffer = new char[0x10000];
		StringBuilder out = new StringBuilder();
		Reader in = new InputStreamReader(is, "UTF-8");
		int read;
		do {
			read = in.read(buffer, 0, buffer.length);
			if (read>0) {
				out.append(buffer, 0, read);
			}
		} while (read>=0);
		return out.toString();		
	}
}
