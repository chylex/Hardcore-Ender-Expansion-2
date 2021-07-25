package chylex.hee.init

import chylex.hee.HEE
import chylex.hee.client.gui.screen.GuiAmuletOfRecovery
import chylex.hee.client.gui.screen.GuiBrewingStandCustom
import chylex.hee.client.gui.screen.GuiLootChest
import chylex.hee.client.gui.screen.GuiPortalTokenStorage
import chylex.hee.client.gui.screen.GuiShulkerBox
import chylex.hee.client.gui.screen.GuiTrinketPouch
import chylex.hee.client.render.EndDimensionRenderInfo
import chylex.hee.client.render.block.RenderTileDarkChest
import chylex.hee.client.render.block.RenderTileEndPortal
import chylex.hee.client.render.block.RenderTileExperienceGate
import chylex.hee.client.render.block.RenderTileIgneousPlate
import chylex.hee.client.render.block.RenderTileJarODust
import chylex.hee.client.render.block.RenderTileLootChest
import chylex.hee.client.render.block.RenderTileMinersBurialAltar
import chylex.hee.client.render.block.RenderTileShulkerBox
import chylex.hee.client.render.block.RenderTileSpawner
import chylex.hee.client.render.block.RenderTileTable
import chylex.hee.client.render.block.RenderTileTablePedestal
import chylex.hee.client.render.block.RenderTileVoidPortal
import chylex.hee.client.render.entity.RenderEntityBossEnderEye
import chylex.hee.client.render.entity.RenderEntityItem
import chylex.hee.client.render.entity.RenderEntityItemNoBob
import chylex.hee.client.render.entity.RenderEntityMobAbstractEnderman
import chylex.hee.client.render.entity.RenderEntityMobAngryEnderman
import chylex.hee.client.render.entity.RenderEntityMobBlobby
import chylex.hee.client.render.entity.RenderEntityMobSpiderling
import chylex.hee.client.render.entity.RenderEntityMobUndread
import chylex.hee.client.render.entity.RenderEntityMobVampireBat
import chylex.hee.client.render.entity.RenderEntityMobVillagerDying
import chylex.hee.client.render.entity.RenderEntityNothing
import chylex.hee.client.render.entity.RenderEntityProjectileEyeOfEnder
import chylex.hee.client.render.entity.RenderEntitySprite
import chylex.hee.client.render.entity.RenderEntityTerritoryLightningBolt
import chylex.hee.client.render.entity.RenderEntityTokenHolder
import chylex.hee.client.render.item.RenderItemTileEntitySimple
import chylex.hee.game.Resource
import chylex.hee.game.block.IHeeBlock
import chylex.hee.game.block.entity.TileEntityDarkChest
import chylex.hee.game.block.entity.TileEntityLootChest
import chylex.hee.game.block.fluid.FluidEnderGoo
import chylex.hee.game.block.fluid.FluidEnderGooPurified
import chylex.hee.game.block.properties.BlockRenderLayer.CUTOUT
import chylex.hee.game.block.properties.BlockRenderLayer.CUTOUT_MIPPED
import chylex.hee.game.block.properties.BlockRenderLayer.TRANSLUCENT
import chylex.hee.game.block.properties.CustomSkull
import chylex.hee.game.item.IHeeItem
import chylex.hee.system.getRegistryEntries
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.SubscribeAllEvents
import chylex.hee.util.forge.SubscribeEvent
import chylex.hee.util.lang.ObjectConstructors
import net.minecraft.block.Block
import net.minecraft.client.gui.ScreenManager
import net.minecraft.client.gui.ScreenManager.IScreenFactory
import net.minecraft.client.gui.screen.inventory.ContainerScreen
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.RenderTypeLookup
import net.minecraft.client.renderer.entity.EndermiteRenderer
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererManager
import net.minecraft.client.renderer.entity.FallingBlockRenderer
import net.minecraft.client.renderer.entity.SilverfishRenderer
import net.minecraft.client.renderer.entity.TNTRenderer
import net.minecraft.client.renderer.entity.model.GenericHeadModel
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer
import net.minecraft.client.renderer.tileentity.SkullTileEntityRenderer
import net.minecraft.client.renderer.tileentity.TileEntityRenderer
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.IRendersAsItem
import net.minecraft.inventory.container.Container
import net.minecraft.inventory.container.ContainerType
import net.minecraft.item.Item
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityType
import net.minecraftforge.client.event.ColorHandlerEvent
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.client.registry.IRenderFactory
import net.minecraftforge.fml.client.registry.RenderingRegistry
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import java.util.concurrent.Callable

@SubscribeAllEvents(Side.CLIENT, modid = HEE.ID, bus = MOD)
object ModRendering {
	val RENDER_ITEM_DARK_CHEST = callable(RenderItemTileEntitySimple(TileEntityDarkChest()))
	val RENDER_ITEM_JAR_O_DUST = callable(RenderTileJarODust.AsItem)
	val RENDER_ITEM_LOOT_CHEST = callable(RenderItemTileEntitySimple(TileEntityLootChest()))
	
