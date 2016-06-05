package org.d2rq.vocab;

import java.util.Collection;
import java.util.HashSet;

import org.d2rq.D2RQTestUtil;
import org.d2rq.vocab.D2RConfig;
import org.d2rq.vocab.D2RQ;
import org.d2rq.vocab.RR;
import org.d2rq.vocab.VocabularySummarizer;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

import static org.junit.Assert.*;

public class VocabularySummarizerTest {

	@Test
	public void testAllPropertiesEmpty() {
		VocabularySummarizer vocab = new VocabularySummarizer(Object.class);
		assertTrue(vocab.getAllProperties().isEmpty());
	}
	
	@Test
	public void testAllClassesEmpty() {
		VocabularySummarizer vocab = new VocabularySummarizer(Object.class);
		assertTrue(vocab.getAllClasses().isEmpty());
	}
	
	@Test
	public void testAllPropertiesContainsProperty() {
		VocabularySummarizer vocab = new VocabularySummarizer(D2RQ.class);
		assertTrue(vocab.getAllProperties().contains(D2RQ.column));
		assertTrue(vocab.getAllProperties().contains(D2RQ.belongsToClassMap));
	}
	
	@Test
	public void testAllPropertiesDoesNotContainClass() {
		VocabularySummarizer vocab = new VocabularySummarizer(D2RQ.class);
		assertFalse(vocab.getAllProperties().contains(D2RQ.Database));
	}
	
	@Test
	public void testAllPropertiesDoesNotContainTermFromOtherNamespace() {
		VocabularySummarizer vocab = new VocabularySummarizer(D2RQ.class);
		assertFalse(vocab.getAllProperties().contains(RDF.type));
	}

	@Test
	public void testAllClassesContainsClass() {
		VocabularySummarizer vocab = new VocabularySummarizer(D2RQ.class);
		assertTrue(vocab.getAllClasses().contains(D2RQ.Database));
	}
	
	@Test
	public void testAllClassesDoesNotContainProperty() {
		VocabularySummarizer vocab = new VocabularySummarizer(D2RQ.class);
		assertFalse(vocab.getAllClasses().contains(D2RQ.column));
	}
	
	@Test
	public void testAllClassesDoesNotContainTermFromOtherNamespace() {
		VocabularySummarizer vocab = new VocabularySummarizer(D2RQ.class);
		assertFalse(vocab.getAllClasses().contains(D2RConfig.Server));
	}
	
	@Test
	public void testGetNamespaceEmpty() {
		assertNull(new VocabularySummarizer(Object.class).getNamespace());
	}
	
	@Test
	public void testGetNamespaceD2RQ() {
		assertEquals(D2RQ.NS, new VocabularySummarizer(D2RQ.class).getNamespace());		
	}

	@Test
	public void testGetNamespaceD2RConfig() {
		assertEquals(D2RConfig.NS, new VocabularySummarizer(D2RConfig.class).getNamespace());		
	}
	
	@Test
	public void testNoUndefinedClassesForEmptyModel() {
		VocabularySummarizer vocab = new VocabularySummarizer(D2RQ.class);
		assertTrue(vocab.getUndefinedClasses(ModelFactory.createDefaultModel()).isEmpty());
	}
	
	@Test
	public void testNoUndefinedClassesWithoutTypeStatement() {
		Model m = D2RQTestUtil.loadTurtle("vocab-summarizer/no-type.ttl");
		VocabularySummarizer vocab = new VocabularySummarizer(D2RQ.class);
		assertTrue(vocab.getUndefinedClasses(m).isEmpty());
	}
	
	@Test
	public void testNoUndefinedClassesIfAllClassesDefined() {
		Model m = D2RQTestUtil.loadTurtle("vocab-summarizer/defined-types.ttl");
		VocabularySummarizer vocab = new VocabularySummarizer(D2RQ.class);
		assertTrue(vocab.getUndefinedClasses(m).isEmpty());
	}
	
