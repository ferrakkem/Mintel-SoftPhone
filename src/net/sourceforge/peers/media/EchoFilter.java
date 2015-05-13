package net.sourceforge.peers.media;


public class EchoFilter extends SoundFilter {

  private short[] delayBuffer;

  private int delayBufferPos;

  private float decay;

  /**
   * Creates an EchoFilter with the specified number of delay samples and the
   * specified decay rate.
   * <p>
   * The number of delay samples specifies how long before the echo is
   * initially heard. For a 1 second echo with mono, 44100Hz sound, use 44100
   * delay samples.
   * <p>
   * The decay value is how much the echo has decayed from the source. A decay
   * value of .5 means the echo heard is half as loud as the source.
   */
  public EchoFilter(int numDelaySamples, float decay) {
    delayBuffer = new short[numDelaySamples];
    this.decay = decay;
  }

  /**
   * Gets the remaining size, in bytes, of samples that this filter can echo
   * after the sound is done playing. Ensures that the sound will have decayed
   * to below 1% of maximum volume (amplitude).
   */
  public int getRemainingSize() {
    float finalDecay = 0.01f;
    // derived from Math.pow(decay,x) <= finalDecay
    int numRemainingBuffers = (int) Math.ceil(Math.log(finalDecay)
        / Math.log(decay));
    int bufferSize = delayBuffer.length * 2;

    return bufferSize * numRemainingBuffers;
  }

  /**
   * Clears this EchoFilter's internal delay buffer.
   */
  public void reset() {
    for (int i = 0; i < delayBuffer.length; i++) {
      delayBuffer[i] = 0;
    }
    delayBufferPos = 0;
  }

  /**
   * Filters the sound samples to add an echo. The samples played are added to
   * the sound in the delay buffer multipied by the decay rate. The result is
   * then stored in the delay buffer, so multiple echoes are heard.
   */
  public void filter(byte[] samples, int offset, int length) {

    for (int i = offset; i < offset + length; i += 2) {
      // update the sample
      short oldSample = getSample(samples, i);
      short newSample = (short) (oldSample + decay
          * delayBuffer[delayBufferPos]);
      setSample(samples, i, newSample);

      // update the delay buffer
      delayBuffer[delayBufferPos] = newSample;
      delayBufferPos++;
      if (delayBufferPos == delayBuffer.length) {
        delayBufferPos = 0;
      }
    }
  }

}


/**
 * The FilteredSoundStream class is a FilterInputStream that applies a
 * SoundFilter to the underlying input stream.
 * 
 * @see SoundFilter
 */



/**
 * A abstract class designed to filter sound samples. Since SoundFilters may use
 * internal buffering of samples, a new SoundFilter object should be created for
 * every sound played. However, SoundFilters can be reused after they are
 * finished by called the reset() method.
 * <p>
 * Assumes all samples are 16-bit, signed, little-endian format.
 * 
 * @see FilteredSoundStream
 */

abstract class SoundFilter {

  /**
   * Resets this SoundFilter. Does nothing by default.
   */
  public void reset() {
    // do nothing
  }

  /**
   * Gets the remaining size, in bytes, that this filter plays after the sound
   * is finished. An example would be an echo that plays longer than it's
   * original sound. This method returns 0 by default.
   */
  public int getRemainingSize() {
    return 0;
  }

  /**
   * Filters an array of samples. Samples should be in 16-bit, signed,
   * little-endian format.
   */
  public void filter(byte[] samples) {
    filter(samples, 0, samples.length);
  }

  /**
   * Filters an array of samples. Samples should be in 16-bit, signed,
   * little-endian format. This method should be implemented by subclasses.
   */
  public abstract void filter(byte[] samples, int offset, int length);

  /**
   * Convenience method for getting a 16-bit sample from a byte array. Samples
   * should be in 16-bit, signed, little-endian format.
   */
  public static short getSample(byte[] buffer, int position) {
    return (short) (((buffer[position + 1] & 0xff) << 8) | (buffer[position] & 0xff));
  }

  /**
   * Convenience method for setting a 16-bit sample in a byte array. Samples
   * should be in 16-bit, signed, little-endian format.
   */
  public static void setSample(byte[] buffer, int position, short sample) {
    buffer[position] = (byte) (sample & 0xff);
    buffer[position + 1] = (byte) ((sample >> 8) & 0xff);
  }

}       
           
         


