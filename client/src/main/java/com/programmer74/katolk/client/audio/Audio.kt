package com.programmer74.katolk.client.audio

import com.programmer74.katolk.client.binary.BinaryMessage
import com.programmer74.katolk.client.binary.BinaryMessageType
import com.programmer74.katolk.client.ws.WsClient
import com.programmer74.katolk.client.gui.MessageBoxes
import java.io.ByteArrayOutputStream
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import javax.sound.sampled.*

class Audio(qualitySetup: Int, val wsClient: WsClient) {

  private val DEBUG = true

  private val encoding = AudioFormat.Encoding.PCM_SIGNED

  private val channels = 1
  private val bigEndian = true

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

//  private var bytesUpload = 0
//  private var bytesDownload = 0

  var micCapturedDataSize = 180

  var myID: Long? = 0L


  private var myQualitySetupForSpeakers = 3
  private var myQualitySetupForMic = 3

  private var newQualitySetupForSpeakers = -1
  private val newQualitySetupForMic = -1

  private val incomingAudioPackets = LinkedBlockingQueue<BinaryMessage?>()

  /*
        Quality setups:
        0 - 8kHz, 8 bit
        1 - 8kHz, 16 bit
        2 - 16kHz, 16 bit
        3 - 16kHz, 24 bit
        4 - 44.1kHz, 16 bit - DROPPED
        5 - 44.1kHz, 24 bit - DROPPED
    */
  private fun getRateByQuality(quality: Int): Float {
    when (quality) {
      0, 1 -> return 8000.0f
      2, 3 -> return 16000.0f
      //4, 5 -> return 44100.0f
    }
    return 16000.0f
  }

  private fun getSSizeByQuality(quality: Int): Int {
    when (quality) {
      0 -> return 8
      1, 2 /*, 4*/ -> return 16
      3 /*, 5*/ -> return 24
    }
    return 16
  }

  fun getMyQualitySetupForSpeakers(): Int {
    return myQualitySetupForSpeakers
  }

  fun setMyQualitySetupForSpeakers(myQualitySetupForSpeakers: Int) {
    this.myQualitySetupForSpeakers = myQualitySetupForSpeakers
    updateSpeakerParamsByQuality(myQualitySetupForSpeakers)
  }

  fun getMyQualitySetupForMic(): Int {
    return myQualitySetupForMic
  }

  fun setMyQualitySetupForMic(myQualitySetupForMic: Int) {
    this.myQualitySetupForMic = myQualitySetupForMic
    updateMicrophoneParamsByQuality(myQualitySetupForMic)
  }

  private fun updateSpeakerParams(rate_spk: Float, sampleSize_spk: Int) {
    val wasListening = isListening
    if (isListening) StopListening()

    try {
      format_spk = AudioFormat(encoding, rate_spk, sampleSize_spk, channels,
          sampleSize_spk / 8 * channels, rate_spk, bigEndian)

      val info_spk = DataLine.Info(SourceDataLine::class.java, format_spk)
      speakers = AudioSystem.getLine(info_spk) as SourceDataLine

    } catch (ex: Exception) {
      format_spk = null
      println("UpdateSpeakerParams: " + ex.toString())
      ex.printStackTrace()
      MessageBoxes.showCriticalErrorAlert(ex.message!!, ex.toString())
    }

    if (wasListening) Listen()
  }

  private fun updateSpeakerParamsByQuality(quality: Int) {
    val rate_spk = getRateByQuality(quality)
    val ssize = getSSizeByQuality(quality)
    updateSpeakerParams(rate_spk, ssize)
    println("Updated Speaker quality to $quality")
  }

  private fun updateMicrophoneParams(rate_mic: Float, sampleSize_mic: Int) {
    val wasTalking = isTalking
    if (isTalking) StopTalking()

    try {
      format_mic = AudioFormat(encoding, rate_mic, sampleSize_mic, channels,
          sampleSize_mic / 8 * channels, rate_mic, bigEndian)

      val info_mic = DataLine.Info(TargetDataLine::class.java, format_mic)
      microphone = AudioSystem.getLine(info_mic) as TargetDataLine
    } catch (ex: Exception) {
      format_mic = null
      println("UpdateMicrophoneParams: " + ex.toString())
      ex.printStackTrace()
      MessageBoxes.showCriticalErrorAlert(ex.message!!, ex.toString())
    }

    if (wasTalking) Talk()
  }

