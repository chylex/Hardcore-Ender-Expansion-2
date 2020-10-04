package chylex.hee.init
import chylex.hee.HEE
import chylex.hee.game.block.BlockShulkerBoxOverride
import chylex.hee.game.block.entity.TileEntityAccumulationTable
import chylex.hee.game.block.entity.TileEntityBrewingStandCustom
import chylex.hee.game.block.entity.TileEntityDarkChest
import chylex.hee.game.block.entity.TileEntityEndPortalAcceptor
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.block.entity.TileEntityExperienceGate
import chylex.hee.game.block.entity.TileEntityExperienceTable
import chylex.hee.game.block.entity.TileEntityIgneousPlate
import chylex.hee.game.block.entity.TileEntityInfusedTNT
import chylex.hee.game.block.entity.TileEntityInfusionTable
import chylex.hee.game.block.entity.TileEntityJarODust
import chylex.hee.game.block.entity.TileEntityLootChest
import chylex.hee.game.block.entity.TileEntityMinersBurialAltar
import chylex.hee.game.block.entity.TileEntityPortalInner
import chylex.hee.game.block.entity.TileEntityShulkerBoxCustom
import chylex.hee.game.block.entity.TileEntitySpawnerObsidianTower
import chylex.hee.game.block.entity.TileEntityTablePedestal
import chylex.hee.game.block.entity.TileEntityVoidPortalStorage
import chylex.hee.system.forge.SubscribeAllEvents
import chylex.hee.system.forge.SubscribeEvent
import chylex.hee.system.forge.named
import chylex.hee.system.forge.registerAllFields
import chylex.hee.system.reflection.ObjectConstructors
import com.google.common.collect.ImmutableSet
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityType
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD

@SubscribeAllEvents(modid = HEE.ID, bus = MOD)
object ModTileEntities{
	@JvmField val ACCUMULATION_TABLE     = build<TileEntityAccumulationTable>(ModBlocks.ACCUMULATION_TABLE_TIER_1, ModBlocks.ACCUMULATION_TABLE_TIER_2, ModBlocks.ACCUMULATION_TABLE_TIER_3) named "accumulation_table"
	@JvmField val BREWING_STAND          = build<TileEntityBrewingStandCustom>(ModBlocks.ENHANCED_BREWING_STAND) named "brewing_stand"
	@JvmField val DARK_CHEST             = build<TileEntityDarkChest>(ModBlocks.DARK_CHEST) named "dark_chest"
	@JvmField val END_PORTAL_ACCEPTOR    = build<TileEntityEndPortalAcceptor>(ModBlocks.END_PORTAL_ACCEPTOR) named "end_portal_acceptor"
	@JvmField val END_PORTAL_INNER       = build<TileEntityPortalInner.End>(ModBlocks.END_PORTAL_INNER) named "end_portal_inner"
	@JvmField val ENERGY_CLUSTER         = build<TileEntityEnergyCluster>(ModBlocks.ENERGY_CLUSTER) named "energy_cluster"
	@JvmField val EXPERIENCE_GATE        = build<TileEntityExperienceGate>(ModBlocks.EXPERIENCE_GATE_CONTROLLER) named "experience_gate"
	@JvmField val EXPERIENCE_TABLE       = build<TileEntityExperienceTable>(ModBlocks.EXPERIENCE_TABLE_TIER_1, ModBlocks.EXPERIENCE_TABLE_TIER_2, ModBlocks.EXPERIENCE_TABLE_TIER_3) named "experience_table"
	@JvmField val IGNEOUS_PLATE          = build<TileEntityIgneousPlate>(ModBlocks.IGNEOUS_PLATE) named "igneous_plate"
	@JvmField val INFUSED_TNT            = build<TileEntityInfusedTNT>(ModBlocks.INFUSED_TNT) named "infused_tnt"
	@JvmField val INFUSION_TABLE         = build<TileEntityInfusionTable>(ModBlocks.INFUSION_TABLE_TIER_1, ModBlocks.INFUSION_TABLE_TIER_2, ModBlocks.INFUSION_TABLE_TIER_3) named "infusion_table"
	@JvmField val JAR_O_DUST             = build<TileEntityJarODust>(ModBlocks.JAR_O_DUST) named "jar_o_dust"
	@JvmField val LOOT_CHEST             = build<TileEntityLootChest>(ModBlocks.LOOT_CHEST) named "loot_chest"
	@JvmField val MINERS_BURIAL_ALTAR    = build<TileEntityMinersBurialAltar>(ModBlocks.MINERS_BURIAL_ALTAR) named "miners_burial_altar"
	@JvmField val SHULKER_BOX            = build<TileEntityShulkerBoxCustom>(*BlockShulkerBoxOverride.ALL_BLOCKS.toTypedArray()) named "shulker_box"
	@JvmField val SPAWNER_OBSIDIAN_TOWER = build<TileEntitySpawnerObsidianTower>(ModBlocks.SPAWNER_OBSIDIAN_TOWERS) named "spawner_obsidian_tower"
	@JvmField val TABLE_PEDESTAL         = build<TileEntityTablePedestal>(ModBlocks.TABLE_PEDESTAL) named "table_pedestal"
	@JvmField val VOID_PORTAL_INNER      = build<TileEntityPortalInner.Void>(ModBlocks.VOID_PORTAL_INNER) named "void_portal_inner"
	@JvmField val VOID_PORTAL_STORAGE    = build<TileEntityVoidPortalStorage>(ModBlocks.VOID_PORTAL_STORAGE, ModBlocks.VOID_PORTAL_STORAGE_CRAFTED) named "void_portal_storage"
	
	@SubscribeEvent
	fun onRegister(e: RegistryEvent.Register<TileEntityType<*>>){
		e.registerAllFields(this)
	}
	
	fun setupVanillaValidBlocks(){
		for(block in arrayOf(BREWING_STAND, TileEntityType.BREWING_STAND)){
			with(block){
				validBlocks = ImmutableSet.builder<Block>()
					.addAll(validBlocks)
					.add(Blocks.BREWING_STAND) // needs the replaced one
					.build()
			}
		}
		
		for(block in arrayOf(SHULKER_BOX, TileEntityType.SHULKER_BOX)){
			with(block){
				validBlocks = ImmutableSet.builder<Block>()
					.addAll(validBlocks)
					.addAll(BlockShulkerBoxOverride.ALL_BLOCKS) // needs the replaced ones
					.build()
			}
		}
		
		with(TileEntityType.SKULL){
			validBlocks = ImmutableSet.builder<Block>()
				.addAll(validBlocks)
				.add(ModBlocks.ENDERMAN_HEAD)
				.add(ModBlocks.ENDERMAN_WALL_HEAD)
				.build()
		}
	}
	
	private inline fun <reified T : TileEntity> build(vararg blocks: Block): TileEntityType<T>{
		return TileEntityType.Builder.create(ObjectConstructors.noArgs<T>(), *blocks).build(null) // UPDATE
	}
}
