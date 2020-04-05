class Main {
    fun run() {
        val reactiveMongoDriver = MongoDriver(DATABASE_NAME)
        val turnstileServerServer = TurnstileServer(reactiveMongoDriver)
        val managerServer = ManagerServer(reactiveMongoDriver)
        val reportServer = ReportServer(reactiveMongoDriver.events)
        Thread(ManagerServerRunner(managerServer)).start()
        Thread(TurnstileServerRunner(turnstileServerServer)).start()
        Thread(ReportServerRunner(reportServer)).start()
    }

    class TurnstileServerRunner(private val entryServer: TurnstileServer) : Runnable {
        override fun run() {
            entryServer.run()
        }

    }

    class ManagerServerRunner(private val managerServer: ManagerServer) : Runnable {
        override fun run() {
            managerServer.run()
        }

    }

    class ReportServerRunner(private val reportServer: ReportServer) : Runnable {
        override fun run() {
            reportServer.run()
        }

    }

    companion object {
        private const val DATABASE_NAME = "fitness-center"
        fun main(args: Array<String>) {
            val main = Main()
            main.run()
        }
    }
}