package akka.remote.kamon.instrumentation.kanela.advisor

import akka.actor.{Address, AddressFromURIString}
import akka.remote.ContextAwareWireFormats.AckAndContextAwareEnvelopeContainer
import akka.remote.RemoteActorRefProvider
import akka.util.ByteString
import kamon.Kamon
import kamon.context.BinaryPropagation.ByteStreamReader
import kamon.instrumentation.akka.AkkaRemoteMetrics
import kanela.agent.libs.net.bytebuddy.asm.Advice.{Argument, OnMethodEnter}

/**
  * Advisor for akka.remote.transport.AkkaPduProtobufCodec$::decodeMessage
  */
class AkkaPduProtobufCodecDecodeMessage
object AkkaPduProtobufCodecDecodeMessage {

  @OnMethodEnter(suppress = classOf[Throwable])
  def enter(@Argument(0) bs: ByteString,
              @Argument(1) provider: RemoteActorRefProvider,
              @Argument(2) localAddress: Address): Unit = {
    val ackAndEnvelope = AckAndContextAwareEnvelopeContainer.parseFrom(bs.toArray)
    if (ackAndEnvelope.hasEnvelope && ackAndEnvelope.getEnvelope.hasTraceContext) {
      val remoteCtx = ackAndEnvelope.getEnvelope.getTraceContext

      if(remoteCtx.getContext.size() > 0) {
        val ctx = Kamon.defaultBinaryPropagation().read(
        ByteStreamReader.of(remoteCtx.getContext.toByteArray)
        )
        Kamon.store(ctx)
      }

      AkkaRemoteMetrics.recordMessageInbound(
      localAddress  = localAddress,
      senderAddress = {
        val senderPath = ackAndEnvelope.getEnvelope.getSender.getPath
        if(senderPath.isEmpty) None else Some(AddressFromURIString(senderPath))
      },
      size = ackAndEnvelope.getEnvelope.getMessage.getMessage.size()
      )
    }
  }
}
