## Proposal for configuration schema

### Metadata
```json
{
  "version": "0.0.1",
  "lastModified": "2016-11-18T23:01:01+02:00",
  "id": "PHQ8",
  "title": "Weekly Checkup",
  "questions": []
}
```
**Notes:**  
`"last_modified"` format: ISO8601 or Unix Epoch Timestamp

### Question type: "Radio"
```json
{
  "id": "PHQ8-1",
  "lead": "Over the past two weeks, how often have you been bothered by any of the following problems?",
  "content": "Little interest or pleasure in doing things.",
  "responses": [
    {"response": "Not at all", "score": 0},
    {"response": "Several days", "score": 1},
    {"response": "More than half the days", "score": 2},
    {"response": "Nearly every day", "score": 3}
  ],
  "type": "radio"
}
```

### Question type: "Range"
```json
{
  "id": "PHQ8-2",
  "lead": "...",
  "content": "...",
  "range": {
    "min": 1,
    "max": 5
  },
  "type": "range"
}
```
**Notes:**  
`"range"` must not exceed 10 steps  
`"min"` & `"max"` must not have negative numbers  

### Question type: "Audio"
```json
{
  "id": "PHQ8-3",
  "lead": "...",
  "content": "...",
  "options": {
    "sampleDuration": 30,
    "idleDuration": 300,
    "startHour": 9,
    "stopHour": 21,
    "configFile": "filename.conf",
    "compressionLevel": 0
  },
  "type": "audio"
}
```
**Notes:**  
`"compressionLevel"` (0:Raw, 1:LLD, 2:Functional, 3:Class label)

