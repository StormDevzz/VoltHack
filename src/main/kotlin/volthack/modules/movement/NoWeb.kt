package volthack.modules.movement

import volthack.setting.Category
import volthack.setting.Module

/**
 * NoWeb – removes cobweb slowdown.
 * The actual speed restoration is done via MixinNoWeb which intercepts
 * Entity#makeStuckInBlock and cancels it when this module is enabled.
 */
object NoWeb : Module("NoWeb", "Removes slowdown in cobwebs", Category.MOVEMENT)
