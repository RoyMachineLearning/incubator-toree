package com.ibm.spark.kernel.protocol.v5.handler

import com.ibm.spark.kernel.protocol.v5._
import com.ibm.spark.kernel.protocol.v5.content.KernelInfoReply
import com.ibm.spark.utils.LogLike
import play.api.libs.json.Json

import scala.concurrent._

/**
 * Receives a KernelInfoRequest KernelMessage and returns a KernelInfoReply
 * KernelMessage.
 */
class KernelInfoRequestHandler(actorLoader: ActorLoader)
  extends BaseHandler(actorLoader) with LogLike
{
  def process(kernelMessage: KernelMessage): Future[_] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    future {
      logger.debug("Sending kernel info reply message")

      val kernelInfo = SparkKernelInfo
      val kernelInfoReply = KernelInfoReply(
        kernelInfo.protocolVersion,
        kernelInfo.implementation,
        kernelInfo.implementationVersion,
        kernelInfo.language,
        kernelInfo.languageVersion,
        kernelInfo.banner
      )

      val replyHeader = Header(
        java.util.UUID.randomUUID.toString,
        "",
        java.util.UUID.randomUUID.toString,
        MessageType.KernelInfoReply.toString,
        kernelInfo.protocolVersion
      )

      val kernelResponseMessage = new KernelMessage(
        kernelMessage.ids,
        "",
        replyHeader,
        kernelMessage.header,
        Metadata(),
        Json.toJson(kernelInfoReply).toString
      )

      actorLoader.load(SystemActorType.KernelMessageRelay) ! kernelResponseMessage
    }
  }
}