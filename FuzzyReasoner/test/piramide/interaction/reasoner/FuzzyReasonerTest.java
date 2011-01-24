/*
 * Copyright (C) 2010 PIRAmIDE-SP3 authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This software consists of contributions made by many individuals, 
 * listed below:
 *
 * Author: Aitor Almeida <aitor.almeida@deusto.es>
 *         Pablo Orduña <pablo.orduna@deusto.es>
 *         Eduardo Castillejo <eduardo.castillejo@deusto.es>
 *
 */
package piramide.interaction.reasoner;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.rule.LinguisticTerm;
import net.sourceforge.jFuzzyLogic.rule.Variable;

import org.junit.Test;

import piramide.interaction.reasoner.creator.InvalidSyntaxException;
import piramide.interaction.reasoner.creator.WarningStore;

public class FuzzyReasonerTest {

	@Test
	public void testExtractInputVariables() throws Exception {
		final String rules =  	"RULE 1 : IF service IS poor OR food IS rancid THEN tip IS cheap;\n" +
								"RULE 2 : IF service IS good THEN tip IS average;\n";
		
		final FuzzyReasoner reasoner = new FuzzyReasoner();
		final Set<String> inputVars = reasoner.extractInputVariables(rules);
		final Set<String> expectedVars = new HashSet<String>();
		expectedVars.add("service");
		expectedVars.add("food");		
		assertEquals(expectedVars,inputVars);
	}
	
	@Test
	public void testGenerateFISobject() throws Exception{
		final String rules =  	"RULE 1 : IF reso_size IS small THEN video IS low;\n";
		
		final Map<String, String[]> inputVariables = new HashMap<String, String[]>();
		final String[] termsReso = {"big", "normal", "small"};
		inputVariables.put("reso_size", termsReso); 
		
		final Map<String, String[]> outputVariables = new HashMap<String, String[]>();
		final String[] termsVideo = {"high", "normal", "low", "veryLow"};
		outputVariables.put("video", termsVideo); 
								
		final Map<String, Object> initialCapabilities =  new HashMap<String, Object>();
		initialCapabilities.put("reso_size", new Integer(10));
		
		final FuzzyReasoner reasoner = new FuzzyReasoner();
		FIS fis = reasoner.generateFISobject("nokia 6630", new WarningStore(), initialCapabilities, inputVariables, Geolocation.ALL, outputVariables, rules);
		Variable reso_size = fis.getVariable("reso_size");
		HashMap<String, LinguisticTerm> resoTerms = reso_size.getLinguisticTerms();
		assertEquals(resoTerms.size(), 3);
		
		Variable video = fis.getVariable("video");
		HashMap<String, LinguisticTerm> videoTerms = video.getLinguisticTerms();
		assertEquals(videoTerms.size(), 4);		
	}
	
	@Test(expected=InvalidSyntaxException.class)
	public void testGenerateFISobjectInvalidSyntax() throws FuzzyReasonerException{
		final String rules =  	"RULE 1 : PATATA MELON FAIL;\n";
		
		final Map<String, String[]> inputVariables = new HashMap<String, String[]>();
		final String[] termsReso = {"big", "normal", "small"};
		inputVariables.put("reso_size", termsReso); 
		
		final Map<String, String[]> outputVariables = new HashMap<String, String[]>();
		final String[] termsVideo = {"high", "normal", "low"};
		outputVariables.put("video", termsVideo); 
								
		final Map<String, Object> initialCapabilities =  new HashMap<String, Object>();
		initialCapabilities.put("reso_size", new Integer(10));
		
		final FuzzyReasoner reasoner = new FuzzyReasoner();
		reasoner.generateFISobject("nokia 6630", new WarningStore(), initialCapabilities, inputVariables, Geolocation.ALL, outputVariables, rules);		

	}
	
	@Test
	public void testInferNewCapabilities() throws FuzzyReasonerException{
		final String rules =  	"RULE 1 : IF reso_size IS small THEN video IS low;\n";
		
		final Map<String, String[]> inputVariables = new HashMap<String, String[]>();
		final String[] termsReso = {"small", "normal", "big"};
		inputVariables.put("reso_size", termsReso); 
		
		final Map<String, String[]> outputVariables = new HashMap<String, String[]>();
		final String[] termsVideo = {"low", "normal", "high"};
		outputVariables.put("video", termsVideo); 
								
		final Map<String, Object> initialCapabilities =  new HashMap<String, Object>();
		initialCapabilities.put("reso_size",  new Integer(0));
		
		final FuzzyReasoner fuzzyReasoner = new FuzzyReasoner();
		
		final FIS fis = fuzzyReasoner.generateFISobject("nokia 6630", new WarningStore(), initialCapabilities, inputVariables, Geolocation.ALL, outputVariables, rules);
		
		final HashMap<String, HashMap<String, Double>> results = new HashMap<String, HashMap<String,Double>>();
		
		for(String variableName : inputVariables.keySet()){
			results.put(variableName, new HashMap<String, Double>());
			
			for(String linguisticTerm : inputVariables.get(variableName)){
				final double currentValue = fis.getVariable(variableName).getMembership(linguisticTerm);
				results.get(variableName).put(linguisticTerm, Double.valueOf(currentValue));
			}
		}
		

		final Variable reso_size = fis.getVariable("reso_size");
		assertEquals(reso_size.getLinguisticTerms().size(), 3);
		final double smallValue = fis.getVariable("reso_size").getMembership("small");
		assertEquals(0.7234203, smallValue, 0.001);
		final double normal1Value = fis.getVariable("reso_size").getMembership("normal");
		assertEquals(0.27657, normal1Value, 0.001);
		final double bigValue = fis.getVariable("reso_size").getMembership("big");
		assertEquals(0.0, bigValue, 0.0000001);

		
		final Variable video = fis.getVariable("video");
		assertEquals(video.getLinguisticTerms().size(), 3);
		assertEquals(0.322, video.defuzzify(), 0.001);
		final double lowValue = fis.getVariable("video").getMembership("low");
		assertEquals(0.67799999, lowValue, 0.0001);
		final double normalValue = fis.getVariable("video").getMembership("normal");
		assertEquals(0.322, normalValue, 0.0001);
		final double highValue = fis.getVariable("video").getMembership("high");
		assertEquals(0.0, highValue, 0.0000001);
	}
	
	@Test
	public void testInferNewCapabilitiesWarnings() throws FuzzyReasonerException{
		final String rules =  	"RULE 1 : IF reso_size IS small THEN video IS low;\n";
		
		final Map<String, String[]> inputVariables = new HashMap<String, String[]>();
		final String[] termsReso = {"small", "a1", "a2", "a3", "a4", "a5", "a6", "a7", "a8", "a9", "a10", "a11", "a12", "a13", "a14", "a15", "a16", "a17"};
		inputVariables.put("reso_size", termsReso); 
		
		final Map<String, String[]> outputVariables = new HashMap<String, String[]>();
		final String[] termsVideo = {"low", "normal", "high"};
		outputVariables.put("video", termsVideo); 
								
		final Map<String, Object> initialCapabilities =  new HashMap<String, Object>();
		initialCapabilities.put("reso_size",  new Integer(0));
		
		WarningStore store = new WarningStore();
		
		final FuzzyReasoner fuzzyReasoner = new FuzzyReasoner();
		
		fuzzyReasoner.generateFISobject("nokia 6630", store, initialCapabilities, inputVariables, Geolocation.ALL, outputVariables, rules);
		
		String[] messages = store.getMessages();
		assertEquals(1, messages.length);
		assertEquals("There are repeated linguistic terms in the universe", messages[0]);
	}

}