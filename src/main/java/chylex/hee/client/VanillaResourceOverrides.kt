package chylex.hee.client

import chylex.hee.HEE
import chylex.hee.system.facades.Resource
import chylex.hee.system.migration.supply
import net.minecraft.client.Minecraft
import net.minecraft.resources.IPackFinder
import net.minecraft.resources.IResourcePack
import net.minecraft.resources.ResourcePackInfo
import net.minecraft.resources.ResourcePackInfo.IFactory
import net.minecraft.resources.ResourcePackInfo.Priority
import net.minecraft.resources.ResourcePackType
import net.minecraftforge.fml.packs.ResourcePackLoader

object VanillaResourceOverrides : IPackFinder {
	fun register() {
		// Minecraft is null when running datagen, but I cannot move this to FMLClientSetupEvent because it only runs after all resource packs are initialized
		with(Minecraft.getInstance() ?: return) {
			resourcePackList.addPackFinder(this@VanillaResourceOverrides)
		}
	}
	
	override fun <T : ResourcePackInfo> addPackInfosToMap(map: MutableMap<String, T>, factory: IFactory<T>) {
		val delegate = ResourcePackLoader.getResourcePackFor(HEE.ID).get()
		val supplier = supply<IResourcePack>(Pack(delegate))
		
		map[HEE.ID] = ResourcePackInfo.createResourcePack("HEE 2", true /* isAlwaysEnabled */, supplier, factory, Priority.TOP)!!
	}
	
	private class Pack(delegate: IResourcePack) : IResourcePack by delegate {
		override fun getName() = "Hardcore Ender Expansion 2"
		override fun isHidden() = true // minecraft doesn't remember the order across restarts anyway
		
		override fun getResourceNamespaces(type: ResourcePackType): MutableSet<String> {
			return mutableSetOf(Resource.NAMESPACE_VANILLA)
		}
	}
}
