package org.radarbase.schema.specification.active.questionnaire

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import org.radarbase.config.OpenConfig
import org.radarbase.schema.specification.active.ActiveSource

@JsonInclude(NON_NULL)
@OpenConfig
class QuestionnaireSource : ActiveSource<QuestionnaireDataTopic>()
