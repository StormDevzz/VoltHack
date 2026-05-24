package volthack.setting

import volthack.VoltHack

object ModuleManager {
    private val modules = mutableListOf<Module>()

    fun register(vararg modules: Module) {
        modules.forEach { this.modules.add(it) }
    }

    fun get(name: String): Module? =
        modules.find { it.name.equals(name, ignoreCase = true) }

    fun getAll(): List<Module> = modules.toList()

    fun getByCategory(category: Category): List<Module> =
        modules.filter { it.category == category }

    fun loadConfigs() {
        for (module in modules) {
            val data = ModuleConfig.load(module.name)

            when (module.autoEnable) {
                AutoEnable.ALWAYS -> {
                    module.enabled = true
                    VoltHack.LOGGER.info("  [${module.name}] always enabled")
                }
                AutoEnable.ONCE -> {
                    if (ModuleConfig.isFirstLaunch) {
                        module.enabled = true
                        VoltHack.LOGGER.info("  [${module.name}] auto-enabled (first launch)")
                    } else {
                        module.enabled = data.enabled
                        VoltHack.LOGGER.info("  [${module.name}] restored: ${data.enabled}")
                    }
                }
                AutoEnable.NORMAL -> {
                    module.enabled = data.enabled
                    VoltHack.LOGGER.info("  [${module.name}] restored: ${data.enabled}")
                }
            }

            if (ModuleConfig.isFirstLaunch && module.autoEnable != AutoEnable.ALWAYS) {
                ModuleConfig.save(module.name, ModuleConfig.ModuleData(enabled = module.enabled))
            }

            module.onStateChanged = { mod ->
                if (mod.autoEnable != AutoEnable.ALWAYS) {
                    ModuleConfig.save(mod.name, ModuleConfig.ModuleData(enabled = mod.enabled))
                }
            }
        }
    }
}
