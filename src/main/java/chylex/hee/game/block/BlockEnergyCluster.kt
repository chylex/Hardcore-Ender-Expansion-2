package chylex.hee.game.block

import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.mechanics.energy.IClusterGenerator
import chylex.hee.game.mechanics.energy.IEnergyQuantity
import chylex.hee.game.mechanics.instability.Instability
import chylex.hee.game.world.allInCenteredSphereMutable
import chylex.hee.game.world.getTile
import chylex.hee.game.world.removeBlock
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModItems
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.math.ceilToInt
import chylex.hee.system.math.floorToInt
import chylex.hee.system.migration.EntityLivingBase
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.random.nextFloat
import net.minecraft.block.BlockRenderType.INVISIBLE
import net.minecraft.block.BlockState
import net.minecraft.client.particle.ParticleManager
import net.minecraft.entity.Entity
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.shapes.ISelectionContext
import net.minecraft.util.math.shapes.VoxelShape
import net.minecraft.util.math.shapes.VoxelShapes
import net.minecraft.world.Explosion.Mode
import net.minecraft.world.IBlockReader
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import java.util.Random
import kotlin.math.pow

class BlockEnergyCluster(builder: BlockBuilder) : BlockSimpleShaped(builder, AxisAlignedBB(0.2, 0.2, 0.2, 0.8, 0.8, 0.8)) {
	companion object {
		fun createSmallLeak(world: World, pos: BlockPos, amount: IEnergyQuantity, causeInstability: Boolean = false) {
			val units = amount.units.value.toFloat()
			val corruptedEnergyLevel = (2F + (units.pow(0.74F) / 9F)).ceilToInt()
			
			ModBlocks.CORRUPTED_ENERGY.spawnCorruptedEnergy(world, pos, corruptedEnergyLevel)
			
			if (causeInstability) {
				Instability.get(world).triggerAction((10 + 2 * corruptedEnergyLevel).toUShort(), pos)
			}
		}
		
		fun createFullLeak(world: World, pos: BlockPos, amount: IEnergyQuantity) {
			val units = amount.units.value.toFloat()
			
			val corruptedEnergyRadius = 1.5 + (units.pow(0.77F) / 70F)
			val corruptedEnergyLevel = (2F + (units.pow(0.74F) / 9F)).ceilToInt()
			
			for(testPos in pos.allInCenteredSphereMutable(corruptedEnergyRadius)) {
				ModBlocks.CORRUPTED_ENERGY.spawnCorruptedEnergy(world, testPos, corruptedEnergyLevel)
			}
		}
	}
	
	override fun hasTileEntity(state: BlockState): Boolean {
		return true
	}
	
	override fun createTileEntity(state: BlockState, world: IBlockReader): TileEntity {
		return TileEntityEnergyCluster().also { it.loadClusterSnapshot(IClusterGenerator.ENERGY_SHRINE.generate(Random()), inactive = false) }
	}
	
	override fun onBlockHarvested(world: World, pos: BlockPos, state: BlockState, player: EntityPlayer) {
		if (player.abilities.isCreativeMode && !player.isSneaking) {
			pos.getTile<TileEntityEnergyCluster>(world)?.breakWithoutExplosion = true
		}
	}
	
	override fun onReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, isMoving: Boolean) {
		val tile = pos.getTile<TileEntityEnergyCluster>(world) ?: return
		@Suppress("DEPRECATION")
		super.onReplaced(state, world, pos, newState, isMoving) // removes the tile entity
		
		if (tile.breakWithoutExplosion) {
			return
		}
		
		val level = tile.energyLevel
		val units = level.units.value.toFloat()
		
		val explosionStength = 2.5F + (units.pow(0.6F) * 0.1F)
		val ethereumToDrop = (world.rand.nextFloat(1.6F, 2.0F) * (units * 0.01F).pow(1.4F)).floorToInt()
		
		world.createExplosion(null, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, explosionStength, false, Mode.DESTROY)
		
		Instability.get(world).triggerAction((100F + units.pow(0.785F)).ceilToInt().toUShort(), pos)
		createFullLeak(world, pos, level)
		
		repeat(ethereumToDrop) {
			spawnAsEntity(world, pos, ItemStack(ModItems.ETHEREUM))
		}
	}
	
	override fun onEntityCollision(state: BlockState, world: World, pos: BlockPos, entity: Entity) {
		if (entity is ProjectileEntity) {
			pos.removeBlock(world)
		}
	}
	
	@Sided(Side.CLIENT) override fun addHitEffects(state: BlockState, world: World, target: RayTraceResult, manager: ParticleManager) = true
	@Sided(Side.CLIENT) override fun addDestroyEffects(state: BlockState, world: World, pos: BlockPos, manager: ParticleManager) = true
	@Sided(Side.CLIENT) override fun addRunningEffects(state: BlockState, world: World, pos: BlockPos, entity: Entity) = true
	@Sided(Side.CLIENT) override fun addLandingEffects(state: BlockState, world: ServerWorld, pos: BlockPos, stateAgain: BlockState, entity: EntityLivingBase, particleAmount: Int) = true
	
	override fun getCollisionShape(state: BlockState, world: IBlockReader, pos: BlockPos, context: ISelectionContext): VoxelShape {
		return VoxelShapes.empty()
	}
	
	override fun getRenderType(state: BlockState) = INVISIBLE
}
