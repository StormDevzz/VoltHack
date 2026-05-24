package volthack.gui.loading

object LoadingState {
    @Volatile var progress: Float = 0f
    @Volatile var status: String = "Initializing..."
    @Volatile var complete: Boolean = false
    @Volatile var consoleLog: MutableList<String> = mutableListOf()

    fun reset() {
        progress = 0f
        status = "Initializing..."
        complete = false
        consoleLog.clear()
    }

    fun step(progress: Float, status: String, log: String? = null) {
        this.progress = progress
        this.status = status
        if (log != null) {
            consoleLog.add(log)
        }
    }

    fun finish() {
        progress = 1f
        status = "Complete!"
        complete = true
    }
}
