## Proposal for configuration schema

### Metadata
```js
{
  "version": "0.0.1",
  "lastModified": "2016-11-18T23:01:01+02:00", // format: ISO8601 or Unix Epoch Timestamp
  "id": "PHQ8",
  "title": "Weekly Checkup",
  "questions": []
}
```

### Question type: "Radio"
```js
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
```js
{
  "id": "PHQ8-2",
  "lead": "...",
  "content": "...",
  "range": {  // must not exceed 10 steps
    "min": 1, // must not have negative numbers
    "max": 5  // must not have negative numbers
  },
  "type": "range"
}
```

### Question type: "Audio"
```js
{
  "id": "PHQ8-3",
  "lead": "...",
  "content": "...",
  "options": {
    "sampleDuration": 30,
    "configFile": "filename.conf",
    "compressionLevel": 0,  // options: (0:Raw, 1:LLD, 2:Functional, 3:Class label)
    "idleDuration": 300,  // passive monitoring only
    "startHour": 9,       // passive monitoring only
    "stopHour": 21        // passive monitoring only
  },
  "type": "audio"
}
```

