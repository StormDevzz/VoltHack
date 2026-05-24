package volthack.setting

enum class AutoEnable {
    NORMAL,
    ONCE,
    ALWAYS
}

abstract class Module(
    val name: String,
    val description: String = "",
    val category: Category,
    val autoEnable: AutoEnable = AutoEnable.NORMAL
) {
    var enabled = false
    var bindKey: Int = 0

    val settings = mutableListOf<Setting<*>>()

    var onStateChanged: ((Module) -> Unit)? = null

    fun toggle() {
        if (enabled) {
            if (autoEnable == AutoEnable.ALWAYS) return
            disable()
        } else {
            enable()
        }
        onStateChanged?.invoke(this)
    }

    fun enable() {
        if (enabled) return
        enabled = true
        onEnable()
        onStateChanged?.invoke(this)
        if (this.name != "Notifications" && volthack.modules.render.Notifications.enabled && volthack.modules.render.Notifications.notifyModules) {
            volthack.util.render.NotificationManager.add(
                "Module Enabled",
                "${this.name} has been enabled",
                volthack.util.render.NotificationType.SUCCESS,
                2000L
            )
        }
    }

    fun disable() {
        if (!enabled) return
        enabled = false
        onDisable()
        onStateChanged?.invoke(this)
        if (this.name != "Notifications" && volthack.modules.render.Notifications.enabled && volthack.modules.render.Notifications.notifyModules) {
            volthack.util.render.NotificationManager.add(
                "Module Disabled",
                "${this.name} has been disabled",
                volthack.util.render.NotificationType.ERROR,
                2000L
            )
        }
    }

    protected open fun onEnable() {}
    protected open fun onDisable() {}

    protected fun boolean(
        name: String,
        default: kotlin.Boolean = false,
        description: String = ""
    ) = Setting.Boolean(name, description, default).also { settings.add(it) }

    protected fun float(
        name: String,
        default: kotlin.Float = 0f,
        min: kotlin.Float = 0f,
        max: kotlin.Float = 1f,
        step: kotlin.Float = 0.1f,
        description: String = ""
    ) = Setting.Float(name, description, default, min, max, step).also { settings.add(it) }

    protected fun int(
        name: String,
        default: kotlin.Int = 0,
        min: kotlin.Int = 0,
        max: kotlin.Int = 10,
        description: String = ""
    ) = Setting.Int(name, description, default, min, max).also { settings.add(it) }

    protected fun mode(
        name: String,
        modes: List<String>,
        default: String = modes.firstOrNull() ?: "",
        description: String = ""
    ) = Setting.Mode(name, description, default, modes).also { settings.add(it) }

    protected fun color(
        name: String,
        default: kotlin.Int = 0xFFFFFFFF.toInt(),
        description: String = ""
    ) = Setting.Color(name, description, default).also { settings.add(it) }

    protected fun text(
        name: String,
        default: kotlin.String = "",
        description: String = ""
    ) = Setting.StringSetting(name, description, default).also { settings.add(it) }
}

enum class Category(val displayName: String) {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    RENDER("Render"),
    PLAYER("Player"),
    MISC("Misc"),
    WORLD("World"),
    CONFIGS("Configs")
}
