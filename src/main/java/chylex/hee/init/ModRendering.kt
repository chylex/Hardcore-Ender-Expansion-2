package chylex.hee.init

import chylex.hee.HEE
import chylex.hee.client.color.asItem
import chylex.hee.client.gui.screen.GuiAmuletOfRecovery
import chylex.hee.client.gui.screen.GuiBrewingStandCustom
import chylex.hee.client.gui.screen.GuiLootChest
import chylex.hee.client.gui.screen.GuiPortalTokenStorage
import chylex.hee.client.gui.screen.GuiShulkerBox
import chylex.hee.client.gui.screen.GuiTrinketPouch
import chylex.hee.client.render.EndDimensionRenderInfo
import chylex.hee.client.render.block.IBlockLayerCutout
import chylex.hee.client.render.block.IBlockLayerCutoutMipped
import chylex.hee.client.render.block.IBlockLayerTranslucent
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
import chylex.hee.game.block.BlockDryVines
import chylex.hee.game.block.BlockPuzzleLogic
import chylex.hee.game.block.BlockTablePedestal
import chylex.hee.game.block.entity.TileEntityDarkChest
import chylex.hee.game.block.entity.TileEntityExperienceGate
import chylex.hee.game.block.entity.TileEntityIgneousPlate
import chylex.hee.game.block.entity.TileEntityJarODust
import chylex.hee.game.block.entity.TileEntityLootChest
import chylex.hee.game.block.entity.TileEntityMinersBurialAltar
import chylex.hee.game.block.entity.TileEntityPortalInner
import chylex.hee.game.block.entity.TileEntityShulkerBoxCustom
import chylex.hee.game.block.entity.TileEntityTablePedestal
import chylex.hee.game.block.entity.base.TileEntityBaseSpawner
import chylex.hee.game.block.entity.base.TileEntityBaseTable
import chylex.hee.game.block.fluid.FluidEnderGoo
import chylex.hee.game.block.fluid.FluidEnderGooPurified
import chylex.hee.game.block.properties.CustomSkull
import chylex.hee.game.entity.effect.EntityTerritoryLightningBolt
import chylex.hee.game.entity.item.EntityFallingBlockHeavy
import chylex.hee.game.entity.item.EntityFallingObsidian
import chylex.hee.game.entity.item.EntityInfusedTNT
import chylex.hee.game.entity.item.EntityItemCauldronTrigger
import chylex.hee.game.entity.item.EntityItemFreshlyCooked
import chylex.hee.game.entity.item.EntityItemIgneousRock
import chylex.hee.game.entity.item.EntityItemNoBob
import chylex.hee.game.entity.item.EntityItemRevitalizationSubstance
import chylex.hee.game.entity.item.EntityTokenHolder
import chylex.hee.game.entity.living.EntityBossEnderEye
import chylex.hee.game.entity.living.EntityMobAbstractEnderman
import chylex.hee.game.entity.living.EntityMobAngryEnderman
import chylex.hee.game.entity.living.EntityMobBlobby
import chylex.hee.game.entity.living.EntityMobEndermite
import chylex.hee.game.entity.living.EntityMobEndermiteInstability
import chylex.hee.game.entity.living.EntityMobSilverfish
import chylex.hee.game.entity.living.EntityMobSpiderling
import chylex.hee.game.entity.living.EntityMobUndread
import chylex.hee.game.entity.living.EntityMobVampireBat
import chylex.hee.game.entity.living.EntityMobVillagerDying
import chylex.hee.game.entity.projectile.EntityProjectileEnderPearl
import chylex.hee.game.entity.projectile.EntityProjectileExperienceBottle
import chylex.hee.game.entity.projectile.EntityProjectileEyeOfEnder
import chylex.hee.game.entity.projectile.EntityProjectileSpatialDash
import chylex.hee.game.entity.technical.EntityTechnicalBase
import chylex.hee.game.entity.technical.EntityTechnicalIgneousPlateLogic
import chylex.hee.game.item.ItemBindingEssence
import chylex.hee.game.item.ItemDeathFlower
import chylex.hee.game.item.ItemEnergyOracle
import chylex.hee.game.item.ItemEnergyReceptacle
import chylex.hee.game.item.ItemPortalToken
import chylex.hee.game.item.ItemTotemOfUndyingCustom
import chylex.hee.game.item.ItemVoidBucket
import chylex.hee.game.item.ItemVoidSalad
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
import net.minecraft.inventory.container.Container
import net.minecraft.inventory.container.ContainerType
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
			when (block) {
				is IBlockLayerCutout       -> RenderTypeLookup.setRenderLayer(block, RenderType.getCutout())
				is IBlockLayerCutoutMipped -> RenderTypeLookup.setRenderLayer(block, RenderType.getCutoutMipped())
				is IBlockLayerTranslucent  -> RenderTypeLookup.setRenderLayer(block, RenderType.getTranslucent())
			}
		}
		
		// fluids
		
		RenderTypeLookup.setRenderLayer(FluidEnderGoo.still, RenderType.getSolid())
		RenderTypeLookup.setRenderLayer(FluidEnderGoo.flowing, RenderType.getSolid())
		
		RenderTypeLookup.setRenderLayer(FluidEnderGooPurified.still, RenderType.getSolid())
		RenderTypeLookup.setRenderLayer(FluidEnderGooPurified.flowing, RenderType.getSolid()) // UPDATE should be translucent but it's not rendering
		
		// properties
		
		ItemDeathFlower.DEATH_LEVEL_PROPERTY.register(ModBlocks.DEATH_FLOWER_DECAYING)
		ItemEnergyOracle.ACTIVITY_INTENSITY_PROPERTY.register(ModItems.ENERGY_ORACLE)
		ItemEnergyReceptacle.HAS_CLUSTER_PROPERTY.register(ModItems.ENERGY_RECEPTACLE)
		ItemPortalToken.TOKEN_TYPE_PROPERTY.register(ModItems.PORTAL_TOKEN)
		ItemTotemOfUndyingCustom.IS_SHAKING_PROPERTY.register(ModItems.TOTEM_OF_UNDYING)
		ItemVoidBucket.VOID_BUCKET_COOLDOWN_PROPERTY.register(ModItems.VOID_BUCKET)
		ItemVoidSalad.VOID_SALAD_TYPE_PROPERTY.register(ModItems.VOID_SALAD)
		
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
		
		registerEntity<EntityBossEnderEye, RenderEntityBossEnderEye>(ModEntities.ENDER_EYE)
		registerEntity<EntityFallingBlockHeavy, FallingBlockRenderer>(ModEntities.FALLING_BLOCK_HEAVY)
		registerEntity<EntityFallingObsidian, FallingBlockRenderer>(ModEntities.FALLING_OBSIDIAN)
		registerEntity<EntityInfusedTNT, TNTRenderer>(ModEntities.INFUSED_TNT)
		registerEntity<EntityItemCauldronTrigger, RenderEntityItem>(ModEntities.ITEM_CAULDRON_TRIGGER)
		registerEntity<EntityItemFreshlyCooked, RenderEntityItem>(ModEntities.ITEM_FRESHLY_COOKED)
		registerEntity<EntityItemIgneousRock, RenderEntityItemNoBob>(ModEntities.ITEM_IGNEOUS_ROCK)
		registerEntity<EntityItemNoBob, RenderEntityItem>(ModEntities.ITEM_NO_BOB)
		registerEntity<EntityItemRevitalizationSubstance, RenderEntityItem>(ModEntities.ITEM_REVITALIZATION_SUBSTANCE)
		registerEntity<EntityMobAbstractEnderman, RenderEntityMobAbstractEnderman>(ModEntities.ENDERMAN)
		registerEntity<EntityMobAbstractEnderman, RenderEntityMobAbstractEnderman>(ModEntities.ENDERMAN_MUPPET)
		registerEntity<EntityMobAngryEnderman, RenderEntityMobAngryEnderman>(ModEntities.ANGRY_ENDERMAN)
		registerEntity<EntityMobBlobby, RenderEntityMobBlobby>(ModEntities.BLOBBY)
		registerEntity<EntityMobEndermite, EndermiteRenderer>(ModEntities.ENDERMITE)
		registerEntity<EntityMobEndermiteInstability, EndermiteRenderer>(ModEntities.ENDERMITE_INSTABILITY)
		registerEntity<EntityMobSilverfish, SilverfishRenderer>(ModEntities.SILVERFISH)
		registerEntity<EntityMobSpiderling, RenderEntityMobSpiderling>(ModEntities.SPIDERLING)
		registerEntity<EntityMobUndread, RenderEntityMobUndread>(ModEntities.UNDREAD)
		registerEntity<EntityMobVampireBat, RenderEntityMobVampireBat>(ModEntities.VAMPIRE_BAT)
		registerEntity<EntityMobVillagerDying, RenderEntityMobVillagerDying>(ModEntities.VILLAGER_DYING)
		registerEntity<EntityProjectileEnderPearl, RenderEntitySprite<EntityProjectileEnderPearl>>(ModEntities.ENDER_PEARL)
		registerEntity<EntityProjectileExperienceBottle, RenderEntitySprite<EntityProjectileExperienceBottle>>(ModEntities.EXPERIENCE_BOTTLE)
		registerEntity<EntityProjectileEyeOfEnder, RenderEntityProjectileEyeOfEnder>(ModEntities.EYE_OF_ENDER)
		registerEntity<EntityProjectileSpatialDash, RenderEntityNothing>(ModEntities.SPATIAL_DASH)
		registerEntity<EntityTechnicalBase, RenderEntityNothing>(ModEntities.CAUSATUM_EVENT)
		registerEntity<EntityTechnicalBase, RenderEntityNothing>(ModEntities.TECHNICAL_PUZZLE)
		registerEntity<EntityTechnicalBase, RenderEntityNothing>(ModEntities.TECHNICAL_TRIGGER)
		registerEntity<EntityTechnicalIgneousPlateLogic, RenderEntityNothing>(ModEntities.IGNEOUS_PLATE_LOGIC)
		registerEntity<EntityTerritoryLightningBolt, RenderEntityTerritoryLightningBolt>(ModEntities.TERRITORY_LIGHTNING_BOLT)
		registerEntity<EntityTokenHolder, RenderEntityTokenHolder>(ModEntities.TOKEN_HOLDER)
		
		// tile entities
		
		registerTile<TileEntityBaseSpawner, RenderTileSpawner>(ModTileEntities.SPAWNER_OBSIDIAN_TOWER)
		registerTile<TileEntityBaseTable, RenderTileTable>(ModTileEntities.ACCUMULATION_TABLE)
		registerTile<TileEntityBaseTable, RenderTileTable>(ModTileEntities.EXPERIENCE_TABLE)
		registerTile<TileEntityBaseTable, RenderTileTable>(ModTileEntities.INFUSION_TABLE)
		registerTile<TileEntityDarkChest, RenderTileDarkChest>(ModTileEntities.DARK_CHEST)
		registerTile<TileEntityExperienceGate, RenderTileExperienceGate>(ModTileEntities.EXPERIENCE_GATE)
		registerTile<TileEntityIgneousPlate, RenderTileIgneousPlate>(ModTileEntities.IGNEOUS_PLATE)
		registerTile<TileEntityJarODust, RenderTileJarODust>(ModTileEntities.JAR_O_DUST)
		registerTile<TileEntityLootChest, RenderTileLootChest>(ModTileEntities.LOOT_CHEST)
		registerTile<TileEntityMinersBurialAltar, RenderTileMinersBurialAltar>(ModTileEntities.MINERS_BURIAL_ALTAR)
		registerTile<TileEntityPortalInner.End, RenderTileEndPortal>(ModTileEntities.END_PORTAL_INNER)
		registerTile<TileEntityPortalInner.Void, RenderTileVoidPortal>(ModTileEntities.VOID_PORTAL_INNER)
		registerTile<TileEntityShulkerBoxCustom, RenderTileShulkerBox>(ModTileEntities.SHULKER_BOX)
		registerTile<TileEntityTablePedestal, RenderTileTablePedestal>(ModTileEntities.TABLE_PEDESTAL)
		
		// miscellaneous
		
		SkullTileEntityRenderer.MODELS[CustomSkull.Enderman] = GenericHeadModel(0, 0, 64, 32)
		SkullTileEntityRenderer.SKINS[CustomSkull.Enderman] = Resource.Custom("textures/entity/enderman_head.png")
	}
	
	@SubscribeEvent
	fun onRegisterBlockItemColors(e: ColorHandlerEvent.Item) {
		with(e.blockColors) {
			with(e.itemColors) {
				register(BlockDryVines.Color, ModBlocks.DRY_VINES)
				register(BlockDryVines.Color.asItem(ModBlocks.DRY_VINES), ModBlocks.DRY_VINES)
				register(BlockTablePedestal.Color, ModBlocks.TABLE_PEDESTAL)
				
				register(ItemBindingEssence.Color, ModItems.BINDING_ESSENCE)
				register(ItemEnergyOracle.Color, ModItems.ENERGY_ORACLE)
				register(ItemEnergyReceptacle.Color, ModItems.ENERGY_RECEPTACLE)
				register(ItemPortalToken.Color, ModItems.PORTAL_TOKEN)
				register(ItemVoidBucket.Color, ModItems.VOID_BUCKET)
				
				for (block in arrayOf(
					ModBlocks.PUZZLE_BURST_3,
					ModBlocks.PUZZLE_BURST_5,
					ModBlocks.PUZZLE_REDIRECT_1,
					ModBlocks.PUZZLE_REDIRECT_2,
					ModBlocks.PUZZLE_REDIRECT_4,
					ModBlocks.PUZZLE_TELEPORT
				)) {
					register(BlockPuzzleLogic.Color, block)
					register(BlockPuzzleLogic.Color.asItem(block), block)
				}
			}
		}
	}
	
	// Utilities
	
	private fun <T : ItemStackTileEntityRenderer> callable(obj: T) = Callable<ItemStackTileEntityRenderer> { obj }
	
	private inline fun <reified T : ContainerScreen<U>, U : Container> registerScreen(type: ContainerType<out U>, constructor: IScreenFactory<U, T>) {
		ScreenManager.registerFactory(type, constructor)
	}
	
	@Suppress("UNCHECKED_CAST")
	private inline fun <reified T : Entity, reified U : EntityRenderer<in T>> registerEntity(type: EntityType<out T>) {
		val handle = ObjectConstructors.generic<U, EntityRenderer<in T>, IRenderFactory<T>>("createRenderFor", EntityRendererManager::class.java)
		RenderingRegistry.registerEntityRenderingHandler(type, handle.invokeExact() as IRenderFactory<T>)
	}
	
	@Suppress("UNCHECKED_CAST")
	private inline fun <reified T : TileEntity, reified U : TileEntityRenderer<in T>> registerTile(type: TileEntityType<out T>) {
		ClientRegistry.bindTileEntityRenderer(type, ObjectConstructors.oneArg<U, TileEntityRendererDispatcher>())
	}
}
