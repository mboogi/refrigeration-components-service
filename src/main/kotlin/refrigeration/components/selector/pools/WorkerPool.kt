package refrigeration.components.selector.pools

import org.springframework.stereotype.Service
import java.util.concurrent.Executor
import java.util.concurrent.Executors
@Service
class WorkerPool : Executor {

    private val executor = Executors.newFixedThreadPool(60)
    override fun execute(command: Runnable) {
        executor.execute(command)
    }
}