	@SubscribeEvent
	@Suppress("unused", "UNUSED_PARAMETER")
	fun onRegisterRenderers(e: FMLClientSetupEvent) {
		
		// dimension
		
		EndDimensionRenderInfo.register()
		
		// blocks
		
		for (block in getRegistryEntries<Block>(ModBlocks)) {
			when ((block as? IHeeBlock)?.renderLayer) {
				CUTOUT        -> RenderTypeLookup.setRenderLayer(block, RenderType.getCutout())
				CUTOUT_MIPPED -> RenderTypeLookup.setRenderLayer(block, RenderType.getCutoutMipped())
				TRANSLUCENT   -> RenderTypeLookup.setRenderLayer(block, RenderType.getTranslucent())
				else          -> continue
			}
		}
		
		// fluids
		
		RenderTypeLookup.setRenderLayer(FluidEnderGoo.still, RenderType.getSolid())
		RenderTypeLookup.setRenderLayer(FluidEnderGoo.flowing, RenderType.getSolid())
		
		RenderTypeLookup.setRenderLayer(FluidEnderGooPurified.still, RenderType.getSolid())
		RenderTypeLookup.setRenderLayer(FluidEnderGooPurified.flowing, RenderType.getSolid()) // UPDATE should be translucent but it's not rendering
		
		// properties
		
		for (block in getRegistryEntries<Block>(ModBlocks)) {
			registerProperties(block.asItem())
		}
		
		for (item in getRegistryEntries<Item>(ModItems)) {
			registerProperties(item)
		}
		
		// screens
		
		val shulkerBox = ::GuiShulkerBox
		
		registerScreen(ModContainers.AMULET_OF_RECOVERY, ::GuiAmuletOfRecovery)
		registerScreen(ModContainers.BREWING_STAND, ::GuiBrewingStandCustom)
		registerScreen(ModContainers.LOOT_CHEST, ::GuiLootChest)
		registerScreen(ModContainers.PORTAL_TOKEN_STORAGE, ::GuiPortalTokenStorage)
		registerScreen(ModContainers.SHULKER_BOX, shulkerBox)
		registerScreen(ModContainers.SHULKER_BOX_IN_INVENTORY, shulkerBox)
		registerScreen(ModContainers.TRINKET_POUCH, ::GuiTrinketPouch)
		
		// entities
		
		ModEntities.ANGRY_ENDERMAN.render(RenderEntityMobAngryEnderman::class.java)
		ModEntities.BLOBBY.render(RenderEntityMobBlobby::class.java)
		ModEntities.CAUSATUM_EVENT.render(RenderEntityNothing::class.java)
		ModEntities.ENDERMAN.render(RenderEntityMobAbstractEnderman::class.java)
		ModEntities.ENDERMAN_MUPPET.render(RenderEntityMobAbstractEnderman::class.java)
		ModEntities.ENDERMITE.render(EndermiteRenderer::class.java)
		ModEntities.ENDERMITE_INSTABILITY.render(EndermiteRenderer::class.java)
		ModEntities.ENDER_EYE.render(RenderEntityBossEnderEye::class.java)
		ModEntities.ENDER_PEARL.renderSprite()
		ModEntities.EXPERIENCE_BOTTLE.renderSprite()
		ModEntities.EYE_OF_ENDER.render(RenderEntityProjectileEyeOfEnder::class.java)
		ModEntities.FALLING_BLOCK_HEAVY.render(FallingBlockRenderer::class.java)
		ModEntities.FALLING_OBSIDIAN.render(FallingBlockRenderer::class.java)
		ModEntities.IGNEOUS_PLATE_LOGIC.render(RenderEntityNothing::class.java)
		ModEntities.INFUSED_TNT.render(TNTRenderer::class.java)
		ModEntities.ITEM_CAULDRON_TRIGGER.render(RenderEntityItem::class.java)
		ModEntities.ITEM_FRESHLY_COOKED.render(RenderEntityItem::class.java)
		ModEntities.ITEM_IGNEOUS_ROCK.render(RenderEntityItemNoBob::class.java)
		ModEntities.ITEM_NO_BOB.render(RenderEntityItem::class.java)
		ModEntities.ITEM_REVITALIZATION_SUBSTANCE.render(RenderEntityItem::class.java)
		ModEntities.SILVERFISH.render(SilverfishRenderer::class.java)
		ModEntities.SPATIAL_DASH.render(RenderEntityNothing::class.java)
		ModEntities.SPIDERLING.render(RenderEntityMobSpiderling::class.java)
		ModEntities.TECHNICAL_PUZZLE.render(RenderEntityNothing::class.java)
		ModEntities.TECHNICAL_TRIGGER.render(RenderEntityNothing::class.java)
		ModEntities.TERRITORY_LIGHTNING_BOLT.render(RenderEntityTerritoryLightningBolt::class.java)
		ModEntities.TOKEN_HOLDER.render(RenderEntityTokenHolder::class.java)
		ModEntities.UNDREAD.render(RenderEntityMobUndread::class.java)
		ModEntities.VAMPIRE_BAT.render(RenderEntityMobVampireBat::class.java)
		ModEntities.VILLAGER_DYING.render(RenderEntityMobVillagerDying::class.java)
		
		// tile entities
		
		ModTileEntities.SPAWNER_OBSIDIAN_TOWER.render(RenderTileSpawner::class.java)
		ModTileEntities.ACCUMULATION_TABLE.render(RenderTileTable::class.java)
		ModTileEntities.EXPERIENCE_TABLE.render(RenderTileTable::class.java)
		ModTileEntities.INFUSION_TABLE.render(RenderTileTable::class.java)
		ModTileEntities.DARK_CHEST.render(RenderTileDarkChest::class.java)
		ModTileEntities.EXPERIENCE_GATE.render(RenderTileExperienceGate::class.java)
		ModTileEntities.IGNEOUS_PLATE.render(RenderTileIgneousPlate::class.java)
		ModTileEntities.JAR_O_DUST.render(RenderTileJarODust::class.java)
		ModTileEntities.LOOT_CHEST.render(RenderTileLootChest::class.java)
		ModTileEntities.MINERS_BURIAL_ALTAR.render(RenderTileMinersBurialAltar::class.java)
		ModTileEntities.END_PORTAL_INNER.render(RenderTileEndPortal::class.java)
		ModTileEntities.VOID_PORTAL_INNER.render(RenderTileVoidPortal::class.java)
		ModTileEntities.SHULKER_BOX.render(RenderTileShulkerBox::class.java)
		ModTileEntities.TABLE_PEDESTAL.render(RenderTileTablePedestal::class.java)
		
		// miscellaneous
		
		SkullTileEntityRenderer.MODELS[CustomSkull.Enderman] = GenericHeadModel(0, 0, 64, 32)
		SkullTileEntityRenderer.SKINS[CustomSkull.Enderman] = Resource.Custom("textures/entity/enderman_head.png")
	}
	
