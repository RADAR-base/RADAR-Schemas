/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package org.radarcns.empatica;

import org.apache.avro.specific.SpecificData;

@SuppressWarnings("all")
/** Data from the electrodermal activity sensor expressed as microsiemens (µS). Uses a galvanic skin response sensor. */
@org.apache.avro.specific.AvroGenerated
public class EmpaticaE4ElectroDermalActivity extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = 2913123988943737453L;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"EmpaticaE4ElectroDermalActivity\",\"namespace\":\"org.radarcns.empatica\",\"doc\":\"Data from the electrodermal activity sensor expressed as microsiemens (µS). Uses a galvanic skin response sensor.\",\"fields\":[{\"name\":\"time\",\"type\":\"double\",\"doc\":\"device timestamp in UTC (s)\"},{\"name\":\"timeReceived\",\"type\":\"double\",\"doc\":\"device receiver timestamp in UTC (s)\"},{\"name\":\"electroDermalActivity\",\"type\":\"float\",\"doc\":\"electrodermal activity (µS)\"}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  /** device timestamp in UTC (s) */
  @Deprecated public double time;
  /** device receiver timestamp in UTC (s) */
  @Deprecated public double timeReceived;
  /** electrodermal activity (µS) */
  @Deprecated public float electroDermalActivity;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public EmpaticaE4ElectroDermalActivity() {}

  /**
   * All-args constructor.
   * @param time device timestamp in UTC (s)
   * @param timeReceived device receiver timestamp in UTC (s)
   * @param electroDermalActivity electrodermal activity (µS)
   */
  public EmpaticaE4ElectroDermalActivity(java.lang.Double time, java.lang.Double timeReceived, java.lang.Float electroDermalActivity) {
    this.time = time;
    this.timeReceived = timeReceived;
    this.electroDermalActivity = electroDermalActivity;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return time;
    case 1: return timeReceived;
    case 2: return electroDermalActivity;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  // Used by DatumReader.  Applications should not call.
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: time = (java.lang.Double)value$; break;
    case 1: timeReceived = (java.lang.Double)value$; break;
    case 2: electroDermalActivity = (java.lang.Float)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'time' field.
   * @return device timestamp in UTC (s)
   */
  public java.lang.Double getTime() {
    return time;
  }

  /**
   * Sets the value of the 'time' field.
   * device timestamp in UTC (s)
   * @param value the value to set.
   */
  public void setTime(java.lang.Double value) {
    this.time = value;
  }

  /**
   * Gets the value of the 'timeReceived' field.
   * @return device receiver timestamp in UTC (s)
   */
  public java.lang.Double getTimeReceived() {
    return timeReceived;
  }

  /**
   * Sets the value of the 'timeReceived' field.
   * device receiver timestamp in UTC (s)
   * @param value the value to set.
   */
  public void setTimeReceived(java.lang.Double value) {
    this.timeReceived = value;
  }

  /**
   * Gets the value of the 'electroDermalActivity' field.
   * @return electrodermal activity (µS)
   */
  public java.lang.Float getElectroDermalActivity() {
    return electroDermalActivity;
  }

  /**
   * Sets the value of the 'electroDermalActivity' field.
   * electrodermal activity (µS)
   * @param value the value to set.
   */
  public void setElectroDermalActivity(java.lang.Float value) {
    this.electroDermalActivity = value;
  }

  /**
   * Creates a new EmpaticaE4ElectroDermalActivity RecordBuilder.
   * @return A new EmpaticaE4ElectroDermalActivity RecordBuilder
   */
  public static org.radarcns.empatica.EmpaticaE4ElectroDermalActivity.Builder newBuilder() {
    return new org.radarcns.empatica.EmpaticaE4ElectroDermalActivity.Builder();
  }

  /**
   * Creates a new EmpaticaE4ElectroDermalActivity RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new EmpaticaE4ElectroDermalActivity RecordBuilder
   */
  public static org.radarcns.empatica.EmpaticaE4ElectroDermalActivity.Builder newBuilder(org.radarcns.empatica.EmpaticaE4ElectroDermalActivity.Builder other) {
    return new org.radarcns.empatica.EmpaticaE4ElectroDermalActivity.Builder(other);
  }

  /**
   * Creates a new EmpaticaE4ElectroDermalActivity RecordBuilder by copying an existing EmpaticaE4ElectroDermalActivity instance.
   * @param other The existing instance to copy.
   * @return A new EmpaticaE4ElectroDermalActivity RecordBuilder
   */
  public static org.radarcns.empatica.EmpaticaE4ElectroDermalActivity.Builder newBuilder(org.radarcns.empatica.EmpaticaE4ElectroDermalActivity other) {
    return new org.radarcns.empatica.EmpaticaE4ElectroDermalActivity.Builder(other);
  }

  /**
   * RecordBuilder for EmpaticaE4ElectroDermalActivity instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<EmpaticaE4ElectroDermalActivity>
    implements org.apache.avro.data.RecordBuilder<EmpaticaE4ElectroDermalActivity> {

    /** device timestamp in UTC (s) */
    private double time;
    /** device receiver timestamp in UTC (s) */
    private double timeReceived;
    /** electrodermal activity (µS) */
    private float electroDermalActivity;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(org.radarcns.empatica.EmpaticaE4ElectroDermalActivity.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.time)) {
        this.time = data().deepCopy(fields()[0].schema(), other.time);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.timeReceived)) {
        this.timeReceived = data().deepCopy(fields()[1].schema(), other.timeReceived);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.electroDermalActivity)) {
        this.electroDermalActivity = data().deepCopy(fields()[2].schema(), other.electroDermalActivity);
        fieldSetFlags()[2] = true;
      }
    }

    /**
     * Creates a Builder by copying an existing EmpaticaE4ElectroDermalActivity instance
     * @param other The existing instance to copy.
     */
    private Builder(org.radarcns.empatica.EmpaticaE4ElectroDermalActivity other) {
            super(SCHEMA$);
      if (isValidValue(fields()[0], other.time)) {
        this.time = data().deepCopy(fields()[0].schema(), other.time);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.timeReceived)) {
        this.timeReceived = data().deepCopy(fields()[1].schema(), other.timeReceived);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.electroDermalActivity)) {
        this.electroDermalActivity = data().deepCopy(fields()[2].schema(), other.electroDermalActivity);
        fieldSetFlags()[2] = true;
      }
    }

    /**
      * Gets the value of the 'time' field.
      * device timestamp in UTC (s)
      * @return The value.
      */
    public java.lang.Double getTime() {
      return time;
    }

    /**
      * Sets the value of the 'time' field.
      * device timestamp in UTC (s)
      * @param value The value of 'time'.
      * @return This builder.
      */
    public org.radarcns.empatica.EmpaticaE4ElectroDermalActivity.Builder setTime(double value) {
      validate(fields()[0], value);
      this.time = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'time' field has been set.
      * device timestamp in UTC (s)
      * @return True if the 'time' field has been set, false otherwise.
      */
    public boolean hasTime() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'time' field.
      * device timestamp in UTC (s)
      * @return This builder.
      */
    public org.radarcns.empatica.EmpaticaE4ElectroDermalActivity.Builder clearTime() {
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'timeReceived' field.
      * device receiver timestamp in UTC (s)
      * @return The value.
      */
    public java.lang.Double getTimeReceived() {
      return timeReceived;
    }

    /**
      * Sets the value of the 'timeReceived' field.
      * device receiver timestamp in UTC (s)
      * @param value The value of 'timeReceived'.
      * @return This builder.
      */
    public org.radarcns.empatica.EmpaticaE4ElectroDermalActivity.Builder setTimeReceived(double value) {
      validate(fields()[1], value);
      this.timeReceived = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'timeReceived' field has been set.
      * device receiver timestamp in UTC (s)
      * @return True if the 'timeReceived' field has been set, false otherwise.
      */
    public boolean hasTimeReceived() {
      return fieldSetFlags()[1];
    }


    /**
      * Clears the value of the 'timeReceived' field.
      * device receiver timestamp in UTC (s)
      * @return This builder.
      */
    public org.radarcns.empatica.EmpaticaE4ElectroDermalActivity.Builder clearTimeReceived() {
      fieldSetFlags()[1] = false;
      return this;
    }

    /**
      * Gets the value of the 'electroDermalActivity' field.
      * electrodermal activity (µS)
      * @return The value.
      */
    public java.lang.Float getElectroDermalActivity() {
      return electroDermalActivity;
    }

    /**
      * Sets the value of the 'electroDermalActivity' field.
      * electrodermal activity (µS)
      * @param value The value of 'electroDermalActivity'.
      * @return This builder.
      */
    public org.radarcns.empatica.EmpaticaE4ElectroDermalActivity.Builder setElectroDermalActivity(float value) {
      validate(fields()[2], value);
      this.electroDermalActivity = value;
      fieldSetFlags()[2] = true;
      return this;
    }

    /**
      * Checks whether the 'electroDermalActivity' field has been set.
      * electrodermal activity (µS)
      * @return True if the 'electroDermalActivity' field has been set, false otherwise.
      */
    public boolean hasElectroDermalActivity() {
      return fieldSetFlags()[2];
    }


    /**
      * Clears the value of the 'electroDermalActivity' field.
      * electrodermal activity (µS)
      * @return This builder.
      */
    public org.radarcns.empatica.EmpaticaE4ElectroDermalActivity.Builder clearElectroDermalActivity() {
      fieldSetFlags()[2] = false;
      return this;
    }

    @Override
    public EmpaticaE4ElectroDermalActivity build() {
      try {
        EmpaticaE4ElectroDermalActivity record = new EmpaticaE4ElectroDermalActivity();
        record.time = fieldSetFlags()[0] ? this.time : (java.lang.Double) defaultValue(fields()[0]);
        record.timeReceived = fieldSetFlags()[1] ? this.timeReceived : (java.lang.Double) defaultValue(fields()[1]);
        record.electroDermalActivity = fieldSetFlags()[2] ? this.electroDermalActivity : (java.lang.Float) defaultValue(fields()[2]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  private static final org.apache.avro.io.DatumWriter
    WRITER$ = new org.apache.avro.specific.SpecificDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  private static final org.apache.avro.io.DatumReader
    READER$ = new org.apache.avro.specific.SpecificDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

}
