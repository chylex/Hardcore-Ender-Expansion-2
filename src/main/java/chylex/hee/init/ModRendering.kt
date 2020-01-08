package chylex.hee.init
import chylex.hee.HEE
import chylex.hee.client.gui.GuiAmuletOfRecovery
import chylex.hee.client.gui.GuiBrewingStandCustom
import chylex.hee.client.gui.GuiLootChest
import chylex.hee.client.gui.GuiPortalTokenStorage
import chylex.hee.client.gui.GuiTrinketPouch
import chylex.hee.client.model.item.ModelItemAmuletOfRecovery
import chylex.hee.client.render.block.RenderTileDarkChest
import chylex.hee.client.render.block.RenderTileEndPortal
import chylex.hee.client.render.block.RenderTileExperienceGate
import chylex.hee.client.render.block.RenderTileIgneousPlate
import chylex.hee.client.render.block.RenderTileJarODust
import chylex.hee.client.render.block.RenderTileLootChest
import chylex.hee.client.render.block.RenderTileMinersBurialAltar
import chylex.hee.client.render.block.RenderTileSpawner
import chylex.hee.client.render.block.RenderTileTable
import chylex.hee.client.render.block.RenderTileTablePedestal
import chylex.hee.client.render.block.RenderTileVoidPortal
import chylex.hee.client.render.entity.RenderEntityBossEnderEye
import chylex.hee.client.render.entity.RenderEntityItemNoBob
import chylex.hee.client.render.entity.RenderEntityMobAbstractEnderman
import chylex.hee.client.render.entity.RenderEntityMobAngryEnderman
import chylex.hee.client.render.entity.RenderEntityMobSpiderling
import chylex.hee.client.render.entity.RenderEntityMobUndread
import chylex.hee.client.render.entity.RenderEntityMobVampireBat
import chylex.hee.client.render.entity.RenderEntityMobVillagerDying
import chylex.hee.client.render.entity.RenderEntityNothing
import chylex.hee.client.render.entity.RenderEntityProjectileEyeOfEnder
import chylex.hee.client.render.entity.RenderEntityTokenHolder
import chylex.hee.client.render.entity.layer.LayerEndermanHead
import chylex.hee.client.render.util.asItem
import chylex.hee.client.util.MC
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
import chylex.hee.game.block.entity.TileEntityTablePedestal
import chylex.hee.game.block.entity.base.TileEntityBaseSpawner
import chylex.hee.game.block.entity.base.TileEntityBaseTable
import chylex.hee.game.container.ContainerAmuletOfRecovery
import chylex.hee.game.container.ContainerLootChest
import chylex.hee.game.container.ContainerPortalTokenStorage
import chylex.hee.game.container.ContainerTrinketPouch
import chylex.hee.game.entity.item.EntityItemNoBob
import chylex.hee.game.entity.item.EntityTokenHolder
import chylex.hee.game.entity.living.EntityBossEnderEye
import chylex.hee.game.entity.living.EntityMobAbstractEnderman
import chylex.hee.game.entity.living.EntityMobAngryEnderman
import chylex.hee.game.entity.living.EntityMobSpiderling
import chylex.hee.game.entity.living.EntityMobUndread
import chylex.hee.game.entity.living.EntityMobVampireBat
import chylex.hee.game.entity.living.EntityMobVillagerDying
import chylex.hee.game.entity.projectile.EntityProjectileEyeOfEnder
import chylex.hee.game.entity.projectile.EntityProjectileSpatialDash
import chylex.hee.game.entity.technical.EntityTechnicalBase
import chylex.hee.game.item.ItemBindingEssence
import chylex.hee.game.item.ItemEnergyOracle
import chylex.hee.game.item.ItemEnergyReceptacle
import chylex.hee.game.item.ItemPortalToken
import chylex.hee.game.item.ItemVoidBucket
import chylex.hee.init.factory.RendererConstructors
import chylex.hee.init.factory.ScreenConstructors
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.migration.vanilla.ContainerBrewingStand
import chylex.hee.system.migration.vanilla.ContainerShulkerBox
import net.minecraft.client.gui.ScreenManager
import net.minecraft.client.gui.screen.inventory.ContainerScreen
import net.minecraft.client.gui.screen.inventory.ShulkerBoxScreen
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.tileentity.TileEntityRenderer
import net.minecraft.entity.Entity
import net.minecraft.inventory.container.Container
import net.minecraft.inventory.container.ContainerType
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.client.event.ColorHandlerEvent
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.client.registry.RenderingRegistry
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent

