package volthack.util.player

import net.minecraft.client.Minecraft
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object ArmorUtils {
    private val player get() = Minecraft.getInstance().player

    fun getItem(slot: EquipmentSlot): ItemStack? = player?.getItemBySlot(slot)

    val helmet: ItemStack? get() = getItem(EquipmentSlot.HEAD)
    val chestplate: ItemStack? get() = getItem(EquipmentSlot.CHEST)
    val leggings: ItemStack? get() = getItem(EquipmentSlot.LEGS)
    val boots: ItemStack? get() = getItem(EquipmentSlot.FEET)

    val allPieces: List<ItemStack> get() = listOfNotNull(helmet, chestplate, leggings, boots)

    fun hasFullSet(): Boolean = allPieces.size == 4 && allPieces.all { !it.isEmpty }

    fun hasPiece(predicate: (ItemStack) -> Boolean): Boolean = allPieces.any(predicate)

    fun isWearingElytra(): Boolean = chestplate?.item == Items.ELYTRA

    fun countPieces(): Int = allPieces.count { !it.isEmpty }
}