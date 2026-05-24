package volthack.modules.world

import volthack.setting.Category
import volthack.setting.Module

object AutoFarm : Module("AutoFarm", "Automatically breaks mature crops", Category.WORLD) {
    private val range by float("Range", 4.5f, 1f, 6f, 0.5f)
    private val replant by boolean("Replant", true)
}
