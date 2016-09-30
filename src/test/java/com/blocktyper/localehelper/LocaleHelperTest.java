package com.blocktyper.localehelper;

import java.io.InputStream;

import junit.framework.TestCase;


public class LocaleHelperTest extends TestCase {
	
	public void testGetLocaleFromFileInputStream(){
		
		String testFileName = "yaml/localeConfig.yml";
		InputStream inputStream = LocaleHelperTest.class.getClassLoader().getResourceAsStream(testFileName);
		if (inputStream == null) {
			throw new RuntimeException("Can not find " + testFileName);
		}
		
		LocaleHelper localeHelper = new LocaleHelper();
		
		String locale = localeHelper.getLocaleFromFileInputStream(inputStream);

		assertEquals("de", locale);
	}

	
}
