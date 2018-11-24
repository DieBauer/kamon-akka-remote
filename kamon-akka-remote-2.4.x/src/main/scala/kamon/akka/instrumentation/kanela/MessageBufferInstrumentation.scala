package kamon.akka.instrumentation.kanela

import kamon.akka.instrumentation.kanela.advisor.{MessageBufferNodeConstructorAdvisor, MessageBufferNodeMethodApplyAdvisor}
import kamon.akka.instrumentation.kanela.mixin.HasTransientContextMixin
import kanela.agent.scala.KanelaInstrumentation

class MessageBufferInstrumentation extends KanelaInstrumentation with AkkaVersionedFilter {

  forTargetType("akka.util.MessageBuffer$Node") { builder ⇒
    filterAkkaVersion(builder)
      .withMixin(classOf[HasTransientContextMixin])
      .withAdvisorFor(Constructor, classOf[MessageBufferNodeConstructorAdvisor])
      .withAdvisorFor(method("apply"), classOf[MessageBufferNodeMethodApplyAdvisor])
      .build()
  }

}
