package net.canadensys.processor;


import static org.junit.Assert.*;

import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

import net.canadensys.processor.geography.DecimalLatLongProcessor;

import org.junit.Test;

/**
 * Test language switching on processors
 * @author canadensys
 */
public class LanguageTest {
	
	/**
	 * Make sure that the language files contain the same keys as the English one.
	 * TODO load all language files automatically
	 */
	@Test
	public void testLanguageFiles(){
		ResourceBundle resourceBundleEN = ResourceBundle.getBundle("languages/errors", new Locale("EN"));
		ResourceBundle resourceBundleES = ResourceBundle.getBundle("languages/errors", new Locale("ES"));
		ResourceBundle resourceBundleFR = ResourceBundle.getBundle("languages/errors", new Locale("FR"));
		
		Enumeration<String> enKeys = resourceBundleEN.getKeys();
		String key;
		while(enKeys.hasMoreElements()){
			key = enKeys.nextElement();
			assertTrue("The key [" +key + "] couldn't be found in the ES language file" ,resourceBundleES.containsKey(key));
			assertTrue("The key [" +key + "] couldn't be found in the FR language file" ,resourceBundleFR.containsKey(key));
		}
	}
	
	@Test
	public void testSpanish(){
		DecimalLatLongProcessor dllProcessor = new DecimalLatLongProcessor();
		dllProcessor.setLocale(new Locale("ES"));
		
		ProcessingResult pr = new ProcessingResult();
		dllProcessor.process("a", "b", Double.class, pr);
		assertTrue(pr.getErrorString().contains("no pudo ser procesado"));
	}
	
	@Test
	public void testFrench(){
		DecimalLatLongProcessor dllProcessor = new DecimalLatLongProcessor();
		dllProcessor.setLocale(new Locale("FR"));
		
		ProcessingResult pr = new ProcessingResult();
		dllProcessor.process("a", "b", Double.class, pr);
		assertTrue(pr.getErrorString().contains("Impossible de traiter la valeur"));
	}
}