  private fun updateMicrophoneParamsByQuality(quality: Int) {
    val rate_mic = getRateByQuality(quality)
    val ssize = getSSizeByQuality(quality)
    updateMicrophoneParams(rate_mic, ssize)
    println("Updated Mic quality to $quality")
  }

  init {
    this.myQualitySetupForSpeakers = qualitySetup
    this.myQualitySetupForMic = qualitySetup
    updateSpeakerParamsByQuality(myQualitySetupForSpeakers)
    updateMicrophoneParamsByQuality(myQualitySetupForMic)

    this.wsClient.binaryConsumers.add(Consumer {
      if (it.type == BinaryMessageType.PACKET_AUDIO) {
        incomingAudioPackets.put(it)
      }
    })

    val spkQualityThread = Thread(Runnable {
      while (true) {
        if (newQualitySetupForSpeakers != -1) {
          if (newQualitySetupForSpeakers != myQualitySetupForSpeakers) {
            updateSpeakerParamsByQuality(newQualitySetupForSpeakers)
            myQualitySetupForSpeakers = newQualitySetupForSpeakers
            newQualitySetupForSpeakers = -1
          }
        }
        Thread.yield()
      }
    })
    spkQualityThread.start()
  }

  fun Talk() {
    println("talk is called")
    if (isTalking) return
    if (format_mic == null) {
      MessageBoxes.showCriticalErrorAlert("format_mic is null", "")
      return
    }
    if (DEBUG) println("Talking initialized")
    if (talkingThread != null) return
    if (DEBUG) println("Talking began")

    val out = ByteArrayOutputStream()

    try {
      microphone!!.open(format_mic)
      microphone!!.start()

    } catch (ex: Exception) {
      println("Error: " + ex.toString())
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

        val msg = BinaryMessage(BinaryMessageType.PACKET_AUDIO, data)
        wsClient.send(msg)
        //System.out.println("Talking!");
      }
      println("Talking thread stopped")
    })
    isTalking = true
    talkingThread!!.start()

    if (DEBUG) println("Talking thread started")
  }

  fun StopTalking() {
    if (isTalking == false) return
    isTalking = false
    try {
      talkingThread!!.join()
    } catch (ex: Exception) {
      ex.printStackTrace()
    }

    talkingThread = null
    microphone!!.drain()
    microphone!!.close()
    if (DEBUG) println("Talking stopped")
  }

  fun Listen() {
    println("listen is called")
    if (isListening) return

    if (format_spk == null) return
    if (DEBUG) println("Listening initialized")
    if (listeningThread != null) return
    if (DEBUG) println("Listening began")

    try {
      speakers!!.open(format_spk)
      isListening = true
      speakers!!.start()
    } catch (ex: Exception) {
      if (DEBUG) {
        println("Listen: " + ex.toString())
      }
      ex.printStackTrace()
      MessageBoxes.showCriticalErrorAlert(ex.message!!, ex.toString())
      isListening = false
    }

    listeningThread = Thread(Runnable {

      while (isListening) {
        try {
          if (isListening == false) break
          val msg = incomingAudioPackets.poll(1, TimeUnit.SECONDS) ?: continue
//          if (packet.getmArg() !== myQualitySetupForSpeakers) {
//            if (newQualitySetupForSpeakers == -1) {
//              println("Mismatch found!")
//              newQualitySetupForSpeakers = packet.getmArg()
//            }
//            continue
//          }
          speakers!!.write(msg.payload, 0, msg.payload.size)
//          bytesDownload += packet.getPayload().length + 3

          //if (DEBUG) System.out.println("Spk: " + packet.getPayload().length + 3);
          //System.out.println("Listening!");

        } catch (ex: Exception) {
          System.err.println("Listening error: " + ex.toString())
          ex.printStackTrace()
        }

      }
      println("Listening thread stopped")
    })
    isListening = true
    listeningThread!!.start()
    println("Listening thread started")
  }

  fun StopListening() {
    if (isListening == false) return
    isListening = false
    try {
      listeningThread!!.join()
    } catch (ex: Exception) {
      if (DEBUG) println("StopListening: " + ex.toString())
    }

    listeningThread = null
    speakers!!.drain()
    speakers!!.close()
    if (DEBUG) println("Listening stopped")
  }
}