	@SubscribeEvent
	fun onRegisterBlockItemColors(e: ColorHandlerEvent.Item) {
		val blockColors = e.blockColors
		val itemColors = e.itemColors
		
		for (block in getRegistryEntries<Block>(ModBlocks)) {
			val blockTint = (block as? IHeeBlock)?.tint
			if (blockTint != null) {
				blockColors.register(blockTint, block)
				blockTint.forItem(block)?.let { itemColors.register(it, block) }
			}
		}
		
		for (item in getRegistryEntries<Item>(ModItems)) {
			val itemTint = (item as? IHeeItem)?.tint
			if (itemTint != null) {
				itemColors.register(itemTint, item)
			}
		}
	}
	
	// Utilities
	
	private fun <T : ItemStackTileEntityRenderer> callable(obj: T) = Callable<ItemStackTileEntityRenderer> { obj }
	
	private fun registerProperties(item: Item) {
		val properties = (item as? IHeeItem)?.properties
		if (!properties.isNullOrEmpty()) {
			for (property in properties) {
				property.register(item)
			}
		}
	}
	
	private inline fun <reified T : ContainerScreen<U>, U : Container> registerScreen(type: ContainerType<out U>, constructor: IScreenFactory<U, T>) {
		ScreenManager.registerFactory(type, constructor)
	}
	
	@Suppress("UNCHECKED_CAST")
	private inline fun <reified T : Entity, reified U : EntityRenderer<in T>> registerEntity(type: EntityType<out T>) {
		val handle = ObjectConstructors.generic<U, EntityRenderer<in T>, IRenderFactory<T>>("createRenderFor", EntityRendererManager::class.java)
		RenderingRegistry.registerEntityRenderingHandler(type, handle.invokeExact() as IRenderFactory<T>)
	}
	
	@Suppress("UNCHECKED_CAST")
	private fun <T : Entity> EntityType<T>.render(renderer: Class<out EntityRenderer<in T>>) {
		val handle = ObjectConstructors.generic(renderer, EntityRenderer::class.java, IRenderFactory::class.java, "createRenderFor", EntityRendererManager::class.java)
		RenderingRegistry.registerEntityRenderingHandler(this, handle.invokeExact() as IRenderFactory<T>)
	}
	
	@Suppress("UNCHECKED_CAST")
	private inline fun <reified T> EntityType<T>.renderSprite() where T : Entity, T : IRendersAsItem {
		registerEntity<T, RenderEntitySprite<T>>(this)
	}
	
	private fun <T : TileEntity> TileEntityType<T>.render(renderer: Class<out TileEntityRenderer<in T>>) {
		ClientRegistry.bindTileEntityRenderer(this, ObjectConstructors.oneArg(renderer, TileEntityRendererDispatcher::class.java))
	}
}