	@Test
	public void testNoUndefinedClassesIfAllInOtherNamespace() {
		Model m = D2RQTestUtil.loadTurtle("vocab-summarizer/other-namespace-types.ttl");
		VocabularySummarizer vocab = new VocabularySummarizer(D2RQ.class);
		assertTrue(vocab.getUndefinedClasses(m).isEmpty());
	}
	
	@Test
	public void testFindOneUndefinedClass() {
		final Model m = D2RQTestUtil.loadTurtle("vocab-summarizer/one-undefined-type.ttl");
		VocabularySummarizer vocab = new VocabularySummarizer(D2RQ.class);
		Collection<Resource> expected = new HashSet<Resource>() {{ 
			this.add(m.createResource(D2RQ.NS + "Pint"));
		}};
		assertEquals(expected, vocab.getUndefinedClasses(m));
	}
	
	@Test
	public void testFindTwoUndefinedClasses() {
		final Model m = D2RQTestUtil.loadTurtle("vocab-summarizer/two-undefined-types.ttl");
		VocabularySummarizer vocab = new VocabularySummarizer(D2RQ.class);
		Collection<Resource> expected = new HashSet<Resource>() {{ 
			this.add(m.createResource(D2RQ.NS + "Pint"));
			this.add(m.createResource(D2RQ.NS + "Shot"));
		}};
		assertEquals(expected, vocab.getUndefinedClasses(m));
	}
	
	@Test
	public void testNoUndefinedPropertiesForEmptyModel() {
		VocabularySummarizer vocab = new VocabularySummarizer(D2RQ.class);
		assertTrue(vocab.getUndefinedProperties(ModelFactory.createDefaultModel()).isEmpty());
	}
	
	@Test
	public void testNoUndefinedPropertiesIfAllPropertiesDefined() {
		Model m = D2RQTestUtil.loadTurtle("vocab-summarizer/defined-properties.ttl");
		VocabularySummarizer vocab = new VocabularySummarizer(D2RQ.class);
		assertTrue(vocab.getUndefinedProperties(m).isEmpty());
	}
	
	@Test
	public void testNoUndefinedPropertiesIfAllInOtherNamespace() {
		Model m = D2RQTestUtil.loadTurtle("vocab-summarizer/other-namespace-properties.ttl");
		VocabularySummarizer vocab = new VocabularySummarizer(D2RQ.class);
		assertTrue(vocab.getUndefinedProperties(m).isEmpty());
	}
	
	@Test
	public void testFindOneUndefinedProperty() {
		final Model m = D2RQTestUtil.loadTurtle("vocab-summarizer/one-undefined-property.ttl");
		VocabularySummarizer vocab = new VocabularySummarizer(D2RQ.class);
		Collection<Property> expected = new HashSet<Property>() {{ 
			this.add(m.createProperty(D2RQ.NS + "price"));
		}};
		assertEquals(expected, vocab.getUndefinedProperties(m));
	}
	
	@Test
	public void testFindTwoUndefinedProperties() {
		final Model m = D2RQTestUtil.loadTurtle("vocab-summarizer/two-undefined-properties.ttl");
		VocabularySummarizer vocab = new VocabularySummarizer(D2RQ.class);
		Collection<Property> expected = new HashSet<Property>() {{ 
			this.add(m.createProperty(D2RQ.NS + "price"));
			this.add(m.createProperty(D2RQ.NS + "parallelUniverse"));
		}};
		assertEquals(expected, vocab.getUndefinedProperties(m));
	}
	
	@Test
	public void testUsesVocabulary() {
		final Model m = D2RQTestUtil.loadTurtle("vocab-summarizer/rr-example.ttl");
		assertTrue(new VocabularySummarizer(RR.class).usesVocabulary(m));
		assertFalse(new VocabularySummarizer(D2RQ.class).usesVocabulary(m));
	}
}
