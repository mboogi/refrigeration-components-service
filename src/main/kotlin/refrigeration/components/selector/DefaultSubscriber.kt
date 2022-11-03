package refrigeration.components.selector

import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import org.slf4j.Logger

class DefaultSubscriber<T>(val name: String, private val logger: Logger) : Subscriber<T> {
    override fun onSubscribe(s: Subscription) {
        s.request(Long.MAX_VALUE)
    }

    override fun onError(t: Throwable) {
        logger.error("[$name] Error ${t.stackTrace}")
        t.printStackTrace()
    }

    override fun onComplete() {
        logger.info("[$name] completed")
    }

    override fun onNext(t: T) {
        logger.info("[$name] received: $t running on thread : ${Thread.currentThread().name}")
    }
}
