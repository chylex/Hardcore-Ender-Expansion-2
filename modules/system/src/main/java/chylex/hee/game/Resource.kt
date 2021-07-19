package chylex.hee.game

import chylex.hee.HEE
import chylex.hee.system.isVanilla
import chylex.hee.system.path
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.util.IItemProvider
import net.minecraft.util.ResourceLocation

object Resource {
	const val NAMESPACE_VANILLA = "minecraft"
	
	fun Vanilla(path: String) = ResourceLocation(NAMESPACE_VANILLA, path)
	fun Custom(path: String)  = ResourceLocation(HEE.ID, path)
	
	fun isVanilla(location: ResourceLocation) = location.namespace == NAMESPACE_VANILLA
	fun isCustom(location: ResourceLocation)  = location.namespace == HEE.ID
	
	operator fun invoke(path: String, isVanilla: Boolean): ResourceLocation {
		return if (isVanilla) Vanilla(path) else Custom(path)
	}
	
	val IItemProvider.locationPrefix
		get() = when (this) {
			is Block -> "block/"
			is Item  -> "item/"
			else     -> throw IllegalArgumentException()
		}
	
	val IItemProvider.location
		get() = when (this) {
			is Block -> Resource(this.locationPrefix + this.path, this.isVanilla)
			is Item  -> Resource(this.locationPrefix + this.path, this.isVanilla)
			else     -> throw IllegalArgumentException()
		}
	
	fun IItemProvider.location(suffix: String): ResourceLocation {
		return when (this) {
			is Block -> Resource(this.locationPrefix + this.path + suffix, this.isVanilla)
			is Item  -> Resource(this.locationPrefix + this.path + suffix, this.isVanilla)
			else     -> throw IllegalArgumentException()
		}
	}
}
