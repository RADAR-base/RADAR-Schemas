package org.radarcns.specifications.validator;

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

import static org.radarcns.specifications.validator.MonitorRoles.validateAggregator;
import static org.radarcns.specifications.validator.MonitorRoles.validateDataType;
import static org.radarcns.specifications.validator.PassiveSourceRoles.validateModelAndVendor;
import static org.radarcns.specifications.validator.ProcessorRoles.validateBaseOutputTopic;
import static org.radarcns.specifications.validator.QuestionRoles.validateContent;
import static org.radarcns.specifications.validator.QuestionRoles.validateLead;
import static org.radarcns.specifications.validator.QuestionRoles.validateResposnses;
import static org.radarcns.specifications.validator.QuestionRoles.validateWidget;
import static org.radarcns.specifications.validator.QuestionnaireRoles.validateQuestionnaireType;
import static org.radarcns.specifications.validator.QuestionnaireRoles.validateQuestions;
import static org.radarcns.specifications.validator.ResponseRoles.validateScore;
import static org.radarcns.specifications.validator.ResponseRoles.validateText;
import static org.radarcns.specifications.validator.GenericRoles.validateDoc;
import static org.radarcns.specifications.validator.GenericRoles.validateKey;
import static org.radarcns.specifications.validator.GenericRoles.validateSampleRate;
import static org.radarcns.specifications.validator.GenericRoles.validateTopic;
import static org.radarcns.specifications.validator.GenericRoles.validateUnit;
import static org.radarcns.specifications.validator.GenericRoles.validateValue;

import java.io.File;
import org.radarcns.specifications.source.active.questionnaire.Question;
import org.radarcns.specifications.source.active.questionnaire.QuestionnaireSource;
import org.radarcns.specifications.source.active.questionnaire.Response;
import org.radarcns.specifications.source.passive.MonitorSource;
import org.radarcns.specifications.source.passive.PassiveSource;
import org.radarcns.specifications.source.passive.Processor;
import org.radarcns.specifications.source.passive.Sensor;
import org.radarcns.specifications.validator.GenericRoles.Package;

//TODO validate enum to be different from UNKNOWN

/**
 * TODO
 */
public final class Validator {

    private Validator() {
        //Static class
    }

    /**
     * TODO.
     * @param source TODO
     * @param file TODO
     * @return TODO
     */
    public static ValidationResult validateMonitor(MonitorSource source, File file) {
        return (ValidationResult) MonitorRoles.validateAppProvider()
                .and(validateAggregator())
                .and(validateDataType())
                .and(validateDoc(source.getDoc(), false))
                .and(validateKey(source.getKey()))
                .and(GenericRoles.validateName(source.getName()))
                .and(validateSampleRate(source.getSampleRate()))
                .and(MonitorRoles.validateSourceType(file))
                .and(validateTopic(source.getTopic()))
                .and(MonitorRoles.validateTopics())
                .and(GenericRoles.validateTopics(source.getTopics()))
                .and(validateValue(Package.MONITOR, source.getValue()))
                .and(validateUnit(source.getUnit()))
                .apply(source);
    }

    /**
     * TODO.
     * @param question TODO
     * @return TODO
     */
    public static ValidationResult validateQuestion(Question question) {
        return (ValidationResult) validateContent()
                .and(validateLead())
                .and(validateWidget())
                .and(validateResposnses())
                .apply(question);
    }

    /**
     * TODO.
     * @param source TODO
     * @param file TODO
     * @return TODO
     */
    public static ValidationResult validateQuestionnaire(QuestionnaireSource source, File file) {
        return (ValidationResult) QuestionnaireRoles.validateAssessmentType()
                .and(validateDoc(source.getDoc(), false))
                .and(validateKey(source.getKey()))
                .and(GenericRoles.validateName(source.getName()))
                .and(validateQuestions())
                .and(validateQuestionnaireType(file))
                .and(validateTopic(source.getTopic()))
                .and(QuestionnaireRoles.validateTopics())
                .and(GenericRoles.validateTopics(source.getTopics()))
                .and(validateValue(Package.QUESTIONNAIRE, source.getValue()))
                .apply(source);
    }

    /**
     * TODO.
     * @param source TODO
     * @param file TODO
     * @return TODO
     */
    public static ValidationResult validatePassive(PassiveSource source, File file) {
        return (ValidationResult) PassiveSourceRoles.validateAppProvider()
                    .and(validateDoc(source.getDoc(), true))
                    .and(validateModelAndVendor(file))
                    .and(GenericRoles.validateName(source.getName()))
                    .and(PassiveSourceRoles.validateSourceType())
                    .and(PassiveSourceRoles.validateTopics())
                    .and(GenericRoles.validateTopics(source.getTopics()))
                    .apply(source);
    }

    /**
     * TODO.
     * @param response TODO
     * @return TODO
     */
    public static ValidationResult validateResponse(Response response) {
        return (ValidationResult) validateScore()
                    .and(validateText())
                    .apply(response);
    }

    /**
     * TODO.
     * @param sensor TODO
     * @param packageName TODO
     * @return TODO
     */
    public static ValidationResult validateSensor(Package packageName, Sensor sensor) {
        return (ValidationResult)
            validateAggregator(sensor.getAggregator(), true)
            .and(SensorRoles.validateAppProvider())
            .and(SensorRoles.validateDataType())
            .and(validateDoc(sensor.getDoc(), true))
            .and(validateKey(sensor.getKey()))
            .and(SensorRoles.validateName())
            .and(validateSampleRate(sensor.getSampleRate()))
            .and(validateTopic(sensor.getTopic()))
            .and(SensorRoles.validateTopics())
            .and(GenericRoles.validateTopics(sensor.getTopics()))
            .and(validateUnit(sensor.getUnit()))
            .and(validateValue(packageName, sensor.getValue()))
            .apply(sensor);
    }

    /**
     * TODO.
     * @param processor TODO
     * @param packageName TODO
     * @return TODO
     */
    public static ValidationResult validateProcessor(Package packageName, Processor processor) {
        return (ValidationResult)
            validateAggregator(processor.getAggregator(), false)
                .and(validateBaseOutputTopic())
                .and(ProcessorRoles.validateDataType())
                .and(validateDoc(processor.getDoc(), true))
                .and(validateKey(processor.getInputKey()))
                .and(ProcessorRoles.validateName())
                .and(validateSampleRate(processor.getSampleRate()))
                .and(validateTopic(processor.getInputTopic()))
                .and(ProcessorRoles.validateTopics())
                .and(GenericRoles.validateTopics(processor.getTopics()))
                .and(validateUnit(processor.getUnit()))
                .and(validateValue(packageName, processor.getInputValue()))
                .apply(processor);
    }
}