@SubscribeAllEvents(Side.CLIENT, modid = HEE.ID, bus = MOD)
object ModRendering{
	val RENDER_ITEM_DARK_CHEST = RenderTileDarkChest.AsItem
	val RENDER_ITEM_LOOT_CHEST = RenderTileLootChest.AsItem
	val RENDER_ITEM_JAR_O_DUST = RenderTileJarODust.AsItem
	// UPDATE val RENDER_ITEM_ENDERMAN_HEAD, RenderTileEndermanHead.AsItem
	
	@JvmStatic
	@SubscribeEvent
	@Suppress("unused", "UNUSED_PARAMETER", "RemoveExplicitTypeArguments")
	fun onRegisterRenderers(e: FMLClientSetupEvent){
		
		// screens
		
		registerScreen<GuiAmuletOfRecovery, ContainerAmuletOfRecovery>(ModContainers.AMULET_OF_RECOVERY)
		registerScreen<GuiBrewingStandCustom, ContainerBrewingStand>(ModContainers.BREWING_STAND)
		registerScreen<GuiLootChest, ContainerLootChest>(ModContainers.LOOT_CHEST)
		registerScreen<GuiPortalTokenStorage, ContainerPortalTokenStorage>(ModContainers.PORTAL_TOKEN_STORAGE)
		registerScreen<ShulkerBoxScreen, ContainerShulkerBox>(ModContainers.SHULKER_BOX_IN_INVENTORY)
		registerScreen<GuiTrinketPouch, ContainerTrinketPouch>(ModContainers.TRINKET_POUCH)
		
		// entities
		
		registerEntity<EntityItemNoBob, RenderEntityItemNoBob>()
		registerEntity<EntityTokenHolder, RenderEntityTokenHolder>()
		
		registerEntity<EntityBossEnderEye, RenderEntityBossEnderEye>()
		
		registerEntity<EntityMobAbstractEnderman, RenderEntityMobAbstractEnderman>()
		registerEntity<EntityMobAngryEnderman, RenderEntityMobAngryEnderman>()
		registerEntity<EntityMobSpiderling, RenderEntityMobSpiderling>()
		registerEntity<EntityMobUndread, RenderEntityMobUndread>()
		registerEntity<EntityMobVampireBat, RenderEntityMobVampireBat>()
		registerEntity<EntityMobVillagerDying, RenderEntityMobVillagerDying>()
		
		registerEntity<EntityProjectileEyeOfEnder, RenderEntityProjectileEyeOfEnder>()
		registerEntity<EntityProjectileSpatialDash, RenderEntityNothing>()
		
		registerEntity<EntityTechnicalBase, RenderEntityNothing>()
		
		// tile entities
		
		registerTile<TileEntityBaseSpawner>(RenderTileSpawner)
		registerTile<TileEntityBaseTable>(RenderTileTable)
		registerTile<TileEntityDarkChest>(RenderTileDarkChest)
		registerTile<TileEntityExperienceGate>(RenderTileExperienceGate)
		registerTile<TileEntityIgneousPlate>(RenderTileIgneousPlate)
		registerTile<TileEntityJarODust>(RenderTileJarODust)
		registerTile<TileEntityLootChest>(RenderTileLootChest)
		registerTile<TileEntityMinersBurialAltar>(RenderTileMinersBurialAltar)
		registerTile<TileEntityPortalInner.End>(RenderTileEndPortal)
		registerTile<TileEntityPortalInner.Void>(RenderTileVoidPortal)
		registerTile<TileEntityTablePedestal>(RenderTileTablePedestal)
		
		// miscellaneous
		
		for(render in MC.renderManager.skinMap.values){
			render.addLayer(LayerEndermanHead(render))
		}
	}
	
