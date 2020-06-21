package com.programmer74.katolk.client.audio

import com.programmer74.katolk.client.gui.MessageBoxes
import com.programmer74.katolk.ws.KatolkBinaryMessage
import com.programmer74.katolk.ws.KatolkBinaryMessageType
import com.programmer74.katolk.ws.WsClient
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import javax.sound.sampled.*

@Suppress("SameParameterValue")
class Audio(private val wsClient: WsClient) {

  private val ENCODING = AudioFormat.Encoding.PCM_SIGNED
  private val RATE = 16000.0f
  private val SAMPLE_SIZE = 16
  private val CHANNELS = 1
  private val IS_BIG_ENDIAN = true

  private var format_spk: AudioFormat? = null
  private var format_mic: AudioFormat? = null

  private var microphone: TargetDataLine? = null
  private var speakers: SourceDataLine? = null

  var isTalking = false
    private set
  var isListening = false
    private set

  private var talkingThread: Thread? = null
  private var listeningThread: Thread? = null

  private val incomingAudioPackets = LinkedBlockingQueue<KatolkBinaryMessage?>()

  private var micCapturedDataSize = 180

  private fun updateSpeakerParams(rate_spk: Float, sampleSize_spk: Int) {
    val wasListening = isListening
    if (isListening) stopListening()

    try {
      format_spk = AudioFormat(
          ENCODING, rate_spk, sampleSize_spk, CHANNELS,
          sampleSize_spk / 8 * CHANNELS, rate_spk, IS_BIG_ENDIAN)

      val infoSpk = DataLine.Info(SourceDataLine::class.java, format_spk)
      speakers = AudioSystem.getLine(infoSpk) as SourceDataLine

    } catch (ex: Exception) {
      format_spk = null
      ex.printStackTrace()
      MessageBoxes.showCriticalErrorAlert(ex.message!!, ex.toString())
    }

    if (wasListening) listen()
  }

  private fun updateMicrophoneParams(rate_mic: Float, sampleSize_mic: Int) {
    val wasTalking = isTalking
    if (isTalking) stopTalking()

    try {
      format_mic = AudioFormat(
          ENCODING, rate_mic, sampleSize_mic, CHANNELS,
          sampleSize_mic / 8 * CHANNELS, rate_mic, IS_BIG_ENDIAN)

      val infoMic = DataLine.Info(TargetDataLine::class.java, format_mic)
      microphone = AudioSystem.getLine(infoMic) as TargetDataLine
    } catch (ex: Exception) {
      format_mic = null
      ex.printStackTrace()
      MessageBoxes.showCriticalErrorAlert(ex.message!!, ex.toString())
    }

    if (wasTalking) talk()
  }

  init {
    updateSpeakerParams(RATE, SAMPLE_SIZE)
    updateMicrophoneParams(RATE, SAMPLE_SIZE)

    this.wsClient.binaryConsumers.add(Consumer {
      if (it.type == KatolkBinaryMessageType.PACKET_AUDIO) {
        incomingAudioPackets.put(it)
      }
    })
  }

  fun talk() {
    println("talk is called")
    if (isTalking) return
    if (format_mic == null) {
      MessageBoxes.showCriticalErrorAlert("format_mic is null", "")
      return
    }
    println("Talking initialized")
    if (talkingThread != null) return
    println("Talking began")

    try {
      microphone!!.open(format_mic)
      microphone!!.start()

    } catch (ex: Exception) {
      println("Error: $ex")
      MessageBoxes.showCriticalErrorAlert(ex.message!!, ex.toString())
      isTalking = false
      return
    }

    talkingThread = Thread(Runnable {
      var numbytesUpload = 0
      val micDataSize = micCapturedDataSize

      val data = ByteArray(micDataSize)

      while (isTalking) {
        numbytesUpload = microphone!!.read(data, 0, micDataSize)
        //        bytesUpload += numbytesUpload

        val msg = KatolkBinaryMessage(
            KatolkBinaryMessageType.PACKET_AUDIO,
            data)
        wsClient.send(msg)
        //System.out.println("Talking!");
      }
      println("Talking thread stopped")
    })
    isTalking = true
    talkingThread!!.start()

    println("Talking thread started")
  }

  fun stopTalking() {
    if (!isTalking) return
    isTalking = false
    try {
      talkingThread!!.join()
    } catch (ex: Exception) {
      ex.printStackTrace()
    }

    talkingThread = null
    microphone!!.drain()
    microphone!!.close()
    println("Talking stopped")
  }

  fun listen() {
    println("listen is called")
    if (isListening) return

    if (format_spk == null) return
    println("Listening initialized")
    if (listeningThread != null) return
    println("Listening began")

    try {
      speakers!!.open(format_spk)
      isListening = true
      speakers!!.start()
    } catch (ex: Exception) {
      println("Listen: $ex")
      ex.printStackTrace()
      MessageBoxes.showCriticalErrorAlert(ex.message!!, ex.toString())
      isListening = false
    }

    listeningThread = Thread(Runnable {

      while (isListening) {
        try {
          if (!isListening) break
          val msg = incomingAudioPackets.poll(1, TimeUnit.SECONDS) ?: continue
          speakers!!.write(msg.payload, 0, msg.payload.size)

        } catch (ex: Exception) {
          System.err.println("Listening error: $ex")
          ex.printStackTrace()
        }

      }
      println("Listening thread stopped")
    })
    isListening = true
    listeningThread!!.start()
    println("Listening thread started")
  }

  fun stopListening() {
    if (!isListening) return
    isListening = false
    try {
      listeningThread!!.join()
    } catch (ex: Exception) {
      println("StopListening: $ex")
    }

    listeningThread = null
    speakers!!.drain()
    speakers!!.close()
    println("Listening stopped")
  }
}