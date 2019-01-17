package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.mechanics.energy.IClusterGenerator
import chylex.hee.game.mechanics.energy.IEnergyQuantity
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModItems
import chylex.hee.system.util.allInCenteredBoxMutable
import chylex.hee.system.util.ceilToInt
import chylex.hee.system.util.distanceSqTo
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.getTile
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.setAir
import chylex.hee.system.util.square
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.state.BlockFaceShape
import net.minecraft.block.state.BlockFaceShape.UNDEFINED
import net.minecraft.block.state.IBlockState
import net.minecraft.client.particle.ParticleManager
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.IProjectile
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumBlockRenderType
import net.minecraft.util.EnumBlockRenderType.INVISIBLE
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraft.world.WorldServer
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.Random
import kotlin.math.pow

class BlockEnergyCluster(builder: BlockSimple.Builder) : BlockSimple(builder), ITileEntityProvider{
	companion object{
		private val SELECTION_AABB = AxisAlignedBB(0.2, 0.2, 0.2, 0.8, 0.8, 0.8)
		
		fun createSmallLeak(world: World, pos: BlockPos, amount: IEnergyQuantity){
			val units = amount.units.value.toFloat()
			val corruptedEnergyLevel = (2F + (units.pow(0.74F) / 9F)).ceilToInt()
			
			ModBlocks.CORRUPTED_ENERGY.spawnCorruptedEnergy(world, pos, corruptedEnergyLevel)
		}
		
		fun createFullLeak(world: World, pos: BlockPos, amount: IEnergyQuantity){
			val units = amount.units.value.toFloat()
			
			val corruptedEnergyRadius = 1.5F + (units.pow(0.77F) / 70F)
			val corruptedEnergyLevel = (2F + (units.pow(0.74F) / 9F)).ceilToInt()
			
			val energyRadiusInteger = corruptedEnergyRadius.ceilToInt()
			val energyRadiusSq = square(corruptedEnergyRadius)
			
			pos.allInCenteredBoxMutable(energyRadiusInteger, energyRadiusInteger, energyRadiusInteger).forEach {
				if (it.distanceSqTo(pos) <= energyRadiusSq){
					ModBlocks.CORRUPTED_ENERGY.spawnCorruptedEnergy(world, it, corruptedEnergyLevel)
				}
			}
		}
	}
	
	override fun createNewTileEntity(world: World, meta: Int): TileEntity{
		return TileEntityEnergyCluster()
	}
	
	override fun onBlockHarvested(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer){
		if (player.capabilities.isCreativeMode && !player.isSneaking){
			pos.getTile<TileEntityEnergyCluster>(world)?.breakWithoutExplosion = true
		}
	}
	
	override fun breakBlock(world: World, pos: BlockPos, state: IBlockState){
		val tile = pos.getTile<TileEntityEnergyCluster>(world) ?: return
		super.breakBlock(world, pos, state) // removes the tile entity
		
		if (tile.breakWithoutExplosion){
			return
		}
		
		val level = tile.energyLevel
		val units = level.units.value.toFloat()
		
		val explosionStength = 2.5F + (units.pow(0.6F) * 0.1F)
		val ethereumToDrop = (world.rand.nextFloat(1.6F, 2.0F) * (units * 0.01F).pow(1.4F)).floorToInt()
		
		world.newExplosion(null, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, explosionStength, false, true)
		createFullLeak(world, pos, level)
		
		repeat(ethereumToDrop){
			spawnAsEntity(world, pos, ItemStack(ModItems.ETHEREUM))
		}
	}
	
	override fun onEntityCollision(world: World, pos: BlockPos, state: IBlockState, entity: Entity){
		if (entity is IProjectile){
			pos.setAir(world)
		}
	}
	
	override fun quantityDropped(rand: Random): Int = 0
	
	@SideOnly(Side.CLIENT) override fun addHitEffects(state: IBlockState, world: World, target: RayTraceResult, manager: ParticleManager): Boolean = true
	@SideOnly(Side.CLIENT) override fun addDestroyEffects(world: World, pos: BlockPos, manager: ParticleManager): Boolean = true
	@SideOnly(Side.CLIENT) override fun addRunningEffects(state: IBlockState, world: World, pos: BlockPos, entity: Entity): Boolean = true
	@SideOnly(Side.CLIENT) override fun addLandingEffects(state: IBlockState, world: WorldServer, pos: BlockPos, stateAgain: IBlockState, entity: EntityLivingBase, particleAmount: Int): Boolean = true
	
	override fun getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos): AxisAlignedBB = SELECTION_AABB
	override fun getCollisionBoundingBox(state: IBlockState, world: IBlockAccess, pos: BlockPos): AxisAlignedBB? = NULL_AABB
	override fun getBlockFaceShape(world: IBlockAccess, state: IBlockState, pos: BlockPos, face: EnumFacing): BlockFaceShape = UNDEFINED
	
	override fun isFullCube(state: IBlockState): Boolean = false
	override fun isOpaqueCube(state: IBlockState): Boolean = false
	override fun getRenderType(state: IBlockState): EnumBlockRenderType = INVISIBLE
}