	@JvmStatic
	@SubscribeEvent
	fun onRegisterBlockItemColors(e: ColorHandlerEvent.Item){
		with(e.blockColors){ with(e.itemColors){
			register(BlockDryVines.Color, ModBlocks.DRY_VINES)
			register(BlockDryVines.Color.asItem(ModBlocks.DRY_VINES), ModBlocks.DRY_VINES)
			register(BlockTablePedestal.Color, ModBlocks.TABLE_PEDESTAL)
			
			register(ItemBindingEssence.Color, ModItems.BINDING_ESSENCE)
			register(ItemEnergyOracle.Color, ModItems.ENERGY_ORACLE)
			register(ItemEnergyReceptacle.Color, ModItems.ENERGY_RECEPTACLE)
			register(ItemPortalToken.Color, ModItems.PORTAL_TOKEN)
			register(ItemVoidBucket.Color, ModItems.VOID_BUCKET)
			
			for(block in arrayOf(
				ModBlocks.PUZZLE_BURST_3,
				ModBlocks.PUZZLE_BURST_5,
				ModBlocks.PUZZLE_REDIRECT_1,
				ModBlocks.PUZZLE_REDIRECT_2,
				ModBlocks.PUZZLE_REDIRECT_4,
				ModBlocks.PUZZLE_TELEPORT
			)){
				register(BlockPuzzleLogic.Color, block)
				register(BlockPuzzleLogic.Color.asItem(block), block)
			}
		}}
	}
	/* TODO
	private fun registerBlockStateMappers(){
		val emptyStateMapper = IStateMapper {
			emptyMap()
		}
		
		val singleStateMapper = IStateMapper {
			val location = ModelResourceLocation(it.registryName!!, "normal")
			it.blockState.validStates.associateWith { location }
		}
		
		setMapper(ModBlocks.END_PORTAL_INNER, emptyStateMapper)
		setMapper(ModBlocks.ENDERMAN_HEAD, emptyStateMapper)
		setMapper(ModBlocks.ENERGY_CLUSTER, emptyStateMapper)
		setMapper(ModBlocks.VOID_PORTAL_INNER, emptyStateMapper)
		
		setMapper(ModBlocks.CORRUPTED_ENERGY, singleStateMapper)
		setMapper(ModBlocks.DARK_CHEST, singleStateMapper)
		setMapper(ModBlocks.ENDER_GOO, singleStateMapper)
		setMapper(ModBlocks.PURIFIED_ENDER_GOO, singleStateMapper)
		setMapper(ModBlocks.LOOT_CHEST, singleStateMapper)
		setMapper(ModBlocks.INFUSED_TNT, singleStateMapper)
		setMapper(ModBlocks.IGNEOUS_PLATE, singleStateMapper)
		setMapper(ModBlocks.WHITEBARK_LEAVES_AUTUMN_BROWN, singleStateMapper)
		setMapper(ModBlocks.WHITEBARK_LEAVES_AUTUMN_ORANGE, singleStateMapper)
		setMapper(ModBlocks.WHITEBARK_LEAVES_AUTUMN_YELLOWGREEN, singleStateMapper)
		setMapper(ModBlocks.WHITEBARK_SAPLING_AUTUMN_BROWN, singleStateMapper)
		setMapper(ModBlocks.WHITEBARK_SAPLING_AUTUMN_ORANGE, singleStateMapper)
		setMapper(ModBlocks.WHITEBARK_SAPLING_AUTUMN_YELLOWGREEN, singleStateMapper)
	}*/
	
