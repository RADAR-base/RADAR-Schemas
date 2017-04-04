/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package org.radarcns.biovotion;

import org.apache.avro.specific.SpecificData;

@SuppressWarnings("all")
/** Heart rate calculated by biovotion device. */
@org.apache.avro.specific.AvroGenerated
public class BiovotionVSMHeartRate extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = -6937998090250118957L;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"BiovotionVSMHeartRate\",\"namespace\":\"org.radarcns.biovotion\",\"doc\":\"Heart rate calculated by biovotion device.\",\"fields\":[{\"name\":\"time\",\"type\":\"double\",\"doc\":\"device timestamp in UTC (s)\"},{\"name\":\"timeReceived\",\"type\":\"double\",\"doc\":\"device receiver timestamp in UTC (s)\"},{\"name\":\"heartRate\",\"type\":\"float\",\"doc\":\"Heart rate value (bpm)\"},{\"name\":\"heartRateQuality\",\"type\":\"float\",\"doc\":\"Heart rate quality (0-1)\"}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  /** device timestamp in UTC (s) */
  @Deprecated public double time;
  /** device receiver timestamp in UTC (s) */
  @Deprecated public double timeReceived;
  /** Heart rate value (bpm) */
  @Deprecated public float heartRate;
  /** Heart rate quality (0-1) */
  @Deprecated public float heartRateQuality;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public BiovotionVSMHeartRate() {}

  /**
   * All-args constructor.
   * @param time device timestamp in UTC (s)
   * @param timeReceived device receiver timestamp in UTC (s)
   * @param heartRate Heart rate value (bpm)
   * @param heartRateQuality Heart rate quality (0-1)
   */
  public BiovotionVSMHeartRate(java.lang.Double time, java.lang.Double timeReceived, java.lang.Float heartRate, java.lang.Float heartRateQuality) {
    this.time = time;
    this.timeReceived = timeReceived;
    this.heartRate = heartRate;
    this.heartRateQuality = heartRateQuality;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return time;
    case 1: return timeReceived;
    case 2: return heartRate;
    case 3: return heartRateQuality;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  // Used by DatumReader.  Applications should not call.
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: time = (java.lang.Double)value$; break;
    case 1: timeReceived = (java.lang.Double)value$; break;
    case 2: heartRate = (java.lang.Float)value$; break;
    case 3: heartRateQuality = (java.lang.Float)value$; break;
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
   * Gets the value of the 'heartRate' field.
   * @return Heart rate value (bpm)
   */
  public java.lang.Float getHeartRate() {
    return heartRate;
  }

  /**
   * Sets the value of the 'heartRate' field.
   * Heart rate value (bpm)
   * @param value the value to set.
   */
  public void setHeartRate(java.lang.Float value) {
    this.heartRate = value;
  }

  /**
   * Gets the value of the 'heartRateQuality' field.
   * @return Heart rate quality (0-1)
   */
  public java.lang.Float getHeartRateQuality() {
    return heartRateQuality;
  }

  /**
   * Sets the value of the 'heartRateQuality' field.
   * Heart rate quality (0-1)
   * @param value the value to set.
   */
  public void setHeartRateQuality(java.lang.Float value) {
    this.heartRateQuality = value;
  }

  /**
   * Creates a new BiovotionVSMHeartRate RecordBuilder.
   * @return A new BiovotionVSMHeartRate RecordBuilder
   */
  public static org.radarcns.biovotion.BiovotionVSMHeartRate.Builder newBuilder() {
    return new org.radarcns.biovotion.BiovotionVSMHeartRate.Builder();
  }

  /**
   * Creates a new BiovotionVSMHeartRate RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new BiovotionVSMHeartRate RecordBuilder
   */
  public static org.radarcns.biovotion.BiovotionVSMHeartRate.Builder newBuilder(org.radarcns.biovotion.BiovotionVSMHeartRate.Builder other) {
    return new org.radarcns.biovotion.BiovotionVSMHeartRate.Builder(other);
  }

  /**
   * Creates a new BiovotionVSMHeartRate RecordBuilder by copying an existing BiovotionVSMHeartRate instance.
   * @param other The existing instance to copy.
   * @return A new BiovotionVSMHeartRate RecordBuilder
   */
  public static org.radarcns.biovotion.BiovotionVSMHeartRate.Builder newBuilder(org.radarcns.biovotion.BiovotionVSMHeartRate other) {
    return new org.radarcns.biovotion.BiovotionVSMHeartRate.Builder(other);
  }

  /**
   * RecordBuilder for BiovotionVSMHeartRate instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<BiovotionVSMHeartRate>
    implements org.apache.avro.data.RecordBuilder<BiovotionVSMHeartRate> {

    /** device timestamp in UTC (s) */
    private double time;
    /** device receiver timestamp in UTC (s) */
    private double timeReceived;
    /** Heart rate value (bpm) */
    private float heartRate;
    /** Heart rate quality (0-1) */
    private float heartRateQuality;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(org.radarcns.biovotion.BiovotionVSMHeartRate.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.time)) {
        this.time = data().deepCopy(fields()[0].schema(), other.time);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.timeReceived)) {
        this.timeReceived = data().deepCopy(fields()[1].schema(), other.timeReceived);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.heartRate)) {
        this.heartRate = data().deepCopy(fields()[2].schema(), other.heartRate);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.heartRateQuality)) {
        this.heartRateQuality = data().deepCopy(fields()[3].schema(), other.heartRateQuality);
        fieldSetFlags()[3] = true;
      }
    }

    /**
     * Creates a Builder by copying an existing BiovotionVSMHeartRate instance
     * @param other The existing instance to copy.
     */
    private Builder(org.radarcns.biovotion.BiovotionVSMHeartRate other) {
            super(SCHEMA$);
      if (isValidValue(fields()[0], other.time)) {
        this.time = data().deepCopy(fields()[0].schema(), other.time);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.timeReceived)) {
        this.timeReceived = data().deepCopy(fields()[1].schema(), other.timeReceived);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.heartRate)) {
        this.heartRate = data().deepCopy(fields()[2].schema(), other.heartRate);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.heartRateQuality)) {
        this.heartRateQuality = data().deepCopy(fields()[3].schema(), other.heartRateQuality);
        fieldSetFlags()[3] = true;
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
    public org.radarcns.biovotion.BiovotionVSMHeartRate.Builder setTime(double value) {
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
    public org.radarcns.biovotion.BiovotionVSMHeartRate.Builder clearTime() {
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
    public org.radarcns.biovotion.BiovotionVSMHeartRate.Builder setTimeReceived(double value) {
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
    public org.radarcns.biovotion.BiovotionVSMHeartRate.Builder clearTimeReceived() {
      fieldSetFlags()[1] = false;
      return this;
    }

    /**
      * Gets the value of the 'heartRate' field.
      * Heart rate value (bpm)
      * @return The value.
      */
    public java.lang.Float getHeartRate() {
      return heartRate;
    }

    /**
      * Sets the value of the 'heartRate' field.
      * Heart rate value (bpm)
      * @param value The value of 'heartRate'.
      * @return This builder.
      */
    public org.radarcns.biovotion.BiovotionVSMHeartRate.Builder setHeartRate(float value) {
      validate(fields()[2], value);
      this.heartRate = value;
      fieldSetFlags()[2] = true;
      return this;
    }

    /**
      * Checks whether the 'heartRate' field has been set.
      * Heart rate value (bpm)
      * @return True if the 'heartRate' field has been set, false otherwise.
      */
    public boolean hasHeartRate() {
      return fieldSetFlags()[2];
    }


    /**
      * Clears the value of the 'heartRate' field.
      * Heart rate value (bpm)
      * @return This builder.
      */
    public org.radarcns.biovotion.BiovotionVSMHeartRate.Builder clearHeartRate() {
      fieldSetFlags()[2] = false;
      return this;
    }

    /**
      * Gets the value of the 'heartRateQuality' field.
      * Heart rate quality (0-1)
      * @return The value.
      */
    public java.lang.Float getHeartRateQuality() {
      return heartRateQuality;
    }

    /**
      * Sets the value of the 'heartRateQuality' field.
      * Heart rate quality (0-1)
      * @param value The value of 'heartRateQuality'.
      * @return This builder.
      */
    public org.radarcns.biovotion.BiovotionVSMHeartRate.Builder setHeartRateQuality(float value) {
      validate(fields()[3], value);
      this.heartRateQuality = value;
      fieldSetFlags()[3] = true;
      return this;
    }

    /**
      * Checks whether the 'heartRateQuality' field has been set.
      * Heart rate quality (0-1)
      * @return True if the 'heartRateQuality' field has been set, false otherwise.
      */
    public boolean hasHeartRateQuality() {
      return fieldSetFlags()[3];
    }


    /**
      * Clears the value of the 'heartRateQuality' field.
      * Heart rate quality (0-1)
      * @return This builder.
      */
    public org.radarcns.biovotion.BiovotionVSMHeartRate.Builder clearHeartRateQuality() {
      fieldSetFlags()[3] = false;
      return this;
    }

    @Override
    public BiovotionVSMHeartRate build() {
      try {
        BiovotionVSMHeartRate record = new BiovotionVSMHeartRate();
        record.time = fieldSetFlags()[0] ? this.time : (java.lang.Double) defaultValue(fields()[0]);
        record.timeReceived = fieldSetFlags()[1] ? this.timeReceived : (java.lang.Double) defaultValue(fields()[1]);
        record.heartRate = fieldSetFlags()[2] ? this.heartRate : (java.lang.Float) defaultValue(fields()[2]);
        record.heartRateQuality = fieldSetFlags()[3] ? this.heartRateQuality : (java.lang.Float) defaultValue(fields()[3]);
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
