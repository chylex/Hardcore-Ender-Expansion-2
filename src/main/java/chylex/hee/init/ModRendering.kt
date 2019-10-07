package chylex.hee.init
import chylex.hee.HEE
import chylex.hee.client.model.item.ModelItemAmuletOfRecovery
import chylex.hee.client.render.block.RenderTileDarkChest
import chylex.hee.client.render.block.RenderTileEndPortal
import chylex.hee.client.render.block.RenderTileEndermanHead
import chylex.hee.client.render.block.RenderTileIgneousPlate
import chylex.hee.client.render.block.RenderTileJarODust
import chylex.hee.client.render.block.RenderTileLootChest
import chylex.hee.client.render.block.RenderTileTablePedestal
import chylex.hee.client.render.block.RenderTileVoidPortal
import chylex.hee.client.render.entity.RenderEntityBossEnderEye
import chylex.hee.client.render.entity.RenderEntityItemNoBob
import chylex.hee.client.render.entity.RenderEntityMobAbstractEnderman
import chylex.hee.client.render.entity.RenderEntityMobSpiderling
import chylex.hee.client.render.entity.RenderEntityMobUndread
import chylex.hee.client.render.entity.RenderEntityMobVillagerDying
import chylex.hee.client.render.entity.RenderEntityNothing
import chylex.hee.client.render.entity.RenderEntityProjectileEyeOfEnder
import chylex.hee.client.render.entity.RenderEntityTokenHolder
import chylex.hee.client.render.entity.layer.LayerEndermanHead
import chylex.hee.client.render.util.asItem
import chylex.hee.client.util.MC
import chylex.hee.game.block.BlockAbstractTable
import chylex.hee.game.block.BlockDryVines
import chylex.hee.game.block.BlockGraveDirt
import chylex.hee.game.block.BlockTablePedestal
import chylex.hee.game.block.BlockVoidPortalInner
import chylex.hee.game.block.entity.TileEntityDarkChest
import chylex.hee.game.block.entity.TileEntityIgneousPlate
import chylex.hee.game.block.entity.TileEntityJarODust
import chylex.hee.game.block.entity.TileEntityLootChest
import chylex.hee.game.block.entity.TileEntityPortalInner
import chylex.hee.game.block.entity.TileEntityTablePedestal
import chylex.hee.game.entity.item.EntityItemNoBob
import chylex.hee.game.entity.item.EntityTokenHolder
import chylex.hee.game.entity.living.EntityBossEnderEye
import chylex.hee.game.entity.living.EntityMobAbstractEnderman
import chylex.hee.game.entity.living.EntityMobSpiderling
import chylex.hee.game.entity.living.EntityMobUndread
import chylex.hee.game.entity.living.EntityMobVillagerDying
import chylex.hee.game.entity.projectile.EntityProjectileEyeOfEnder
import chylex.hee.game.entity.projectile.EntityProjectileSpatialDash
import chylex.hee.game.entity.technical.EntityTechnicalBase
import chylex.hee.game.item.ItemBindingEssence
import chylex.hee.game.item.ItemEnergyOracle
import chylex.hee.game.item.ItemEnergyReceptacle
import chylex.hee.game.item.ItemPortalToken
import chylex.hee.game.item.ItemVoidBucket
import chylex.hee.game.item.ItemVoidSalad
import chylex.hee.init.factory.RendererConstructors
import chylex.hee.system.Resource
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import net.minecraft.block.Block
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.client.renderer.block.statemap.IStateMapper
import net.minecraft.client.renderer.color.IBlockColor
import net.minecraft.client.renderer.color.IItemColor
import net.minecraft.client.renderer.entity.Render
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.entity.Entity
import net.minecraft.item.Item
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.client.registry.RenderingRegistry
import net.minecraftforge.fml.common.registry.ForgeRegistries

@SubscribeAllEvents(Side.CLIENT, modid = HEE.ID)
object ModRendering{
	
	// Entities
	
	fun registerEntities(){
		registerEntity<EntityItemNoBob, RenderEntityItemNoBob>()
		registerEntity<EntityTokenHolder, RenderEntityTokenHolder>()
		
		registerEntity<EntityBossEnderEye, RenderEntityBossEnderEye>()
		
		registerEntity<EntityMobAbstractEnderman, RenderEntityMobAbstractEnderman>()
		registerEntity<EntityMobSpiderling, RenderEntityMobSpiderling>()
		registerEntity<EntityMobUndread, RenderEntityMobUndread>()
		registerEntity<EntityMobVillagerDying, RenderEntityMobVillagerDying>()
		
		registerEntity<EntityProjectileEyeOfEnder, RenderEntityProjectileEyeOfEnder>()
		registerEntity<EntityProjectileSpatialDash, RenderEntityNothing>()
		
		registerEntity<EntityTechnicalBase, RenderEntityNothing>()
	}
	
	fun registerLayers(){
		for(render in MC.renderManager.skinMap.values){
			render.addLayer(LayerEndermanHead(render.mainModel.bipedHead))
		}
	}
	
	@Suppress("unused", "RemoveExplicitTypeArguments")
	fun registerTileEntities(){
		registerTile<TileEntityPortalInner.End>(RenderTileEndPortal)
		registerTile<TileEntityPortalInner.Void>(RenderTileVoidPortal)
		registerTile<TileEntityDarkChest>(RenderTileDarkChest)
		registerTile<TileEntityLootChest>(RenderTileLootChest)
		registerTile<TileEntityJarODust>(RenderTileJarODust)
		registerTile<TileEntityIgneousPlate>(RenderTileIgneousPlate)
		registerTile<TileEntityTablePedestal>(RenderTileTablePedestal)
		
		registerTileStack(ModBlocks.DARK_CHEST, RenderTileDarkChest.AsItem)
		registerTileStack(ModBlocks.LOOT_CHEST, RenderTileLootChest.AsItem)
		
		registerTileStack(ModBlocks.JAR_O_DUST, RenderTileJarODust.AsItem)
		
		registerTileStack(ModItems.ENDERMAN_HEAD, RenderTileEndermanHead.AsItem)
	}
	