	@JvmStatic
	@SubscribeEvent
	fun onRegisterModels(@Suppress("UNUSED_PARAMETER") e: ModelRegistryEvent){
		
		// special models
		
		ModelItemAmuletOfRecovery.register()
		
		// UPDATE
		/* TODO
		val tables = arrayOf(
			ModBlocks.TABLE_BASE,
			ModBlocks.ACCUMULATION_TABLE
		)
		
		for(block in tables){
			for(tier in BlockAbstractTable.MIN_TIER..BlockAbstractTable.MAX_TIER){
				setModel(block, tier, variant = "tier=$tier")
			}
		}
		
		setModel(ModBlocks.DEATH_FLOWER_DECAYING, 0..2, Resource.Custom("death_flower_1"))
		setModel(ModBlocks.DEATH_FLOWER_DECAYING, 3..6, Resource.Custom("death_flower_2"))
		setModel(ModBlocks.DEATH_FLOWER_DECAYING, 7..10, Resource.Custom("death_flower_3"))
		setModel(ModBlocks.DEATH_FLOWER_DECAYING, 11..13, Resource.Custom("death_flower_4"))
		
		setModel(ModBlocks.VOID_PORTAL_INNER, BlockVoidPortalInner.Type.values().indices)
		
		setModel(ModItems.VOID_SALAD, ItemVoidSalad.Type.DOUBLE.ordinal, Resource.Custom("void_void_salad"))
		setModel(ModItems.VOID_SALAD, ItemVoidSalad.Type.MEGA.ordinal, Resource.Custom("mega_void_salad"))
		
		with(ForgeRegistries.ITEMS){
			val skippedBlocks = arrayOf(
				ModBlocks.DEATH_FLOWER_DECAYING,
				ModBlocks.POTTED_DEATH_FLOWER_DECAYING,
				ModBlocks.POTTED_DEATH_FLOWER_HEALED,
				ModBlocks.POTTED_DEATH_FLOWER_WITHERED,
				*tables
			).map(Item::getItemFromBlock).toSet()
			
			for(item in keys.filter(Resource::isCustom).map(::getValue).requireNoNulls().filterNot(skippedBlocks::contains)){
				setModel(item)
			}
		}*/
	}
	
	// Utilities
	
	private inline fun <reified T : ContainerScreen<U>, U : Container> registerScreen(type: ContainerType<out U>){
		ScreenManager.registerFactory(type, ScreenConstructors.get(T::class.java))
	}
	
	private inline fun <reified T : Entity, reified U : EntityRenderer<in T>> registerEntity(){
		RenderingRegistry.registerEntityRenderingHandler(T::class.java, RendererConstructors.get(U::class.java))
	}
	
	private inline fun <reified T : TileEntity> registerTile(renderer: TileEntityRenderer<in T>){
		ClientRegistry.bindTileEntitySpecialRenderer(T::class.java, renderer)
	}
	
	/* UPDATE
	private fun setMapper(block: Block, mapper: IStateMapper){
		ModelLoader.setCustomStateMapper(block, mapper)
	}
	
	private fun setModel(item: Item, metadata: Int = 0, location: ResourceLocation = item.registryName!!, variant: String = "inventory"){
		ModelLoader.setCustomModelResourceLocation(item, metadata, ModelResourceLocation(location, variant))
	}
	
	private fun setModel(block: Block, metadata: Int = 0, location: ResourceLocation = block.registryName!!, variant: String = "inventory"){
		setModel(Item.getItemFromBlock(block), metadata, location, variant)
	}
	
	private fun setModel(block: Block, metadatas: IntRange, location: ResourceLocation = block.registryName!!, variant: String = "inventory"){
		for(metadata in metadatas){
			setModel(block, metadata, location, variant)
		}
	}*/
}
