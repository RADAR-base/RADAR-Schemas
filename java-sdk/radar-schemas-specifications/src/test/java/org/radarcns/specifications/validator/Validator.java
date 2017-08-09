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
   /* public static ValidationResult validateMonitor(MonitorSource source, File file) {
        return (ValidationResult) MonitorRoles.validateAppProvider()
                .and(MonitorRoles.validateAggregator())
                .and(MonitorRoles.validateDataType())
                .and(GenericRoles.validateDoc(source.getDoc(), false))
                //.and(GenericRoles.validateKey(source.getKey()))
                .and(GenericRoles.validateName(source.getName()))
                .and(GenericRoles.validateSampleRate(source.getSampleRate()))
                .and(MonitorRoles.validateSourceType(file))
                //.and(GenericRoles.validateTopic(source.getTopic()))
                .and(AggregatableRoles.validateTopics())
                .and(GenericRoles.validateTopicNames(source.getTopics()))
                //.and(GenericRoles.validateValue(Package.MONITOR, source.getValue()))
                .and(GenericRoles.validateUnit(source.getUnit()))
                .apply(source);
    }*/

    /**
     * TODO.
     * @param question TODO
     * @return TODO
     */
    /*public static ValidationResult validateQuestion(Question question) {
        return (ValidationResult) QuestionRoles.validateContent()
                .and(QuestionRoles.validateLead())
                .and(QuestionRoles.validateWidget())
                .and(QuestionRoles.validateResponses())
                .apply(question);
    }*/

    /**
     * TODO.
     * @param source TODO
     * @param file TODO
     * @return TODO
     */
    /*public static ValidationResult validateQuestionnaire(QuestionnaireSource source, File file) {
        return (ValidationResult) ActiveSourceRoles.validateAssessmentType()
                .and(GenericRoles.validateDoc(source.getDoc(), false))
                .and(GenericRoles.validateKey(source.getKey()))
                .and(GenericRoles.validateName(source.getName()))
                .and(QuestionnaireRoles.validateQuestions())
                .and(QuestionnaireRoles.validateQuestionnaireType(file))
                .and(GenericRoles.validateTopic(source.getTopic()))
                .and(ActiveSourceRoles.validateTopics())
                .and(GenericRoles.validateTopicNames(source.getTopics()))
                .and(GenericRoles.validateValue(Package.QUESTIONNAIRE, source.getValue()))
                .apply(source);
    }*/

    /**
     * TODO.
     * @param source TODO
     * @param file TODO
     * @return TODO
     */
    /*public static ValidationResult validatePassive(PassiveSource source, File file) {
        return (ValidationResult) PassiveSourceRoles.validateAppProvider()
                .and(GenericRoles.validateDoc(source.getDoc(), true))
                .and(PassiveSourceRoles.validateModelAndVendor(file))
                .and(GenericRoles.validateName(source.getName()))
                .and(PassiveSourceRoles.validateSensors())
                .and(PassiveSourceRoles.validateSourceType())
                .and(PassiveSourceRoles.validateTopics())
                .and(GenericRoles.validateTopicNames(source.getTopics()))
                .apply(source);
    }*/

    /**
     * TODO.
     * @param response TODO
     * @return TODO
     */
    /*public static ValidationResult validateResponse(Response response) {
        return (ValidationResult) ResponseRoles.validateScore()
                .and(ResponseRoles.validateText())
                .apply(response);
    }*/

    /**
     * TODO.
     * @param sensor TODO
     * @param packageName TODO
     * @return TODO
     */
    /*public static ValidationResult validateSensor(Package packageName, Sensor sensor) {
        return (ValidationResult) SensorRoles.validateAppProvider()
                .and(SensorRoles.validateDataType())
                .and(GenericRoles.validateDoc(sensor.getDoc(), true))
                //.and(GenericRoles.validateKey(sensor.getKey()))
                .and(SensorRoles.validateName())
                .and(GenericRoles.validateSampleRate(sensor.getSampleRate()))
                .and(GenericRoles.validateTopic(sensor.getInputTopic()))
                //.and(GenericRoles.validateTopic(sensor.getTopic()))
                .and(AggregatableRoles.validateTopics())
                .and(AggregatableRoles.validateAggregator(false))
                .and(GenericRoles.validateTopicNames(sensor.getTopics()))
                .and(GenericRoles.validateUnit(sensor.getUnit()))
                //.and(GenericRoles.validateValue(packageName, sensor.getValue()))
                .apply(sensor);
    }*/

    /**
     * TODO.
     * @param processor TODO
     * @param packageName TODO
     * @return TODO
     */
    /*public static ValidationResult validateProcessor(Package packageName, Processor processor) {
        return (ValidationResult) ProcessorRoles.validateBaseOutputTopic()
                .and(ProcessorRoles.validateDataType())
                .and(GenericRoles.validateDoc(processor.getDoc(), true))
                //.and(GenericRoles.validateKey(processor.getInputKey()))
                .and(ProcessorRoles.validateName())
                .and(GenericRoles.validateSampleRate(processor.getSampleRate()))
                .and(GenericRoles.validateTopic(processor.getInputTopic()))
                .and(AggregatableRoles.validateTopics())
                .and(AggregatableRoles.validateAggregator(false))
                .and(GenericRoles.validateTopicNames(processor.getTopics()))
                .and(GenericRoles.validateUnit(processor.getUnit()))
                //.and(GenericRoles.validateValue(packageName, processor.getInputValue()))
                .apply(processor);
    }*/
}