	// Blocks & items
	
	fun registerBlockItemColors(){
		setColor(ModBlocks.DRY_VINES, BlockDryVines.Color)
		setColor(ModBlocks.DRY_VINES, BlockDryVines.Color.asItem(ModBlocks.DRY_VINES))
		setColor(ModBlocks.TABLE_PEDESTAL, BlockTablePedestal.Color)
		
		setColor(ModItems.BINDING_ESSENCE, ItemBindingEssence.Color)
		setColor(ModItems.ENERGY_ORACLE, ItemEnergyOracle.Color)
		setColor(ModItems.ENERGY_RECEPTACLE, ItemEnergyReceptacle.Color)
		setColor(ModItems.PORTAL_TOKEN, ItemPortalToken.Color)
		setColor(ModItems.VOID_BUCKET, ItemVoidBucket.Color)
	}
	
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
	}
	
	private fun registerSpecialModels(){
		ModelItemAmuletOfRecovery.register()
	}
	
	@JvmStatic
	@SubscribeEvent
	fun onModels(@Suppress("UNUSED_PARAMETER") e: ModelRegistryEvent){
		registerBlockStateMappers()
		registerSpecialModels()
		
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
		
		setModel(ModBlocks.GRAVE_DIRT, BlockGraveDirt.Type.PLAIN.ordinal, variant = "full=false,type=plain")
		setModel(ModBlocks.GRAVE_DIRT, BlockGraveDirt.Type.SPIDERLING.ordinal, variant = "full=false,type=spiderling")
		setModel(ModBlocks.GRAVE_DIRT, BlockGraveDirt.Type.LOOT.ordinal, variant = "full=false,type=loot")
		
		setModel(ModBlocks.VOID_PORTAL_INNER, BlockVoidPortalInner.Type.values().indices)
		
		setModel(ModItems.VOID_SALAD, ItemVoidSalad.Type.DOUBLE.ordinal, Resource.Custom("void_void_salad"))
		setModel(ModItems.VOID_SALAD, ItemVoidSalad.Type.MEGA.ordinal, Resource.Custom("mega_void_salad"))
		
		with(ForgeRegistries.ITEMS){
			val skippedBlocks = arrayOf(
				ModBlocks.DEATH_FLOWER_DECAYING,
				ModBlocks.POTTED_DEATH_FLOWER_DECAYING,
				ModBlocks.POTTED_DEATH_FLOWER_HEALED,
				ModBlocks.POTTED_DEATH_FLOWER_WITHERED,
				ModBlocks.GRAVE_DIRT,
				*tables
			).map(Item::getItemFromBlock).toSet()
			
			for(item in keys.filter { it.namespace == HEE.ID }.map(::getValue).requireNoNulls().filterNot(skippedBlocks::contains)){
				setModel(item)
			}
		}
	}
	
	// Utilities
	
	private inline fun <reified T : Entity, reified R : Render<in T>> registerEntity(){
		RenderingRegistry.registerEntityRenderingHandler(T::class.java, RendererConstructors.get(R::class.java))
	}
	
	private inline fun <reified T : TileEntity> registerTile(renderer: TileEntitySpecialRenderer<in T>){
		ClientRegistry.bindTileEntitySpecialRenderer(T::class.java, renderer)
	}
	
	private fun registerTileStack(block: Block, renderer: TileEntityItemStackRenderer){
		Item.getItemFromBlock(block).tileEntityItemStackRenderer = renderer
	}
	
	private fun registerTileStack(item: Item, renderer: TileEntityItemStackRenderer){
		item.tileEntityItemStackRenderer = renderer
	}
	
	private fun setMapper(block: Block, mapper: IStateMapper){
		ModelLoader.setCustomStateMapper(block, mapper)
	}
	
	private fun setColor(block: Block, color: IBlockColor){
		MC.instance.blockColors.registerBlockColorHandler(color, block)
	}
	
	private fun setColor(block: Block, color: IItemColor){
		MC.instance.itemColors.registerItemColorHandler(color, block)
	}
	
	private fun setColor(item: Item, color: IItemColor){
		MC.instance.itemColors.registerItemColorHandler(color, item)
	}
	
	private fun setModel(item: Item, metadata: Int = 0, location: ResourceLocation = item.registryName!!, variant: String = "inventory"){
		ModelLoader.setCustomModelResourceLocation(item, metadata, ModelResourceLocation(location, variant))
	}
	
	private fun setModel(block: Block, metadata: Int = 0, location: ResourceLocation = block.registryName!!, variant: String = "inventory"){
		setModel(Item.getItemFromBlock(block), metadata, location, variant)
	}
	
	private fun setModel(item: Item, metadatas: IntRange, location: ResourceLocation = item.registryName!!, variant: String = "inventory"){
		for(metadata in metadatas){
			setModel(item, metadata, location, variant)
		}
	}
	
	private fun setModel(block: Block, metadatas: IntRange, location: ResourceLocation = block.registryName!!, variant: String = "inventory"){
		for(metadata in metadatas){
			setModel(block, metadata, location, variant)
		}
	}
}
