package org.radarbase.schema.specification.active.questionnaire;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.radarbase.schema.specification.active.ActiveSource;

@JsonInclude(Include.NON_NULL)
public class QuestionnaireSource extends ActiveSource<QuestionnaireDataTopic> {
}
