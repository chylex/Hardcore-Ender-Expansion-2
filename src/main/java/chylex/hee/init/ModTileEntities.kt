package chylex.hee.init

import chylex.hee.HEE
import chylex.hee.game.block.BlockShulkerBoxOverride
import chylex.hee.game.block.entity.IHeeTileEntityType
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
import chylex.hee.system.named
import chylex.hee.system.registerAllFields
import chylex.hee.util.forge.SubscribeAllEvents
import chylex.hee.util.forge.SubscribeEvent
import chylex.hee.util.lang.ObjectConstructors
import com.google.common.collect.ImmutableSet
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityType
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD

@SubscribeAllEvents(modid = HEE.ID, bus = MOD)
object ModTileEntities {
	@JvmField val ACCUMULATION_TABLE     = build(TileEntityAccumulationTable.Type) named "accumulation_table"
	@JvmField val BREWING_STAND          = build(TileEntityBrewingStandCustom.Type) named "brewing_stand"
	@JvmField val DARK_CHEST             = build(TileEntityDarkChest.Type) named "dark_chest"
	@JvmField val END_PORTAL_ACCEPTOR    = build(TileEntityEndPortalAcceptor.Type) named "end_portal_acceptor"
	@JvmField val END_PORTAL_INNER       = build(TileEntityPortalInner.End.Type) named "end_portal_inner"
	@JvmField val ENERGY_CLUSTER         = build(TileEntityEnergyCluster.Type) named "energy_cluster"
	@JvmField val EXPERIENCE_GATE        = build(TileEntityExperienceGate.Type) named "experience_gate"
	@JvmField val EXPERIENCE_TABLE       = build(TileEntityExperienceTable.Type) named "experience_table"
	@JvmField val IGNEOUS_PLATE          = build(TileEntityIgneousPlate.Type) named "igneous_plate"
	@JvmField val INFUSED_TNT            = build(TileEntityInfusedTNT.Type) named "infused_tnt"
	@JvmField val INFUSION_TABLE         = build(TileEntityInfusionTable.Type) named "infusion_table"
	@JvmField val JAR_O_DUST             = build(TileEntityJarODust.Type) named "jar_o_dust"
	@JvmField val LOOT_CHEST             = build(TileEntityLootChest.Type) named "loot_chest"
	@JvmField val MINERS_BURIAL_ALTAR    = build(TileEntityMinersBurialAltar.Type) named "miners_burial_altar"
	@JvmField val SHULKER_BOX            = build(TileEntityShulkerBoxCustom.Type) named "shulker_box"
	@JvmField val SPAWNER_OBSIDIAN_TOWER = build(TileEntitySpawnerObsidianTower.Type) named "spawner_obsidian_tower"
	@JvmField val TABLE_PEDESTAL         = build(TileEntityTablePedestal.Type) named "table_pedestal"
	@JvmField val VOID_PORTAL_INNER      = build(TileEntityPortalInner.Void.Type) named "void_portal_inner"
	@JvmField val VOID_PORTAL_STORAGE    = build(TileEntityVoidPortalStorage.Type) named "void_portal_storage"
	
	@SubscribeEvent
	fun onRegister(e: RegistryEvent.Register<TileEntityType<*>>) {
		e.registerAllFields(this)
	}
	
	fun setupVanillaValidBlocks() {
		arrayOf(BREWING_STAND, TileEntityType.BREWING_STAND).extendValidBlocks {
			add(Blocks.BREWING_STAND)
		}
		
		arrayOf(SHULKER_BOX, TileEntityType.SHULKER_BOX).extendValidBlocks {
			addAll(BlockShulkerBoxOverride.ALL_BLOCKS)
		}
		
		TileEntityType.SKULL.extendValidBlocks {
			add(ModBlocks.ENDERMAN_HEAD, ModBlocks.ENDERMAN_WALL_HEAD)
		}
	}
	
	private inline fun <reified T : TileEntity> build(type: IHeeTileEntityType<T>): TileEntityType<T> {
		@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
		return TileEntityType.Builder.create(ObjectConstructors.noArgs<T>(), *type.blocks).build(null)
	}
	
	private inline fun <T : TileEntity> Array<TileEntityType<out T>>.extendValidBlocks(setCallback: ImmutableSet.Builder<Block>.() -> ImmutableSet.Builder<Block>) {
		for (tileEntityType in this) {
			tileEntityType.extendValidBlocks(setCallback)
		}
	}
	
	private inline fun <T : TileEntity> TileEntityType<T>.extendValidBlocks(setCallback: ImmutableSet.Builder<Block>.() -> ImmutableSet.Builder<Block>) {
		this.validBlocks = ImmutableSet.builder<Block>().addAll(this.validBlocks).let(setCallback).build()
	}
}
