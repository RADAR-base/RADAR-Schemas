package org.radarcns.specifications.value.active;

/*
 * Copyright 2017 King's College London and The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.radarcns.specifications.value.Utility.testTopicNonAggregatable;

import org.junit.BeforeClass;
import org.junit.Test;
import org.radarcns.active.questionnaire.QuestionnaireType;
import org.radarcns.catalogue.ActiveSourceType;
import org.radarcns.catalogue.RadarWidget;
import org.radarcns.specifications.SourceCatalogue;
import org.radarcns.specifications.source.active.questionnaire.QuestionnaireSource;
import org.radarcns.specifications.source.active.questionnaire.Response;

/**
 * TODO.
 */
public class Phq8Test {

    private static QuestionnaireSource phq8;

    @BeforeClass
    public static void initSource() {
        phq8 = SourceCatalogue.getActiveSource(QuestionnaireType.PHQ8);
    }

    @Test
    public void validateHeader() {
        assertEquals(ActiveSourceType.QUESTIONNAIRE.name(), phq8.getAssessmentType().name());
        assertNotNull(phq8.getDoc());
        assertEquals(QuestionnaireType.PHQ8.name(), phq8.getName());
        assertEquals(QuestionnaireType.PHQ8.name(), phq8.getQuestionnaireType().name());

        testTopicNonAggregatable("questionnaire_phq8",
            "org.radarcns.kafka.key.KeyMeasurement",
            "org.radarcns.active.questionnaire.Questionnaire",
            phq8.getTopic());


    }

    @Test
    public void validateQuestions() {
        assertNotNull(phq8.getQuestions());
        assertFalse(phq8.getQuestions().isEmpty());
        assertEquals(8, phq8.getQuestions().size(), 0);

        assertEquals("Little interest or pleasure in doing things.",
                phq8.getQuestions().get(0).getContent());
        assertEquals("Over the past two weeks, how often have you been bothered by any "
                + "of the following problems?", phq8.getQuestions().get(0).getLead());
        phq8.getQuestions().get(0).getResponses().forEach(response -> validateResponse(response));
        assertEquals(RadarWidget.RADIO.name(), phq8.getQuestions().get(0).getWidget().name());

        assertEquals("Feeling down, depressed, or hopeless.",
                phq8.getQuestions().get(1).getContent());
        assertEquals("Over the past two weeks, how often have you been bothered by any "
                + "of the following problems?", phq8.getQuestions().get(1).getLead());
        phq8.getQuestions().get(1).getResponses().forEach(response -> validateResponse(response));
        assertEquals(RadarWidget.RADIO.name(), phq8.getQuestions().get(1).getWidget().name());

        assertEquals("Trouble falling or staying asleep, or sleeping too much.",
                phq8.getQuestions().get(2).getContent());
        assertEquals("Over the past two weeks, how often have you been bothered by any of"
                + " the following problems?", phq8.getQuestions().get(2).getLead());
        phq8.getQuestions().get(2).getResponses().forEach(response -> validateResponse(response));
        assertEquals(RadarWidget.RADIO.name(), phq8.getQuestions().get(2).getWidget().name());

        assertEquals("Feeling tired or having little energy.",
                phq8.getQuestions().get(3).getContent());
        assertEquals("Over the past two weeks, how often have you been bothered by any "
                + "of the following problems?", phq8.getQuestions().get(3).getLead());
        phq8.getQuestions().get(3).getResponses().forEach(response -> validateResponse(response));
        assertEquals(RadarWidget.RADIO.name(), phq8.getQuestions().get(3).getWidget().name());

        assertEquals("Poor appetite or overeating.",
                phq8.getQuestions().get(4).getContent());
        assertEquals("Over the past two weeks, how often have you been bothered by any "
                + "of the following problems?", phq8.getQuestions().get(4).getLead());
        phq8.getQuestions().get(4).getResponses().forEach(response -> validateResponse(response));
        assertEquals(RadarWidget.RADIO.name(), phq8.getQuestions().get(4).getWidget().name());

        assertEquals("Feeling bad about yourself, or that you are a failure, or have "
                + "let yourself or your family down.", phq8.getQuestions().get(5).getContent());
        assertEquals("Over the past two weeks, how often have you been bothered by any "
                + "of the following problems?", phq8.getQuestions().get(5).getLead());
        phq8.getQuestions().get(5).getResponses().forEach(response -> validateResponse(response));
        assertEquals(RadarWidget.RADIO.name(), phq8.getQuestions().get(5).getWidget().name());

        assertEquals("Trouble concentrating on things, such as reading the newspaper"
                + " or watching television.",
                phq8.getQuestions().get(6).getContent());
        assertEquals("Over the past two weeks, how often have you been bothered by any "
                + "of the following problems?", phq8.getQuestions().get(6).getLead());
        phq8.getQuestions().get(6).getResponses().forEach(response -> validateResponse(response));
        assertEquals(RadarWidget.RADIO.name(), phq8.getQuestions().get(6).getWidget().name());

        assertEquals("Moving or speaking so slowly that other people could have "
                + "noticed. Or the opposite – being so fidgety or restless that you have been "
                + "moving around a lot more than usual.", phq8.getQuestions().get(7).getContent());
        assertEquals("Over the past two weeks, how often have you been bothered by any "
                + "of the following problems?", phq8.getQuestions().get(7).getLead());
        phq8.getQuestions().get(7).getResponses().forEach(response -> validateResponse(response));
        assertEquals(RadarWidget.RADIO.name(), phq8.getQuestions().get(7).getWidget().name());
    }

    private void validateResponse(Response response) {
        switch (response.getScore()) {
            case 0:
                assertEquals("Not at all", response.getText());
                break;
            case 1:
                assertEquals("Several days", response.getText());
                break;
            case 2:
                assertEquals("More than half the days", response.getText());
                break;
            case 3:
                assertEquals("Nearly every day", response.getText());
                break;
            default: fail(response.getScore() + " is invalid.");
        }
    }
}
