package chylex.hee.proxy
import chylex.hee.HEE
import chylex.hee.client.model.item.ModelItemAmuletOfRecovery
import chylex.hee.client.render.block.RenderTileDarkChest
import chylex.hee.client.render.block.RenderTileEndPortal
import chylex.hee.client.render.block.RenderTileEndermanHead
import chylex.hee.client.render.block.RenderTileLootChest
import chylex.hee.client.render.block.RenderTileTablePedestal
import chylex.hee.client.render.entity.RenderEntityItemNoBob
import chylex.hee.client.render.entity.RenderEntityMobAbstractEnderman
import chylex.hee.client.render.entity.RenderEntityMobVillagerDying
import chylex.hee.client.render.entity.RenderEntityNothing
import chylex.hee.client.render.entity.RenderEntityProjectileEyeOfEnder
import chylex.hee.client.render.entity.layer.LayerEndermanHead
import chylex.hee.game.block.BlockAbstractTable
import chylex.hee.game.block.BlockDeathFlowerDecaying
import chylex.hee.game.block.BlockDryVines
import chylex.hee.game.block.BlockTablePedestal
import chylex.hee.game.block.entity.TileEntityDarkChest
import chylex.hee.game.block.entity.TileEntityLootChest
import chylex.hee.game.block.entity.TileEntityTablePedestal
import chylex.hee.game.commands.HeeClientCommand
import chylex.hee.game.entity.item.EntityItemNoBob
import chylex.hee.game.entity.living.EntityMobAbstractEnderman
import chylex.hee.game.entity.living.EntityMobVillagerDying
import chylex.hee.game.entity.projectile.EntityProjectileEyeOfEnder
import chylex.hee.game.entity.technical.EntityTechnicalBase
import chylex.hee.game.item.ItemBindingEssence
import chylex.hee.game.item.ItemEnergyOracle
import chylex.hee.game.item.ItemEnergyReceptacle
import chylex.hee.game.item.ItemPortalToken
import chylex.hee.game.item.ItemVoidBucket
import chylex.hee.game.item.ItemVoidSalad
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModItems
import chylex.hee.init.factory.RendererConstructors
import chylex.hee.system.Resource
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.client.renderer.block.statemap.IStateMapper
import net.minecraft.client.renderer.color.IItemColor
import net.minecraft.client.renderer.entity.Render
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityEndPortal
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.client.registry.RenderingRegistry
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.registry.ForgeRegistries
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@Suppress("unused", "RemoveExplicitTypeArguments")
@SideOnly(Side.CLIENT)
class ModClientProxy : ModCommonProxy(){
	override fun getClientSidePlayer(): EntityPlayer? = Minecraft.getMinecraft().player
	
	override fun onPreInit(){
		MinecraftForge.EVENT_BUS.register(this)
		
		registerEntityRenderer<EntityItemNoBob, RenderEntityItemNoBob>()
		
		registerEntityRenderer<EntityMobAbstractEnderman, RenderEntityMobAbstractEnderman>()
		registerEntityRenderer<EntityMobVillagerDying, RenderEntityMobVillagerDying>()
		
		registerEntityRenderer<EntityProjectileEyeOfEnder, RenderEntityProjectileEyeOfEnder>()
		
		registerEntityRenderer<EntityTechnicalBase, RenderEntityNothing>()
	}
	
	override fun onInit(){
		ClientCommandHandler.instance.registerCommand(HeeClientCommand)
		
		// renderers
		
		registerTileRenderer<TileEntityEndPortal>(RenderTileEndPortal)
		registerTileRenderer<TileEntityDarkChest>(RenderTileDarkChest)
		registerTileRenderer<TileEntityLootChest>(RenderTileLootChest)
		registerTileRenderer<TileEntityTablePedestal>(RenderTileTablePedestal)
		
		Item.getItemFromBlock(ModBlocks.DARK_CHEST).tileEntityItemStackRenderer = RenderTileDarkChest.AsItem
		Item.getItemFromBlock(ModBlocks.LOOT_CHEST).tileEntityItemStackRenderer = RenderTileLootChest.AsItem
		ModItems.ENDERMAN_HEAD.tileEntityItemStackRenderer = RenderTileEndermanHead.AsItem
		
		for(render in Minecraft.getMinecraft().renderManager.skinMap.values){
			render.addLayer(LayerEndermanHead(render.mainModel.bipedHead))
		}
		
		// colors
		
		val blockColors = Minecraft.getMinecraft().blockColors
		val itemColors = Minecraft.getMinecraft().itemColors
		
		with(blockColors){
			registerBlockColorHandler(BlockDryVines.Color, ModBlocks.DRY_VINES)
			registerBlockColorHandler(BlockTablePedestal.Color, ModBlocks.TABLE_PEDESTAL)
		}
		
		with(itemColors){
			registerItemColorHandler(IItemColor {
				stack, tintIndex -> blockColors.colorMultiplier((stack.item as ItemBlock).block.getStateFromMeta(stack.metadata), null, null, tintIndex) // UPDATE
			}, ModBlocks.DRY_VINES)
			
			registerItemColorHandler(ItemBindingEssence.Color, ModItems.BINDING_ESSENCE)
			registerItemColorHandler(ItemEnergyOracle.Color, ModItems.ENERGY_ORACLE)
			registerItemColorHandler(ItemEnergyReceptacle.Color, ModItems.ENERGY_RECEPTACLE)
			registerItemColorHandler(ItemPortalToken.Color, ModItems.PORTAL_TOKEN)
			registerItemColorHandler(ItemVoidBucket.Color, ModItems.VOID_BUCKET)
		}
	}
	
	@SubscribeEvent
	fun onModels(e: ModelRegistryEvent){
		
		// block state mappers
		
		val emptyStateMapper = IStateMapper {
			emptyMap()
		}
		
		val singleStateMapper = IStateMapper {
			val location = ModelResourceLocation(it.registryName!!, "normal")
			it.blockState.validStates.associateWith { location }
		}
		
		ModelLoader.setCustomStateMapper(ModBlocks.END_PORTAL_INNER, emptyStateMapper)
		ModelLoader.setCustomStateMapper(ModBlocks.ENDERMAN_HEAD, emptyStateMapper)
		ModelLoader.setCustomStateMapper(ModBlocks.ENERGY_CLUSTER, emptyStateMapper)
		
		ModelLoader.setCustomStateMapper(ModBlocks.CORRUPTED_ENERGY, singleStateMapper)
		ModelLoader.setCustomStateMapper(ModBlocks.DARK_CHEST, singleStateMapper)
		ModelLoader.setCustomStateMapper(ModBlocks.ENDER_GOO, singleStateMapper)
		ModelLoader.setCustomStateMapper(ModBlocks.INFUSED_TNT, singleStateMapper)
		ModelLoader.setCustomStateMapper(ModBlocks.LOOT_CHEST, singleStateMapper)
		
		// item models
		
		with(ForgeRegistries.ITEMS){
			val skippedBlocks = arrayOf(
				ModBlocks.ACCUMULATION_TABLE,
				ModBlocks.DEATH_FLOWER_DECAYING,
				ModBlocks.TABLE_BASE
			).map(Item::getItemFromBlock).toSet()
			
			for(item in keys.asSequence().filter { it.namespace == HEE.ID }.map(::getValue).filterNot(skippedBlocks::contains)){
				ModelLoader.setCustomModelResourceLocation(item!!, 0, ModelResourceLocation(item.registryName!!, "inventory"))
			}
		}
		
		ModelLoader.setCustomModelResourceLocation(ModItems.VOID_SALAD, ItemVoidSalad.Type.DOUBLE.ordinal, ModelResourceLocation(Resource.Custom("void_void_salad"), "inventory"))
		ModelLoader.setCustomModelResourceLocation(ModItems.VOID_SALAD, ItemVoidSalad.Type.MEGA.ordinal, ModelResourceLocation(Resource.Custom("mega_void_salad"), "inventory"))
		
		for(block in arrayOf(
			ModBlocks.TABLE_BASE,
			ModBlocks.ACCUMULATION_TABLE
		)){
			val item = Item.getItemFromBlock(block)
			val registryName = item.registryName!!
			
			for(tier in BlockAbstractTable.MIN_TIER..BlockAbstractTable.MAX_TIER){
				ModelLoader.setCustomModelResourceLocation(item, tier, ModelResourceLocation(registryName, "tier=$tier"))
			}
		}
		
		Item.getItemFromBlock(ModBlocks.DEATH_FLOWER_DECAYING).let {
			for(level in BlockDeathFlowerDecaying.MIN_LEVEL..BlockDeathFlowerDecaying.MAX_LEVEL){
				val texture = when(level){
					in 1..3   -> 1
					in 4..7   -> 2
					in 8..11  -> 3
					in 12..14 -> 4
					else      -> throw IllegalStateException()
				}
				
				ModelLoader.setCustomModelResourceLocation(it, level - BlockDeathFlowerDecaying.MIN_LEVEL, ModelResourceLocation(Resource.Custom("death_flower_$texture"), "inventory"))
			}
		}
		
		ModelItemAmuletOfRecovery.register()
	}
	
	private inline fun <reified T : Entity, reified R : Render<in T>> registerEntityRenderer(){
		RenderingRegistry.registerEntityRenderingHandler(T::class.java, RendererConstructors.get(R::class.java))
	}
	
	private inline fun <reified T : TileEntity> registerTileRenderer(renderer: TileEntitySpecialRenderer<in T>){
		ClientRegistry.bindTileEntitySpecialRenderer(T::class.java, renderer)
	}
	
	// Particles
	
	private var prevParticleSetting = Int.MAX_VALUE
	
	override fun pauseParticles(){
		val settings = Minecraft.getMinecraft().gameSettings
		
		if (settings.particleSetting != Int.MAX_VALUE){
			prevParticleSetting = settings.particleSetting
		}
		
		settings.particleSetting = Int.MAX_VALUE
	}
	
	override fun resumeParticles(){
		if (prevParticleSetting != Int.MAX_VALUE){
			Minecraft.getMinecraft().gameSettings.particleSetting = prevParticleSetting
		}
	}
}
